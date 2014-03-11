/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.Color;
import java.awt.Toolkit;
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
    private boolean fixedPath=false;
    private JavaCodeBrowser browser;
    private String folder;
    public JavaRunner(JTerminal t,Gui gui,JavaCodeBrowser browser){
        terminal=t;
        this.gui=gui;
        this.relay=new InputRelayer(t);
        this.browser=browser;
        new File("runtimeFiles\\").mkdir();
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
                    
                    try {
                        //otherwise output goes after run finished.
                        Thread.sleep(1000); //otherwise output goes after run finished.
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if(code!=0||numRunsLeft==0){
                        
                        terminal.append("\nRun Finished: "+code+"\n",Color.GRAY);
                    }
                    if(numRunsLeft>0){
                        System.out.println("running new file.");
                        runFile(currentFiles,mainFile,numRunsLeft,folder,false);
                    }
                    else{
                        running=null;
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
                    Thread.sleep(100); //basically if the number is smaller than 1000 multiple writes happen before a read
                    //figure out why later...
                    //now is later, this is causing issues, why...
                }
                else{
                    Thread.sleep(100);
                }
                //saves resources on slow computers (like the ones in the library)
                //also fixes printing output after saying run finished on slow computers.
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    public void stopProcess(){
        stopProcess(false);
    }
    private void stopProcess(boolean normalExit){
        if(running!=null){
            running.destroy();
            if(!normalExit){
                terminal.append("Run Canceled\n", Color.GRAY);
                numRunsLeft=0;
            }
        }
        running=null;     
    }
    public boolean runFile(JavaFile[] files,JavaFile runChoice,int numTimes,String folder){
        return runFile(files,runChoice,numTimes,folder,true);
    }
    private boolean runFile(JavaFile[] files,JavaFile runChoice, int numTimes,String folder,boolean compile){
        if(files.length==0){
            return false;
        }
        if(compile)
            terminal.setText("");
        String errorSaving=browser.saveFile();
        if(errorSaving!=null&&!errorSaving.trim().equals(""))
            terminal.append("Error saving file: "+errorSaving,Color.RED);
        
        numRunsLeft=numTimes;
        currentFiles=files;
        mainFile=runChoice;
        this.folder=folder;
        
        stopProcess(true);
        boolean containsPackages=false;
        for(JavaFile f: files){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        File[] filess=new File("runtimeFiles\\").listFiles();
        int highest=filess.length/2;
        terminal.setInputFile(new File("runtimeFiles\\input"+highest+".log"));
        relay.changeReadFile(new File("runtimeFiles\\output"+highest+".log"));
        
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
        if(compile){
            //System.out.println("Compiling "+Arrays.toString(filePaths));
            try {
                //System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_25\\jre");
                    terminal.append("Compile Started\n",Color.GRAY);
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                if(compiler==null){
                    if(!fixedPath){
                        fixJavaPath();
                        compiler=ToolProvider.getSystemJavaCompiler();
                    }
                }
                terminal.append("The java.home path is: "+System.getProperty("java.home")+"\n",Color.GRAY);
                int result=compiler.run(null, System.out, errorRelay, filePaths); //if the compiler couldnt be found it will crash here. NPE
                if(result!=0){
                    terminal.append("Compile Failed\n\n",Color.RED);
                    gui.proccessEnded();
                    return false;
                }
                else{
                    terminal.append("Compile Finished\n\n",Color.GRAY);
                }
                
            } catch(Exception e){
                System.err.println("Error logged when compiling "+e);
            }
        }
        try{
            int index=-1;
            for(int x=0;x<files.length;x++){
                if(files[x].equals(runChoice)){
                    index=x;
                }
            }
            if(index==-1){
                System.out.println("Main File doesnt exist!");
                return false;
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
            System.out.println(javaExe);
            javaExe="java";
            String directory=folder;
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            builder.inheritIO();
            //builder.directory(new File(directory)); //if this is uncommented it wont generate the input/output files in the right place.
            System.out.println("Running from: "+directory);
            if(compile)
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
              return true;
            //}
        } catch (IOException ex) {
           Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
           return false;
        }
    }
    private void fixJavaPath(){
        String path="";
//        String path=System.getProperty("java.home");
//        if(!path.endsWith("jre")){
//            String version=System.getProperty("java.version");
//            path="C:\\Program Files\\Java\\jdk"+version+"\\jre";
//        }
//        System.setProperty("java.home", path);
//        if(ToolProvider.getSystemJavaCompiler()==null){
//            String version=System.getProperty("java.version");
//            path="C:\\Program Files (x86)\\Java\\jdk"+version+"\\jre";
//            System.setProperty("java.home", path);
//        }
        if(ToolProvider.getSystemJavaCompiler()==null){
            File dir=new File("C:\\Program Files\\Java\\");
            File[] files=dir.listFiles();
            for(File f:files){
                if(f.getName().contains("jdk")){
                    path=f.getAbsolutePath();
                    System.setProperty("java.home", path);
                    if(ToolProvider.getSystemJavaCompiler()!=null){
                        fixedPath=true;
                        return;
                    }
                }
            }
            dir=new File("C:\\Program Files (x86)\\Java\\");
            files=dir.listFiles();
            for(File f:files){
                if(f.getName().contains("jdk")){
                    path=f.getAbsolutePath();
                    System.setProperty("java.home", path);
                    if(ToolProvider.getSystemJavaCompiler()!=null){
                        fixedPath=true;
                        return;
                    }
                }
            }
        }
        if(System.getProperty("os.name").contains("Windows"))
            terminal.append("No JDK found, please install any version of the java JDK.",Color.RED);
        else
            terminal.append("This program must be run using a JDK in order to compile code.\n"
                    + "Set the JAVA_HOME variable to the path of a JDK and restart this program to compile code.");
        fixedPath=true;
    }
    public InputRelayer getRelay(){
        return relay;
    }
}
