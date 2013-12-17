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
    private String code;
    public JavaFile(String location,boolean containsMain,String packageF){
        super(location);
        setMainMethod(containsMain);
        packageFolder=packageF;
    }
    public JavaFile(File f){
        super(f.getPath());
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
            ex.printStackTrace();
            return ex+"";
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
"            java.io.File f=new java.io.File(\"runtimeFiles\\\\input\"+x+\".log\");\n" +
"            while(f.exists()){\n" +
"                f=new java.io.File(\"runtimeFiles\\\\input\"+x+\".log\");\n" +
"                x++;\n" +
"            }\n" +
"            x-=2;\n" +
"            f=new java.io.File(\"runtimeFiles\\\\input\"+x+\".log\");\n" +
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
"        printDropbox=new java.io.PrintStream(new java.io.FileOutputStream(\"runtimeFiles\\\\output\"+x+\".log\"));\n" +
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
                            currentFile+=inject;
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
            boolean removePackage=false;
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
                    //check if it is in the right folder, then move it.
                    File parent=this.getParentFile();
                    if(parent!=null&&parent.isDirectory()){
                        String name=parent.getName();
                        String[] folders=packageDir.split(".");
                        if(folders.length<=0){
                            folders=new String[1];
                            folders[0]=packageDir;
                        }
                        if(!name.equals(folders[0])){
                            removePackage=true;
                        }
                       
                    }
                    //read="//"+line+" //This was commented out by DropboxGrader in order to run the code.";
                    //System.out.println("Line "+line+" contained a package declaration.");
                }
                if(line.contains("public")&&line.contains("static")&&line.contains("void")&&line.replace(" ", "").contains("main(String[]")){ //if it contains a main method
                    if(!mainMethod){
                        reader.close();
                        setMainMethod(true);
                        validateFile(f);
                        return;
                    }
                }
            }
            reader.close();
            if(removePackage){
                removePackage(code);
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

    private void removePackage(String code) {
        packageFolder=null;
        String newCode="";
        String[] lines=code.split("\n");
        for(int x=0;x<lines.length;x++){
            if(!lines[x].contains("package")){
                newCode+=lines[x]+"\n";
            }
        }
        changeCode(newCode);
    }
}
