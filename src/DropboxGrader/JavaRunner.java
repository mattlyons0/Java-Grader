/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
    private JTerminal terminal;
    private Gui gui;
    private Thread thread;
    private InputRelayer relay;
    private RelayStream errorRelay;
    private int numRunsLeft;
    private JavaFile[] currentFiles;
    private JavaFile mainFile;
    public JavaRunner(JTerminal t,Gui gui,InputRelayer relay){
        terminal=t;
        this.gui=gui;
        this.relay=relay;
        
        errorRelay=new RelayStream(System.out,terminal);
        
        thread=new Thread(this);
        thread.setName("CheckProccessStateThread");
        thread.start();
    }
    @Override
    public void run(){
        while(true){
            System.out.flush(); //doesnt update state of running unless flush is called, oddly enough. Only at school too.
            if(running!=null){
                try{
                    int code=running.exitValue();
                    //if it gets this far it has ended
                    if(code!=0||numRunsLeft==0)
                        terminal.append("\nRun Finished: "+code+"\n",Color.GRAY);
                    if(numRunsLeft>0){
                        clearInOutFiles();
                        runClass();
                    }
                    else{
                        gui.proccessEnded();
                        currentFiles=null;
                        mainFile=null;
                    }
                    
                }
                catch(IllegalThreadStateException e){
                }
            }
            try {
                if(numRunsLeft>0){
                    Thread.sleep(1000); //basically if the number is smaller than 1000 multiple writes happen before a read
                    //figure out why later...
                }
                else{
                    Thread.sleep(1000);
                }
                //saves resources on slow computers (like the ones in the library)
                //also fixes printing output after saying run finished on slow computers.
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    public void clearInOutFiles(){
        relay.stop();
        try {
            File output=new File("output.log");
            File input=new File("input.log");
            FileWriter outputWriter=new FileWriter(output);
            FileWriter inputWriter = new FileWriter(input);
            outputWriter.write("");
            inputWriter.write("");
            outputWriter.close();
            inputWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public void stopProcess(){
        if(running!=null){
            running.destroy();
            clearInOutFiles();
        }
        running=null;
    }
    public void runFile(JavaFile[] files,JavaFile runChoice, int numTimes){
        if(files.length==0){
            return;
        }
        numRunsLeft=numTimes;
        currentFiles=files;
        mainFile=runChoice;
        
        terminal.setText("");
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
        //System.out.println("Compiling "+Arrays.toString(filePaths));
        try {
                terminal.append("Compile Started\n",Color.GRAY);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int result=compiler.run(null, System.out, errorRelay, filePaths);
            if(result!=0){
                terminal.append("Compile Failed\n\n",Color.RED);
                gui.proccessEnded();
                return;
            }
            else{
                terminal.append("Compile Finished\n\n",Color.GRAY);
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
            String className="";
            if(files[index].hasPackage()){
                className+=files[index].packageFolder()+"/";
            }
            className+=files[index].getName();
            className=className.substring(0,className.length()-5); //removes .java
            String javaExe=System.getProperty("java.home")+"\\bin\\java.exe";
            javaExe="java";
            String directory=System.getProperty("java.home")+"\\bin\\";
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            builder.inheritIO();
            //builder.directory(new File(directory));
            //System.out.println("Running from: "+directory);
                    terminal.append("Run Started: \n\n",Color.GRAY);
            running=builder.start();
            relay.start();
            numRunsLeft--;
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
    private void runClass(){
        boolean containsPackages=false;
        for(JavaFile f: currentFiles){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        int manualArgNum=4;
        String[] filePaths=new String[currentFiles.length+manualArgNum];
        
        filePaths[0]="-cp";
        String path=currentFiles[0].getAbsolutePath();
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
        for(int x=manualArgNum;x<currentFiles.length+manualArgNum;x++){
            filePaths[x]=currentFiles[x-manualArgNum].getAbsolutePath();
        }
        int index=-1;
        for(int x=0;x<currentFiles.length;x++){
            if(currentFiles[x].equals(mainFile)){
                index=x;
            }
        }
        if(index==-1){
            System.out.println("Main File doesnt exist!");
            return;
        }
        String classpath=filePaths[1].substring(1, filePaths[1].length()-1); //removes quotes in filepaths[1]
        String className="";
        if(currentFiles[index].hasPackage()){
            className+=currentFiles[index].packageFolder()+"/";
        }
        className+=currentFiles[index].getName();
        className=className.substring(0,className.length()-5); //removes .java
        String javaExe=System.getProperty("java.home")+"\\bin\\java.exe";
        javaExe="java";
        ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
        builder.inheritIO();
        try {
            running=builder.start();
        } catch (IOException ex) {
            Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        relay.start();
            numRunsLeft--;
    }
    public InputRelayer getRelay(){
        return relay;
    }
}
