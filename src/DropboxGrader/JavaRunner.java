/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *http://stackoverflow.com/questions/16140031/how-to-use-runtime-getruntime-to-execute-a-java-file
 * @author 141lyonsm
 */
public class JavaRunner implements Runnable{
    private Process running;
    private JTerminal area;
    private OutputStream stream;
    private InputStream inStream;
    private Scanner s1,s2;
    private PrintStream printStream;
    private Thread thread;
    public JavaRunner(JTerminal a,OutputStream stream,InputStream inStream){
        area=a;
        this.stream=stream;
        this.inStream=inStream;

        thread=new Thread(this);
        thread.setName("OutputReadThread");
        //thread.start();
    }
    @Override
    public void run(){
        while(true){
            //System.out.println(); //why the hell doesnt it work without this
            if(running!=null){
                if(s1.hasNext()){
                    area.append(s1.next());
                }
                if(s2.hasNext()){
                    area.append(s2.next());
                }
            }
        }
    }
    public void stopProcess(){
        if(running!=null){
            running.destroy();
        }
        running=null;
    }
    public void runFile(JavaFile[] files,JavaFile runChoice){
        if(files.length==0){
            return;
        }
        area.setText("");
        stopProcess();
        
        boolean containsPackages=false;
        for(JavaFile f: files){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        int manualArgNum=4;
        String[] filePaths=new String[files.length+manualArgNum];
        
        filePaths[0]="-cp";
        String path=files[0].getAbsolutePath();
        if(path.length()!=0){
            path=path.replace("\\", "=");
            String[] pathPart=path.split("="); //cant split \ for whatever reason
            path=path.replace("=", "\\");
            path=path.substring(0, path.length()-pathPart[pathPart.length-1].length());
            if(containsPackages){
                path=path.substring(0, path.length()-pathPart[pathPart.length-2].length()-1);
            }
            if(pathPart.length==1){
                path="";
            }
        }
        filePaths[1]="\""+path+"\""; //careful if removed, referenced in the run loop.
        filePaths[2]="-sourcepath";
        filePaths[3]=filePaths[1];
        for(int x=manualArgNum;x<files.length+manualArgNum;x++){
            filePaths[x]=files[x-manualArgNum].getAbsolutePath();
        }
        System.out.println("Compiling "+Arrays.toString(filePaths));
        try {
            area.append("Compiling.");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int result=compiler.run(inStream, stream, stream, filePaths);
            if(result!=0){
                area.append("Compile Failed.");
                return;
            }
            else{
                area.append("Compile Successful.");
            }
            int index=-1;
            for(int x=0;x<files.length;x++){
                if(files[x].equals(runChoice)){
                    index=x;
                }
            }
            if(index==-1){
                System.out.println("Main File doesnt exist!");
                return;
            }
            //for(int x=0;x<files.length;x++){
            String classpath=filePaths[1].substring(1, filePaths[1].length()-1); //removes quotes in filepaths[1]
            String className=files[index].getName();
            className=className.substring(0,className.length()-5); //removes .java
            String javaExe=System.getProperty("java.home")+"\\bin\\java.exe";
            javaExe="java";
            String directory=System.getProperty("java.home")+"\\bin\\";
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            builder.inheritIO();
            //builder.directory(new File(directory));
            //System.out.println("Running from: "+directory);
            running=builder.start();
            //System.setOut(s);
            //running=Runtime.getRuntime().exec("java "+call);
//            s1=new Scanner(new InputStreamReader(running.getInputStream()));
//            s1.useDelimiter("\n");
//            s2=new Scanner(new InputStreamReader(running.getErrorStream()));
//            s2.useDelimiter("\n");
//            FilterOutputStream filterStream=(FilterOutputStream) running.getOutputStream();
//            printStream=new PrintStream(filterStream);
            System.out.println("Making call: "+javaExe+" -cp "+classpath+" "+className);
            //}
        } catch (IOException ex) {
            Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
