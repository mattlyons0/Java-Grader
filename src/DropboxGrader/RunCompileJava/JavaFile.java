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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;

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
    private String[] otherClassPackages;
    private HashSet<String> classDependencies;
    private boolean moved;
    
    public JavaFile(File f,DbxFile dbx){
        super(f.getPath());
        dbxFile=dbx;
        classDependencies=new HashSet();
        
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
                moved=false;
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
                                    moved=true;
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
                                    File[] filesInFolder=currentFolder.listFiles();
                                    if(filesInFolder!=null){
                                        for(File files:filesInFolder){
                                            File newFile=new File(newFolder.getPath()+files.getPath().replace(parent.getPath(), ""));
                                            if(!newFile.getPath().equals(newFolder.getPath())){
                                                System.out.println("Renamed "+files.getPath()+" to "+newFile.getPath());
                                                files.renameTo(newFile);
                                            }
                                        }
                                        moved=true;       
                                    }
                                }
                                parent=parent.getParentFile();
                            }
                        }
                    }
                if(moved){
                    dbxFile.movedFiles();
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public String getCode(){
        return code;
    }
    public void setOtherClasses(JavaFile[] javaFiles){
        otherClassNames=new String[javaFiles.length];
        otherClassPackages=new String[javaFiles.length];
        for(int i=0;i<javaFiles.length;i++){
            JavaFile jf=javaFiles[i];
            String name=jf.getName();
            name=name.substring(0,name.length()-5); //get rid of .java
            otherClassNames[i]=name;
            
            otherClassPackages[i]=jf.packageFolder();
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
            if(line.contains("import ")&&line.contains(".*")&&line.contains(";")){ //if they are importing something.*
                int importIndex=line.indexOf("import ");
                int importIndexEnd=line.indexOf(".*");
                String sub=line.substring(importIndex+"import ".length(),importIndexEnd);
                for(String packag:otherClassPackages){ //if they are * importing something they wrote we gotta compile every class
                    if(packag!=null&&packag.contains(sub)){
                        classDependencies.addAll(Arrays.asList(otherClassNames));
                        return;
                    }
                }
            }
            if(!otherClass.equals(thisClassName)&&(containsClass(otherClass,line))){
                classDependencies.add(otherClass);
            }
        }
    }
    private boolean containsClass(String other,String line){
        if(line.contains(" "+other+" ")||line.contains(" "+other+"(")||
                line.contains(" "+other+".")||line.contains("."+other+" ")||
                line.contains("."+other+";")||line.contains("."+other+".")||
                line.contains("("+other+" ")||line.contains("<"+other+">")||
                line.contains("("+other+".")||line.contains("("+other+"(")||
                line.contains("."+other+"(")||line.contains("."+other+")")||
                line.contains(" "+other+"{")||line.contains(" "+other+",")||
                line.contains(","+other+"{")||line.contains(","+other+",")){
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
    public boolean moved(){
        return moved;
    }
    public DbxFile getDbx(){
        return dbxFile;
    }
}
