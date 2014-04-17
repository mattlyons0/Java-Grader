/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.UnitTesting.SimpleTesting.JavaMethod;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import com.dropbox.core.DbxException;
import java.io.File;
import java.io.FileNotFoundException;
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
    JavaFile currentFile;
    String currentPreviousCode;
    
    private ArrayList<Boolean> testResults;
    private ArrayList<String> testStatus;
    
    public UnitTester(Gui gui,TextAssignment assign){
        this.gui=gui;
        assignment=assign;
        
        testResults=new ArrayList();
        testStatus=new ArrayList();
    }
    public void runTests(){
        while(currentFile!=null){
            try {
                Thread.sleep(50); //this isn't really good, but at the same time we can't run the tests concurrently to eachother if someone calls the method twice
            } catch (InterruptedException ex) {
                Logger.getLogger(UnitTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileManager manager=gui.getManager();
        DbxFile[] files=manager.getFiles().toArray(new DbxFile[0]);
        
        for(int i=0;i<files.length;i++){
            if(files[i].getAssignmentNumber()==assignment.number){
                prepareTest(files[i]);
            }
        }
    }
    private void prepareTest(DbxFile file){
        JavaFile[] javaFiles=file.getJavaFiles();
        if(javaFiles==null){
            //we need to download the file
            System.err.println("There are files that need to be downloaded in order to be unit tested.");
            return;
        }
        for(int i=0;i<javaFiles.length;i++){
            JavaMethod[] methods=javaFiles[i].getMethods();
            for(JavaMethod m:methods){
                if(assignment.simpleUnitTests!=null){
                    for(UnitTest test:assignment.simpleUnitTests){
                        if(testMatch(m,test)){
                            runSimpleTest(file,test,i);
                        }
                    }
                }
            }
            if(assignment.junitTests!=null){
                for(String testLoc:assignment.junitTests){
                    try {
                        if(!testLoc.equals("")&&gui.getDbxSession().getClient().getMetadata(testLoc)!=null){
                            String[] testPaths=testLoc.split(Pattern.quote("/"));
                            String testName;
                            if(testPaths.length==0)
                                testName=testLoc;
                            else
                                testName=testPaths[testPaths.length-1];
                            String localTestLoc=gui.getManager().getDownloadFolder()+"/"+testName;
                            try{
                                FileOutputStream f = new FileOutputStream(localTestLoc);
                                System.out.println(testLoc);
                                gui.getDbxSession().getClient().getFile(testLoc, null, f); //downloads from dropbox server
                                f.close();
                                runJUnitTest(file,new File(localTestLoc));
                            } catch(IOException ex){
                                System.err.println("Error downloading unit test file.\n"+ex);
                                ex.printStackTrace();
                            }
                        }
                    } catch (DbxException ex) {
                        System.err.println("Error communicating with dropbox when downloading unit test file.\n"+ex);
                        ex.printStackTrace();
                    }
                }
            }

            //write grade
            double grade;
            int successes=0;
            for(Boolean result:testResults){
                if(result)
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
            String status="(Unit Tested) Passed "+successes+"/"+totalTests+", ";
            for(int testNum=0;testNum<totalTests;testNum++){
                if(testNum<testStatus.size()){
                    status+="Test "+(testNum+1)+": "+testStatus.get(testNum)+" ";
                }
                else if(assignment.simpleUnitTests!=null&&testNum<assignment.simpleUnitTests.length){
                    String argTypes=assignment.simpleUnitTests[testNum].getArgumentTypesString();
                    if(argTypes==null)
                        argTypes="";
                    String method=assignment.simpleUnitTests[testNum].getMethodName()+"("+argTypes+")";
                    status+="Test "+(testNum+1)+": Failed, Method "+method+" does not exist ";
                }
                else if(assignment.junitTests!=null&&(assignment.simpleUnitTests==null||testNum>=assignment.simpleUnitTests.length)){
                    
                }
            }
            TextGrader grader=gui.getGrader();
            Double gradeNum=grader.getGradeNum(file.getFirstLastName(), assignment.number);
            if(gradeNum==null||gradeNum!=grade){
                grader.setGrade(file.getFirstLastName(), assignment.number, grade,status, (gradeNum!=null));
            }

            //reset test data for the next person
            testResults.clear();
            testStatus.clear();
        }
    }
    private void runJUnitTest(DbxFile file,File unitTest){
        JavaRunner runner=gui.getRunner();
        String results=runner.runJUnit(unitTest,file);
        System.out.println(results);
        if(results==null){
            System.err.println("Error getting results from JUnit Test, result was null.");
            return;
        }
        String[] resultsLines=results.split("\n");
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
                    }
                    else{
                        passed[x]=false;
                        testResults.add(false);
                        testStatus.add("Failed");
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
                    statusText=" "+line.substring(line.indexOf("("));
                else{
                    statusText=line;
                    index=lastIndex;
                }
                String currentStatus=testStatus.get(index);
                if(!currentStatus.equals("Failed")){ //first 2 lines of error get put in the comment
                    statusText=currentStatus+statusText;
                    lastIndex=-1;
                }
                else
                    lastIndex=index;
                testStatus.set(index,statusText);
            }
        }
    }
    private void runSimpleTest(DbxFile file,UnitTest unitTest,int javaFileIndex){
        String types=unitTest.getArgumentTypesString();
        if(types==null)
            types="";
        String method=unitTest.getMethodName()+"("+types+")";
        currentFile=file.getJavaFiles()[javaFileIndex];
        String code=currentFile.getCode();
        String args=unitTest.getArgumentData();
        if(args==null)
            args="";
        int lastIndex=code.lastIndexOf("}");
        String inject="//INJECTED-FOR-UNIT-TEST\n"
                + "public static void main(String[] args){";
        inject+="System.out.println("+unitTest.getMethodName()+"("+args+"));";
        inject+="}\n//INJECTED-FOR-UNIT-TEST\n";
        if(code.contains(inject)){
            
        }
        code=code.substring(0,lastIndex-1)+inject+code.substring(lastIndex,code.length());
        currentPreviousCode=currentFile.getCode();
        String result=currentFile.changeCode(code);
        if(!result.equals("")){
            System.err.println("Error running unit tests. Could not modify file: "+result);
        }
        String value=gui.getRunner().runTest(file.getJavaFiles(),currentFile,this);
        if(value==null)
            compileFinished();
        System.out.println("Unit Test on "+file.getFileName()+" returned "+value+" Expected: "+
                (unitTest.getExpectedReturnValue()==null?null:unitTest.getExpectedReturnValue().trim()));
        
        String status="";
        if(value!=null&&unitTest.getExpectedReturnValue()!=null&&value.trim().equals(unitTest.getExpectedReturnValue().trim())){
            testResults.add(true);
            status="Passed";
            testStatus.add(status);          
        }
        else{
            testResults.add(false);
            status="Failed while testing method "+method+" Expected: "+
                    (unitTest.getExpectedReturnValue()==null?null:unitTest.getExpectedReturnValue().trim())
                    +" Actual: "+(value==null?null:value.trim());
            testStatus.add(status);
        }
    }
    public void compileFinished(){
        String result=currentFile.changeCode(currentPreviousCode);
        if(!result.equals("")){
            System.err.println("Error restoring code to state before unit testing.\n"+result);
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
}
