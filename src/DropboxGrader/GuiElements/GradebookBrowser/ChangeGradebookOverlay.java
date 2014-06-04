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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author matt
 */
public class ChangeGradebookOverlay extends ContentOverlay{
    private GradebookView view;
    
    private JScrollPane spreadsheets;
    private JPanel spreadsheetsPane;
    private ArrayList<JButton> buttons;
    private JButton addButton;
    private JLabel statusLabel;
    
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
        spreadsheets.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        statusLabel=new JLabel();
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.anchor=GridBagConstraints.NORTH;
        cons.insets=new Insets(5,5,5,5);
        add(new JLabel("Select a Gradebook for Period "+Config.dropboxPeriod),cons);
        cons.gridy++;
        cons.weighty=500;
        add(spreadsheets,cons);
        cons.gridy++;
        cons.weighty=1;
        add(statusLabel);
        
        Dimension parentSize = view.getGui().getSize();
        setSize((int)(parentSize.width*0.75),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Change Gradebook");
        
        setVisible(true);
        setupButtons();
    }
    private void setupButtons(){
        statusLabel.setText("");
        statusLabel.setIcon(null);
        
        final GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        //display spinner while we do the lookup in the background.
        final JLabel loadingLabel=new JLabel("Populating Gradebooks...");
        spreadsheetsPane.add(loadingLabel,cons);
        final JLabel loader=new JLabel("");
        loader.setIcon(new ImageIcon(getClass().getResource("/Resources/ajax-loader.gif")));
        cons.gridy++;
        spreadsheetsPane.add(loader,cons);
        
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
                                if(name.equals(selected)){
                                    spreadsheetButton.setText("<html>"+spreadsheetButton.getText()+" <b>(Selected)</b></html>");
                                    spreadsheetButton.setToolTipText("Selected");
                                }
                                else{
                                    spreadsheetButton.setActionCommand(name);
                                    spreadsheetButton.addActionListener(ChangeGradebookOverlay.this);
                                }
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
                    }
                    else{
                        loadingLabel.setText("An Unknown Error Occured.");
                        loader.setIcon(null);
                        System.err.println("This should never happen.\nThe Gradebook Folder doesnt exist when looking for all existing gradebooks.");
                    }
                } catch(IOException|DbxException e){
                    loadingLabel.setText("Error Downloading List of Gradebooks. "+e);
                    if(e instanceof DbxException){
                        System.err.println("Error downloading list of gradebooks.");
                    }
                    else{
                        System.err.println("Error downloading selected gradebook.");
                    }
                    loader.setIcon(null);
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
        if(e.getSource().equals(addButton)){
            view.getGui().getViewManager().removeOverlay("NewGradebookOverlay");
            NewGradebookOverlay overlay=new NewGradebookOverlay(view,this);
            view.getGui().getViewManager().addOverlay(overlay);
        }
        else{ //the action command is the sheet name
            view.getGui().getBackgroundThread().invokeLater(new Runnable() {

                @Override
                public void run() {
                    try{
                        statusLabel.setText("Changing Gradebooks...");
                        statusLabel.setIcon(new ImageIcon(getClass().getResource("/Resources/ajax-loader.gif")));
                        for(JButton b:buttons)
                            b.setEnabled(false);
                        addButton.setEnabled(false);

                        String name=e.getActionCommand();
                        if(name.equals("Default"))
                            name="";
                        Gui gui=view.getGui();
                        DbxClient c=gui.getDbxSession().getClient();
                        File f=new File(gui.getGrader().getSelectedPath());
                        f.createNewFile();
                        DbxSession.writeToFile(f, name);
                        FileInputStream input = new FileInputStream(f);
                        c.uploadFile(gui.getGrader().getSelectedRemotePath(), DbxWriteMode.force(), f.length(), input);
                        input.close();

                        setupButtons();
                        gui.getGrader().refresh();
                        gui.fileBrowserDataChanged();
                        view.dataChanged();
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
    public void gradebooksChanged(){
        setupButtons();
    }
}
