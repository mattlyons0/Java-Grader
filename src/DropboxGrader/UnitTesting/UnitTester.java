/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrader;
import java.util.ArrayList;

/**
 *
 * @author matt
 */
public class UnitTester {
    private Gui gui;
    private TextAssignment assignment;
    
    private ArrayList<Boolean> testResults;
    private ArrayList<String> testStatus;
    
    public UnitTester(Gui gui,TextAssignment assign){
        this.gui=gui;
        assignment=assign;
        
        testResults=new ArrayList();
        testStatus=new ArrayList();
    }
    public void runTests(){
        FileManager manager=gui.getManager();
        DbxFile[] files=manager.getFiles().toArray(new DbxFile[0]);
        for(int i=0;i<files.length;i++){
            if(files[i].getAssignmentNumber()==assignment.number)
                prepareTest(files[i]);
        }
    }
    private void prepareTest(DbxFile file){
        JavaFile[] javaFiles=file.getJavaFiles();
        if(javaFiles==null){
            //we need to download the file
            return;
        }
        for(int i=0;i<javaFiles.length;i++){
            JavaMethod[] methods=javaFiles[i].getMethods();
            for(JavaMethod m:methods){
                for(UnitTest test:assignment.unitTests){
                    if(m.getMethodName().equals(test.getMethodName())){
                        runTest(file,test,i);
                    }
                }
                
                //write grade
                double grade;
                int successes=0;
                for(Boolean result:testResults){
                    if(result)
                        successes++;
                }
                grade=successes/(double)testResults.size()*assignment.totalPoints;
                String status="(Unit Tested) "+successes+" Passed of "+testResults.size()+", ";
                for(int testNum=0;testNum<testStatus.size();testNum++){
                    status+="Test "+(testNum+1)+": "+testStatus.get(testNum)+" ";
                }
                TextGrader grader=gui.getGrader();
                Double gradeNum=grader.getGradeNum(file.getFirstLastName(), assignment.number);
                if(gradeNum==null||gradeNum!=grade){
                    grader.setGrade(file.getFirstLastName(), assignment.number, grade,status, true);
                }
                
                //reset test data for the next person
                testResults.clear();
                testStatus.clear();
            }
        }
    }
    private void runTest(DbxFile file,UnitTest unitTest,int javaFileIndex){
        JavaFile testFile=file.getJavaFiles()[javaFileIndex];
        String code=testFile.getCode();
        int lastIndex=code.lastIndexOf("}");
        String args=unitTest.getArgumentData();
        if(args==null)
            args="";
        String inject="//INJECTED-FOR-UNIT-TEST\n"
                + "public static void main(String[] args){";
        inject+="System.out.println("+unitTest.getMethodName()+"("+args+"));";
        inject+="}\n//INJECTED-FOR-UNIT-TEST\n";
        if(code.contains(inject)){
            
        }
        code=code.substring(0,lastIndex-1)+inject+code.substring(lastIndex,code.length());
        String oldCode=testFile.getCode();
        testFile.changeCode(code);
        String value=gui.getRunner().runTest(file.getJavaFiles(),testFile);
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
            status="Failed while testing method: "+unitTest.getMethodName()+" Expected: "+
                    (unitTest.getExpectedReturnValue()==null?null:unitTest.getExpectedReturnValue().trim())
                    +" Actual: "+(value==null?null:value.trim());
            testStatus.add(status);
        }
        testFile.changeCode(oldCode);
    }
}
