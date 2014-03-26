/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import DropboxGrader.DbxFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * I hate this class
 * @author Matt
 */
public class JavaFile extends File{
    private DbxFile dbxFile;
    private boolean mainMethod;
    private String packageFolder;
    private String code;
    private String[] otherClassNames;
    private ArrayList<String> classDependencies;
    
    public JavaFile(File f,DbxFile dbx){
        super(f.getPath());
        dbxFile=dbx;
        classDependencies=new ArrayList();
        
        validateFile(f);
        
    }
    public String changeCode(String newCode){
        mainMethod=false;
        try {
            PrintWriter writer=new PrintWriter(new FileWriter(this));
            writer.write(newCode);
            writer.close();
            validateFile(this);
        } catch (IOException ex) {
            if(exists()){ //we check if the file still exists to see if we moved it or not
                ex.printStackTrace();
                return ex+"";
            }
        }
        return "";
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
            int braces=0;
            boolean didFirstInject=false;
            boolean didLastInject=false;
            try {
                //inject code
                String inject="	//DROPBOXGRADERCODESTART\n" +
"        java.io.FileInputStream iDropbox=null;\n" +
"        java.io.PrintStream printDropbox=null;\n" +
"        try{	\n" +
"            int x=0;\n" +
"            java.io.File f=new java.io.File(\"runtimeFiles/input\"+x+\".log\");\n" +
"            while(f.exists()){\n" +
"                f=new java.io.File(\"runtimeFiles/input\"+x+\".log\");\n" +
"                x++;\n" +
"            }\n" +
"            x-=2;\n" +
"            f=new java.io.File(\"runtimeFiles/input\"+x+\".log\");\n" +
"            iDropbox=new java.io.FileInputStream(f){\n" +
"            //int runNum=0; //requires everything to be written twice, for stupid reasons.\n" +
"                @Override\n" +
"                public int read(byte[] b, int off, int len) throws java.io.IOException {\n" +
"                    int read=super.read(b, off, len);\n" +
"                    while(read==-1){ //every 2nd call is for caching and doesnt matter\n" +
"                        read=super.read(b, off, len);\n" +
"                    }\n" +
"                    return read;\n" +
"                }\n" +
"            };\n" +
"        System.setIn(iDropbox);\n" +
"        printDropbox=new java.io.PrintStream(new java.io.FileOutputStream(\"runtimeFiles/output\"+x+\".log\"));\n" +
"        System.setOut(printDropbox);\n" +
"        System.setErr(printDropbox);\n" +
"        } catch(java.io.IOException e){\n" +
"            System.out.println(\"Injection code has failed. \"+e);\n" +
"        }\n" +
"        //DROPBOXGRADERCODEEND\n";
                String inject2="";
                Scanner s=new Scanner(this);
                s.useDelimiter("\n");
                String currentFile="";
                while(s.hasNext()){
                    String line=s.next()+"\n";
                    if(line.contains("{")||line.contains("}")){
                        char[] chars=line.toCharArray();
                        for(char c:chars){
                            if(c=='{'){
                                braces++;
                            }
                            else if(c=='}'){
                                braces--;
                            }
                        }
                    }
                    if(line.contains("public")&&line.contains("static")&&line.contains("void")&&line.replace(" ", "").contains("main(String[]")){
                        if(s.hasNext()){
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
                            //System.out.println("Injecting after "+line.substring(0, index));
                            //currentFile+=inject; //we no longer need to inject code since we use pipes now.
                            currentFile+=line.substring(index);
                            currentFile+=lineAfter+"\n";
                            didFirstInject=true;
                        }
                    }
                    else if(braces==0&&didFirstInject&&!didLastInject){
                        currentFile+=inject2;
                        currentFile+=line;
                        didLastInject=true;
                    }
                    else{
                        currentFile+=line;
                    }
                }
                s.close();
                
                writer = new PrintWriter(new FileWriter(this,false));
                writer.write(currentFile);
                writer.close();
                
            } catch (IOException ex) {
                Logger.getLogger(JavaFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private void validateFile(File f){
        try {
            code="";
            Scanner reader=new Scanner(f);
            reader.useDelimiter("\n");
            while(reader.hasNext()){
                String packageDir=null;
                String line=reader.next();
                code+=line+"\n";
                if(line.contains("package ")){ //if it contains packages we need to make sure its in the right folder
                    packageDir=line.substring(0,line.length()-1); //-1 to get rid of \n
                    packageDir=packageDir.replace("package ", "");
                    packageDir=packageDir.replace(" ", "");
                    packageDir=packageDir.replace(";", "");
                    packageDir=packageDir.replace(".", "/");
                    setPackage(packageDir);
                    //read="//"+line+" //This was commented out by DropboxGrader in order to run the code.";
                    //System.out.println("Line "+line+" contained a package declaration.");
                }
                
                if(line.contains("public")&&line.contains("static")&&line.contains("void")&&line.replace(" ", "").contains("main(String")&&line.replace(" ", "").contains("[]")){ //if it contains a main method
                    if(!mainMethod){
                        reader.close();
                        setMainMethod(true);
                        validateFile(f);
                        return;
                    }
                }
            }
            reader.close();
            //check if initial package is wrong and attempt to fix that
            if(!dbxFile.changedFolder()){
                boolean movedFiles=false;
                File parent=getParentFile();
                if(packageFolder!=null&&parent!=null&&parent.isDirectory()){
                        //get the package folders
                        String[] folders=packageFolder.split("/");
                        if(folders.length<=0){
                            folders=new String[1];
                            folders[0]=packageFolder;
                        }
                        for(int i=folders.length-1;i>=0;i--){
                            if(parent!=null){
                                if(parent.getName().equals(folders[i])){
                                    //cool this far, keep going
                                }
                                else if(parent.getParentFile()==null||
                                        !parent.getParentFile().getName().equals("downloads")){ //upper folder isn't the right name and it is not the zip folder
                                    System.out.println("Renamed "+parent.getPath()+" to "+parent.getPath().substring(0,parent.getPath().length()-parent.getName().length())+folders[i]);
                                    File dest=new File(parent.getPath().substring(0,parent.getPath().length()-parent.getName().length())+folders[i]);
                                    parent.renameTo(dest);
                                    movedFiles=true;
                                }
                                else{ //upper folder isnt the right name and parent is the zip folder
                                    File currentFolder;
                                    File newFolder;
                                    if(JavaRunner.onWindows){
                                        currentFolder=new File(getPath().substring(0,getPath().lastIndexOf("\\")));
                                        newFolder=new File(currentFolder.getPath()+"\\"+folders[i]);
                                    }
                                    else{
                                        currentFolder=new File(getPath().substring(0,getPath().lastIndexOf("/")));
                                        newFolder=new File(currentFolder.getPath()+"/"+folders[i]);
                                    }
                                    newFolder.mkdir();
                                    System.out.println("Made new folder "+newFolder.getPath());
                                    if(currentFolder.listFiles()!=null){
                                        for(File files:currentFolder.listFiles()){
                                            File newFile=new File(newFolder.getPath()+files.getPath().replace(parent.getPath(), ""));
                                            if(!newFile.equals(currentFolder)){
                                                System.out.println("Renamed "+files.getPath()+" to "+newFile.getPath());
                                                files.renameTo(newFile);
                                            }
                                        }
                                        movedFiles=true;       
                                    }
                                }
                                parent=parent.getParentFile();
                            }
                        }
                    }
                if(movedFiles){
                    dbxFile.movedFiles();
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public String getCode(){
        String c="";
        boolean inInject=false;
        for(String line:code.split("\n")){
            if(line.contains("//DROPBOXGRADERCODESTART")){
                inInject=true;
            }
            else if(inInject){
                if(line.contains("//DROPBOXGRADERCODEEND")){
                    inInject=false;
                }
            }
            else{
                c+=line+"\n";
            }
        }
        return c;
    }
    private String readCode(){
        try {
            Scanner reader=new Scanner(this);
            reader.useDelimiter("\n");
            boolean inDropboxInjected=false;
            String read="";
            while(reader.hasNext()){
                String line=reader.next();
                if(!inDropboxInjected&&line.contains("//DROPBOXGRADERCODESTART")){
                    inDropboxInjected=true;
                }
                else if(inDropboxInjected&&line.contains("//DROPBOXGRADERCODEEND")){
                    inDropboxInjected=false;
                }
                else if(!inDropboxInjected){
                    read+=line+"\n";
                }
            }
            reader.close();
//            if(packageDir!=null){
//                System.out.println("Package at "+packageDir);
//                if(packageFolder==null){
//                    packageFolder=packageDir;
//                }
//                else{
//                    //determine which is higher level
//                    if(!packageFolder.equals(packageDir)){
//                        String path=f.getPath();
//                        path=path.replace("\\", "="); //cant split a \ for whatever reason
//                        String[] pathFolders= path.split("=");
//                        for(int x=0;x<pathFolders.length;x++){
//                            pathFolders[x]=pathFolders[x].replace("=", "");
//
//                            if(pathFolders[x].equals(packageFolder)){
//                                return read;
//                            }
//                            if(pathFolders[x].equals(packageDir)){
//                                packageFolder=packageDir;
//                                return read;
//                            }
//                        }
//                    }
//                    else{
//                        return read;
//                    }
//                }
//                if(!f.getParent().endsWith(packageDir)){
//                    //need to move file into directory with packageName
//                    File f2=new File(f.getParent()+"\\"+packageDir);
//                    f2.mkdir();
//                    
//                    File movedF=new File(f2.getPath()+f.getName());
//                    BufferedWriter writer=new BufferedWriter(new FileWriter(f));
//                    writer.write(read);
//                    writer.close();
//                    f.delete();
//                }
//            }
            
            return read;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public void setOtherClasses(JavaFile[] javaFiles){
        otherClassNames=new String[javaFiles.length];
        for(int i=0;i<javaFiles.length;i++){
            JavaFile jf=javaFiles[i];
            String name=jf.getName();
            name=name.substring(0,name.length()-5); //get rid of .java
            otherClassNames[i]=name;
        }
        calculateDependencies();
    }
    private void calculateDependencies(){
        if(otherClassNames==null||otherClassNames.length==0){
            System.err.println("Error calculating dependencies of "+getName()+". The other classes were not provided.");
            return;
        }
        classDependencies.clear();

        String[] lines=code.split("\n");
        boolean inComment=false; //for multiline comments
        for(String line:lines){
            if(!inComment&&!line.contains("//")&&!line.contains("/*")){
                addDependencies(line);
            }
            if(line.contains("/*")&&!line.contains("//")&&!inComment){
                String subLine=line.substring(0,line.indexOf("/*"));
                addDependencies(subLine);
                inComment=true;
            }
            else if(line.contains("//")&&!inComment&&!line.contains("*/")){
                String subLine=line.substring(0,line.indexOf("//"));
                addDependencies(subLine);
            }
            else if(line.contains("//")&&line.contains("/*")&&!inComment){
                String subLine=line.substring(0,line.indexOf("//"));
                if(subLine.contains("/*")&&!subLine.contains("*/")){ //if the /* is before the // ignore the //
                    addDependencies(subLine);
                    inComment=true;
                }
                else if(subLine.contains("/*")&&subLine.contains("*/")){
                    String subLine1=subLine.substring(0,subLine.indexOf("/*"));
                    String subLine2=subLine.substring(subLine.indexOf("*/"));
                    addDependencies(subLine1);
                    addDependencies(subLine2);
                }
                else if(subLine.contains("/*")&&line.contains("*/")){ //if there is a */ later in the line, after the //
                    String subLine1=line.substring(line.indexOf("*/"));
                    addDependencies(subLine1);
                    inComment=false;
                }
                else if(!subLine.contains("/*")){ //if // comes before /*
                    addDependencies(subLine);
                }
                else{
                    System.err.println("Error, there is something with the comments that is not accounted for in line: "+line);
                }
            }
            else if(line.contains("*/")&&line.contains("//")){
                String subLine=line.substring(0,line.indexOf("//"));
                if(inComment){
                    if(subLine.contains("*/")){
                        String subLine1=subLine.substring(subLine.indexOf("*/"));
                        addDependencies(subLine1);
                        inComment=false;
                    }
                }
                else{ //not in a comment
                    addDependencies(subLine);                    
                }
            }
            //not else if since it would not catch /* and */ in the same line without a //
            if(line.contains("*/")&&inComment&&!line.contains("//")){
                String subLine=line.substring(line.indexOf("*/"));
                addDependencies(subLine);
                inComment=false;
            }
        }
    }
    /**
     * Adds dependencies to classDependencies from a string, ignoring comments
     * @param line string to evaluate
     * Adds to classDependencies
     */
    private void addDependencies(String line){
        if(line.length()==0){
            return;
        }
        String thisClassName=getName().substring(0,getName().length()-5); //remove .java
        for(String otherClass:otherClassNames){
            line=line.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " "); //so we can check for spaces
            if(!otherClass.equals(thisClassName)&&(containsClass(otherClass,line))){
                if(!classDependencies.contains(otherClass)){
                    classDependencies.add(otherClass);
                }
            }
        }
    }
    private boolean containsClass(String other,String current){
        if(current.contains(" "+other+" ")||current.contains(" "+other+"(")||
                current.contains(" "+other+".")||current.contains("."+other+" ")||
                current.contains("."+other+";")||current.contains("."+other+".")||
                current.contains("("+other+" ")||current.contains("<"+other+">")||
                current.contains("("+other+".")||current.contains("("+other+"(")||
                current.contains("."+other+"(")||current.contains("."+other+")")||
                current.contains(" "+other+"{")||current.contains(" "+other+",")||
                current.contains(","+other+"{")||current.contains(","+other+",")){
            return true;
        }
        return false;
    }
    public String[] getDependencies(){
        String[] arr=new String[0];
        return classDependencies.toArray(arr);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==this){
            return true;
        }
        if(obj instanceof JavaFile){
            JavaFile jf=(JavaFile)obj;
            if(jf.getPath().equals(getPath())){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.dbxFile);
        return hash;
    }
    @Override
    public String toString(){
        return getName();
    }
}
