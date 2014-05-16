/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.FileBrowser;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.FileManagement.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.WorkerThread;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author 141lyonsm
 */
public class BrowserView extends ContentView{
    private Gui gui;
    private FileManager fileManager;
    
    private FileBrowserData fileBrowserData;
    private FileBrowserTable fileBrowserTable;
    private JScrollPane fileBrowserScroll;
    private FileBrowserListener fileBrowserListener;
    private GridBagConstraints constraints;
    private JButton refreshButton;
    private JButton spreadsheetButton;
    private JButton configButton;
    private JProgressBar progressBar;
    private JButton gradeButton;
    private JLabel statusText;
    private BulkFilterComponent bulkActions;
    
    
    public BrowserView(Gui gui,FileManager manager){
        super("BrowserView");
        
        this.gui=gui;
        fileManager=manager;
    }
    @Override
    public void setup() {        
        constraints=new GridBagConstraints();
        bulkActions=new BulkFilterComponent(this);
        fileBrowserData=new FileBrowserData(fileManager);
        fileManager.setTableData(fileBrowserData);
        fileBrowserListener=new FileBrowserListener(gui);
        fileBrowserTable=new FileBrowserTable(fileBrowserData,fileBrowserListener);
        fileBrowserTable.getSelectionModel().addListSelectionListener(bulkActions);
        fileBrowserScroll=new JScrollPane(fileBrowserTable);
        
        refreshButton=new JButton("Refresh");
        refreshButton.addActionListener(this);
        if(statusText==null)
            statusText=new JLabel("");
        spreadsheetButton=new JButton("Gradebook");
        spreadsheetButton.addActionListener(this);
        configButton=new JButton("Settings");
        configButton.addActionListener(this);
        progressBar=new JProgressBar(0,0,100);
        gradeButton=new JButton("Grade");
        gradeButton.addActionListener(this);
        
        constraints.anchor=GridBagConstraints.WEST;
        constraints.insets=new Insets(5,5,0,5);
        constraints.gridx=0;
        constraints.gridy=0;
        //constraints.weightx=0.05;
        constraints.weighty=GridBagConstraints.RELATIVE;
        constraints.gridwidth=1;
        add(refreshButton,constraints);
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.gridx=3;
        add(statusText,constraints);
        constraints.anchor=GridBagConstraints.EAST;
        constraints.gridx=4;
        add(spreadsheetButton,constraints);
        constraints.gridx=5;
        //constraints.weightx=0.9;
        add(configButton,constraints);
        constraints.gridx=0;
        constraints.gridy++;
        constraints.gridwidth=6;
        constraints.insets=new Insets(0,0,0,0);
        constraints.anchor=GridBagConstraints.WEST;
        add(bulkActions,constraints);
        constraints.insets=new Insets(5,5,5,5);
        constraints.gridwidth=1;
        constraints.fill=GridBagConstraints.BOTH;
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.insets=new Insets(0,5,5,5);
        constraints.gridx=0;
        constraints.gridy++;
        constraints.gridwidth=6;
        constraints.weightx=100;
        constraints.weighty=0.9;
        add(fileBrowserScroll,constraints);
        constraints.gridwidth=5;
        constraints.gridy++;
        constraints.fill=GridBagConstraints.HORIZONTAL;
        constraints.weighty=GridBagConstraints.RELATIVE;
        constraints.ipady=5;
        constraints.weightx=1;
        add(progressBar,constraints);
        constraints.ipady=0;
        constraints.weightx=GridBagConstraints.RELATIVE;
        constraints.gridwidth=1;
        constraints.gridx=5;
        constraints.gridwidth=1;
        constraints.anchor=GridBagConstraints.EAST;
        constraints.fill=GridBagConstraints.NONE;
        add(gradeButton,constraints);
        
        revalidate();
    }
    public void refreshTable(){
        WorkerThread workerThread=gui.getBackgroundThread();
        if(statusText!=null)
            statusText.setText("Refreshing File Listings...");
        if(fileBrowserTable!=null){
            fileBrowserTable.setRowSelectionAllowed(false);
        }
        
        if(workerThread!=null){
            workerThread.refreshData();
        }
        else{
            if(fileManager!=null)
                fileManager.refresh();
            refreshFinished();
        }
    }
    public void refreshFinished(){
        if(fileBrowserData==null||fileBrowserListener==null||fileBrowserTable==null){
            return;
        }
        fileBrowserTable.setRowSelectionAllowed(true);
    }
    public void dataChanged(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fileBrowserTable.dataChanged();
                gui.getManager().refreshCellColors();
                if(statusText!=null)
                    statusText.setText("");
            }
        });
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(refreshButton)){
            refreshTable();
        }
        else if(e.getSource().equals(configButton)){
            gui.setupConfigGui();
        }
        else if(e.getSource().equals(gradeButton)){
            if(!fileBrowserTable.getRowSelectionAllowed()){
                return;
            }
            int[] selected=fileBrowserTable.getSelectedRows();
            if(selected.length==0){
                statusText.setText("You must select at least one assignment to grade.");
                return;
            }
            for(int element:selected){
                gui.getSelectedFiles().add(fileBrowserTable.convertRowIndexToModel(element));
            }
            setStatus("Opening "+fileManager.getFile(gui.getSelectedFiles().get(0)).getFileName()+" ...");
            gui.getBackgroundThread().download(gui.getSelectedFiles(),true);
            
            gui.setCurrentFile(fileManager.getFile(gui.getSelectedFiles().get(0)));
        }
        else if(e.getSource().equals(spreadsheetButton)){
            gui.setupGradebookGui();
        }
    }

    @Override
    public void switchedTo() {
        //statusText.setText("");
        progressBar.setValue(0);
    }

    public void updateProgress(int val) {
        progressBar.setValue(val);
    }
    public FileBrowserTable getTable(){
        return fileBrowserTable;
    }
    public FileBrowserData getTableData(){
        return fileBrowserData;
    }
    public void setStatus(String status){
        statusText.setText(status);
    }
    public void gradeRows(){
        actionPerformed(new ActionEvent(gradeButton,0,null));
    }

    public JButton getGradebookButton(){
        return spreadsheetButton;
    }
    public Gui getGui(){
        return gui;
    }
    public int[] getSelected(){
        return fileBrowserTable.getSelectedRows();
    }
}
