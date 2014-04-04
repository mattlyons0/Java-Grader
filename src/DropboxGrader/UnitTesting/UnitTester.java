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

/**
 *
 * @author matt
 */
public class UnitTester {
    private Gui gui;
    private TextAssignment assignment;
    
    public UnitTester(Gui gui,TextAssignment assign){
        this.gui=gui;
        assignment=assign;
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
        for(int i=0;i<javaFiles.length;i++){
            JavaMethod[] methods=javaFiles[i].getMethods();
            for(JavaMethod m:methods){
                if(m.getMethodName().equals(assignment.unitTest.getMethodName())){
                    runTest(file,i);
                }
            }
        }
    }
    private void runTest(DbxFile file,int javaFileIndex){
        UnitTest unitTest=assignment.unitTest;
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
        System.out.println("Unit Test on "+file.getFileName()+" returned "+value+" Expected: "+unitTest.getExpectedReturnValue());
        
        String status;
        double grade;
        TextGrader grader=gui.getGrader();
        if(value!=null&&unitTest.getExpectedReturnValue()!=null&&value.trim().equals(unitTest.getExpectedReturnValue().trim())){
            status="Unit Test Passed";
            grade=assignment.totalPoints;            
            if(grader.getGradeNum(file.getFirstLastName(), assignment.number)!=grade){
                grader.setGrade(file.getFirstLastName(), assignment.number, assignment.totalPoints,status, true);
            }
        }
        else{
            status="Unit Test Failed while testing method: "+unitTest.getMethodName()+" Expected: "+unitTest.getExpectedReturnValue()
                    +" Actual: "+value==null?value:value.trim();
            grade=0;
            if(grader.getGradeNum(file.getFirstLastName(), assignment.number)!=grade){
                grader.setGrade(file.getFirstLastName(), assignment.number, grade,status, true);
            }
        }
        testFile.changeCode(oldCode);
    }
}
