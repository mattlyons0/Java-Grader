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
import DropboxGrader.GuiElements.MiscComponents.JGhostTextField;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextSpreadsheet;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 *
 * @author matt
 */
public class NewGradebookOverlay extends ContentOverlay{
    private GradebookView view;
    private ChangeGradebookOverlay overlay;
    
    private JCheckBox keepNames;
    private JCheckBox keepAssignments;
    private JCheckBox keepGrades;
    private JTextField spreadsheetName;
    private JButton createSpreadsheet;
    
    public NewGradebookOverlay(GradebookView view,ChangeGradebookOverlay overlay){
        super("NewGradebookOverlay",true);
        
        this.view=view;
        this.overlay=overlay;
    }
    @Override
    public void setup() {
        keepNames=new JCheckBox("Keep Names");
        keepNames.addActionListener(this);
        keepAssignments=new JCheckBox("Keep Assignments");
        keepAssignments.addActionListener(this);
        keepGrades=new JCheckBox("Keep Grades");
        keepGrades.addActionListener(this);
        keepGrades.setEnabled(false);
        spreadsheetName=new JGhostTextField(15,"Gradebook Name");
        createSpreadsheet=new JButton("Create");
        createSpreadsheet.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        add(keepNames,cons);
        cons.gridx++;
        add(keepAssignments,cons);
        cons.gridx++;
        add(keepGrades,cons);
        cons.gridx=0;
        cons.gridy++;
        cons.gridwidth=3;
        add(spreadsheetName,cons);
        cons.gridwidth=1;
        cons.gridy++;
        cons.gridx=2;
        cons.insets=new Insets(5,5,5,5);
        cons.anchor=GridBagConstraints.SOUTHEAST;
        add(createSpreadsheet,cons);
        
        
        Dimension parentSize = view.getGui().getSize();
        setSize((int)(parentSize.width*0.75),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Add New Gradebook");
        setVisible(true);
    }

    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(keepNames)||e.getSource().equals(keepAssignments)){
            if(keepNames.isSelected()&&keepAssignments.isSelected())
                keepGrades.setEnabled(true);
            else{
                keepGrades.setEnabled(false);
                keepGrades.setSelected(false);
            }
        }
        else if(e.getSource().equals(keepGrades)){
            
        }
        else if(e.getSource().equals(createSpreadsheet)){
            if(spreadsheetName.getText().replaceAll(" ", "").equals("")){
                GuiHelper.alertDialog("Gradebook Must Have a Name");
                return;
            }
            if(spreadsheetName.getText().equals("Default")){
                GuiHelper.alertDialog("'Default' is a reserved name, pick another name.");
                return;
            }
            dispose();
            view.getGui().getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    spreadsheetName.setText(spreadsheetName.getText().replaceAll(Pattern.quote("/"), ""));
                    spreadsheetName.setText(spreadsheetName.getText().replaceAll(Pattern.quote("\\"), ""));
                    Gui gui=view.getGui();
                    String filename="/Grades-Period"+Config.dropboxPeriod+spreadsheetName.getText()+".txt";
                    String remoteName="/"+Config.dropboxSpreadsheetFolder+filename;
                    String selectedPath=gui.getGrader().getSelectedRemotePath();
                    DbxClient client=gui.getDbxSession().getClient();
                    File downloadLoc=new File(gui.getGrader().getSelectedPath());
                    try{
                        DbxEntry existing=client.getMetadata(remoteName);
                        if(existing!=null&&existing instanceof DbxEntry.File){
                            boolean overwrite=GuiHelper.yesNoDialog(spreadsheetName.getText()+" for Period "+Config.dropboxPeriod+" already exists!\n"
                                    + "Are you sure you want to overwrite it?");
                            if(!overwrite)
                                return;
                        }
                        DbxSession.writeToFile(downloadLoc,spreadsheetName.getText());
                        FileInputStream in=new FileInputStream(downloadLoc);
                        client.uploadFile(selectedPath,DbxWriteMode.force(),downloadLoc.length(),in);
                        in.close();
                        gui.getGrader().lock(); //ensure nothing changes the remote data while we do this

                        File spreadsheetLoc=new File(gui.getManager().getDownloadFolder()+filename);
                        spreadsheetLoc.createNewFile();
                        TextSpreadsheet sheet=gui.getGrader().getSpreadsheet();
                        if(!keepGrades.isSelected())
                            sheet.deleteAllGrades();
                        if(!keepNames.isSelected())
                            sheet.deleteAllNames();
                        if(!keepAssignments.isSelected())
                            sheet.deleteAllAssignments();
                        sheet.writeToFile(spreadsheetLoc);

                        in=new FileInputStream(spreadsheetLoc);
                        client.uploadFile(remoteName,DbxWriteMode.force(),spreadsheetLoc.length(),in);
                        in.close();

                        overlay.gradebooksChanged();
                        gui.getGrader().refresh();
                        gui.getGrader().unlock();

                        gui.fileBrowserDataChanged();
                        view.dataChanged();

                    } catch(DbxException|IOException ex){
                        System.err.println("Error creating new gradebook.");
                        ex.printStackTrace();
                    }
                }
            });
        }
    }
    
}
