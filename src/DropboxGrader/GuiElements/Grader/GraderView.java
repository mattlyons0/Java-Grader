/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.Grader;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.GuiElements.MiscComponents.JGhostTextField;
import DropboxGrader.GuiHelper;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.TextGrader.TextGrader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

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
    private JTextPane gradeComment;
    private JButton recordGradeButton;
    private JLabel gradeStatus;
    private JSplitPane graderDivider;
    private JSplitPane gradeWriteDivider;
    private JPanel codeSortPanel;
    private JComboBox codeSortMode;
    private JComboBox codeSortOrder;
    
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
        JPanel sortPanel=new JPanel();
        sortPanel.setLayout(new FlowLayout());
        codeSortPanel=new JPanel();
        codeSortPanel.setLayout(new GridBagLayout());
        codeSortMode=new JComboBox(JavaCodeBrowser.sortModes);
        codeSortMode.addActionListener(this);
        codeSortOrder=new JComboBox(new String[] {"Ascending","Descending"});
        codeSortOrder.addActionListener(this);
        JLabel sortLabel=new JLabel("Sort: ");
        sortPanel.add(sortLabel);
        sortPanel.add(codeSortMode);
        sortPanel.add(codeSortOrder);
        GridBagConstraints cons=new GridBagConstraints();
        cons.anchor=GridBagConstraints.NORTHEAST;
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        cons.gridx=0;
        cons.gridy=0;
        cons.weighty=1;
        cons.weightx=1;
        codeSortPanel.add(sortPanel,cons);
        cons.insets=new Insets(0,0,0,0);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.weighty=999;
        cons.gridy=1;
        cons.gridx=0;
        codeSortPanel.add(javaCode,cons);
        codeOutputArea=new JTerminal();
        codeOutputArea.setMinimumSize(new Dimension(100,50));
        codeOutputScroll=new JScrollPane(codeOutputArea);
        runner=new JavaRunner(codeOutputArea,gui);   
        gui.getGuiListener().setRunner(runner);
        
        JPanel topBar=new JPanel();
        topBar.setLayout(new GridBagLayout());
        graderDivider=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,codeSortPanel,codeOutputScroll);
        graderDivider.setDividerLocation(Config.dividerLocation);
        graderDivider.setContinuousLayout(true);
        JPanel navPanel=new JPanel();
        navPanel.setLayout(new FlowLayout());
        backButton=new JButton("Back to Browser");
        backButton.addActionListener(this);
        fileInfoLabel=new JLabel("");
        //fileInfoLabel.setMinimumSize(new Dimension(10,10));
        //fileInfoLabel.setPreferredSize(new Dimension(100,10));
        navPanel.add(backButton);
        navPanel.add(fileInfoLabel);
        //navPanel.setMinimumSize(new Dimension(175,35)); //causes scrollbar in code to break gui
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
        
        
        cons=new GridBagConstraints();
        cons.weightx=1;
        runPanel.add(runButton,cons);
        cons.gridx=1;
        cons.ipadx=10;
        runPanel.add(iterationsField,cons);
        
        gradeNumber=new JGhostTextField(6,"Grade");
        gradeNumber.setText("");
        gradeNumber.setHorizontalAlignment(JTextField.CENTER);
        gradeNumber.setMinimumSize(new Dimension(30,30));
        gradeNumber.addActionListener(this);
        gradeNumber.setDocument(new PlainDocument(){
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                for(int i=0;i<str.length();i++){
                    char c=str.charAt(i);
                    if(Character.isDigit(c)||c=='.'){
                        if(c=='.'){
                            if(gradeNumber.getText().contains(".")){
                                return;
                            }
                        }
                        super.insertString(offs, str, a);
                    }
                }
            }
        });
        JLabel gradeLabel2=new JLabel(" Comment: ");
        gradeComment=new JTextPane();
        gradeComment.setText("");
        gradeComment.setMinimumSize(new Dimension(250,30));
        JScrollPane gradeCommentScroll=new JScrollPane(gradeComment);
        
        JPanel gradePanel=new JPanel();
        gradePanel.setLayout(new GridBagLayout());
        cons=new GridBagConstraints();
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.WEST;
        cons.weightx=0.01;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        gradePanel.add(new JLabel("Grade: "),cons);
        cons.gridx++;
        gradePanel.add(gradeNumber,cons);
        cons.gridx++;
        gradePanel.add(gradeLabel2,cons);
        cons.gridx++;
        cons.weighty=99;
        cons.fill=GridBagConstraints.BOTH;
        cons.weightx=1;
        gradePanel.add(gradeCommentScroll,cons);
        cons.gridx++;
        cons.weightx=1000;
        gradePanel.add(new JLabel(" "));
        cons.weightx=0.01;
        cons.fill=GridBagConstraints.NONE;
        cons.gridx++;
        cons.anchor=GridBagConstraints.EAST;
        cons.weightx=1;
        recordGradeButton=new JButton("Grade");
        recordGradeButton.addActionListener(this);
        gradePanel.add(gradeStatus,cons);
        cons.gridx++;
        gradePanel.add(recordGradeButton,cons);
        
        gradeWriteDivider=new JSplitPane(JSplitPane.VERTICAL_SPLIT,graderDivider,gradePanel);
        gradeWriteDivider.setDividerLocation(Config.bottomDividerLocation);
        gradeWriteDivider.setContinuousLayout(true);
        
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
        add(gradeWriteDivider,cons);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(runButton)){
            if(runButton.getText().equals("Run")&&!e.getActionCommand().equals("Ended")){
                String errorSaving=javaCode.saveFile();
                if(errorSaving!=null&&!errorSaving.trim().equals(""))
                    codeOutputArea.append("Error saving file: "+errorSaving,Color.RED);
                try{
                    int times=Integer.parseInt(iterationsField.getText().trim());
                    if(times>0){
                        boolean running=fileManager.getFile(gui.getSelectedFiles().get(0)).run(times,javaCode);
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
                javaCode.setRunningFile(null);
            }
        }
        else if(e.getSource().equals(backButton)){
            runner.stopProcess();
            javaCode.saveFile();
            gui.getSelectedFiles().clear();
            gui.setupFileBrowserGui();
        }
        else if(e.getSource().equals(recordGradeButton)){
            if(gradeNumber.getText().equals("")){
                gradeStatus.setText("No grade has been entered.");
                return;
            }
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
                        assign, Double.parseDouble(gradeNumber.getText()),gradeComment.getText(),
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
        else if(e.getSource().equals(codeSortMode)){
            javaCode.setSortMode(codeSortMode.getSelectedIndex());
        }
        else if(e.getSource().equals(codeSortOrder)){
            javaCode.setSortOrder(codeSortOrder.getSelectedIndex());
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
        Double grade=null;
        String comment=null;
        if(gui.getGrader()!=null){
            grade=gui.getGrader().getGradeNum(file.getFirstLastName(), file.getAssignmentNumber());
            comment=gui.getGrader().getComment(file.getFirstLastName(), file.getAssignmentNumber());
        }
        if(comment==null){
            comment="";
        }
        //if the divider somehow gets completely hidden lets make it small
        if(graderDivider.getDividerLocation()>gui.getRootPane().getSize().width-50){
            graderDivider.setDividerLocation(gui.getRootPane().getSize().width-50);
            Config.dividerLocation=gui.getRootPane().getSize().width-50;
        }
        if(gradeWriteDivider.getDividerLocation()>gui.getRootPane().getSize().height-50){
            gradeWriteDivider.setDividerLocation(gui.getRootPane().getSize().height-50);
            Config.bottomDividerLocation=gui.getRootPane().getSize().height-50;
        }
        if(grade!=null)
            gradeNumber.setText(grade+"");
        else
            gradeNumber.setText("");
        gradeComment.setText(comment);
        
        javaCode.setFile(file);
        fileInfoLabel.setText(file.toString());
        gradeStatus.setText("");
        codeOutputArea.setText("");
        runButton.setText("Run");
        
        javaCode.setSort(Config.codeSortMode,Config.codeSortOrder);
        codeSortMode.setSelectedIndex(Config.codeSortMode);
        codeSortOrder.setSelectedIndex(Config.codeSortOrder);
        
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
    public JSplitPane getBottomDivider(){
        return gradeWriteDivider;
    }
    public JavaCodeBrowser getCodeBrowser(){
        return javaCode;
    }
}
