/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.dropbox.core.DbxClient;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

/**
 *
 * @author Matt
 */
public class Gui extends JFrame implements ActionListener{
    //Raw Data Instance Vars
    private FileManager fileManager;
    private DbxSession dbxSession;
    private DbxClient client;
    private GoogSession googSession;
    private SpreadsheetGrader gradeWriter;
    private WorkerThread workerThread;
    private GuiListener listener;
    
    //First Stage (Authentication) Instance Vars
    private JLabel status;
    private JTextField keyField;
    private JButton submitButton;
    
    //Second Stage (File Browser) Instance Vars
    private JPanel fileBrowserPanel; 
    private FileBrowserData fileBrowserData;
    private FileBrowser fileBrowserTable;
    private JScrollPane fileBrowserScroll;
    private FileBrowserListener fileBrowserListener;
    private GridBagConstraints constraints;
    private JButton refreshButton;
    private JButton deleteButton;
    private JButton configButton;
    private JProgressBar progressBar;
    private JButton gradeButton;
    private JLabel statusText;
    private ArrayList<Integer> selectedFiles;
    private ArrayList<Integer> previousSelection;
    
    //Third Stage (Grader) Instance Vars
    private DbxFile currentFile;
    private JPanel gradingPanel;
    private JavaCodeBrowser javaCode;
    private JButton backButton;
    private JButton runButton;
    private JTextField iterationsField;
    private JPanel runPanel;
    private JLabel fileInfoLabel;
    private JavaRunner runner;
    private static JTerminal codeOutputArea;
    private JScrollPane codeOutputScroll;
    private JTextField gradeNumber;
    private JTextField gradeComment;
    private JButton recordGradeButton;
    private JLabel gradeStatus;
    private JSplitPane graderDivider;
    
    //Config Instance Vars
    private JPanel configPanel;
    private JTextField spreadsheetName;
    private JTextField dropboxFolder;
    private JTextField dropboxPeriod;
    private JTextField runTimes;
    private JCheckBox autoRun;
    private JButton backToBrowser;
    
    public Gui(){
        super("Dropbox Grader");
        
        UIManager.put("ProgressBar.foreground", new Color(120,200,55)); //color the progressbar green.
        previousSelection=new ArrayList();
        
        listener=new GuiListener(this);
        addWindowListener(listener);
        getContentPane().addComponentListener(listener);
        addWindowStateListener(listener);
        
        init();
    }
    private void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(375,130);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        status=new JLabel("Connecting to Dropbox...");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(status);
        
        setVisible(true);
        
        dbxSession=new DbxSession(this);
    }
    private void initGoogSession(){
        googSession=new GoogSession();
        gradeWriter=new SpreadsheetGrader(Config.spreadsheetName,googSession.getService(),this);
        fileManager.setGrader(gradeWriter);
        setupFileBrowserGui();
    }
    private void createSession(){
        if(client!=null||dbxSession!=null){
            fileManager=new FileManager(Config.dropboxFolder,Config.dropboxPeriod,client,this);
            workerThread=new WorkerThread(fileManager,this);
            new Thread(workerThread).start();
        }
    }
    
    public void setupFileBrowserGui(){
        if(gradingPanel!=null){
            remove(gradingPanel);
            gradingPanel=null;
            Config.dividerLocation=graderDivider.getDividerLocation();
        }
        if(status!=null){
            remove(status);
            status=null;
        }
        if(configPanel!=null){
            remove(configPanel);
            configPanel=null;
        }
        if(fileBrowserPanel!=null){
            remove(fileBrowserPanel);
            fileBrowserPanel=null;
        }
        
        fileBrowserPanel=new JPanel();
        fileBrowserPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        constraints=new GridBagConstraints();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        fileBrowserData=new FileBrowserData(fileManager);
        fileManager.setTableData(fileBrowserData);
        fileBrowserListener=new FileBrowserListener(this);
        fileBrowserTable=new FileBrowser(fileBrowserData,fileBrowserListener);
        fileBrowserScroll=new JScrollPane(fileBrowserTable);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        fileBrowserScroll.setBounds(0, 0, screenSize.width,screenSize.height);
        
        refreshButton=new JButton("Refresh");
        refreshButton.addActionListener(this);
        deleteButton=new JButton("Delete");
        deleteButton.addActionListener(this);
        if(statusText==null)
            statusText=new JLabel("");
        configButton=new JButton("Settings");
        configButton.addActionListener(this);
        progressBar=new JProgressBar(0,0,100);
        gradeButton=new JButton("Grade");
        gradeButton.addActionListener(this);
        
        constraints.anchor=GridBagConstraints.WEST;
        constraints.ipady=5;
        constraints.ipadx=10;
        constraints.gridx=0;
        constraints.gridy=0;
        //constraints.weightx=0.05;
        constraints.weighty=0.01;
        fileBrowserPanel.add(refreshButton,constraints);
        constraints.gridx=1;
        fileBrowserPanel.add(deleteButton,constraints);
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.gridx=3;
        fileBrowserPanel.add(statusText,constraints);
        constraints.anchor=GridBagConstraints.EAST;
        constraints.gridx=4;
        //constraints.weightx=0.9;
        fileBrowserPanel.add(configButton,constraints);
        constraints.fill=GridBagConstraints.BOTH;
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.gridx=0;
        constraints.gridy=1;
        constraints.gridwidth=5;
        constraints.weightx=100;
        constraints.weighty=0.9;
        fileBrowserPanel.add(fileBrowserScroll,constraints);
        constraints.gridwidth=4;
        constraints.gridy=2;
        constraints.fill=GridBagConstraints.HORIZONTAL;
        constraints.weighty=0.01;
        constraints.weightx=1;
        fileBrowserPanel.add(progressBar,constraints);
        constraints.weightx=0.01;
        constraints.gridwidth=1;
        constraints.gridx=4;
        constraints.gridwidth=1;
        constraints.anchor=GridBagConstraints.EAST;
        constraints.fill=GridBagConstraints.NONE;
        fileBrowserPanel.add(gradeButton,constraints);
        
        constraints=new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH;
        constraints.weightx=1;
        constraints.weighty=1;
        add(fileBrowserPanel,constraints);
        
        usePreviousSelection();
        
        revalidate();
    }
    public void setupGraderGui(){
        if(fileBrowserPanel!=null){
            remove(fileBrowserPanel);
            fileBrowserPanel=null;
        }
        if(runner!=null){
            runner.stopProcess();
        }
        if(gradingPanel!=null){
            remove(gradingPanel);
            gradingPanel=null;
        }
        DbxFile file=fileManager.getFile(selectedFiles.get(0));
        if(file==null){
            setupFileBrowserGui();
            statusText.setText("Invalid File");
            return;
        }
        gradeStatus=new JLabel("");
        gradeStatus.setHorizontalAlignment(JLabel.CENTER);
        String[] grade=null;
        if(gradeWriter!=null){
            grade=gradeWriter.getEntryAt(file.getFirstLastName(), file.getAssignmentNumber(), gradeStatus);
        }
        if(grade==null){
            grade=new String[2];
            grade[0]="";
            grade[1]="";
        }
        javaCode=new JavaCodeBrowser(file);
        javaCode.setMinimumSize(new Dimension(300,50));
        if(codeOutputArea==null)
            codeOutputArea=new JTerminal(this);
        else
            codeOutputArea.setText("");
        codeOutputArea.setMinimumSize(new Dimension(100,50));
        if(codeOutputScroll==null)
            codeOutputScroll=new JScrollPane(codeOutputArea);
        if(runner==null)
            runner=new JavaRunner(codeOutputArea,this,javaCode);   
        listener.setRunner(runner);
        
        gradingPanel=new JPanel();
        gradingPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        JPanel topBar=new JPanel();
        topBar.setLayout(new GridBagLayout());
        graderDivider=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,javaCode,codeOutputScroll);
        graderDivider.setDividerLocation(Config.dividerLocation);
        JPanel navPanel=new JPanel();
        navPanel.setLayout(new FlowLayout());
        backButton=new JButton("Back to Browser");
        backButton.addActionListener(this);
        fileInfoLabel=new JLabel(file.toString());
        fileInfoLabel.setMinimumSize(new Dimension(10,10));
        navPanel.add(backButton);
        navPanel.add(fileInfoLabel);
        navPanel.setMinimumSize(new Dimension(175,35));
        runButton=new JButton("Run");
        if(Config.autoRun)
            runButton.setText("Stop Running");
        runButton.addActionListener(this);
        iterationsField=new JTextField(3);
        iterationsField.setText(Config.runTimes+"");
        iterationsField.setToolTipText("Times to run.");
        iterationsField.setMaximumSize(new Dimension(300,10));
        iterationsField.setHorizontalAlignment(JTextField.CENTER);
        runPanel=new JPanel();
        runPanel.setLayout(new GridBagLayout());
        runPanel.setMinimumSize(new Dimension(100,25));
        
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.weightx=1;
        runPanel.add(runButton,cons);
        cons.gridx=1;
        cons.ipadx=10;
        runPanel.add(iterationsField,cons);
        JPanel gradePanel=new JPanel();
        gradePanel.setLayout(new GridBagLayout());
        JLabel gradeLabel1=new JLabel("Grade: ");
        gradeNumber=new JTextField(3);
        gradeNumber.setText(grade[0]);
        gradeNumber.setHorizontalAlignment(JTextField.CENTER);
        gradeNumber.setMinimumSize(new Dimension(30,15));
        gradeNumber.addActionListener(this);
        JLabel gradeLabel2=new JLabel(" Comment: ");
        gradeComment=new JTextField(25);
        gradeComment.setText(grade[1]);
        gradeComment.setHorizontalAlignment(JTextField.CENTER);
        gradeComment.setMinimumSize(new Dimension(250,15));
        gradeComment.addActionListener(this);
        cons=new GridBagConstraints();
        cons.fill=GridBagConstraints.BOTH;
        cons.weightx=1;
        gradePanel.add(gradeLabel1,cons);
        cons.gridx=1;
        gradePanel.add(gradeNumber,cons);
        cons.gridx=2;
        gradePanel.add(gradeLabel2,cons);
        cons.gridx=3;
        gradePanel.add(gradeComment,cons);
        JPanel gradeButtonPanel=new JPanel();
        gradeButtonPanel.setLayout(new FlowLayout());
        recordGradeButton=new JButton("Grade");
        recordGradeButton.addActionListener(this);
        gradeButtonPanel.add(gradeStatus);
        gradeButtonPanel.add(recordGradeButton);
        
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.WEST;
        cons.gridx=0;
        cons.gridy=0;
        cons.gridheight=1;
        cons.gridwidth=2;
        topBar.add(navPanel,cons);
        cons.gridx=2;
        cons.gridwidth=1;
        cons.anchor=GridBagConstraints.EAST;
        topBar.add(runPanel,cons);
        
        constraints.anchor=GridBagConstraints.WEST;
        constraints.fill=GridBagConstraints.BOTH;
        constraints.gridx=0;
        constraints.gridy=0;
        constraints.gridheight=1;
        constraints.gridwidth=4;
        //constraints.weightx=0.33;
        constraints.weighty=0.01;
        gradingPanel.add(topBar,constraints);
        
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.fill=GridBagConstraints.BOTH;
        constraints.gridheight=1;
        constraints.gridwidth=4;
        constraints.gridx=0;
        constraints.gridy=1;
        //constraints.weightx=0.66;
        constraints.weighty=0.98;
        gradingPanel.add(graderDivider,constraints);
        
        constraints.fill=GridBagConstraints.NONE;
        constraints.anchor=GridBagConstraints.WEST;
        constraints.gridx=0;
        constraints.gridy=2;
        constraints.weighty=0.01;
        constraints.gridwidth=1;
        gradingPanel.add(gradePanel,constraints);
        
        constraints.anchor=GridBagConstraints.EAST;
        constraints.gridx=1;
        gradingPanel.add(gradeButtonPanel,constraints);
        
        constraints=new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH;
        constraints.weightx=1;
        constraints.weighty=1;
        add(gradingPanel,constraints);
        
        revalidate();
        
        if(Config.autoRun){
            //put this on the new thread
            workerThread.runFile(selectedFiles.get(0),Config.runTimes);
        }
    }
    public void setupConfigGui(){
        if(fileBrowserPanel!=null){
            remove(fileBrowserPanel);
            fileBrowserPanel=null;
        }
        setLayout(new GridBagLayout());
        configPanel=new JPanel();
        configPanel.setLayout(new GridBagLayout());
        spreadsheetName=new JTextField(25);
        spreadsheetName.setText(Config.spreadsheetName);
        dropboxFolder=new JTextField(25);
        dropboxFolder.setText(Config.dropboxFolder);
        dropboxPeriod=new JTextField(3);
        dropboxPeriod.setText(Config.dropboxPeriod);
        runTimes=new JTextField(3);
        runTimes.setText(Config.runTimes+"");
        autoRun=new JCheckBox("AutoRun");
        autoRun.setSelected(Config.autoRun);     
        backToBrowser=new JButton("Back");
        backToBrowser.addActionListener(this);
        JLabel creditsLabel=new JLabel("Created by Matt Lyons, Class of 2014");
        creditsLabel.setHorizontalAlignment(JLabel.CENTER);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.NORTHWEST;
        cons.weighty=1;
        cons.weightx=1;
        cons.gridx=0;
        cons.gridy=0;
        configPanel.add(backToBrowser,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridy=1;
        configPanel.add(new JLabel("Spreadsheet Name: "),cons);
        cons.gridx=1;
        configPanel.add(spreadsheetName,cons);
        cons.gridx=0;
        cons.gridy=2;
        configPanel.add(new JLabel("Dropbox Folder: "),cons);
        cons.gridx=1;
        configPanel.add(dropboxFolder,cons);
        cons.gridx=2;
        configPanel.add(new JLabel("Class Period: "),cons);
        cons.gridx=3;
        configPanel.add(dropboxPeriod,cons);
        cons.gridy=3;
        cons.gridx=0;
        configPanel.add(new JLabel("Default Output Runs: "),cons);
        cons.gridx=1;
        configPanel.add(runTimes,cons);
        cons.gridx=3;
        configPanel.add(autoRun,cons);
        cons.gridy=4;
        cons.gridx=0;
        cons.gridwidth=4;
        configPanel.add(creditsLabel,cons);
        cons=new GridBagConstraints();
        cons.fill=GridBagConstraints.BOTH;
        cons.weightx=1;
        constraints.weighty=1;
        add(configPanel,cons);
        
        setSize(620,150);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
        
        revalidate();
        repaint();
    }
    public void promptKey(){
        status.setText("Please login and paste the code here: ");
        keyField=new JTextField(30);
        keyField.addActionListener(this);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        add(keyField);
        add(submitButton);

        revalidate();
    }
    public void badKey(){
        if(!status.getText().contains("Invalid Key, "))
        status.setText("Invalid Key, "+status.getText());
    }
    public void goodKey(String loginName,DbxClient client){
        this.client=client;
        status.setText("Logged in as "+loginName+". Building File Directory...");
        if(keyField!=null)
            remove(keyField);
        if(submitButton!=null)
            remove(submitButton);
        createSession();
        initGoogSession();
    }
    public SpreadsheetGrader getGrader(){
        return gradeWriter;
    }
    public JavaRunner getRunner(){
        return runner;
    }
    public void updateProgress(int val){
        progressBar.setValue(val);
    }
    public void repaintTable(){
        if(fileBrowserTable!=null){
            fileBrowserData.refresh();
            fileBrowserTable.revalidate();
            fileBrowserTable.repaint();
        }
    }
    public void refreshTable(){
        if(statusText!=null)
            statusText.setText("Refreshing File Listings...");
        saveSelection();
        if(fileBrowserTable!=null){
            fileBrowserTable.setRowSelectionAllowed(false);
        }
        
        if(workerThread!=null){
            workerThread.refreshData();
        }
        else{
            if(statusText!=null)
            statusText.setText("Try again in a few seconds.");
        }
    }
    public void refreshFinished(){
        if(fileBrowserData==null||fileBrowserListener==null){
            return;
        }
        fileBrowserTable.setRowSelectionAllowed(true);
        fileBrowserTable.dataChanged();
        usePreviousSelection();
        
        if(statusText!=null)
            statusText.setText("");
    }
    public void setStatus(String status){
        if(this.status!=null){
            this.status.setText(status);
        }
        else if(statusText!=null){
            statusText.setText(status);
        }
    }
    public void saveSelection(){
        if(fileBrowserTable==null){
            return;
        }
        int[] rows=fileBrowserTable.getSelectedRows();
        for(int r:rows){
            previousSelection.add(r);
        }
    }
    public void usePreviousSelection(){
        if(fileBrowserTable==null){
            return;
        }
        if(!previousSelection.isEmpty()){
            for(int s:previousSelection){
                try{
                    fileBrowserTable.setRowSelectionInterval(s,s);
                } catch(IllegalArgumentException ex){
                    //whatever, we wont remember the row then.
                }
            }
            previousSelection.clear();
        }
    }
    public void gradeRows(){
        actionPerformed(new ActionEvent(gradeButton,0,null));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(statusText!=null){
            statusText.setText("");
        }
        
        if(e.getSource().equals(submitButton)){
            if(fileManager==null){
                dbxSession.setKey(keyField.getText());
            }
        }
        else if(e.getSource().equals(keyField)){ //return throws the action event
            actionPerformed(new ActionEvent(submitButton,0,""));
        }
        else if(e.getSource().equals(refreshButton)){
            refreshTable();
        }
        else if(e.getSource().equals(deleteButton)){
            int[] selected=fileBrowserTable.getSelectedRows();
            if(selected.length==0){
                statusText.setText("You must select at least one assignment to delete.");
            }
            ArrayList<Integer> select=new ArrayList();
            for(int x=0;x<selected.length;x++){
                select.add(fileBrowserTable.convertRowIndexToModel(selected[x]));
            }
            boolean deleted=false;
            boolean kept=false;
            for(int x=0;x<select.size();x++){ //check if there is a grade for assignment
                int i=select.get(x);
                DbxFile f=fileManager.getFile(i);
                if(f!=null){
                    int assignment=Integer.parseInt(f.getAssignmentNumber());
                    boolean written=gradeWriter.gradeWritten(f.getFirstLastName(), assignment,new JLabel());
                    if(!written){
                        kept=true;
                        select.remove(x);
                        x--;
                    }
                    else{
                        deleted=true;
                        workerThread.delete(select.get(x));
                    }
                }
            }
            for(int x=0;x<select.size();x++){
                fileManager.delete(fileManager.getFile(select.get(x)));
            }
            if(deleted)
                setupFileBrowserGui();
            if(deleted&&kept){
                statusText.setText("Deleted some files, kept other files becuase they weren't graded.");
            }
            else if(deleted){
                statusText.setText("Deleted.");
            }
            else
                statusText.setText("Kept all files becuase none of them were graded.");
        }
        else if(e.getSource().equals(configButton)){
            setupConfigGui();
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
            selectedFiles=new ArrayList();
            for(int x=0;x<selected.length;x++){
                selectedFiles.add(fileBrowserTable.convertRowIndexToModel(selected[x]));
                previousSelection.add(selected[x]);
            }
            
            workerThread.download(selectedFiles,true);
            
            currentFile=fileManager.getFile(selectedFiles.get(0));
        }
        else if(e.getSource().equals(runButton)){
            if(runButton.getText().equals("Run")&&!e.getActionCommand().equals("Ended")){
                try{
                    int times=Integer.parseInt(iterationsField.getText().trim());
                    if(times>0){
                        boolean running=fileManager.getFile(selectedFiles.get(0)).run(runner,times);
                        if(running)
                            runButton.setText("Stop Running");
                    }
                    else{
                        codeOutputArea.append("Cannot run less than 1 time.\n");
                    }
                }
                catch(NumberFormatException ex){
                    codeOutputArea.append("Times to run must be a number.\n");
                }
            }
            else{
                runner.stopProcess();
                runButton.setText("Run");
            }
        }
        else if(e.getSource().equals(backButton)){
            runner.stopProcess();
            selectedFiles.clear();
            setupFileBrowserGui();
        }
        else if(e.getSource().equals(recordGradeButton)){
            if(gradeWriter==null){
                gradeStatus.setText("You have not authenticated your google account with this grader yet.");
                return;
            }
            try{
                int assign=Integer.parseInt(currentFile.getAssignmentNumber());
                System.out.println(assign);
                boolean success=gradeWriter.setGrade(currentFile.getFirstLastName(), assign, gradeNumber.getText(),gradeComment.getText(),gradeStatus);
                if(success){
                    if(selectedFiles.size()>1){
                        selectedFiles.remove(0);
                        currentFile=fileManager.getFile(selectedFiles.get(0));
                        setupGraderGui();
                    }
                    else if(selectedFiles.size()==1){
                        selectedFiles.clear();
                    }
                }
            } catch(NumberFormatException ex){
                gradeStatus.setText("Error reading assignment number: "+currentFile.getAssignmentNumber());
            }
        }
        else if(e.getSource().equals(gradeComment)||e.getSource().equals(gradeNumber)){ //return was pressed in the text field.
            actionPerformed(new ActionEvent(recordGradeButton,0,null));
        }
        else if(e.getSource().equals(backToBrowser)){
            Config.spreadsheetName=spreadsheetName.getText();
            Config.dropboxFolder=dropboxFolder.getText();
            Config.dropboxPeriod=dropboxPeriod.getText();
            try{
                Config.runTimes=Integer.parseInt(runTimes.getText());
            } catch(NumberFormatException ex){
                statusText.setText("Default Output Runs was set to a invalid number.");
            }
            Config.autoRun=autoRun.isSelected();
            Config.writeConfig();
            
            fileManager=new FileManager(Config.dropboxFolder,Config.dropboxPeriod,client,this);
            fileManager.setGrader(gradeWriter);
            setupFileBrowserGui();
        }
    }
    public void proccessEnded(){
        actionPerformed(new ActionEvent(runButton,0,"Ended"));
    }
    public JTerminal getTerminal(){
        return codeOutputArea;
    }
    public FileManager getManager(){
        return fileManager;
    }
    public void isClosing(){
        if(graderDivider!=null)
            Config.dividerLocation=graderDivider.getDividerLocation();
    }
    public WorkerThread getBackgroundThread(){
        return workerThread;
    }
}
