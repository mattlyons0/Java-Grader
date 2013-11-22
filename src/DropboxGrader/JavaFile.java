/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class JavaFile extends File{
    private boolean mainMethod;
    private String packageFolder;
    public JavaFile(String location,boolean containsMain,String packageF){
        super(location);
        setMainMethod(containsMain);
        packageFolder=packageF;
    }
    public JavaFile(File f){
        super(f.getPath());
        setMainMethod(false);
    }
    public boolean containsMain(){
        return mainMethod;
    }
    public String packageFolder(){
        return packageFolder;
    }
    public boolean hasPackage(){
        if(packageFolder==null){
            return false;
        }
        return true;
    }
    public void setPackage(String packageF){
        packageFolder=packageF;
    }
    public void setMainMethod(boolean hasMain){
        mainMethod=hasMain;
        
        if(hasMain){
            PrintWriter writer=null;
            try {
                //inject code
                String inject="	//DROPBOXGRADERCODESTART\n" +
    "	try {\n" +
    "            java.io.PrintStream printStream=new java.io.PrintStream(new java.io.FileOutputStream(\"output.log\"));\n" +
    "            System.setOut(printStream);\n" +
    "            System.setErr(printStream);\n" +
    "            java.io.File f=new java.io.File(\"input.log\");\n" +
    "            int runNum=0;\n" +
    "            System.setIn(new java.io.FileInputStream(f){\n" +
    "                private int runNum=0;\n" +
    "                @Override\n" +
    "                public int read(byte[] b, int off, int len) throws java.io.IOException {\n" +
    "                    int read=super.read(b, off, len);\n" +
    "                    while(runNum%2==0&&read==-1){ //every 2nd call is for caching and doesnt matter\n" +
    "                        read=super.read(b, off, len);\n" +
    "                    }\n" +
    "                    runNum++;\n" +
    "                    return read;\n" +
    "                }\n" +
    "            });\n" +
    "	} catch (java.io.FileNotFoundException ex) {\n" +
    "            System.out.println(\"The DropboxGrader output logger has failed.\"+ex);\n" +
    "        }\n" +
    "            //DROPBOXGRADERCODEEND\n";
                Scanner s=new Scanner(this);
                s.useDelimiter("\n");
                String currentFile="";
                while(s.hasNext()){
                    String line=s.next()+"\n";
                    if(line.contains("public")&&line.contains("static")&&line.contains("void")&&line.replace(" ", "").contains("main(String[]")){
                        String lineAfter=s.next();
                        if(!line.contains("{")){
                            line+=lineAfter;
                            lineAfter=s.next();
                        }
                        if(lineAfter.contains("//DROPBOXGRADERCODESTART")){
                            return;
                        }
                        int index=line.indexOf("{")+1; 
                        //if someone is an idiot and puts a sout on the same line as the main method header it will catch it.
                        currentFile+=line.substring(0, index)+"\n";
                        System.out.println("Injecting after "+line.substring(0, index));
                        currentFile+=inject;
                        currentFile+=line.substring(index);
                        currentFile+=lineAfter+"\n";
                    }
                    else{
                        currentFile+=line;
                    }
                }
                writer = new PrintWriter(new FileWriter(this,false));
                writer.write(currentFile);
                writer.close();
                
            } catch (IOException ex) {
                Logger.getLogger(JavaFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
