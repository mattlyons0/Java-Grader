/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.GradebookBrowser;

import DropboxGrader.Config;
import DropboxGrader.DbxSession;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiHelper;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxEntry.WithChildren;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 *
 * @author matt
 */
public class ChangeGradebookOverlay extends ContentOverlay implements MouseListener{
    private GradebookView view;
    
    private JScrollPane spreadsheets;
    private JPanel spreadsheetsPane;
    private ArrayList<JButton> buttons;
    private JButton addButton;
    private JLabel statusLabel;
    private final String statusText="Select a Gradebook for Period "+Config.dropboxPeriod;
    
    public ChangeGradebookOverlay(GradebookView view){
        super("ChangeGradebookOverlay",true);
        
        this.view=view;
        buttons=new ArrayList();
    }

    @Override
    public void setup() {
        spreadsheetsPane=new JPanel();
        spreadsheetsPane.setLayout(new GridBagLayout());
        
        spreadsheets=new JScrollPane(spreadsheetsPane);
        
        statusLabel=new JLabel();
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=10;
        cons.weighty=1;
        cons.anchor=GridBagConstraints.NORTH;
        cons.insets=new Insets(5,5,5,5);
        statusLabel=new JLabel(statusText);
        add(statusLabel,cons);
        cons.gridy++;
        cons.weighty=500;
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        add(spreadsheets,cons);
        
        Dimension parentSize = view.getGui().getSize();
        setSize((int)(parentSize.width*0.75),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Change Gradebook");
        
        setVisible(true);
        setupButtons();
    }
    private void setupButtons(){
        statusLabel.setText(statusText);
        statusLabel.setIcon(null);
        
        final GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        //display spinner while we do the lookup in the background.
        statusLabel.setText("Populating Gradebooks...");
        statusLabel.setIcon(new ImageIcon(getClass().getResource("/Resources/ajax-loader.gif")));
        
        final Gui gui=view.getGui();
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                //get list of files in grades directory
                try{
                    DbxClient c=gui.getDbxSession().getClient();
                    DbxEntry selectedSpreadsheet=c.getMetadata(gui.getGrader().getSelectedRemotePath());
                    String selected;
                    if(selectedSpreadsheet==null)
                        selected="Default";
                    else{
                        FileOutputStream f=new FileOutputStream(gui.getGrader().getSelectedPath());
                        c.getFile(gui.getGrader().getSelectedRemotePath(),null,f);
                        f.close();
                        selected=DbxSession.readFromFile(new File(gui.getGrader().getSelectedPath()));
                    }
                    WithChildren children=c.getMetadataWithChildren("/"+Config.dropboxSpreadsheetFolder);
                    if(children!=null&&children.children!=null){
                        buttons.clear();
                        for(DbxEntry entry:children.children){
                            if(entry.name.startsWith("Grades-Period"+Config.dropboxPeriod)&&entry.name.endsWith(".txt")){
                                //we found a gradebook
                                String name=entry.name.replace("Grades-Period"+Config.dropboxPeriod, "").replace(".txt", "");
                                if(name.equals(""))
                                    name="Default";
                                JButton spreadsheetButton=new JButton(name);
                                if(name.equals(selected)||(selected.equals("")&&name.equals("Default"))){
                                    String book=spreadsheetButton.getText();
                                    spreadsheetButton.setText("<html>"+book+" <b>(Selected)</b></html>");
                                    spreadsheetButton.setToolTipText("Selected");
                                    spreadsheetButton.setActionCommand(book);
                                }
                                else{
                                    spreadsheetButton.setActionCommand(name);
                                    spreadsheetButton.addActionListener(ChangeGradebookOverlay.this);
                                }
                                spreadsheetButton.addMouseListener(ChangeGradebookOverlay.this);
                                buttons.add(spreadsheetButton);
                            }
                        }
                        spreadsheetsPane.removeAll();
                        cons.gridy=0;
                        cons.gridx=0;
                        if(buttons.isEmpty()){
                            spreadsheetsPane.add(new JLabel("No Gradebooks Found"),cons);
                        }
                        else{
                            for(JButton b:buttons){
                                spreadsheetsPane.add(b,cons);
                                cons.gridy++;
                            }
                            spreadsheetsPane.add(new JLabel(" "),cons);
                            cons.gridy++;
                            if(addButton==null){
                                addButton=new JButton("Add New Gradebook");
                                addButton.addActionListener(ChangeGradebookOverlay.this);
                            }
                            addButton.setEnabled(true);
                            spreadsheetsPane.add(addButton,cons);
                        }
                        statusLabel.setText(statusText);
                        statusLabel.setIcon(null);
                    }
                    else{
                        statusLabel.setText("An Unknown Error Occured.");
                        statusLabel.setIcon(null);
                        System.err.println("This should never happen.\nThe Gradebook Folder doesnt exist when looking for all existing gradebooks.");
                    }
                } catch(IOException|DbxException e){
                    statusLabel.setText("Error Downloading List of Gradebooks. "+e);
                    if(e instanceof DbxException){
                        System.err.println("Error downloading list of gradebooks.");
                    }
                    else{
                        System.err.println("Error downloading selected gradebook.");
                    }
                    statusLabel.setIcon(null);
                    e.printStackTrace();
                }
                revalidate();
                repaint();
            }
        });
    }
    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        return true;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if(e.getSource() instanceof JButton){
            if(e.getSource().equals(addButton)){
                view.getGui().getViewManager().removeOverlay("NewGradebookOverlay");
                NewGradebookOverlay overlay=new NewGradebookOverlay(view,this);
                view.getGui().getViewManager().addOverlay(overlay);
            }
            else{ //the action command is the sheet name
                statusLabel.setText("Changing Gradebooks...");
                statusLabel.setIcon(new ImageIcon(getClass().getResource("/Resources/ajax-loader.gif")));
                for(JButton b:buttons)
                    b.setEnabled(false);
                addButton.setEnabled(false);

                view.getGui().getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String name=e.getActionCommand();
                        if(name.equals("Default"))
                            name="";
                        final Gui gui=view.getGui();
                        final String fname=name;
                        try{
                            DbxClient c=gui.getDbxSession().getClient();
                            File f=new File(gui.getGrader().getSelectedPath());
                            f.createNewFile();
                            DbxSession.writeToFile(f, fname);
                            FileInputStream input = new FileInputStream(f);
                            c.uploadFile(gui.getGrader().getSelectedRemotePath(), DbxWriteMode.force(), f.length(), input);
                            input.close();

                            changeData();
                        } catch(IOException|DbxException ex){
                            if(ex instanceof IOException){
                                System.err.println("Error writing to new gradebook.");
                                GuiHelper.alertDialog("<html>Error Accessing Hard Drive. <br/>"+ex+"</html>");
                            }
                            else{
                                System.err.println("Error uploading new gradebook.");
                                GuiHelper.alertDialog("Error Uploading New Gradebook to Dropbox.");
                            }
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
        else if(e.getSource() instanceof JMenuItem){
            view.getGui().getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    String command=e.getActionCommand();
                    if(command.startsWith("MoveUp")){
                        String book=GradebookTable.extractString("MoveUp",command);

                    }
                    else if(command.startsWith("MoveDown")){
                        String book=GradebookTable.extractString("MoveDown",command);
                    }
                    else if(command.startsWith("Rename")){
                        String book=GradebookTable.extractString("Rename",command);
                        String newName=JOptionPane.showInputDialog("What would you like to name this gradebook?",book);
                        if(newName!=null&&!newName.replaceAll(" ", "").equals("")&&!newName.equals(book)){
                            newName=newName.replaceAll(Pattern.quote("/"), "");
                            newName=newName.replaceAll(Pattern.quote("\\"), "");
                            if(newName.equals("Default"))
                                newName="";
                            try{
                                DbxClient c=view.getGui().getDbxSession().getClient();
                                String newFilename="/Grades-Period"+Config.dropboxPeriod+newName+".txt";
                                String newRemoteName="/"+Config.dropboxSpreadsheetFolder+newFilename;
                                boolean overwrite=false;
                                if(c.getMetadata(newRemoteName)!=null){
                                    String friendlyName=newName.equals("")?"Default":newName;
                                    int choice=GuiHelper.multiOptionPane("<html>Gradebook '"+friendlyName+"' already exists.<br/>"
                                            + "The data in '"+friendlyName+ "' will be permanently deleted.<br/>"+
                                            "Are you sure you would like to overwrite it?", new String[]{"Yes","No"});
                                    if(choice!=0)
                                        return;
                                    overwrite=true;
                                }
                                File f=new File(view.getGui().getGrader().getSelectedPath());
                                f.createNewFile();
                                DbxSession.writeToFile(f, newName);
                                FileInputStream input = new FileInputStream(f);
                                c.uploadFile(view.getGui().getGrader().getSelectedRemotePath(), DbxWriteMode.force(), f.length(), input);
                                input.close();

                                String oldFilename="/Grades-Period"+Config.dropboxPeriod+book+".txt";
                                String oldRemoteName="/"+Config.dropboxSpreadsheetFolder+oldFilename;
                                if(overwrite)
                                    c.delete(newRemoteName);
                                c.move(oldRemoteName, newRemoteName);
                                changeData();
                            } catch(IOException|DbxException ex){
                                if(ex instanceof IOException){
                                    System.err.println("Error writing to new gradebook.");
                                    GuiHelper.alertDialog("<html>Error Accessing Hard Drive. <br/>"+ex+"</html>");
                                }
                                else{
                                    System.err.println("Error uploading new gradebook.");
                                    GuiHelper.alertDialog("Error Uploading New Gradebook to Dropbox.");
                                }
                                ex.printStackTrace();
                            }
                        }
                    }
                    else if(command.startsWith("Delete")){
                        String book=GradebookTable.extractString("Delete",command);
                        int selection=GuiHelper.multiOptionPane("<html>Are you sure you want to delete "+book+
                                "?<br/>The grades will be permanently deleted.</html>", new String[]{"Yes","No"});
                        if(selection==0){
                            if(book.equals("Default"))
                                book="";
                            Gui gui=view.getGui();
                            DbxClient client=gui.getDbxSession().getClient();
                            String filename="/Grades-Period"+Config.dropboxPeriod+book+".txt";
                            String remoteName="/"+Config.dropboxSpreadsheetFolder+filename;
                            String selectedRemote=gui.getGrader().getSelectedRemotePath();
                            try{
                                client.delete(remoteName);
                                if(client.getMetadata(selectedRemote)!=null&&(
                                        DbxSession.readFromFile(new File(view.getGui().getGrader().getSelectedPath())).equals(book)||book.equals("")))
                                    client.delete(selectedRemote);
                                changeData();
                                if(book.equals(""))
                                    gui.getBackgroundThread().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setupButtons(); //extra refresh to add default back to the list
                                    }
                                });
                            } catch(DbxException ex){
                                System.err.println("Error deleting gradebook. "+ex);
                                GuiHelper.alertDialog("<html>An error occured deleting gradebook.<br/>"+ex+"</html>");
                            }
                        }
                    }
                }
            });
        }
    }
    public void gradebooksChanged(){
        setupButtons();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){ //right click
            String command=((JButton)e.getComponent()).getActionCommand();
            JPopupMenu m=new JPopupMenu();
            JMenuItem m1=new JMenuItem("Move Up");
            m1.setActionCommand("MoveUp"+command);
            m1.addActionListener(this);
            JMenuItem m2=new JMenuItem("Move Down");
            m2.setActionCommand("MoveDown"+command);
            m2.addActionListener(this);
            JMenuItem m3=new JMenuItem("Rename");
            m3.setActionCommand("Rename"+command);
            m3.addActionListener(this);
            JMenuItem m4=new JMenuItem("Delete");
            m4.setActionCommand("Delete"+command);
            m4.addActionListener(this);
            //m.add(m1); //no time to implement
            //m.add(m2);
            m.add(m3);
            m.add(m4);
            m.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
    private void changeData(){
        final Gui gui=view.getGui();
        setupButtons();
        gui.getGradebook().getGradebookTable().getData().setRefreshing(true);
        gui.getGrader().refresh();
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.fileBrowserDataChanged();
                view.dataChanged();
            }
        });
    }
}
