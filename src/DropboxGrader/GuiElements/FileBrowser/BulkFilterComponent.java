/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import DropboxGrader.FileManagement.DbxFile;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author 141lyonsm
 */
public class BulkFilterComponent extends JComponent implements ActionListener,ListSelectionListener{
    private String[] bulkOperations={"Bulk Actions","Change Assignment Number","Change Assignment Name",
        "Delete", "Forcibly Delete"};
    private BrowserView view;
    
    private JComboBox bulkActionsBox;
    private JButton applyBulkActionsButton;
    
    private JButton filterButton;
    
    private JLabel bulkLabel;
    
    public BulkFilterComponent(BrowserView view){
        this.view=view;
        
        init();
    }
    
    private void init(){
        bulkActionsBox=new JComboBox(bulkOperations);
        bulkActionsBox.addActionListener(this);
        applyBulkActionsButton=new JButton("Apply");
        applyBulkActionsButton.addActionListener(this);
        bulkLabel=new JLabel("");
        
        filterButton=new JButton("Filter");
        filterButton.addActionListener(this);
        
        setLayout(new GridBagLayout());
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        
        add(bulkActionsBox,cons);
        cons.gridx++;
        add(applyBulkActionsButton,cons);
        
        cons.anchor=GridBagConstraints.EAST;
        cons.gridx++;
        cons.fill=GridBagConstraints.BOTH;
        add(new JLabel("\t"),cons); //adds space between apply and filter
        cons.gridx++;
        cons.fill=GridBagConstraints.NONE;
        add(filterButton,cons);
        cons.gridx++;
        add(bulkLabel,cons);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(bulkActionsBox)){
            int index=bulkActionsBox.getSelectedIndex();
            int[] selected=view.getSelected();
            if(selected.length==0){
                bulkLabel.setText("You must select at least one assignment");
            }
            else if(index==1){
                bulkLabel.setText("Will Change "+selected.length+" Assignment Numbers");
            } else if(index==2){
                bulkLabel.setText("Will Change "+selected.length+" Assignment Names");
            } else if(index==3){
                int num=selected.length;
                for(int i=0;i<selected.length;i++){
                    DbxFile f=view.getGui().getManager().getFile(selected[i]);
                    if(!view.getGui().getGrader().gradeWritten(f.getFirstLastName(), f.getAssignmentNumber()))
                            num--;//if theres no grade written we won't delete that one.
                }
                bulkLabel.setText("Will Delete "+num+" Graded Assignments from Dropbox ");
            } else if(index==4){
                bulkLabel.setText("Will Delete "+selected.length+" Assignments from Dropbox"
                        + " (Regardless of if they are graded)");
            }
        }
        else if(e.getSource().equals(applyBulkActionsButton)){
            int index=bulkActionsBox.getSelectedIndex();
            int[] selected=view.getSelected();
            if(selected.length==0||index==0){
                return;
            }
            else if(index==1){ //change assignment number
                bulkLabel.setText("Will Change "+selected.length+" Assignment Numbers");
            } else if(index==2){ //change assignment name
                bulkLabel.setText("Will Change "+selected.length+" Assignment Names");
            } else if(index==3){ //delete
                ArrayList<DbxFile> selectedFiles=new ArrayList();
                for(int i=0;i<selected.length;i++){
                    DbxFile f=view.getGui().getManager().getFile(selected[i]);
                    if(view.getGui().getGrader().gradeWritten(f.getFirstLastName(), f.getAssignmentNumber()))
                            selectedFiles.add(f);//if theres a grade written add it to the list to be deleted
                }
                
            } else if(index==4){
                bulkLabel.setText("Will Delete "+selected.length+" Assignments from Dropbox"
                        + " (Regardless of if they are graded)");
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        actionPerformed(new ActionEvent(bulkActionsBox,0,null));
    }
}
