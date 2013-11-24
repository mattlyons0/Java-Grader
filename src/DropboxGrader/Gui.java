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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
    private Config config;
    
    //First Stage (Authentication) Instance Vars
    private JLabel status;
    private JTextField keyField;
    private JButton submitButton;
    
    //Second Stage (File Browser) Instance Vars
    private JPanel fileBrowserPanel; 
    private FileBrowserData fileBrowserData;
    private JTable fileBrowserTable;
    private JScrollPane fileBrowserScroll;
    private GridBagConstraints constraints;
    private JButton refreshButton;
    private JButton downloadAllButton;
    private JButton configButton;
    private JProgressBar progressBar;
    private JButton gradeButton;
    private JLabel statusText;
    private int selectedFile;
    
    //Third Stage (Grader) Instance Vars
    private DbxFile currentFile;
    private JPanel gradingPanel;
    private JavaCodeBrowser javaCode;
    private JButton backButton;
    private JButton runButton;
    private JTextField iterationsField;
    private JPanel runPanel;
    private JLabel fileInfoLabel;
    private InputRelayer outputRelay;
    private JavaRunner runner;
    private static JTerminal codeOutputArea;
    private JScrollPane codeOutputScroll;
    private JTextField gradeNumber;
    private JTextField gradeComment;
    private JButton recordGradeButton;
    private JLabel gradeStatus;
    public Gui(){
        super("Dropbox Grader");
        config=new Config(); //TODO: make config
        
        UIManager.put("ProgressBar.foreground", new Color(120,200,55)); //color the progressbar green.
        
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
        
        googSession=new GoogSession();
        gradeWriter=new SpreadsheetGrader("APCS PERIOD 4 ASSIGNMENTS",googSession.getService());
    }
    private void createSession(){
        if(client!=null||dbxSession!=null){
            fileManager=new FileManager("DROPitTOme","P2",client,this);
            setupFileBrowserGui();
        }
    }
    
    public void setupFileBrowserGui(){
        if(gradingPanel!=null){
            remove(gradingPanel);
            gradingPanel=null;
        }
        if(status!=null){
            remove(status);
            status=null;
        }
        
        fileBrowserPanel=new JPanel();
        fileBrowserPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        constraints=new GridBagConstraints();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        fileBrowserData=new FileBrowserData(fileManager);
        fileManager.setTableData(fileBrowserData);
        fileBrowserTable=new FileBrowser(fileBrowserData);
        fileBrowserScroll=new JScrollPane(fileBrowserTable);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        fileBrowserScroll.setBounds(0, 0, screenSize.width,screenSize.height);
        
        refreshButton=new JButton("Refresh");
        refreshButton.addActionListener(this);
        downloadAllButton=new JButton("Download All");
        downloadAllButton.addActionListener(this);
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
        fileBrowserPanel.add(downloadAllButton,constraints);
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
        gradingPanel=new JPanel();
        gradingPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        
        javaCode=new JavaCodeBrowser(fileManager.getFile(selectedFile));
        backButton=new JButton("Back to Browser");
        backButton.addActionListener(this);
        fileInfoLabel=new JLabel(fileManager.getFile(selectedFile).toString());
        runButton=new JButton("Run");
        runButton.addActionListener(this);
        iterationsField=new JTextField(2);
        iterationsField.setText("1");
        iterationsField.setToolTipText("Times to run.");
        runPanel=new JPanel();
        runPanel.setLayout(new FlowLayout());
        runPanel.add(runButton);
        runPanel.add(iterationsField);
        JPanel gradePanel=new JPanel();
        gradePanel.setLayout(new FlowLayout());
        JLabel gradeLabel1=new JLabel("Grade: ");
        gradeNumber=new JTextField(3);
        gradeNumber.setHorizontalAlignment(JTextField.CENTER);
        gradeNumber.addActionListener(this);
        gradePanel.add(gradeLabel1);
        gradePanel.add(gradeNumber);
        JLabel gradeLabel2=new JLabel(" Comment: ");
        gradeComment=new JTextField(25);
        gradeComment.setHorizontalAlignment(JTextField.CENTER);
        gradeComment.addActionListener(this);
        gradePanel.add(gradeLabel2);
        gradePanel.add(gradeComment);
        JPanel gradeButtonPanel=new JPanel();
        gradeButtonPanel.setLayout(new FlowLayout());
        gradeStatus=new JLabel("");
        gradeStatus.setHorizontalAlignment(JLabel.CENTER);
        recordGradeButton=new JButton("Grade");
        recordGradeButton.addActionListener(this);
        gradeButtonPanel.add(gradeStatus);
        gradeButtonPanel.add(recordGradeButton);
        
        if(codeOutputArea==null)
            codeOutputArea=new JTerminal(this);
        else
            codeOutputArea.setText("");
        if(codeOutputScroll==null)
            codeOutputScroll=new JScrollPane(codeOutputArea);
        outputRelay=new InputRelayer(codeOutputArea);
        if(runner==null)
            runner=new JavaRunner(codeOutputArea,this,outputRelay);   
        addWindowListener(new GuiListener(this,runner));
        
        constraints.anchor=GridBagConstraints.WEST;
        constraints.fill=GridBagConstraints.NONE;
        constraints.gridheight=1;
        constraints.gridwidth=1;
        //constraints.weightx=0.33;
        constraints.weighty=0.01;
        gradingPanel.add(backButton,constraints);
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.fill=GridBagConstraints.BOTH;
        constraints.gridx=1;
        constraints.gridwidth=1;
        //constraints.weightx=0.33;
        gradingPanel.add(fileInfoLabel,constraints);
        constraints.gridwidth=1;
        //constraints.weightx=0.33;
        constraints.gridx=2;
        constraints.anchor=GridBagConstraints.EAST;
        constraints.fill=GridBagConstraints.NONE;
        gradingPanel.add(runPanel,constraints);
        
        constraints.fill=GridBagConstraints.BOTH;
        constraints.gridheight=1;
        constraints.gridwidth=2;
        constraints.gridx=0;
        constraints.gridy=1;
        constraints.weightx=0.66;
        constraints.weighty=0.98;
        gradingPanel.add(javaCode,constraints);
        
        constraints.gridx=2;
        constraints.weightx=0.33;
        constraints.gridwidth=1;
        gradingPanel.add(codeOutputScroll,constraints);
        
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
        repaint();
        createSession();
    }
    public JavaRunner getRunner(){
        return runner;
    }
    public void updateProgress(double val){
        progressBar.setValue((int)val*100);
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
            fileManager.refresh();
            fileBrowserTable.revalidate();
        }
        else if(e.getSource().equals(downloadAllButton)){
            fileManager.downloadAll();
            fileBrowserTable.repaint();
        }
        else if(e.getSource().equals(configButton)){
            //do config code soon
        }
        else if(e.getSource().equals(gradeButton)){
            int selected=fileBrowserTable.getSelectedRow();
            if(selected==-1){
                statusText.setText("You must select an assignment to grade.");
                return;
            }
            selectedFile=selected;
            fileManager.download(selectedFile);
            currentFile=fileManager.getFile(selectedFile);
            fileBrowserTable.repaint();
            setupGraderGui();
        }
        else if(e.getSource().equals(runButton)){
            if(runButton.getText().equals("Run")&&!e.getActionCommand().equals("Ended")){
                try{
                    int times=Integer.parseInt(iterationsField.getText().trim());
                    if(times>0){
                        boolean running=fileManager.getFile(selectedFile).run(runner,times);
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
            setupFileBrowserGui();
        }
        else if(e.getSource().equals(recordGradeButton)){
            try{
                int assign=Integer.parseInt(currentFile.getAssignmentNumber());
                boolean success=gradeWriter.setGrade(currentFile.getFirstLastName(), assign, gradeNumber.getText(),gradeComment.getText(),gradeStatus);
            } catch(NumberFormatException ex){
                gradeStatus.setText("Error reading assignment number: "+currentFile.getAssignmentNumber());
            }
        }
        else if(e.getSource().equals(gradeComment)||e.getSource().equals(gradeNumber)){ //return was pressed in the text field.
            actionPerformed(new ActionEvent(recordGradeButton,0,null));
        }
    }
    public void proccessEnded(){
        actionPerformed(new ActionEvent(runButton,0,"Ended"));
    }
    public JTerminal getTerminal(){
        return codeOutputArea;
    }
}
