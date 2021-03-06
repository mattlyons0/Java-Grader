/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.FileManagement.Date;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.FileManagement.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.MiscOverlays.NameOverlay;
import DropboxGrader.GuiElements.UnitTesting.UnitTestOverlay;
import DropboxGrader.GuiHelper;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextName;
import DropboxGrader.TextGrader.TextSpreadsheet;
import DropboxGrader.UnitTesting.SimpleTesting.JavaMethod;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 
 * @author matt
 */
public class UnitTester {
    private Gui gui;
    private TextAssignment assignment;
    private UnitTestOverlay overlay;
    private JavaFile currentFile;
    private String currentPreviousCode;
    private boolean force;
    
    private ArrayList<Boolean> testResults;
    private ArrayList<String> testStatus;
    
    public static String unitTestDirectory;
    
    public UnitTester(Gui gui,TextAssignment assign){
        this.gui=gui;
        assignment=assign;
        
        testResults=new ArrayList();
        testStatus=new ArrayList();
        
        unitTestDirectory=gui.getManager().getDownloadFolder()+"/JUnitTests/";
        new File(unitTestDirectory).mkdir();
    }
    public UnitTester(Gui gui,TextAssignment assign,UnitTestOverlay overlay){
        this(gui,assign);
        this.overlay=overlay;
    }
    public UnitTester(Gui gui,TextAssignment assign,UnitTestOverlay overlay,boolean forceTest){
        this(gui,assign,overlay);
        this.force=forceTest;
    }
    public void runTests(){
        while(currentFile!=null){
            System.err.println("Something tried to run tests while we are running tests.");
            try {
                Thread.sleep(50); //this isn't really good, but at the same time we can't run the tests concurrently to eachother if someone calls the method twice
            } catch (InterruptedException ex) {
                Logger.getLogger(UnitTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(overlay!=null)
            overlay.setStatus("Searching for Tests");
        FileManager manager=gui.getManager();
        DbxFile[] files=manager.getFiles().toArray(new DbxFile[0]);
        
        for(int i=0;i<files.length;i++){
            if((overlay==null||!overlay.isCanceled())&&files[i].getAssignmentNumber()==assignment.number){
                prepareTest(files[i]);
            }
        }
        if(overlay!=null){
            overlay.setStatus("Unit Tests Finished");
            overlay.setDescription("Assignment: "+assignment);
        }
    }
    private void prepareTest(final DbxFile file){
        if(overlay!=null){
            overlay.setStatus("Running Tests");
            overlay.setDescription("File: "+file.getFileName());
        }
        if(overlay!=null)
            overlay.setStatus("Running Unit Tests");
        if((overlay==null||!overlay.isCanceled())&&assignment.simpleUnitTests!=null&&assignment.simpleUnitTests.length>0){
            if(overlay!=null)
                overlay.setDescription("Assignment: "+assignment+" File: "+file.getFileName());
            for(UnitTest test:assignment.simpleUnitTests){
                TextGrade grade=gui.getGrader().getGrade(file.getFirstLastName(), assignment.number);
                if(test!=null&&(force||grade==null||!grade.unitTested||grade.dateGraded==null||!Date.before(file.getSubmittedDate(), grade.dateGraded)||
                        !Date.before(test.updateDate, grade.dateGraded))){ //reasons to test
                    JavaFile[] javaFiles=file.getJavaFiles();
                    if(!file.isDownloaded()){
                        //we need to download the file
                        if(overlay!=null)
                            overlay.setStatus("Downloading File to be Tested");
                        boolean success=file.download()!=null;
                        if(!success){
                            System.err.println("Error downloading file to be unit tested.");
                            if(overlay!=null)
                                overlay.append("Error downloading file from dropbox, either dropbox "
                                        + "is having issues or the internet is down. "+Color.RED);
                        }
                        prepareTest(file);
                        return;
                    }
                    test:
                    for(int i=0;i<javaFiles.length;i++){
                        JavaMethod[] methods=javaFiles[i].getMethods();
                        for(JavaMethod m:methods){
                            if((overlay==null||!overlay.isCanceled())&&(testMatch(m,test))){
                                if(overlay!=null)
                                    overlay.setDescription("Method: "+m.getMethodString()+" Unit Test: "+(test.getDescription()==null?test.getMethodName():("'"+test.getDescription()+"'"))+" Assignment: "+assignment+" File: "+file.getFileName());
                                runSimpleTest(file,test,i,m);
                                if(overlay!=null)
                                    overlay.setDescription("Assignment: "+assignment+" File: "+file.getFileName());
                                break test;
                            }
                        }
                    }
                }
                else{ //didnt need to test
                        testStatus.add("SKIPPED");
                        testResults.add(true);
                }
        } //this is tabbed wrong
        if(overlay!=null)
            overlay.setDescription("File: "+file.getFileName());
        }
        if(overlay!=null)
            overlay.setStatus("Running JUnit Tests");
        if((overlay==null||!overlay.isCanceled())&&assignment.junitTests!=null&&assignment.junitTests.length>0){
            for(String testLoc:assignment.junitTests){
                try {
                    DbxEntry entry=gui.getDbxSession().getClient().getMetadata(testLoc);
                    TextGrade grade=gui.getGrader().getGrade(file.getFirstLastName(), assignment.number);
                    if(force||grade==null||!grade.unitTested||grade.dateGraded==null||!Date.before(file.getSubmittedDate(), 
                            grade.dateGraded)||(entry.isFile()&&!Date.before(new Date(((DbxEntry.File)entry).lastModified),
                            grade.dateGraded))){ //reasons it needs to be tested
                        if(overlay!=null)
                            overlay.setStatus("Downloading JUnit Test File");
                        if(!testLoc.equals("")&&gui.getDbxSession().getClient().getMetadata(testLoc)!=null){
                            String[] testPaths=testLoc.split(Pattern.quote("/"));
                            String testName;
                            if(testPaths.length==0)
                                testName=testLoc;
                            else
                                testName=testPaths[testPaths.length-1];
                            if(overlay!=null)
                                overlay.setDescription("JUnit Test: "+testName+" File: "+file.getFileName());
                            String localTestLoc=unitTestDirectory+"/"+testName;
                            try{
                                FileOutputStream f = new FileOutputStream(localTestLoc);
                                gui.getDbxSession().getClient().getFile(testLoc, null, f); //downloads from dropbox server
                                f.close();
                                File downloadedJTest=new File(localTestLoc);
                                if(downloadedJTest.getName().substring(0,downloadedJTest.getName().length()-5).endsWith(")")){ //-5 for .java
                                    //it was renamed due to duplicate files
                                    String path=downloadedJTest.getPath();
                                    boolean confirmed=false;
                                    Character last=null;
                                    for(int i=path.length()-1-5;i>=0;i--){ //-1 for size -5 for .java
                                        char c=path.charAt(i);
                                        if(last==null&&c!=')')
                                            break;
                                        else if(last==null)
                                            last=c;
                                        else if(Character.isDigit(last)&&!(Character.isDigit(c)||c=='('))
                                            break;
                                        else if(Character.isDigit(last)&&c=='('){
                                            confirmed=true;
                                            break;
                                        }
                                        else
                                            last=c;
                                    }
                                    if(confirmed){
                                        int subIndex=path.lastIndexOf("(");
                                        String newName=path.substring(0,subIndex)+".java";
                                        File destinationName=new File(newName);
                                        if(destinationName.exists())
                                            destinationName.delete();
                                        downloadedJTest.renameTo(destinationName); //rename it to its original name (so that the class name is the same as the filename)
                                        downloadedJTest=destinationName;
                                    }
                                }
                                JavaFile runFile=new JavaFile(downloadedJTest,null);
                                JavaFile compileFile=runFile;
                                if(runFile.packageFolder()!=null){ //we gotta verify its in the right directory
                                    compileFile=validateDirectory(runFile,null); //moves it to the right directory, but doesnt update pointer so that it gets called correctly
                                }
                                if(!file.isDownloaded()){
                                    //we need to download the file
                                    if(overlay!=null)
                                        overlay.setStatus("Downloading File to be Tested");
                                    boolean success=file.download()!=null;
                                    if(!success){
                                        System.err.println("Error downloading file to be unit tested.");
                                        if(overlay!=null)
                                            overlay.append("Error downloading file from dropbox, either dropbox "
                                                    + "is having issues or the internet is down. "+Color.RED);
                                    }
                                    prepareTest(file);
                                    return;
                                }
                                if(overlay!=null)
                                    overlay.setStatus("Running JUnit Tests");
                                runJUnitTest(file,compileFile,runFile);
                            } catch(IOException ex){
                                System.err.println("Error downloading unit test file.\n"+ex);
                                if(overlay!=null)
                                    overlay.append("Error downloading JUnit Test: "+testLoc+" from dropbox!",Color.RED);
                                ex.printStackTrace();
                            }
                        }
                        else{
                            String errorS="Error, JUnit Test "+testLoc+" for assignment "+assignment.number+" does not exist on Dropbox Servers!";
                            System.err.println(errorS);
                            if(overlay!=null)
                                overlay.append(errorS,Color.red);
                            else
                                GuiHelper.alertDialog(errorS);
                            testResults.add(null);
                            testStatus.add(null);
                        }
                        if(overlay!=null)
                            overlay.setDescription("File: "+file.getFileName());
                    }
                    else{ //didnt need to test
                        testStatus.add("SKIPPED");
                        testResults.add(true);
                    }
                } catch (DbxException ex) {
                    System.err.println("Error communicating with dropbox when downloading unit test file.\n"+ex);
                    if(overlay!=null)
                        overlay.append("Error downloading JUnit Test: "+testLoc+" from dropbox!",Color.RED);
                    testResults.add(null);
                    testStatus.add(null);
                    ex.printStackTrace();
                }
                if(overlay!=null&&overlay.isCanceled())
                    break;
            }
        }
        if(overlay!=null)
            overlay.setStatus("Grading Unit Tests");
        //write grade
        double grade;
        int successes=0;
        for(String s:testStatus){//it just got skipped because it didnt need
        //to be graded again, but this will skip it out of the loops anyways.
            if(s!=null&&s.equals("SKIPPED")){
                testResults.clear();
                testStatus.clear();
                if(overlay!=null){
                    overlay.setDescription("");
                    overlay.setStatus("");
                }
                return;
            }
        }
        boolean errorTesting=false;
        for(Boolean result:testResults){
            if(result==null){
                errorTesting=true;
                break;
            }
            else if(result)
                successes++;
        }
        int totalTests=0;
        if(assignment.simpleUnitTests!=null)
            totalTests+=assignment.simpleUnitTests.length;
        if(assignment.junitTests!=null)
            totalTests+=testResults.size();
        if(assignment.junitTests!=null&&assignment.simpleUnitTests!=null)
            totalTests-=assignment.simpleUnitTests.length;
        if(totalTests==0)
            return;
        grade=successes/(double)totalTests*assignment.totalPoints;
        String status="(Unit Tested) Passed "+successes+"/"+totalTests+", \n";
        for(int testNum=0;testNum<totalTests;testNum++){
            if(testNum<testStatus.size()){
                status+="Test "+(testNum+1)+": "+testStatus.get(testNum)+" \n";
            }
            else if(assignment.simpleUnitTests!=null&&testNum<assignment.simpleUnitTests.length){
                String argTypes=assignment.simpleUnitTests[testNum].getArgumentTypesString();
                if(argTypes==null)
                    argTypes="";
                String method=assignment.simpleUnitTests[testNum].getMethodName()+"("+argTypes+")";
                String access=getAccesses(assignment.simpleUnitTests[testNum]);
                String modifiers="and may be "+getModifiers(assignment.simpleUnitTests[testNum]);
                CheckboxStatus[] modifierss=CheckboxStatus.getAllowed(assignment.simpleUnitTests[testNum].getModifiers());
                status+="Test "+(testNum+1)+": Failed, Method "+method+" does not exist. It must be  "+access+
                        (!modifiers.replaceAll("and may be ", "").equals("")?modifiers:"")+
                        "and must return a "+assignment.simpleUnitTests[testNum].getReturnTypeString()+".\n";
            }
        }
        if(assignment.dateDue!=null&&!Date.before(file.getSubmittedDate(), assignment.dateDue)){ //it was submitted late
            grade/=2;
            status+=Date.differenceBefore(assignment.dateDue,file.getSubmittedDate())+" Late";
        }
        final TextGrader grader=gui.getGrader();
        final TextGrade tGrade=grader.getGrade(file.getFirstLastName(), assignment.number);
        final Double gradeNum=grader.getGradeNum(file.getFirstLastName(), assignment.number);
        final String gradeComment=grader.getComment(file.getFirstLastName(), assignment.number);
        final double fGrade=grade;
        final String fStatus=status;
        if(!errorTesting&&(gradeNum==null||gradeNum!=fGrade||gradeComment==null||!gradeComment.equals(status)||!tGrade.unitTested)){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    grader.downloadSheet();
                    TextGrade curGrade=grader.getGrade(file.getFirstLastName(), assignment.number);
                    final TextSpreadsheet sheet=grader.getSpreadsheet();
                    TextName name=sheet.getName(file.getFirstLastName());
                    boolean graded=false;
                    if(name==null){
                        String[] splitName=gui.getGrader().splitName(file.getFirstLastName());
                        if(splitName!=null){
                            sheet.addName(splitName[0], splitName[1], null);
                            name=sheet.getName(splitName[0]+splitName[1]);
                        }
                        else{
                            graded=true;
                            final NameOverlay overlay=new NameOverlay(gui);
                            overlay.setData(file.getFirstLastName(), file.getFirstLastName(), null);
                            overlay.setCallback(new Runnable() {
                                @Override
                                public void run() {
                                    String names=overlay.getNames()[0]+overlay.getNames()[1];
                                    sheet.addName(overlay.getNames()[0],overlay.getNames()[1],overlay.getNames()[2]);
                                    TextName name=sheet.getName(names);
                                    sheet.setGrade(name, 
                                            assignment, fGrade, fStatus, true);
                                    TextGrade g=grader.getGrade(names, assignment.number);
                                    g.unitTested=true;
                                    g.dateGraded=Date.currentDate();
                                    
                                    gui.getEmailer().emailGraded(assignment, name, g, null);
                                    grader.uploadTable();
                                    gui.repaint();
                                    gui.gradebookDataChanged();
                                }
                            });
                        }
                        
                    }
                    if(!graded){
                        TextGrade oldGrade=null;
                        if(curGrade==null){
                            sheet.setGrade(name,sheet.getAssignment(assignment.number), fGrade, fStatus, true);
                            curGrade=grader.getGrade(file.getFirstLastName(), assignment.number);
                            curGrade.unitTested=true;
                            curGrade.dateGraded=Date.currentDate();

                            gui.getEmailer().emailGraded(assignment, name, curGrade, null);
                        }
                        else{
                            oldGrade=new TextGrade(curGrade);
                            curGrade.grade=fGrade;
                            curGrade.comment=fStatus;
                            curGrade.unitTested=true;
                            curGrade.dateGraded=Date.currentDate();

                            gui.getEmailer().emailGraded(assignment, name, curGrade, oldGrade);
                        }
                        grader.uploadTable();
                        gui.repaint();
                        gui.gradebookDataChanged();
                    }
                }
            });
        }
        else if(!errorTesting){ //lets update the date graded
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    TextGrader grader=gui.getGrader();
                    grader.downloadSheet();
                    TextGrade g=grader.getGrade(file.getFirstLastName(), assignment.number);
                    g.dateGraded=Date.currentDate();
                    grader.uploadTable();
                }
            });
        }

        //reset test data for the next person
        testResults.clear();
        testStatus.clear();
    }
    private void runJUnitTest(DbxFile file,JavaFile compileTest,JavaFile runTest){
        if(overlay!=null)
            overlay.append("Running JUnit Test on "+file.getFileName());
        JavaRunner runner=gui.getRunner();
        String[] libraries=assignment.libraries;
        if(libraries==null)
            libraries=new String[0];
        String libsFolder=gui.getManager().getDownloadFolder()+"/";
        for(int i=0;i<libraries.length;i++){
            try{
                File outputFile=new File(libsFolder+libraries[i]);
                if(!outputFile.exists()){
                    FileOutputStream f = new FileOutputStream(libsFolder+libraries[i]);
                    gui.getDbxSession().getClient().getFile(libraries[i], null, f); //downloads from dropbox server
                    f.close();
                }
            } catch(IOException|DbxException e){
                System.err.println("Error downloading libraries to run with.");
                e.printStackTrace();
            }
        }
        String[] results=runner.runJUnit(compileTest,runTest,file,assignment.libraries);
        if(!results[1].equals("")||results[0].contains("Could not find class:")){
            String error="There were errors running JUnit Test \nErrors: '"+results[1]+"' \nOutput: '"+results[0]+"'";
            System.err.println(error);
            testResults.add(null);
            testStatus.add(null);
            if(overlay!=null)
                overlay.append(error,Color.RED);
            else
                GuiHelper.alertDialog(error);
            return;
        }
        if(results==null){
            System.err.println("Unknown error running JUnitTest '"+file.getFileName()+"' Program wouldn't run.");
            if(overlay!=null)
                overlay.append("Unknown error running JUnitTest '"+file.getFileName()+"' Program wouldn't run.",Color.RED);
            return;
        }
        String[] resultsLines=results[0].split("\n");
        boolean[] passed;
        int lastIndex=-1;
        for(int i=0;i<resultsLines.length;i++){
            int error=-1;
            String line=resultsLines[i];
            String[] passResults=line.split(Pattern.quote("."));
            if(i==1){
                passed=new boolean[passResults.length];
                for(int x=1;x<passResults.length;x++){
                    if(passResults[x].equals("")){
                        passed[x]=true;
                        testResults.add(true);
                        testStatus.add("Passed");
                        if(overlay!=null)
                            overlay.append("Passed Test "+x);
                    }
                    else{
                        passed[x]=false;
                        testResults.add(false);
                        testStatus.add("Failed");
                        if(overlay!=null)
                            overlay.append("Failed Test "+x);
                    }
                }
            }
            else if(lastIndex!=-1||junitErrorNum(line)!=-1){
                if(lastIndex==-1){
                    for(int z=0;z<testStatus.size();z++){
                        if(testStatus.get(z).equals("Failed")){
                            error=z;
                            break;
                        }
                    }
                }
                int index=testStatus.size()-(testStatus.size()-error);
                String statusText;
                if(junitErrorNum(line)!=-1)
                    statusText=" while running test '"+line.substring(line.indexOf(") ")+2,line.indexOf("("))+"'"; //extract test method name
                else{
                    statusText=line.substring(line.indexOf(":")+1); //extract exception
                    index=lastIndex;
                }
                String currentStatus=testStatus.get(index);
                if(!currentStatus.equals("Failed")){ //first 2 lines of error get put in the comment
                    lastIndex=-1;
                }
                else
                    lastIndex=index;
                statusText=currentStatus+statusText;
                testStatus.set(index,statusText);
            }
        }
    }
    private void runSimpleTest(DbxFile file,UnitTest unitTest,int javaFileIndex,JavaMethod testMethod){
        String types=unitTest.getArgumentTypesString();
        if(types==null)
            types="";
        String method=unitTest.getMethodName()+"("+types+")";
        if(overlay!=null)
            overlay.append("Running Unit Test: "+(unitTest.getDescription()==null?method:("'"+unitTest.getDescription()+"'"))+" on "+file.getFileName());
        currentFile=file.getJavaFiles()[javaFileIndex];
        String code=currentFile.getCode();
        if(code.contains("//INJECTED-FOR-UNIT-TEST")){
            file.forceDownload();
            runSimpleTest(file,unitTest,javaFileIndex,testMethod);
            return;
        }
        for(JavaMethod m:currentFile.getMethods()){
            if(m.methodName.equals("main")){ //we need to rename a method
                int index=code.indexOf(m.getMethodString().trim());
                if(index!=-1){
                    String sub=code.substring(index,index+m.getMethodString().length());
                    String checkString=" main ";
                    int mainIndex=sub.indexOf(checkString);
                    if(mainIndex==-1){
                        checkString=" main(";
                        mainIndex=sub.indexOf(checkString);
                        checkString=checkString.substring(0,checkString.length()-1);
                    }
                    sub=sub.substring(0,mainIndex)+" main2 "+sub.substring(mainIndex+checkString.length());
                    code=code.substring(0,index)+sub+code.substring(index+m.getMethodString().length());
                } else{
                    System.err.println("Thought there was a existing main method, but then couldn't find it.");
                }
                
                break;
            }
        }
        String args=unitTest.getArgumentData();
        if(args==null)
            args="";
        int lastIndex=code.lastIndexOf("}");
        String methodCallString=unitTest.getMethodName();
        if(!testMethod.modifiers.staticMod){
            //if we need to use a constructor to access it
            String className=currentFile.getName().substring(0,currentFile.getName().length()-5); //remove .java
            methodCallString="new "+className+"()."+methodCallString;
        }
        String classInjected=null;
        if(testMethod.accessType==MethodAccessType.PACKAGEPRIVATE||testMethod.accessType==MethodAccessType.PRIVATE||
                testMethod.accessType==MethodAccessType.PROTECTED){
            //we need to inject a method to make it work.
            classInjected="public static void m1(){System.out.println("+methodCallString+"("+args+"));}";
        }
        String inject="//INJECTED-FOR-UNIT-TEST\n";
        if(classInjected!=null){
            inject+=classInjected+"\n";
            inject+="public static void main(String[] args){m1();";
        }
        else{
            inject+="public static void main(String[] args){System.out.println("+methodCallString+"("+args+"));";
        }
        inject+="}\n//INJECTED-FOR-UNIT-TEST\n";
        code=code.substring(0,lastIndex-1)+inject+code.substring(lastIndex,code.length());
        currentPreviousCode=currentFile.getCode();
        String result=currentFile.changeCode(code);
        if(!result.equals("")){
            System.err.println("Error running unit tests. Could not modify file: "+result);
        }
        String[] libraries=assignment.libraries;
        if(libraries==null)
            libraries=new String[0];
        String libsFolder=gui.getManager().getDownloadFolder()+"/";
        for(int i=0;i<libraries.length;i++){
            try{
                File outputFile=new File(libsFolder+libraries[i]);
                if(!outputFile.exists()){
                    FileOutputStream f = new FileOutputStream(libsFolder+libraries[i]);
                    gui.getDbxSession().getClient().getFile(libraries[i], null, f); //downloads from dropbox server
                    f.close();
                }
            } catch(IOException|DbxException e){
                System.err.println("Error downloading libraries to run with.");
                e.printStackTrace();
            }
        }
        String[] value=gui.getRunner().runTest(file.getJavaFiles(),currentFile,this,assignment.libraries);
        if(value[0]==null){
            compileFinished();
            testResults.add(false);
            testStatus.add("Error running Unit Test:\n"+value[1]);
            
            if(overlay!=null)
                overlay.append("Error running Simple Unit Test '"+file.getFileName()+"':\n"+value[1],Color.RED);
            System.err.println("Error running Simple Unit Test '"+file.getFileName()+"':\n"+value[1]);
            return;
        }
        
        String status="";
        if(value[0]!=null&&unitTest.getExpectedReturnValue()!=null&&value[0].trim().equals(unitTest.getExpectedReturnValue().trim())){
            testResults.add(true);
            status="Passed";
            testStatus.add(status);    
            if(overlay!=null)
                overlay.append("Test Passed");
        }
        else if(value[0]!=null){
            testResults.add(false);
            String description=unitTest.getDescription();
            status="Failed while running test ";
            if(description!=null)
                status+="'"+description+"' ";
            status+="on method "+method+" Expected: "+
                    (unitTest.getExpectedReturnValue()==null?null:unitTest.getExpectedReturnValue().trim())
                    +" Actual: "+(value[0]==null?null:value[0].trim());
            testStatus.add(status);
            if(overlay!=null)
                overlay.append(status);
        }
    }
    public void compileFinished(){
        String result=currentFile.changeCode(currentPreviousCode);
        if(!result.equals("")){
            System.err.println("Error restoring code to state before unit testing.\n"+result);
            if(overlay!=null)
                overlay.append("Error saving code. "+result,Color.RED);
        }
        currentFile=null;
        currentPreviousCode=null;
    }
    public static boolean testMatch(JavaMethod method,UnitTest test){ //I really hate that it has to be this way, but I dont know an alternative
        //if the name is the same
        if(!method.methodName.equals(test.getMethodName()))
            return false;
        //if the return type is the same
        if(!method.returnType.equals(test.getReturnType()))
            return false;
        //if test requres against a certain method type
        if(method.accessType==MethodAccessType.PACKAGEPRIVATE&&test.accessPackagePrivate==CheckboxStatus.DISALLOWED)
            return false;
        else if(method.accessType==MethodAccessType.PRIVATE&&test.accessPrivate==CheckboxStatus.DISALLOWED) //else is there because it makes this mess feel a bit more structured
            return false;
        else if(method.accessType==MethodAccessType.PROTECTED&&test.accessProtected==CheckboxStatus.DISALLOWED)
            return false;
        else if(method.accessType==MethodAccessType.PUBLIC&&test.accessPublic==CheckboxStatus.DISALLOWED)
            return false;
        //if test requires against a certain modifier
        if(method.modifiers.abstractMod&&test.modAbstract==CheckboxStatus.DISALLOWED)
            return false;
        if(method.modifiers.finalMod&&test.modFinal==CheckboxStatus.DISALLOWED)
            return false;
        if(method.modifiers.staticMod&&test.modStatic==CheckboxStatus.DISALLOWED)
            return false;
        if(method.modifiers.synchronizedMod&&test.modSynchronized==CheckboxStatus.DISALLOWED)
            return false;
        //if arguments are the same
        if(method.arguments==null&&test.getArgumentTypes()!=null){
            if(test.getArgumentTypes().length!=0)
                return false;
        }
        if(method.arguments!=null&&test.getArgumentTypes()==null){
            if(method.arguments.length!=0)
                return false;
        }
        if(method.arguments!=null&&test.getArgumentTypes()!=null){
            if(method.arguments.length!=test.getArgumentTypes().length)
                return false;
            for(int i=0;i<method.arguments.length;i++){
                if(!method.arguments[i].equals(test.getArgumentTypes()[i]))
                    return false;
            }
        }

        return true;
    }
    public static int count(char c,String s){
        int total=0;
        for(int i=0;i<s.length();i++){
            char current=s.charAt(i);
            if(current==c)
                total++;
        }
        return total;
    }
    private int junitErrorNum(String line){
        String num="";
        for(int i=0;i<line.length();i++){
            char c=line.charAt(i);
            if(i==0&&!Character.isDigit(c)){
                return -1;
            }
            else if(i==0){
                num+=c;
            }
            if(i!=0&&(Character.isDigit(c)&&Character.isDigit(line.charAt(i-1)))){
                num+=c;
            }
            else if(i!=0&&Character.isDigit(line.charAt(i-1))&&c==')'){
                return Integer.parseInt(num);
            }
        }
        return -1;
    }
    private JavaFile validateDirectory(JavaFile file,String packageDir){
        if(packageDir==null)
            packageDir=file.packageFolder();
        if(packageDir==null||packageDir.equals(""))
            return file;
        String[] packages=packageDir.split(Pattern.quote("/"));
        File parent=file.getParentFile();
        if(parent.getName().equals(packages[0]))
            return validateDirectory(file,arrayToString(1,packages.length,packages,"/"));
        //move into the last package directory
        File[] currentFiles=parent.listFiles();
        if(currentFiles!=null){
            for(File f:currentFiles){ //check if we already have the folder made and somethings in it because we cant overwrite with renameTo
                if(f.getName().equals(packages[0])&&f.isDirectory()){
                    File[] newFiles=f.listFiles();
                    if(newFiles!=null){
                        for(File ff:newFiles){
                            if(ff.getName().equals(file.getName()))
                                ff.delete();
                        }
                    }
                }
            }
        }
        File newFolder=new File(parent.getAbsolutePath()+"/"+packages[0]);
        newFolder.mkdir();
        File newFile=new File(newFolder.getAbsolutePath()+"/"+file.getName());
        file.renameTo(newFile);
        return validateDirectory(new JavaFile(newFile,null),arrayToString(1,packages.length,packages,"/"));
    }
    public static String arrayToString(int offset,int length,String[] arr,String delim){
        String ret="";
        for(int i=offset;i<length;i++){
            ret+=arr[i];
            if(i!=length-1)
                ret+=delim;
        }
        return ret;
    }
    public static String getAccesses(UnitTest t){
        String access="";
        CheckboxStatus[] accesses=CheckboxStatus.getAllowed(t.getAccess());
        for(int i=0;i<accesses.length;i++){
            if(accesses[i]!=null){
                if(i==0)
                    access+="public";
                else if(i==1)
                    access+="protected";
                else if(i==2)
                    access+="private";
                else if(i==3)
                    access+="package-private";
                int numMore=0;
                for(int x=i+1;x<accesses.length;x++){
                    if(accesses[x]!=null)
                        numMore++;
                }
                if(numMore>1)
                    access+=", ";
                else if(numMore==1)
                    access+=" or ";
                else
                    access+=" ";
            }
        }
        return access;
    }
    public static String getModifiers(UnitTest t){
        CheckboxStatus[] modifierss=CheckboxStatus.getAllowed(t.getModifiers());
        String modifiers="";
        for(int i=0;i<modifierss.length;i++){
            if(modifierss[i]!=null){
                if(i==0)
                    modifiers+="static";
                else if(i==1)
                    modifiers+="final";
                else if(i==2)
                    modifiers+="abstract";
                else if(i==3)
                    modifiers+="synchronized";
                int numMore=0;
                for(int x=i+1;x<modifierss.length;x++){
                    if(modifierss[x]!=null)
                        numMore++;
                }
                if(numMore>1)
                    modifiers+=", ";
                else if(numMore==1)
                    modifiers+=" and/or ";
                else
                    modifiers+=" ";
            }
        }
        return modifiers;
    }
}
