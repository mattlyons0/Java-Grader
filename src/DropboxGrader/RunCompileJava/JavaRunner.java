/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.RunCompileJava;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.Grader.JTerminal;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.RunCompileJava.InputRelayer;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private String folder;
    public final static boolean onWindows=System.getProperty("os.name").contains("Windows");
    
    public JavaRunner(JTerminal t,Gui gui){
        terminal=t;
        this.gui=gui;
        this.relay=new InputRelayer(t);
        
        new File("runtimeFiles/").mkdir();
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
                    
                    if(code!=0){ //abnormal exit
                        terminal.append("\nRun Stopped: "+code+"\n",Color.RED);
                    }
                    else if(numRunsLeft==0){ //normal exit
                        terminal.append("\nRun Finished\n",Color.GRAY);
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
        File[] filess=new File("runtimeFiles/").listFiles();
        int highest=filess.length/2;
        terminal.setInputFile(new File("runtimeFiles/input"+highest+".log"));
        relay.changeReadFile(new File("runtimeFiles/output"+highest+".log"));
        
        int manualArgNum=4;
        ArrayList<JavaFile> dependentFiles=calcDependencies(runChoice,Arrays.copyOf(files, files.length));
        dependentFiles.add(mainFile);
        String[] filePaths=new String[dependentFiles.size()+manualArgNum];

        filePaths[0]="-cp";
        String path=runChoice.getAbsolutePath();
        if(path.length()!=0){
            if(onWindows)
                path=path.replace("\\", "="); //windows uses stupid slashes when everything else doesnt
            else
                path=path.replace("/","=");
            String[] pathPart=path.split("="); //cant split \ for whatever reason (regex strikes again!)..
            path=path.replace("=", "/");
            path=path.substring(0, path.length()-pathPart[pathPart.length-1].length());
            if(containsPackages){
                if(runChoice.hasPackage()){
                    
                    String firstPackage="";
                    if(runChoice.packageFolder().contains("/")){
                        firstPackage=runChoice.packageFolder().substring(0,runChoice.packageFolder().indexOf("/"));
                    }
                    else{
                        firstPackage=runChoice.packageFolder();
                    }
                    int partIndex=-1;
                    for(int i=0;i<pathPart.length;i++){
                        if(pathPart[i].equals(firstPackage)){
                            partIndex=i;
                            //don't break because we want the most recent one that equals it
                            //i could go backwards and then break but whatever
                        }
                    }
                    if(partIndex==-1){
                        System.err.println("Error determining package to compile for "+path+" with package "+runChoice.packageFolder()
                            +"\nFile: "+runChoice.getPath());
                        terminal.append("Error determining package structure.\n"+
                                "This file does not have the same folder structure as it does package structure.\n\n", Color.red);
                    }
                    path="";
                    for(int i=0;i<partIndex;i++){
                        path+=pathPart[i];
                        if(i<=partIndex){
                            path+="/";
                        }
                    }
                }
            }
            if(pathPart.length==1){
                path="";
            }
        }
        filePaths[1]="\""+path+"\""; //careful if removed, referenced in the run loop.
        filePaths[2]="-sourcepath";
        filePaths[3]=filePaths[1];
        for(int i=manualArgNum;i<filePaths.length;i++){
            filePaths[i]=dependentFiles.get(i-manualArgNum).getAbsolutePath();
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
                String javaVersion=Runtime.class.getPackage().getImplementationVersion();
                terminal.append("JDK: "+System.getProperty("java.home")+"\n",Color.GRAY);
                terminal.append("JRE: "+javaVersion+"\n",Color.GRAY);
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
            //for(int x=0;x<files.length;x++){
            String classpath=filePaths[1].substring(1, filePaths[1].length()-1); //removes quotes in filepaths[1]
            String className="";
            if(runChoice.hasPackage()){
                className+=runChoice.packageFolder()+"/";
            }
            className+=runChoice.getName();
            className=className.substring(0,className.length()-5); //removes .java
            String javaExe=System.getProperty("java.home")+"/bin/java.exe";
            System.out.println(javaExe);
            javaExe="java"; //since we set the java.home the keyword java will go to the right place
            String directory=folder;
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            //builder.directory(runChoice.getParentFile().getParentFile()); //do something like this but safer to set proper working directory
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
            File dir=new File("C:/Program Files/Java/");
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
            dir=new File("C:/Program Files (x86)/Java/");
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
        if(onWindows)
            terminal.append("No JDK found, please install any version of the java JDK.",Color.RED);
        else
            terminal.append("This program must be run using a JDK in order to compile code.\n"
                    + "Set the JAVA_HOME variable to the path of a JDK and restart this program to compile code.");
        fixedPath=true;
    }
    public InputRelayer getRelay(){
        return relay;
    }
    public boolean isRunning(){
        return running!=null;
    }
    private ArrayList<JavaFile> calcDependencies(JavaFile mainFile,JavaFile[] files){
        ArrayList<JavaFile> depFiles=new ArrayList();
        String[] fileDeps=mainFile.getDependencies();
        //ArrayList<JavaFile> allFiles=new ArrayList(Arrays.asList(files));
        //allFiles.remove(mainFile);
        for(int i=0;i<files.length;i++){
            JavaFile f=files[i];
            if(f!=null&&f.equals(mainFile)){
                files[i]=null;
            }
        }
        for(String dep:fileDeps){
            dep+=".java";
            for(JavaFile jf:files){
                if(jf!=null&&jf.getName().equals(dep)){
                    if(!depFiles.contains(jf)){
                        depFiles.add(jf);
                        depFiles.addAll(calcDependencies(jf,files));
                    }
                }
            }
        }
        
        return depFiles;
    }
}
