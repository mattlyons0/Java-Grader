/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.RunCompileJava;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.Grader.JTerminal;
import DropboxGrader.UnitTesting.UnitTester;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    public final static boolean onWindows=System.getProperty("os.name").contains("Windows");
    
    public JavaRunner(final JTerminal t,Gui gui){
        terminal=t;
        this.gui=gui;
        this.relay=new InputRelayer(t);
        
        errorRelay=new RelayStream(System.err,new Evaluable() {
            @Override
            public void evaluate(String line) {
                t.append(line,Color.BLACK);
            }
        },
        new Evaluable() {
            @Override
            public void evaluate(String line) {
                t.append(line,Color.RED);
            }
        });
        thread=new Thread(this);
        thread.setName("CheckProccessStateThread");
        thread.start();
    }
    @Override
    public void run(){
        while(true){
            //System.out.flush(); //doesnt update state of running unless flush is called, oddly enough. Only at school too.
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
                        runFile(currentFiles,mainFile,numRunsLeft,false);
                    }
                    else{
                        running=null;
                        gui.proccessEnded();
                        currentFiles=null;
                        mainFile=null;
                    }
                    
                }
                catch(IllegalThreadStateException e){
                    e.printStackTrace();
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
    //this has even more copied code from runTest() I really need to merge these...
    public String[] runJUnit(File unitTest,DbxFile testedFile){
        if(!new File(Config.jUnitHamcrestJarLocation).exists()||!new File(Config.jUnitJarLocation).exists()){
            System.err.println("Error when JUnit testing. Hamcrest or JUnit Jar is missing.");
            return new String[]{null,"The Hamcrest or JUnit Jar is missing, Configure their paths in the settings menu."};
        }
        char sym=onWindows?';':':'; //unix : windows ;
        String unitTestName=unitTest.getName().substring(0,unitTest.getName().length()-5); //remove .java
        String[] args={"java","-cp",new File(Config.jUnitJarLocation).getAbsolutePath()+sym+
                new File(Config.jUnitHamcrestJarLocation).getAbsolutePath()+sym+
                testedFile.getFile().getAbsolutePath()+sym+unitTest.getParentFile().getAbsolutePath(),
                "org.junit.runner.JUnitCore",unitTestName};
        
        JavaFile[] files=testedFile.getJavaFiles();
        boolean containsPackages=false;
        for(JavaFile f: files){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        int manualArgNum=5;
        String[] filePaths=new String[files.length+manualArgNum];

        filePaths[0]="-cp";
        String path=files[0].getAbsolutePath();
        if(path.length()!=0){
            if(onWindows)
                path=path.replace("\\", "="); //windows uses stupid slashes when everything else doesnt
            else
                path=path.replace("/","=");
            String[] pathPart=path.split("="); //cant split \ for whatever reason (regex strikes again!)..
            path=path.replace("=", "/");
            path=path.substring(0, path.length()-pathPart[pathPart.length-1].length());
            if(containsPackages){
                if(files[0].hasPackage()){
                    
                    String firstPackage;
                    if(files[0].packageFolder().contains("/")){
                        firstPackage=files[0].packageFolder().substring(0,files[0].packageFolder().indexOf("/"));
                    }
                    else{
                        firstPackage=files[0].packageFolder();
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
                        System.err.println("Error determining package to compile for "+path+" with package "+files[0].packageFolder()
                            +"\nFile: "+files[0].getPath());
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
        filePaths[1]=args[2]; //careful if removed, referenced in the run loop.
        filePaths[2]="-sourcepath";
        filePaths[3]=filePaths[1];
        filePaths[4]=unitTest.getAbsolutePath();
        for(int i=manualArgNum;i<filePaths.length;i++){
            filePaths[i]=files[i-manualArgNum].getAbsolutePath();
        }
        //System.out.println("Compiling "+Arrays.toString(filePaths));
        try {
            //System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_25\\jre");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler==null){
                if(!fixedPath){
                    fixJavaPath();
                    compiler=ToolProvider.getSystemJavaCompiler();
                }
            }
            String javaVersion=Runtime.class.getPackage().getImplementationVersion();
            StringStream compileStream=new StringStream(System.err);
            int result=compiler.run(System.in, System.out, compileStream, filePaths); //if the compiler couldnt be found it will crash here. NPE
            compileStream.close();
            if(result!=0&&!compileStream.getOutput().equals("")){
                return new String[]{null,compileStream.getOutput()};
            }
        } catch(Exception e){
            System.err.println("Error logged when compiling "+e);
            e.printStackTrace();
        }
        try{
            System.out.println("Running with commands: "+Arrays.toString(args));
            ProcessBuilder builder=new ProcessBuilder(args);
            Process testProc=builder.start();
            StringRelayer relayer=new StringRelayer(testProc.getInputStream(),testProc.getErrorStream());
            testProc.waitFor();
            relayer.stop();
            relayer.getProc().join();
            return new String[]{relayer.getOutput(),relayer.getError()};
        } catch (IOException|InterruptedException ex) {
           Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
           return new String[]{null,"Error running JUnit Tests.\n "+ex};
        }
    }
    //this has a lot of code copied from runFile() In the future merge them...
    //returns String{Output,Error}
    public String[] runTest(JavaFile[] files,JavaFile testFile,UnitTester tester){
        boolean containsPackages=false;
        for(JavaFile f: files){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        int manualArgNum=4;
        ArrayList<JavaFile> dependentFiles=calcDependencies(testFile,Arrays.copyOf(files, files.length));
        dependentFiles.add(testFile);
        String[] filePaths=new String[dependentFiles.size()+manualArgNum];

        filePaths[0]="-cp";
        String path=testFile.getAbsolutePath();
        if(path.length()!=0){
            if(onWindows)
                path=path.replace("\\", "="); //windows uses stupid slashes when everything else doesnt
            else
                path=path.replace("/","=");
            String[] pathPart=path.split("="); //cant split \ for whatever reason (regex strikes again!)..
            path=path.replace("=", "/");
            path=path.substring(0, path.length()-pathPart[pathPart.length-1].length());
            if(containsPackages){
                if(testFile.hasPackage()){
                    
                    String firstPackage;
                    if(testFile.packageFolder().contains("/")){
                        firstPackage=testFile.packageFolder().substring(0,testFile.packageFolder().indexOf("/"));
                    }
                    else{
                        firstPackage=testFile.packageFolder();
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
                        System.err.println("Error determining package to compile for "+path+" with package "+testFile.packageFolder()
                            +"\nFile: "+testFile.getPath());
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
        //System.out.println("Compiling "+Arrays.toString(filePaths));
        try {
            //System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_25\\jre");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler==null){
                if(!fixedPath){
                    fixJavaPath();
                    compiler=ToolProvider.getSystemJavaCompiler();
                }
            }
            String javaVersion=Runtime.class.getPackage().getImplementationVersion();
            StringStream compileStream=new StringStream(System.err);
            int result=compiler.run(System.in, System.out, compileStream, filePaths); //if the compiler couldnt be found it will crash here. NPE
            if(result!=0&&!compileStream.getOutput().equals("")){
                return new String[]{null,compileStream.getOutput()};
            }
            tester.compileFinished();
        } catch(Exception e){
            System.err.println("Error logged when compiling "+e);
            e.printStackTrace();
        }
        try{
            //for(int x=0;x<files.length;x++){
            String classpath=filePaths[1].substring(1, filePaths[1].length()-1); //removes quotes in filepaths[1]
            String className="";
            if(testFile.hasPackage()){
                className+=testFile.packageFolder()+"/";
            }
            className+=testFile.getName();
            className=className.substring(0,className.length()-5); //removes .java
            String javaExe=System.getProperty("java.home")+"/bin/java.exe";
            //System.out.println(javaExe);
            javaExe="java"; //since we set the java.home the keyword java will go to the right place
            //String directory=folder;
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            File runningFrom=runFrom(testFile);
            builder.directory(runningFrom); //do something like this but safer to set proper working directory
            //todo: verify this works with packages
            //builder.inheritIO();
            //System.out.println("Running from: "+runningFrom);
            Process testProc=builder.start();
            StringRelayer relayer=new StringRelayer(testProc.getInputStream(),testProc.getErrorStream());
            testProc.waitFor();
            relayer.stop();
            relayer.getProc().join();
            return new String[]{relayer.getOutput(),relayer.getError()};
            //}
        } catch (IOException|InterruptedException ex) {
           Logger.getLogger(JavaRunner.class.getName()).log(Level.SEVERE, null, ex);
           return new String[]{null,"Error running Simple Unit Tests.\n"+ex};
        }
    }
    public boolean runFile(JavaFile[] files,JavaFile runChoice,int numTimes,String folder){
        return runFile(files,runChoice,numTimes,true);
    }
    private boolean runFile(JavaFile[] files,JavaFile runChoice, int numTimes,boolean compile){
        if(files.length==0){
            return false;
        }
        if(compile)
            terminal.setText("");
        
        numRunsLeft=numTimes;
        currentFiles=files;
        mainFile=runChoice;
        
        stopProcess(true);
        boolean containsPackages=false;
        for(JavaFile f: files){
            if(f.hasPackage()){
                containsPackages=true;
                break;
            }
        }
        
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
                    
                    String firstPackage;
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
                e.printStackTrace();
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
            //System.out.println(javaExe);
            javaExe="java"; //since we set the java.home the keyword java will go to the right place
            //String directory=folder;
            //directory=directory.substring(0, directory.length()-runChoice.getName().length());
            ProcessBuilder builder=new ProcessBuilder(javaExe,"-cp",classpath,className);
            File runningFrom=runFrom(runChoice);
            builder.directory(runningFrom); //do something like this but safer to set proper working directory
            //todo: verify this works with packages
            //builder.inheritIO();
            System.out.println("Running from: "+runningFrom);
            if(compile)
                terminal.append("Run Started: \n\n",Color.GRAY);
            running=builder.start();
            relay.changeReadProccess(running.getInputStream(), running.getErrorStream());
            terminal.setInputStream(running.getOutputStream());
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
    private File runFrom(JavaFile mainFile){
        String parent=mainFile.getDbx().getFileName();
        parent=parent.substring(0,parent.length()-4); //get rid of .zip
        String[] folders;
        if(onWindows)
            folders=mainFile.getAbsolutePath().split(Pattern.quote("\\"));
        else
            folders=mainFile.getAbsolutePath().split(Pattern.quote("/"));
        int parentIndex=-1;
        for(int i=0;i<folders.length;i++){
            if(parentIndex==-1){
                if(folders[i].equals(parent)){
                    parentIndex=i;
                }
            }
            else{ //we have found the parent
                File file=makeFile(folders,0,i);
                File[] files=file.listFiles();
                if(files.length>1){
                    return file.getAbsoluteFile();
                }
            }
        }
        return new File("/downloads/"+parent+"/").getAbsoluteFile();
    }
    /**
     * Makes a file from an array of folders
     */
    private File makeFile(String[] folders,int offset,int length){
        String path="";
        for(int i=offset;i<length;i++){
            path+=folders[i]+"/";
        }
        return new File(path);
    }
    public boolean isRunning(){
        if(numRunsLeft>0||running!=null){
            return true;
        }
        return false;
    }
}
