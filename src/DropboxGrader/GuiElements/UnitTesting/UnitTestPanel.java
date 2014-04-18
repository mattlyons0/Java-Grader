/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 * All unit testing gui stuff should go in here
 * This will be the panel in the assignment overlay for tests
 * @author matt
 */
public class UnitTestPanel extends JPanel implements ActionListener{
    
    private Gui gui;
    
    //Single Elements
    private JButton addTestsButton;
    private JButton addTestButton;
    private JButton addJTestsButton;
    private JButton addJTestButton;
    
    //Multiple Elements
    //Simple UnitTest
    private ArrayList<JTextField> methodNames;
    private ArrayList<JTextField> returnTypes;
    private ArrayList<JTextField> argumentTypes;
    private ArrayList<JTextField> arguments;
    private ArrayList<JTextField> expectedValues;
    private ArrayList<JButton> removeTestButtons;
    //JUnitTest
    private ArrayList<JLabel> jFilenames;
    private ArrayList<JButton> jFileChoosers;
    private ArrayList<JButton> removeJTestButtons;
    
    //Data
    private ArrayList<UnitTest> unitTests;
    private ArrayList<String> junitTests;
    private String assignmentName;
    private int assignmentNumber;
    
    private FileFilter javaFilter;
    private String jLabelText="JUnit Test:  ";
    public UnitTestPanel(UnitTest[] tests,String[] jtests,String assignName,int assignNum,Gui gui){
        super();
        this.gui=gui;
        assignmentNumber=assignNum;
        assignmentName=assignName;
        setLayout(new GridBagLayout());
        
        javaFilter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.getName().toLowerCase().endsWith(".java")||file.isDirectory())
                    return true;
                return false;
            }
            @Override
            public String getDescription() {
                return "*.java";
            }
        };
        
        unitTests=new ArrayList();
        if(tests!=null){
            unitTests.addAll(Arrays.asList(tests));
        }
        junitTests=new ArrayList();
        if(jtests!=null){
            junitTests.addAll(Arrays.asList(jtests));
        }
        
        methodNames=new ArrayList();
        returnTypes=new ArrayList();
        argumentTypes=new ArrayList();
        arguments=new ArrayList();
        expectedValues=new ArrayList();
        removeTestButtons=new ArrayList();
        
        jFilenames=new ArrayList();
        jFileChoosers=new ArrayList();
        removeJTestButtons=new ArrayList();
        
        
        setup();
        setVisible(true);
    }
    
    private void setup(){
        removeAll();
        if(addJTestsButton==null){
            addJTestsButton=new JButton("Add JUnit Test");
            addJTestsButton.addActionListener(this);
        }
        if(addTestsButton==null){
            addTestsButton=new JButton("Add Simple Unit Test");
            addTestsButton.addActionListener(this);
        }
        
        if(unitTests.isEmpty()&&junitTests.isEmpty()){
            setupNoTests();
        }
        else{
            setupTests();
        }
        revalidate();
        repaint();
    }
    private void setupNoTests(){

        GridBagConstraints cons=new GridBagConstraints();
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        add(addTestsButton,cons);
        cons.gridx=1;
        add(addJTestsButton,cons);
    }
    private void setupTests(){
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.fill=GridBagConstraints.NONE;
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        
        for(int i=0;i<unitTests.size();i++){
            if(i>=methodNames.size()){ //if we don't have stored fields for this index
                JTextField methodNameField=new JTextField(15);
                JTextField returnTypeField=new JTextField(10);
                JTextField argumentTypesField=new JTextField(15);
                JTextField argumentsField=new JTextField(10);
                JTextField expectedValueField=new JTextField(15);
                JButton removeButton=new JButton("-");
                removeButton.setActionCommand("RemoveUnitTest"+i);
                removeButton.addActionListener(this);
                removeButton.setToolTipText("Remove JUnit Test");
                
                UnitTest unitTest=unitTests.get(i);
                if(unitTest!=null){
                    methodNameField.setText(unitTest.getMethodName());
                    returnTypeField.setText(unitTest.getReturnTypeString());
                    String argsTypes=unitTest.getArgumentTypesString();
                    if(argsTypes!=null)
                        argumentTypesField.setText(argsTypes);
                    if(unitTest.getArgumentData()!=null)
                        argumentsField.setText(unitTest.getArgumentData());
                    if(unitTest.getExpectedReturnValue()!=null)
                        expectedValueField.setText(unitTest.getExpectedReturnValue());
                }
                methodNames.add(methodNameField);
                returnTypes.add(returnTypeField);
                argumentTypes.add(argumentTypesField);
                arguments.add(argumentsField);
                expectedValues.add(expectedValueField);
                removeTestButtons.add(removeButton);
            }
            if(addTestButton==null){
                addTestButton=new JButton("+");
                addTestButton.addActionListener(this);
                addTestButton.setToolTipText("Add Simple Unit Test");
            }
            
            cons.gridx=0;
            cons.weightx=1;
            add(new JLabel("Method Name: "),cons);
            cons.gridx=1;
            cons.weightx=10;
            add(methodNames.get(i),cons);
            cons.gridx=2;
            cons.weightx=1;
            add(new JLabel("Return Type: "),cons);
            cons.gridx=3;
            cons.weightx=10;
            add(returnTypes.get(i),cons);
            cons.gridx=4;
            cons.weightx=1;
            add(new JLabel("Argument Types: "),cons);
            cons.gridx=5;
            cons.weightx=10;
            add(argumentTypes.get(i),cons);
            cons.gridx=6;
            cons.weightx=1;
            add(new JLabel("Argument Data: "),cons);
            cons.gridx=7;
            cons.weightx=10;
            add(arguments.get(i),cons);
            cons.gridx=8;
            cons.weightx=1;
            add(new JLabel("Expected Return Value: "),cons);
            cons.gridx=9;
            cons.weightx=10;
            add(expectedValues.get(i),cons);
            cons.gridx=11;
            cons.weightx=1;
            add(removeTestButtons.get(i),cons);
            if(i==unitTests.size()-1){
                cons.gridx=12;
                add(addTestButton,cons);
            }
            
            cons.gridy++;
        }
        for(int i=0;i<junitTests.size();i++){
            if(i>=jFilenames.size()){ //if we need to add the elements
                jFilenames.add(new JLabel(jLabelText+"Not Set"));
                if(!junitTests.get(i).equals(""))
                    jFilenames.get(i).setText(jLabelText+junitTests.get(i));
                JButton chooser=new JButton("Browse");
                chooser.setActionCommand("BrowseJUnitTest"+i);
                chooser.addActionListener(this);
                jFileChoosers.add(chooser);
                JButton remove=new JButton("-");
                remove.setToolTipText("Remove This Test");
                remove.setActionCommand("RemoveJUnitTest"+i);
                remove.addActionListener(this);
                removeJTestButtons.add(remove);
            }
            if(addJTestButton==null){
                addJTestButton=new JButton("+");
                addJTestButton.setToolTipText("Add JUnit Test");
                addJTestButton.addActionListener(this);
            }
            cons.gridx=0;
            add(jFilenames.get(i),cons);
            cons.gridx=1;
            add(jFileChoosers.get(i),cons);
            cons.gridx=2;
            add(removeJTestButtons.get(i),cons);
            if(i==junitTests.size()-1){
                cons.gridx=3;
                add(addJTestButton,cons);
            }                
            
            cons.gridy++;
        }
        
        cons.gridx=0;
        cons.gridwidth=13;
        if(junitTests.isEmpty())
            add(addJTestsButton,cons);
        else if(unitTests.isEmpty())
            add(addTestsButton,cons);
    }
    public void setUnitTests(UnitTest[] tests){
        if(tests!=null){
            unitTests.clear();
            unitTests.addAll(Arrays.asList(tests));
        }
    }
    public void setJUnitTests(String[] jtests){
        if(jtests!=null){
            junitTests.clear();
            junitTests.addAll(Arrays.asList(jtests));
        }
    }
    public UnitTest[] getUnitTest(){
        return unitTests.toArray(new UnitTest[0]);
    }
    public String[] getJUnitTests(){
        return junitTests.toArray(new String[0]);
    }
    public void save(){
        for(int i=0;i<unitTests.size();i++){
            UnitTest test=unitTests.get(i);
            test.setMethodName(methodNames.get(i).getText());
            test.setReturnType(returnTypes.get(i).getText());
            test.setArgumentTypes(argumentTypes.get(i).getText().split(","));
            test.setArgumentData(arguments.get(i).getText().split(","));
            test.setExpectedReturnValue(expectedValues.get(i).getText());
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(addTestsButton)||
                e.getSource().equals(addTestButton)){
            unitTests.add(new UnitTest());
            setup();
        }
        else if(e.getActionCommand().startsWith("RemoveUnitTest")){
            int testNum=GradebookTable.extractNumber("RemoveUnitTest", e.getActionCommand());
            unitTests.remove(testNum);
            setup();
        }
        else if(e.getSource().equals(addJTestsButton)||
                e.getSource().equals(addJTestButton)){
            junitTests.add("");
            setup();
        }
        else if(e.getActionCommand().startsWith("BrowseJUnitTest")){
            final int id=GradebookTable.extractNumber("BrowseJUnitTest", e.getActionCommand());
            JFileChooser fc=new JFileChooser(System.getProperty("userprofile"));
            fc.setFileFilter(javaFilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File file=fc.getSelectedFile();
                final String oldFile=junitTests.get(id);
                final JLabel label=jFilenames.get(id);
                junitTests.set(id, Config.jUnitTestsLocation+"/"+file.getName());
                label.setText(jLabelText+file.getName());
                final String remoteNameF=junitTests.get(id);
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
                                remoteName=remoteName.substring(0,remoteName.length()-5); //remove .java
                                label.setText(jLabelText+remoteName);
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
                                remoteName+=".java";
                                entry=client.getMetadata(remoteName);
                            }
                            if(!junitTests.get(id).equals(remoteName))
                                junitTests.set(id, remoteName);
                            label.setText(jLabelText+remoteName);
                            FileInputStream sheetStream = new FileInputStream(file);
                            client.uploadFile(remoteName, DbxWriteMode.force(), file.length(), sheetStream);
                            sheetStream.close();
                        } catch(DbxException|IOException e){
                            System.err.println("Error uploading unit test.\n"+e);
                            GuiHelper.alertDialog("<html>Error Uploading "+file.getName()+" to Dropbox.<br/>"+e+"</html>");
                            label.setText(jLabelText);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        else if(e.getActionCommand().startsWith("RemoveJUnitTest")){
            final int id=GradebookTable.extractNumber("RemoveJUnitTest", e.getActionCommand());
            final String path=junitTests.get(id);
            //check if any other tests use this path
            boolean shouldDelete=true;
            loop:
            for(TextAssignment assign:gui.getGrader().getSpreadsheet().getAllAssignments()){
                if(assign.junitTests!=null){
                    for(String test:assign.junitTests){
                        if(test.equals(path)&&!(assign.number==assignmentNumber&&assign.name.equals(assignmentName))){
                            shouldDelete=false;
                            break loop;
                        }
                            
                    }
                }
            }
            if(shouldDelete&&!path.equals("")){
                gui.getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(gui.getDbxSession().getClient().getMetadata(path)!=null)
                            gui.getDbxSession().getClient().delete(path);
                        } catch (DbxException ex) {
                            System.err.println("Error deleting unit test from dropbox.\n"+ex);
                            ex.printStackTrace();
                        }
                    }
                });
            }
            junitTests.remove(id);
            jFileChoosers.remove(id);
            jFilenames.remove(id);
            setup();
            
        }
    }
}
