/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.MiscOverlays.ChangeAssignmentOverlay;
import DropboxGrader.GuiElements.MiscOverlays.FilterOverlay;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author 141lyonsm
 */
public class BulkFilterComponent extends JComponent implements ActionListener,ListSelectionListener{
    private final String[] bulkOperations={"Bulk Actions","Change Assignments",
        "Delete", "Forcibly Delete"};
    private BrowserView view;
    
    private JComboBox bulkActionsBox;
    private JButton applyBulkActionsButton;
    private JButton filterButton;
    private JLabel bulkLabel;
    
    private String filterData;
    
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
            if(index==0){
                bulkLabel.setText("");
                return;
            }
            int[] selected=view.getSelected();
            if(selected.length==0)
                bulkLabel.setText("You must select at least one assignment");
            else if(index==1){
                bulkLabel.setText("Will Change "+selected.length+" Assignments");
            } else if(index==2){
                int num=selected.length;
                for(int i=0;i<selected.length;i++){
                    DbxFile f=view.getGui().getManager().getFile(view.getTable().convertRowIndexToModel(selected[i]));
                    if(!view.getGui().getGrader().gradeWritten(f.getFirstLastName(), f.getAssignmentNumber()))
                            num--;//if theres no grade written we won't delete that one.
                }
                bulkLabel.setText("Will Delete "+num+" Graded Assignments from Dropbox ");
            } else if(index==3){
                bulkLabel.setText("Will Delete "+selected.length+" Assignments from Dropbox"
                        + " (Regardless of if they are graded)");
            }
        }
        else if(e.getSource().equals(applyBulkActionsButton)){
            if(!view.getSelectionAllowed())
                return;
            int index=bulkActionsBox.getSelectedIndex();
            final int[] selected=view.getSelected();
            if(selected.length==0){
                bulkLabel.setText("You must select at least one assignment");
                return;
            }
            else if(index==0){
                bulkLabel.setText("You must select an action");
                return;
            }
            else if(index==1){ //change assignment
                final Gui gui=view.getGui();
                final ChangeAssignmentOverlay overlay=new ChangeAssignmentOverlay(gui);
                overlay.setTitle("Change "+selected.length+" Submitted Assignments");
                overlay.setCallback(new Runnable() {
                    @Override
                    public void run() {
                        gui.setStatus("Changing Assignments...");
                        gui.updateProgress(0);
                        
                        ArrayList<DbxFile> files=new ArrayList();
                        for(int i=0;i<selected.length;i++){
                            files.add(gui.getManager().getFile(view.getTable().convertRowIndexToModel(selected[i])));
                        }
                        Integer num=overlay.getAssignment();
                        ArrayList<DbxFile> filesToChangeNumber=new ArrayList();
                        if(num!=null)
                            for(int i=0;i<files.size();i++){
                                filesToChangeNumber.add(files.get(i));
                            }
                        String name=overlay.getAssignmentName();
                        ArrayList<DbxFile> filesToChangeName=new ArrayList();
                        if(name!=null)
                            for(int i=0;i<files.size();i++){
                                filesToChangeName.add(files.get(i));
                            }
                        for(int i=0;i<filesToChangeNumber.size()+filesToChangeName.size();i++){
                            int progress=(int)((double)i/(filesToChangeNumber.size()+filesToChangeName.size())*100);
                            gui.updateProgress(progress);
                            if(i<filesToChangeNumber.size()){
                                filesToChangeNumber.get(i).setAssignmentNumber(num);
                            }
                            else{
                                filesToChangeName.get(i-filesToChangeNumber.size()).setAssignmentName(name);
                            }
                            view.getTable().repaint();
                        }
                        if(num!=null||name!=null)
                            gui.refreshTable();
                        gui.updateProgress(0);
                    }
                });
                gui.getViewManager().addOverlay(overlay);
            } else if(index==2){ //delete
                if(Config.demoMode){
                    view.getGui().setStatus("This normally deletes files on dropbox, but in Demo Mode this functionality has been disabled.");
                    return;
                }   
                if(selected.length==0){
                    view.getGui().setStatus("You must select at least one assignment to delete.");
                }
                ArrayList<DbxFile> select=new ArrayList();
                for(int x=0;x<selected.length;x++){
                    select.add(view.getGui().getManager().getFile(view.getTable().convertRowIndexToModel(selected[x])));
                }
                view.getGui().setStatus("Deleting Files...");
                boolean deleted=false;
                boolean kept=false;
                for(int x=0;x<select.size();x++){ //check if there is a grade for assignment
                    DbxFile f=select.get(x);
                    int progress=(int)(((float)(x+1)/(select.size()))*100);
                    if(f!=null){
                        int assignment=f.getAssignmentNumber();
                        boolean written=view.getGui().getGrader().gradeWritten(f.getFirstLastName(), assignment);
                        if(!written){
                            kept=true;
                            select.remove(x);
                            x--;
                        }
                        else{
                            deleted=true;
                            view.getGui().getBackgroundThread().delete(select.get(x),progress);
                        }
                    }
                }
                for(int x=0;x<select.size();x++){
                    view.getGui().getManager().delete(select.get(x));
                }
                final boolean deletedF=deleted;
                final boolean keptF=kept;
                view.getGui().getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(deletedF)
                            view.getGui().refreshTable();
                        if(deletedF&&keptF){
                            view.getGui().setStatus("Deleted some files, kept other files becuase they weren't graded.");
                        }
                        else if(deletedF){
                            view.getGui().setStatus("Deleted.");
                        }
                        else
                            view.getGui().setStatus("Kept all files becuase none of them were graded.");
                        view.getGui().updateProgress(0);
                    }
                });
            } else if(index==3){ //force delete
                if(Config.demoMode){
                    view.getGui().setStatus("This normally deletes files on dropbox, but in Demo Mode this functionality has been disabled.");
                    return;
                }   
                if(selected.length==0){
                    return;
                }
                ArrayList<DbxFile> select=new ArrayList();
                for(int x=0;x<selected.length;x++){
                    select.add(view.getGui().getManager().getFile(view.getTable().convertRowIndexToModel(selected[x])));
                }
                view.getGui().setStatus("Deleting Files...");
                boolean deleted=false;
                for(int x=0;x<select.size();x++){ //check if there is a grade for assignment
                    DbxFile f=select.get(x);
                    int progress=(int)(((float)(x+1)/(select.size()))*100);
                    if(f!=null){
                        deleted=true;
                        view.getGui().getBackgroundThread().delete(select.get(x),progress);
                    }
                }
                for(int x=0;x<select.size();x++){
                    view.getGui().getManager().delete(select.get(x));
                }
                final boolean deletedF=deleted;
                view.getGui().getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(deletedF){
                            view.getGui().refreshTable();
                            view.getGui().setStatus("Deleted.");
                        }
                        
                        view.getGui().updateProgress(0);
                    }
                });
            }
        }
        else if(e.getSource().equals(filterButton)){
            view.getGui().getViewManager().removeOverlay("FilterOverlay");
            
            FilterOverlay overlay=new FilterOverlay(this,view.getGui());
            view.getGui().getViewManager().addOverlay(overlay);
            if(filterData!=null)
                overlay.setData(filterData);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        actionPerformed(new ActionEvent(bulkActionsBox,0,null));
    }
    public void setFilter(RowFilter filter){
        TableRowSorter sorter=(TableRowSorter)view.getTable().getRowSorter();
        sorter.setRowFilter(filter);
    }
    public void setFilterText(String text){
        filterButton.setText(text);
    }
    public void setFilterData(String data){
        filterData=data;
    }
}
