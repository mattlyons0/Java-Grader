/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.UnitTesting.UnitTestOverlay;
import DropboxGrader.GuiHelper;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.UnitTesting.SimpleTesting.JavaMethod;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import com.dropbox.core.DbxException;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    JavaFile currentFile;
    String currentPreviousCode;
    
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
        JavaFile[] javaFiles=file.getJavaFiles();
        if(javaFiles==null){
            //we need to download the file
            if(overlay!=null)
                overlay.setStatus("Downloading File to be Tested");
            file.download();
            return;
        }
        if(overlay!=null)
            overlay.setStatus("Running Unit Tests");
        for(int i=0;i<javaFiles.length;i++){
            JavaMethod[] methods=javaFiles[i].getMethods();
            for(JavaMethod m:methods){
                if((overlay==null||!overlay.isCanceled())&&assignment.simpleUnitTests!=null){
                    if(overlay!=null)
                        overlay.setDescription("Assignment: "+assignment+" File: "+file.getFileName());
                    for(UnitTest test:assignment.simpleUnitTests){
                        if((overlay==null||!overlay.isCanceled())&&(test!=null&&testMatch(m,test))){
                            if(overlay!=null)
                                overlay.setDescription("Method: "+m.getMethodString()+" Unit Test: "+test.getDescription()+" Assignment: "+assignment+" File: "+file.getFileName());
                            runSimpleTest(file,test,i);
                            if(overlay!=null)
                                overlay.setDescription("Assignment: "+assignment+" File: "+file.getFileName());
                        }
                    }
                }
                if(overlay!=null)
                    overlay.setDescription("File: "+file.getFileName());
            }
        }
        if(overlay!=null)
            overlay.setStatus("Running JUnit Tests");
        if((overlay==null||!overlay.isCanceled())&&assignment.junitTests!=null){
            for(String testLoc:assignment.junitTests){
                try {
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
                            JavaFile runFile=new JavaFile(new File(localTestLoc),null);
                            JavaFile compileFile=runFile;
                            if(runFile.packageFolder()!=null){ //we gotta verify its in the right directory
                                compileFile=validateDirectory(runFile,null); //moves it to the right directory, but doesnt update pointer so that it gets called correctly
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
                } catch (DbxException ex) {
                    System.err.println("Error communicating with dropbox when downloading unit test file.\n"+ex);
                    if(overlay!=null)
                        overlay.append("Error downloading JUnit Test: "+testLoc+" from dropbox!",Color.RED);
                    ex.printStackTrace();
                }
                if(overlay!=null&&overlay.isCanceled())
                    break;
            }
        }
        if(overlay!=null)
            overlay.setStatus("Grading Unit Tests");
        //write grade
        final double grade;
        int successes=0;
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
                status+="Test "+(testNum+1)+": Failed, Method "+method+" does not exist \n";
            }
        }
        final TextGrader grader=gui.getGrader();
        final Double gradeNum=grader.getGradeNum(file.getFirstLastName(), assignment.number);
        final String fStatus=status;
        if(!errorTesting&&(gradeNum==null||gradeNum!=grade)){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    grader.setGrade(file.getFirstLastName(), assignment.number, grade,fStatus, (gradeNum!=null));
                    gui.repaint();
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
        String[] results=runner.runJUnit(compileTest,runTest,file);
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
            System.err.println("Error getting results from JUnit Test, result was null.");
            if(overlay!=null)
                overlay.append("Unknown error running JUnitTest.",Color.RED);
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
    private void runSimpleTest(DbxFile file,UnitTest unitTest,int javaFileIndex){
        if(overlay!=null)
            overlay.append("Running Unit Test: "+unitTest.getDescription()+" on "+file.getFileName());
        String types=unitTest.getArgumentTypesString();
        if(types==null)
            types="";
        String method=unitTest.getMethodName()+"("+types+")";
        currentFile=file.getJavaFiles()[javaFileIndex];
        String code=currentFile.getCode();
        if(code.contains("//INJECTED-FOR-UNIT-TEST\npublic static void main(String[] args){")){
            file.forceDownload();
            runSimpleTest(file,unitTest,javaFileIndex);
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
        if(unitTest.modStatic==CheckboxStatus.IGNORED||unitTest.modStatic==CheckboxStatus.REQUIREDFALSE){
            //if we might need to use a constructor to access it
            String className=currentFile.getName().substring(0,currentFile.getName().length()-5); //remove .java
            methodCallString="new "+className+"()."+methodCallString;
        }
        if(unitTest.accessPackagePrivate==CheckboxStatus.IGNORED||unitTest.accessPackagePrivate==CheckboxStatus.REQUIREDTRUE||
                unitTest.accessPrivate==CheckboxStatus.IGNORED||unitTest.accessPrivate==CheckboxStatus.REQUIREDTRUE||
                unitTest.accessProtected==CheckboxStatus.IGNORED||unitTest.accessProtected==CheckboxStatus.REQUIREDTRUE){
            //we need to inject a method to make it work.
        }
        String inject="//INJECTED-FOR-UNIT-TEST\n"
                + "public static void main(String[] args){";
        inject+="System.out.println("+methodCallString+"("+args+"));";
        inject+="}\n//INJECTED-FOR-UNIT-TEST\n";
        code=code.substring(0,lastIndex-1)+inject+code.substring(lastIndex,code.length());
        currentPreviousCode=currentFile.getCode();
        String result=currentFile.changeCode(code);
        if(!result.equals("")){
            System.err.println("Error running unit tests. Could not modify file: "+result);
        }
        String[] value=gui.getRunner().runTest(file.getJavaFiles(),currentFile,this);
        if(value[0]==null){
            compileFinished();
            testResults.add(null);
            testStatus.add(null);
            GuiHelper.alertDialog("Error running Simple Unit Tests. "+value[1]);
            System.err.println("Error running Simple Unit Tests. "+value[1]);
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
        if(method.accessType==MethodAccessType.PACKAGEPRIVATE&&test.accessPackagePrivate==CheckboxStatus.REQUIREDFALSE)
            return false;
        else if(method.accessType==MethodAccessType.PRIVATE&&test.accessPrivate==CheckboxStatus.REQUIREDFALSE) //else is there because it makes this mess feel a bit more structured
            return false;
        else if(method.accessType==MethodAccessType.PROTECTED&&test.accessProtected==CheckboxStatus.REQUIREDFALSE)
            return false;
        else if(method.accessType==MethodAccessType.PUBLIC&&test.accessPublic==CheckboxStatus.REQUIREDFALSE)
            return false;
        //if a test requires a certain method type
        if(test.accessPackagePrivate==CheckboxStatus.REQUIREDTRUE&&method.accessType!=MethodAccessType.PACKAGEPRIVATE)
            return false;
        else if(test.accessPrivate==CheckboxStatus.REQUIREDTRUE&&method.accessType!=MethodAccessType.PRIVATE)
            return false;
        else if(test.accessProtected==CheckboxStatus.REQUIREDTRUE&&method.accessType!=MethodAccessType.PROTECTED)
            return false;
        else if(test.accessPublic==CheckboxStatus.REQUIREDTRUE&&method.accessType!=MethodAccessType.PUBLIC)
            return false;
        //if test requires against a certain modifier
        if(method.modifiers.abstractMod&&test.modAbstract==CheckboxStatus.REQUIREDFALSE)
            return false;
        if(method.modifiers.finalMod&&test.modFinal==CheckboxStatus.REQUIREDFALSE)
            return false;
        if(method.modifiers.staticMod&&test.modStatic==CheckboxStatus.REQUIREDFALSE)
            return false;
        if(method.modifiers.synchronizedMod&&test.modSynchronized==CheckboxStatus.REQUIREDFALSE)
            return false;
        //if a test requres a certain modifier
        if(!method.modifiers.abstractMod&&test.modAbstract==CheckboxStatus.REQUIREDTRUE)
            return false;
        if(!method.modifiers.finalMod&&test.modFinal==CheckboxStatus.REQUIREDTRUE)
            return false;
        if(!method.modifiers.staticMod&&test.modStatic==CheckboxStatus.REQUIREDTRUE)
            return false;
        if(!method.modifiers.synchronizedMod&&test.modSynchronized==CheckboxStatus.REQUIREDTRUE)
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
}
