/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiHelper;
import DropboxGrader.JTerminal;
import DropboxGrader.JavaCodeBrowser;
import DropboxGrader.JavaRunner;
import DropboxGrader.TextGrader.TextGrader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

/**
 *
 * @author Matt
 */
public class GraderView extends ContentView{
    private Gui gui;
    private FileManager fileManager;
    
    private JavaCodeBrowser javaCode;
    private JButton backButton;
    private JButton runButton;
    private JTextField iterationsField;
    private JPanel runPanel;
    private JLabel fileInfoLabel;
    private JavaRunner runner;
    private JTerminal codeOutputArea;
    private JScrollPane codeOutputScroll;
    private JTextField gradeNumber;
    private JTextField gradeComment;
    private JButton recordGradeButton;
    private JLabel gradeStatus;
    private JSplitPane graderDivider;
    
    public GraderView(Gui gui,FileManager fileManager){
        super("GraderView");
        
        this.gui=gui;
        this.fileManager=fileManager;
    }
    @Override
    public void setup() {
        gradeStatus=new JLabel("");
        gradeStatus.setHorizontalAlignment(JLabel.CENTER);
        javaCode=new JavaCodeBrowser(null);
        javaCode.setMinimumSize(new Dimension(300,50));
        codeOutputArea=new JTerminal(gui);
        codeOutputArea.setMinimumSize(new Dimension(100,50));
        codeOutputScroll=new JScrollPane(codeOutputArea);
        runner=new JavaRunner(codeOutputArea,gui,javaCode);   
        gui.getGuiListener().setRunner(runner);
        
        JPanel topBar=new JPanel();
        topBar.setLayout(new GridBagLayout());
        graderDivider=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,javaCode,codeOutputScroll);
        graderDivider.setDividerLocation(Config.dividerLocation);
        JPanel navPanel=new JPanel();
        navPanel.setLayout(new FlowLayout());
        backButton=new JButton("Back to Browser");
        backButton.addActionListener(this);
        fileInfoLabel=new JLabel("");
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
        gradeNumber.setText("");
        gradeNumber.setHorizontalAlignment(JTextField.CENTER);
        gradeNumber.setMinimumSize(new Dimension(30,20));
        gradeNumber.addActionListener(this);
        JLabel gradeLabel2=new JLabel(" Comment: ");
        gradeComment=new JTextField(25);
        gradeComment.setText("");
        gradeComment.setHorizontalAlignment(JTextField.CENTER);
        gradeComment.setMinimumSize(new Dimension(250,20));
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
        
        cons=new GridBagConstraints();
        cons.anchor=GridBagConstraints.WEST;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridx=0;
        cons.gridy=0;
        cons.gridheight=1;
        cons.gridwidth=4;
        cons.weightx=1;
        cons.weighty=GridBagConstraints.RELATIVE;
        add(topBar,cons);
        
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridheight=1;
        cons.gridwidth=4;
        cons.gridx=0;
        cons.gridy=1;
        cons.weighty=0.66;
        add(graderDivider,cons);
        
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.WEST;
        cons.gridx=0;
        cons.gridy=2;
        cons.weighty=GridBagConstraints.RELATIVE;
        cons.gridwidth=1;
        add(gradePanel,cons);
        
        cons.anchor=GridBagConstraints.EAST;
        cons.gridx=1;
        add(gradeButtonPanel,cons);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(runButton)){
            if(runButton.getText().equals("Run")&&!e.getActionCommand().equals("Ended")){
                try{
                    int times=Integer.parseInt(iterationsField.getText().trim());
                    if(times>0){
                        boolean running=fileManager.getFile(gui.getSelectedFiles().get(0)).run(runner,times);
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
            gui.getSelectedFiles().clear();
            gui.setupFileBrowserGui();
        }
        else if(e.getSource().equals(recordGradeButton)){
            TextGrader grader=gui.getGrader();
            DbxFile currentFile=gui.getCurrentFile();
            ArrayList<Integer> selectedFiles=gui.getSelectedFiles();
            
            if(grader==null){
                gradeStatus.setText("Grader has not been initialized.");
                return;
            }
            try{
                int assign=currentFile.getAssignmentNumber();
                boolean success=grader.setGrade(currentFile.getFirstLastName(), 
                        assign, gradeNumber.getText(),gradeComment.getText(),
                        grader.gradeWritten(currentFile.getFirstLastName(), assign));
                if(success){
                    gradeStatus.setText("Graded");
                    if(selectedFiles.size()>1){
                        selectedFiles.remove(0);
                        gui.setCurrentFile(fileManager.getFile(selectedFiles.get(0)));
                        gui.setupGraderGui();
                    }
                    else if(selectedFiles.size()==1){
                        selectedFiles.clear();
                    }
                }
                else{
                    gradeStatus.setText("Canceled Grading");
                }
            } catch(NumberFormatException ex){
                gradeStatus.setText("Error reading assignment number: "+currentFile.getAssignmentNumber());
            }
        }
        else if(e.getSource().equals(gradeComment)||e.getSource().equals(gradeNumber)){ //return was pressed in the text field.
            actionPerformed(new ActionEvent(recordGradeButton,0,null));
        }
    }

    @Override
    public void switchedTo() {
        DbxFile file=fileManager.getFile(gui.getSelectedFiles().get(0));
        if(file==null){
            gui.setupFileBrowserGui();
            GuiHelper.alertDialog("Invalid File");
            return;
        }
        String grade=null,comment=null;
        if(gui.getGrader()!=null){
            grade=gui.getGrader().getGradeNum(file.getFirstLastName(), file.getAssignmentNumber());
            comment=gui.getGrader().getComment(file.getFirstLastName(), file.getAssignmentNumber());
        }
        if(grade==null){
            grade="";
        }
        if(comment==null){
            comment="";
        }
        gradeNumber.setText(grade);
        gradeComment.setText(comment);
        
        javaCode.setFile(file);
        fileInfoLabel.setText(file.toString());
        gradeStatus.setText("");
        codeOutputArea.setText("");
        runButton.setText("Run");
        
        if(Config.autoRun&&!gui.getSelectedFiles().isEmpty()){
            //If it should autorun, go autorun on the other thread.
            runButton.setText("Stop Running");
            gui.getBackgroundThread().runFile(gui.getSelectedFiles().get(0),Config.runTimes);
        }
    }
    public JavaRunner getRunner(){
        return runner;
    }
    public void proccessEnded(){
        actionPerformed(new ActionEvent(runButton,0,"Ended"));
    }
    public JTerminal getTerminal(){
        return codeOutputArea;
    }
    public JSplitPane getDivider(){
        return graderDivider;
    }
    
}
