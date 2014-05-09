/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.Date;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.GuiElements.MiscComponents.JGhostTextField;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.JavaClass;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import DropboxGrader.UnitTesting.UnitTester;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
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
    private ArrayList<JButton> methodAccess;
    private ArrayList<JButton> methodModifiers;
    private ArrayList<JTextField> methodNames;
    private ArrayList<JTextField> returnTypes;
    private ArrayList<JButton> argumentsButtons;
    private ArrayList<JTextField> expectedValues;
    private ArrayList<JButton> removeTestButtons;
    private ArrayList<JTextField> descriptionValues;
    //JUnitTest
    private ArrayList<JLabel> jFilenames;
    private ArrayList<JButton> jFileChoosers;
    private ArrayList<JButton> downloadJTests;
    private ArrayList<JButton> removeJTestButtons;
    
    private ArrayList<WatchKey> watchKeys;
    
    //Data
    private ArrayList<UnitTest> unitTests;
    private ArrayList<String> junitTests;
    private String assignmentName;
    private Integer assignmentNumber;
    
    private FileFilter javaFilter;
    private String jLabelText="JUnit Test:  ";
    public UnitTestPanel(UnitTest[] tests,String[] jtests,String assignName,Integer assignNum,Gui gui){
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
        
        methodAccess=new ArrayList();
        methodModifiers=new ArrayList();
        methodNames=new ArrayList();
        returnTypes=new ArrayList();
        argumentsButtons=new ArrayList();
        expectedValues=new ArrayList();
        descriptionValues=new ArrayList();
        removeTestButtons=new ArrayList();
        
        watchKeys=new ArrayList();
        
        jFilenames=new ArrayList();
        jFileChoosers=new ArrayList();
        downloadJTests=new ArrayList();
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
                JButton accessButton=new JButton("Method Access");
                accessButton.addActionListener(this);
                accessButton.setActionCommand("SetMethodAccess"+i);
                JButton modifierButton=new JButton("Method Modifiers");
                modifierButton.addActionListener(this);
                modifierButton.setActionCommand("SetMethodModifiers"+i);
                JTextField methodNameField=new JGhostTextField(15,"Method Name");
                JTextField returnTypeField=new JGhostTextField(10,"Return Type");
                JButton argumentsButton=new JButton("Arguments");
                argumentsButton.setToolTipText("Method Arguments");
                argumentsButton.setActionCommand("SetArguments"+i);
                argumentsButton.addActionListener(this);
                JTextField argumentTypesField=new JGhostTextField(15,"Argument Types");
                JTextField argumentsField=new JGhostTextField(10,"Argument Data");
                JTextField expectedValueField=new JGhostTextField(15,"Expected Return Value");
                JTextField descriptionField=new JGhostTextField(30,"Test Description");
                descriptionField.setToolTipText("This will be shown when a test fails.");
                JButton removeButton=new JButton("-");
                removeButton.setActionCommand("RemoveUnitTest"+i);
                removeButton.addActionListener(this);
                removeButton.setToolTipText("Remove JUnit Test");
                
                UnitTest unitTest=unitTests.get(i);
                if(unitTest!=null){
                    if(unitTest.getMethodName()!=null)
                        methodNameField.setText(unitTest.getMethodName());
                    if(unitTest.getReturnTypeString()!=null)
                        returnTypeField.setText(unitTest.getReturnTypeString());
                    String argsTypes=unitTest.getArgumentTypesString();
                    if(argsTypes!=null)
                        argumentTypesField.setText(argsTypes);
                    if(unitTest.getArgumentData()!=null)
                        argumentsField.setText(unitTest.getArgumentData());
                    if(unitTest.getExpectedReturnValue()!=null)
                        expectedValueField.setText(unitTest.getExpectedReturnValue());
                    if(unitTest.getDescription()!=null)
                        descriptionField.setText(unitTest.getDescription());
                }
                methodAccess.add(accessButton);
                methodModifiers.add(modifierButton);
                methodNames.add(methodNameField);
                returnTypes.add(returnTypeField);
                argumentsButtons.add(argumentsButton);
                expectedValues.add(expectedValueField);
                descriptionValues.add(descriptionField);
                removeTestButtons.add(removeButton);
                
                //set buttons text to use data from test
                setAccessType(null,null,i);
                setModifierType(null,-1,i);
                setArgs(null,null,i);
            }
            else{
                methodAccess.get(i).setActionCommand("SetMethodAccess"+i);
                methodModifiers.get(i).setActionCommand("SetMethodModifiers"+i);
                argumentsButtons.get(i).setActionCommand("SetArguments"+i);
                removeTestButtons.get(i).setActionCommand("RemoveUnitTest"+i);
            }
            if(addTestButton==null){
                addTestButton=new JButton("+");
                addTestButton.addActionListener(this);
                addTestButton.setToolTipText("Add Simple Unit Test");
            }
            
            cons.weightx=10;
            cons.gridx=0;
            add(methodAccess.get(i),cons);
            cons.gridx=1;
            add(methodModifiers.get(i),cons);
            cons.gridx=2;
            add(methodNames.get(i),cons);
            cons.gridx=3;
            add(returnTypes.get(i),cons);
            cons.weightx=1;
            cons.gridx=4;
            add(new JLabel("("),cons);
            cons.weightx=10;
            cons.gridx=5;
            add(argumentsButtons.get(i),cons);
            cons.weightx=1;
            cons.gridx=6;
            add(new JLabel(")"),cons);
            cons.gridx=7;
            add(new JLabel("=="),cons);
            cons.weightx=10;
            cons.gridx=8;
            add(expectedValues.get(i),cons);
            cons.gridx=9;
            add(descriptionValues.get(i),cons);
            cons.weightx=1;
            cons.gridx=10;
            add(removeTestButtons.get(i),cons);
            if(i==unitTests.size()-1){
                cons.gridx=11;
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
                JButton download=new JButton("Edit");
                download.setToolTipText("Download JUnit test file so it can be changed.\nThe test will automatically be uploaded after it is modified.");
                download.setActionCommand("DownloadJUnitTest"+i);
                download.addActionListener(this);
                download.setEnabled(false);
                downloadJTests.add(download);
                if(!junitTests.get(i).equals(""))
                    download.setEnabled(true);
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
            if(watchKeys.size()<=i)
                watchKeys.add(null);
            cons.gridx=0;
            add(jFilenames.get(i),cons);
            cons.gridx=1;
            add(jFileChoosers.get(i),cons);
            cons.gridx=2;
            add(downloadJTests.get(i),cons);
            cons.gridx=3;
            add(removeJTestButtons.get(i),cons);
            if(i==junitTests.size()-1){
                cons.gridx=4;
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
            test.setExpectedReturnValue(expectedValues.get(i).getText());
            test.setDescription(descriptionValues.get(i).getText());
            test.updateDate=Date.currentDate();
            
            gui.getViewManager().removeOverlay("MethodAccessOverlay"+i);
            gui.getViewManager().removeOverlay("MethodModifiersOverlay"+i);
            gui.getViewManager().removeOverlay("MethodArgumentsOverlay"+i);
        }
        for(int i=0;i<junitTests.size();i++){
            final String remoteTestName=junitTests.get(i);
            if(remoteTestName.equals("")){
                junitTests.remove(i);
                i--;
            }
            else if(!watchKeys.isEmpty()){
                WatchKey key=watchKeys.get(i);
                if(key!=null){
                    List<WatchEvent<?>> events=key.pollEvents();
                    if(events!=null&&!events.isEmpty()){
                        for(WatchEvent<?> event:events){
                            final File localFile;
                            String[] testPaths=remoteTestName.split(Pattern.quote("/"));
                            String testName;
                            if(testPaths.length==0)
                                testName=remoteTestName;
                            else
                                testName=testPaths[testPaths.length-1];
                            localFile=new File(UnitTester.unitTestDirectory+"/"+testName);
                            final Path changed = (Path) event.context();
                            if (changed.endsWith(localFile.getName())) {
                                gui.getBackgroundThread().invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try{
                                            DbxClient client=gui.getDbxSession().getClient();
                                            if(!remoteTestName.equals("")&&client.getMetadata(remoteTestName)!=null){
                                                client.delete(remoteTestName);
                                            }
                                            FileInputStream sheetStream = new FileInputStream(localFile);
                                            client.uploadFile(remoteTestName, DbxWriteMode.force(), localFile.length(), sheetStream);
                                            sheetStream.close();
                                        } catch(DbxException|IOException e){
                                            System.err.println("Error uploading unit test. "+(localFile!=null?localFile.getName():null)+"\n");
                                            GuiHelper.alertDialog("<html>Error Uploading "+(localFile!=null?localFile.getName():null)+" to Dropbox.<br/>"
                                                    + "Dropbox is probably under heavy load or down.<br/>"+e.getMessage()+"</html>");
                                            e.printStackTrace();
                                        }
                                        gui.getTestManager().test();
                                    }
                                });
                                break;
                            }
                        }
                    }
                    key.cancel();
                }
            }
        }
    }
    public void setAccessType(MethodAccessType type,CheckboxStatus status,int testIndex){
        UnitTest test=unitTests.get(testIndex);
        if(type!=null&&status!=null){
            if(type.equals(MethodAccessType.PUBLIC))
                test.accessPublic=status;
            else if(type.equals(MethodAccessType.PROTECTED))
                test.accessProtected=status;
            else if(type.equals(MethodAccessType.PRIVATE))
                test.accessPrivate=status;
            else if(type.equals(MethodAccessType.PACKAGEPRIVATE))
                test.accessPackagePrivate=status;
        }
        //write button text
        ArrayList<String> labels=new ArrayList();
        String typeName;
        if(test.accessPublic!=CheckboxStatus.DISALLOWED){
            typeName="public";
            addType(labels,typeName,test.accessPublic);
        }
        if(test.accessProtected!=CheckboxStatus.DISALLOWED){
            typeName="protected";
            addType(labels,typeName,test.accessProtected);
        }
        if(test.accessPrivate!=CheckboxStatus.DISALLOWED){
            typeName="private";
            addType(labels,typeName,test.accessPrivate);
        }
        if(test.accessPackagePrivate!=CheckboxStatus.DISALLOWED){
            typeName="<small>packageprivate</small>";
            addType(labels,typeName,test.accessPackagePrivate);
        }
        String label="<html>";
        for(int i=0;i<labels.size();i++){
            label+=labels.get(i);
            if(i!=labels.size()-1)
                label+="/";
        }
        if(labels.isEmpty())
            label+="<i>Access Types</i>";
        label+="</html>";
        methodAccess.get(testIndex).setText(label);
    }
    public void setModifierType(CheckboxStatus status,int modIndex,int testIndex){
        UnitTest test=unitTests.get(testIndex);
        if(status!=null&&modIndex!=-1){
            if(modIndex==0)
                test.modStatic=status;
            else if(modIndex==1)
                test.modFinal=status;
            else if(modIndex==2)
                test.modAbstract=status;
            else if(modIndex==3)
                test.modSynchronized=status;
        }
        //write button text
        String label="<html>";
        ArrayList<String> labels=new ArrayList();
        if(test.modAbstract!=CheckboxStatus.DISALLOWED)
            addType(labels,"abstract",test.modAbstract);
        if(test.modFinal!=CheckboxStatus.DISALLOWED)
            addType(labels,"final",test.modFinal);
        if(test.modStatic!=CheckboxStatus.DISALLOWED)
            addType(labels,"static",test.modStatic);
        if(test.modSynchronized!=CheckboxStatus.DISALLOWED)
            addType(labels,"synchronized",test.modSynchronized);
        for(int i=0;i<labels.size();i++){
            label+=labels.get(i);
            if(i!=labels.size()-1)
                label+="/";
        }
        if(labels.isEmpty())
            label+="<i>Modifiers</i>";
        label+="</html>";
        methodModifiers.get(testIndex).setText(label);
    }
    public void setArgs(JavaClass[] types,String[] data,int testIndex){
        UnitTest test=unitTests.get(testIndex);
        if(types!=null&&data!=null){
            test.setArgumentTypes(types);
            test.setArgumentData(data);
        }
        
        if(test.getArgumentTypes()!=null){
            String label="";
            for(int i=0;i<test.getArgumentTypes().length;i++){
                String argTypes="",args="";
                if(test.getArgumentTypes()[i]!=null)
                    argTypes=test.getArgumentTypes()[i].toText();
                if(test.getArguments()[i]!=null)
                    args=test.getArguments()[i];
                label+=argTypes+" "+args;
                if(i!=test.getArgumentTypes().length-1)
                    label+=", ";
            }
            if(label.equals(""))
                label="<html><i>Arguments</i></html>";
            argumentsButtons.get(testIndex).setText(label);
        }
    }
    private void addType(ArrayList<String> ar,String typeName,CheckboxStatus status){
        if(status==CheckboxStatus.ALLOWED)
            ar.add(typeName);
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
            
            methodAccess.remove(testNum);
            methodModifiers.remove(testNum);
            methodNames.remove(testNum);
            returnTypes.remove(testNum);
            argumentsButtons.remove(testNum);
            expectedValues.remove(testNum);
            removeTestButtons.remove(testNum);
            descriptionValues.remove(testNum);
            if(watchKeys.get(testNum)!=null)
                watchKeys.get(testNum).cancel();
            watchKeys.remove(testNum);
            
            gui.getViewManager().removeOverlay("MethodAccessOverlay"+testNum);
            gui.getViewManager().removeOverlay("MethodModifiersOverlay"+testNum);
            gui.getViewManager().removeOverlay("MethodArgumentsOverlay"+testNum);
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
                final JButton downloadButton=downloadJTests.get(id);
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
                            downloadButton.setEnabled(true);
                            FileInputStream sheetStream = new FileInputStream(file);
                            client.uploadFile(remoteName, DbxWriteMode.force(), file.length(), sheetStream);
                            sheetStream.close();
                        } catch(DbxException|IOException e){
                            System.err.println("Error uploading unit test.\n"+e);
                            GuiHelper.alertDialog("<html>Error Uploading "+file.getName()+" to Dropbox.<br/>"+e+"</html>");
                            label.setText(jLabelText);
                            downloadButton.setEnabled(false);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        else if(e.getActionCommand().startsWith("RemoveJUnitTest")){
            final int id=GradebookTable.extractNumber("RemoveJUnitTest", e.getActionCommand());
            final String path=junitTests.get(id);
            jFilenames.remove(id);
            jFileChoosers.remove(id);
            downloadJTests.remove(id);
            removeJTestButtons.remove(id);
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
            setup();
            
        }
        else if(e.getActionCommand().startsWith("SetMethodAccess")){
            int index=GradebookTable.extractNumber("SetMethodAccess", e.getActionCommand());
            MethodAccessOverlay overlay=new MethodAccessOverlay(gui,this,index);
            overlay.setTest(unitTests.get(index));
            gui.getViewManager().addOverlay(overlay);
        }
        else if(e.getActionCommand().startsWith("SetMethodModifiers")){
            int index=GradebookTable.extractNumber("SetMethodModifiers", e.getActionCommand());
            MethodModifiersOverlay overlay=new MethodModifiersOverlay(gui,this,index);
            overlay.setTest(unitTests.get(index));
            gui.getViewManager().addOverlay(overlay);
        }
        else if(e.getActionCommand().startsWith("SetArguments")){
            int index=GradebookTable.extractNumber("SetArguments", e.getActionCommand());
            MethodArgumentsOverlay overlay=new MethodArgumentsOverlay(gui,this,index);
            overlay.setUnitTest(unitTests.get(index));
            gui.getViewManager().addOverlay(overlay);
        }
        else if(e.getActionCommand().startsWith("DownloadJUnitTest")){
            final int index=GradebookTable.extractNumber("DownloadJUnitTest",e.getActionCommand());
            if(watchKeys.size()<=index||watchKeys.get(index)==null){
                gui.getBackgroundThread().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String testLoc=junitTests.get(index);
                            if(!testLoc.equals("")&&gui.getDbxSession().getClient().getMetadata(testLoc)!=null){
                                String[] testPaths=testLoc.split(Pattern.quote("/"));
                                String testName;
                                if(testPaths.length==0)
                                    testName=testLoc;
                                else
                                    testName=testPaths[testPaths.length-1];
                                String localTestLoc=UnitTester.unitTestDirectory+"/"+testName;
                                try{
                                    FileOutputStream f = new FileOutputStream(localTestLoc);
                                    gui.getDbxSession().getClient().getFile(testLoc, null, f); //downloads from dropbox server
                                    f.close();

                                    File testFile=new File(localTestLoc);
                                    try{
                                        Path testPath=testFile.getParentFile().toPath();
                                        WatchService watcher=FileSystems.getDefault().newWatchService();
                                        WatchKey key=testPath.register(watcher, ENTRY_MODIFY);
                                        if(watchKeys.get(index)==null)
                                            watchKeys.set(index, key);
                                        else{
                                            save();
                                            watchKeys.set(index, key);
                                        }
                                    } catch(IOException e){
                                        System.err.println("Error attaching watch proccess to "+testFile);
                                        e.printStackTrace();
                                        GuiHelper.alertDialog("Error watching file for changes.\nYou will have to re-upload the test after making modifications to it.");
                                    }

                                    if(Desktop.isDesktopSupported()){
                                        Desktop desk=Desktop.getDesktop();
                                        int choice=-2;
                                        if(desk.isSupported(Desktop.Action.OPEN)&&desk.isSupported(Desktop.Action.BROWSE))
                                            choice=GuiHelper.multiOptionPane("How would you like to edit the JUnit Test?",new String[]{"Open","Open Directory"});
                                        else if(desk.isSupported(Desktop.Action.OPEN))
                                            choice=0;
                                        else if(desk.isSupported(Desktop.Action.BROWSE))
                                            choice=1;

                                        if(choice==0)
                                            desk.browse(testFile.toURI());
                                        else if(choice==1)
                                            desk.browse(testFile.getParentFile().toURI());
                                        else if(choice==-1){//dont show more stuff
                                            watchKeys.get(index).cancel();
                                            watchKeys.set(index, null);
                                        }
                                        else
                                            GuiHelper.alertDialog("Test was downloaded to "+new File(localTestLoc).getAbsolutePath());
                                    } else
                                        GuiHelper.alertDialog("Test was downloaded to "+new File(localTestLoc).getAbsolutePath());
                                } catch(IOException ex){
                                    System.err.println("Error downloading unit test file.\n");
                                    ex.printStackTrace();
                                    GuiHelper.alertDialog("Error downloading the unit test file.\n\n"+ex);
                                }
                            }
                            else{
                                GuiHelper.alertDialog("JUnit Test Doesn't Exist!");
                            }
                        } catch (DbxException ex) {
                            System.err.println("Error communicating with dropbox when downloading unit test file.\n");
                            ex.printStackTrace();
                            GuiHelper.alertDialog("Error communicating with dropbox.\nDropbox may be under heavy load or down.");
                        }
                    }
                });
            } else{ //we already downloaded it
                String testLoc=junitTests.get(index);
                String[] testPaths=testLoc.split(Pattern.quote("/"));
                String testName;
                if(testPaths.length==0)
                    testName=testLoc;
                else
                    testName=testPaths[testPaths.length-1];
                String localTestLoc=UnitTester.unitTestDirectory+"/"+testName;
                File testFile=new File(localTestLoc);
                Desktop desk=Desktop.getDesktop();
                int choice=-2;
                if(desk.isSupported(Desktop.Action.OPEN)&&desk.isSupported(Desktop.Action.BROWSE))
                    choice=GuiHelper.multiOptionPane("How would you like to edit the JUnit Test?",new String[]{"Open","Open Directory"});
                else if(desk.isSupported(Desktop.Action.OPEN))
                    choice=0;
                else if(desk.isSupported(Desktop.Action.BROWSE))
                    choice=1;
                try{
                    if(choice==0)
                        desk.browse(testFile.toURI());
                    else if(choice==1)
                        desk.browse(testFile.getParentFile().toURI());
                    else if(choice==-1){//dont show more stuff
                        watchKeys.get(index).cancel();
                        watchKeys.set(index, null);
                    }
                    else
                        GuiHelper.alertDialog("Test was downloaded to "+new File(localTestLoc).getAbsolutePath());
                } catch(IOException ex){
                    GuiHelper.alertDialog("Test was downloaded to "+new File(localTestLoc).getAbsolutePath());
                }
            }
        }
    }
    public boolean hasTests(){
        if(junitTests.isEmpty()&&unitTests.isEmpty())
            return false;
        return true;
    }
}
