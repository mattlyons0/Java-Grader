/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.Assignment;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * Handles the gui for adding a library to be run with an assignment
 * @author 141lyonsm
 */
public class LibraryPanel extends JPanel implements ActionListener{
    private Gui gui;
    
    private ArrayList<String> libraries;
    
    private ArrayList<JButton> browseButtons;
    private ArrayList<JLabel> statuses;
    private ArrayList<JButton> removeLibraries;
    private JButton plusButton;
    private JButton addLibraryButton;
    
    private final FileFilter jarFilter;
    private final String statusText="Library: ";
    
    private Integer assignmentNumber;
    private String assignmentName;
    public LibraryPanel(String[] libs,Integer assignmentNum,String assignmentName,Gui gui){
        this.gui=gui;
        this.assignmentNumber=assignmentNum;
        this.assignmentName=assignmentName;
        
        setLayout(new GridBagLayout());
        jarFilter=new FileFilter() { //taken from ConfigView.java, I probably just should have made it static, but whatever
            @Override
            public boolean accept(File file) {
                if(file.getName().toLowerCase().endsWith(".jar")||file.isDirectory())
                    return true;
                return false;
            }
            @Override
            public String getDescription() {
                return "*.jar";
            }
        };
        
        browseButtons=new ArrayList();
        statuses=new ArrayList();
        removeLibraries=new ArrayList();
        
        libraries=new ArrayList();
        if(libs!=null)
            libraries.addAll(Arrays.asList(libs));
        
        plusButton=new JButton("+");
        plusButton.setToolTipText("Add Library");
        plusButton.addActionListener(this);
        addLibraryButton=new JButton("Add Library");
        addLibraryButton.addActionListener(this);
        
        setup();
    }
    private void setup(){
        removeAll();
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        for(int i=0;i<libraries.size();i++){
            if(browseButtons.size()<=i){
                JLabel status=new JLabel(statusText+"Not Set");
                JButton browse=new JButton("Browse");
                browse.setActionCommand("BrowseLibrary"+i);
                browse.addActionListener(this);
                JButton remove=new JButton("-");
                remove.setToolTipText("Remove This Library");
                remove.setActionCommand("RemoveLibrary"+i);
                remove.addActionListener(this);
                statuses.add(status);
                browseButtons.add(browse);
                removeLibraries.add(remove);
                
                if(libraries.get(i)!=null)
                    status.setText(statusText+libraries.get(i));
            }
            else{
                browseButtons.get(i).setActionCommand("BrowseLibrary"+i);
                removeLibraries.get(i).setActionCommand("RemoveLibrary"+i);
            }
            cons.gridx=0;
            add(statuses.get(i),cons);
            cons.gridx++;
            add(browseButtons.get(i),cons);
            cons.gridx++;
            add(removeLibraries.get(i),cons);
            
            if(i==libraries.size()-1){
                cons.gridx++;
                add(plusButton,cons);
            }
            cons.gridy++;            
        }
        if(libraries.isEmpty()){
            add(addLibraryButton);
        }
        
        revalidate();
        repaint();
    }
    public void save(){
        for(int i=0;i<libraries.size();i++){
            if(libraries.get(i)==null||libraries.get(i).equals("")){
                libraries.remove(i);
                i--;
            }
        }
    }
    public String[] getLibs(){
        return libraries.toArray(new String[0]);
    }
    public void setLibs(String[] libs){
        if(libs==null)
            return;
        libraries.clear();
        libraries.addAll(Arrays.asList(libs));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(plusButton)||e.getSource().equals(addLibraryButton)){
            libraries.add(null);
            setup();
        }
        else if(e.getActionCommand().startsWith("RemoveLibrary")){
            int num=GradebookTable.extractNumber("RemoveLibrary", e.getActionCommand());
            final String path=libraries.get(num);
            //check if any other tests use this path
            boolean shouldDelete=true;
            loop:
            for(TextAssignment assign:gui.getGrader().getSpreadsheet().getAllAssignments()){
                if(assign.libraries!=null){
                    for(String test:assign.libraries){
                        if(test.equals(path)&&!(assign.number==assignmentNumber&&assign.name.equals(assignmentName))){
                            shouldDelete=false;
                            break loop;
                        }
                            
                    }
                }
            }
            if(shouldDelete&&!(path==null||path.equals(""))){
                gui.getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(gui.getDbxSession().getClient().getMetadata(path)!=null)
                            gui.getDbxSession().getClient().delete(path);
                        } catch (DbxException ex) {
                            System.err.println("Error deleting library from dropbox.\n"+ex);
                            ex.printStackTrace();
                        }
                    }
                });
            }
            
            libraries.remove(num);
            
            browseButtons.remove(num);
            removeLibraries.remove(num);
            statuses.remove(num);
            setup();
        }
        else if(e.getActionCommand().startsWith("BrowseLibrary")){
            final int id=GradebookTable.extractNumber("BrowseLibrary", e.getActionCommand());
            JFileChooser fc=new JFileChooser(System.getProperty("userprofile"));
            fc.setFileFilter(jarFilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file=fc.getSelectedFile();
                final String oldFile=libraries.get(id);
                final JLabel label=statuses.get(id);
                libraries.set(id, Config.librariesLocation+"/"+file.getName());
                label.setText(statusText+file.getName());
                final String remoteNameF=libraries.get(id);
                gui.getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            DbxClient client=gui.getDbxSession().getClient();
                            if(oldFile!=null&&!oldFile.equals("")&&client.getMetadata(oldFile)!=null){
                                client.delete(oldFile);
                            }
                            String remoteName=remoteNameF;
                            
                            DbxEntry entry=client.getMetadata(remoteName);
                            while(entry!=null){
                                boolean changed=false;
                                remoteName=remoteName.substring(0,remoteName.length()-4); //remove .jar
                                label.setText(statusText+remoteName);
                                if(remoteName.endsWith(")")&&remoteName.contains("(")){
                                    int endIndex=remoteName.lastIndexOf(")");
                                    if(endIndex-1>0&&Character.isDigit(remoteName.charAt(endIndex-1))){
                                        int startIndex=remoteName.lastIndexOf("(");
                                        int currentNum=DbxFile.safeStringToInt(remoteName.substring(startIndex,endIndex));
                                        currentNum++;
                                        remoteName=remoteName.substring(0,startIndex)+"("+currentNum+")";
                                        
                                        changed=true;
                                    }
                                }
                                if(!changed){
                                    remoteName+="(2)";
                                }
                                remoteName+=".jar";
                                entry=client.getMetadata(remoteName);
                            }
                            if(!libraries.get(id).equals(remoteName))
                                libraries.set(id, remoteName);
                            label.setText(statusText+remoteName);
                            FileInputStream sheetStream = new FileInputStream(file);
                            client.uploadFile(remoteName, DbxWriteMode.force(), file.length(), sheetStream);
                            sheetStream.close();
                        } catch(DbxException|IOException e){
                            System.err.println("Error uploading library.\n"+e);
                            GuiHelper.alertDialog("<html>Error Uploading "+file.getName()+" to Dropbox.<br/>"+e+"</html>");
                            label.setText(statusText+"Not Set");
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
    public boolean hasLibraries(){
        return !libraries.isEmpty();
    }
}
