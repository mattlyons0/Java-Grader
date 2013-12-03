/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Matt
 */
public class DbxFile {
    private FileManager fileManager;
    private DbxEntry.File entry;
    private DbxClient client;
    private File downloadedFile;
    private JavaFile[] javaFiles;
    private final String errorMsg;
    public DbxFile(DbxEntry.File entry,FileManager fileMan,DbxClient client){
        this.entry=entry;
        fileManager=fileMan;
        this.client=client;
        
        errorMsg="File Naming Error: "+entry.name;
        
        checkExists();
    }
    private void checkExists(){ //ties reference if it is already downloaded
        String zipPath=fileManager.getDownloadFolder()+"/"+entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        File file=new File(zipPath);
        if(file.exists()){ //could lead to different copy locally than remotely, but files arent supposed to be overwritten anyway.
            setFile(file);
        }
    }
    public File download(){
        String zipPath=fileManager.getDownloadFolder()+"/"+entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        File file=new File(zipPath);
        if(file.exists()){
            return file;
        }
        try {
            FileOutputStream f = new FileOutputStream(fileManager.getDownloadFolder()+"/"+entry.name);
            client.getFile(entry.path, null, f); //downloads from dropbox server
            f.close();
            Unzip.unzip(file.getPath()+".zip", fileManager.getUnzipFolder());
            new File(fileManager.getDownloadFolder()+"/"+entry.name).delete();
            setFile(file);
            return file;
        } catch (DbxException | IOException ex) {
            Logger.getLogger(DbxFile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    public String getAssignmentNumber(){
        String s=entry.name;
        
        int num=safeStringToInt(s.split("_")[2]);
        if(num==-1){
            return errorMsg;
        }
        return num+"";//assignment number is the 3rd underscore
    }
    public String getAssignmentName(int row,int col){
        String s=entry.name;
        if(!s.endsWith(".zip")){
            return "Error file doesn't end with .zip!";
        }
        String[] splits=s.split("_");
        if(splits.length<4){
            return errorMsg;
        }
        if(splits[3].length()<4){
            if(!isNotFirstYear(splits[2])&&splits.length==5){
                fileManager.getTableData().setColorAt(Color.YELLOW, new CellLocation(fileManager.getAttributes()[col],row));
                return splits[3]+" (Resubmit)";
            }
            else if(splits.length==5){
                fileManager.getTableData().setColorAt(Color.CYAN.darker(), new CellLocation(fileManager.getAttributes()[col],row));
                int year=safeStringToInt(splits[2]);
                return splits[4].substring(0, splits[4].length()-4)+" (Year "+year+")";
            }
            else{
                return errorMsg;
            }
        }
        return splits[3].substring(0, splits[3].length()-4);//assignment name is 4th underscore, .zip is the last 4 characters
    }
    private boolean isNotFirstYear(String s){
        if(s.contains("Yr")||s.contains("yr")||s.contains("YR")||s.contains("Year")||s.contains("year")||s.contains("YEAR")){
            return true;
        }
        return false;
    }
    /**
     * The submission time on the dropbox server. This is the the most recent revision date.
     * @param checkRevisions if true, will check if the file has been modified since initial submission
     * @param row set by manager
     * @param col set by manager
     * @return if checkRevisions is false will return the date of the newest version, otherwise will
     * return the date or if it has been modified both dates.
     */
    public String getSubmitDate(boolean checkRevisions,int row,int col){
        if(checkRevisions&&entry.lastModified.after(entry.clientMtime)&&row>-1&&col>-1){
            fileManager.getTableData().setColorAt(new Color(230,120,120), new CellLocation(fileManager.getAttributes()[col],row));
            return "Modified "+entry.lastModified+" Originally "+entry.clientMtime;
        }
        return entry.clientMtime.toString();
    }
    public String getFirstLastName(){
        String s=entry.name;
        return s.split("_")[1];
    }
    public String getStatus(int row,int col){
        try{
            int num=Integer.parseInt(getAssignmentNumber());
            if(fileManager.getGrader()!=null){
                String grade=fileManager.getGrader().gradeAt(getFirstLastName(), num,new JLabel());
                if(grade!=null){
                    fileManager.getTableData().setColorAt(Color.GREEN, new CellLocation(fileManager.getAttributes()[col],row));
                    return "Grade: "+grade;
                }
            }
        } catch(NumberFormatException ex){
            
        }
        if(downloadedFile==null){
            return "On Server";
        }
        if(downloadedFile.exists()){
            return "Downloaded";
        }
        download();
        return "Downloaded"; //downloaded but file doesnt exist. Usually when a local copy is invalid.
    }
    public static int safeStringToInt(String s){
        char[] chars=s.toCharArray();
        String num="";
        for(int x=0;x<s.length();x++){
            if(Character.isDigit(chars[x])){
                num+=chars[x];
            }
        }
        if(num.length()==0){
            return -1;
        }
        return Integer.parseInt(num);
    }
    private void setFile(File f){
        if(downloadedFile==null){
            downloadedFile=f;
            searchJavaFiles();
        }
    }
    private void searchJavaFiles(){
        javaFiles=searchForFiles(downloadedFile.getPath(),".java");
    }
    /**
     * Recursive file search
     * @param directory directory to search in
     * @param fileType files which end in this will be returned.
     * @return an array of files with specified ending characters.
     */
    private JavaFile[] searchForFiles(String directory,String fileType){
        //System.out.println("Searching in "+directory);
        ArrayList<File> files=new ArrayList();
        ArrayList<File> filesWithType;
        filesWithType=new ArrayList();
        File folder=new File(directory);
        files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                if(f.getName().endsWith(fileType.toLowerCase())||f.getName().endsWith(fileType.toUpperCase())){
                    //if file is .Java it wont get added, but that is stupid capitalization that nothing would save as anyway.
                    JavaFile jf=new JavaFile(f);
                    filesWithType.add(jf);
                    //System.out.println("Adding "+f);
                }
            }
            else if(f.isDirectory()){
                if(!f.getName().endsWith(".git")){ //skip git folder for performance
                    filesWithType.addAll(Arrays.asList(searchForFiles(directory+"\\"+f.getName(),fileType)));
                }
            }
        }
        
        JavaFile[] fileArr=new JavaFile[filesWithType.size()];
        return filesWithType.toArray(fileArr);
    }
    private String readCode(JavaFile f){
        try {
            Scanner reader=new Scanner(f);
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
    public JavaFile[] getJavaFiles(){
        return javaFiles;
    }
    public String[] getJavaCode(){
        String[] code=new String[javaFiles.length];
        for(int x=0;x<javaFiles.length;x++){
            code[x]=readCode(javaFiles[x]);
        }
        return code;
    }
    
    public boolean run(JavaRunner runner, int times){
        ArrayList<JavaFile> mainMethods=new ArrayList();
        for(JavaFile f:javaFiles){
            if(f.containsMain()){
                mainMethods.add(f);
            }
        }
        if(mainMethods.isEmpty()){
            GuiHelper.alertDialog("No classes contain main methods.");
            return false;
        }
        int choice=0;
        if(mainMethods.size()>1){
            String[] choices=new String[mainMethods.size()];
            for(int x=0;x<mainMethods.size();x++){
                String path=mainMethods.get(x).packageFolder()+"."+mainMethods.get(x).getName();
                choices[x]=path;
            }
            choice=GuiHelper.multiOptionPane("There are multiple main methods, which would you like to run?", choices);
        }
        if(choice==-1){
            return false;
        }
        
        runner.runFile(javaFiles,mainMethods.get(choice),times);
        return true;
    }
    public String getFileName(){
        return entry.name;
    }
    public void delete(){
        searchForFilesToDelete(downloadedFile.getPath());
        try {
            client.delete(entry.path);
        } catch (DbxException ex) {
            System.err.println("Error occured deleting "+entry.name+" from dropbox.");
        }
    }
    private void searchForFilesToDelete(String directory){
        ArrayList<File> files=new ArrayList();
        File folder=new File(directory);
        files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                f.delete();
            }
            else if(f.isDirectory()){
                searchForFilesToDelete(directory+"\\"+f.getName());
                f.delete();
            }
        }
        new File(directory).delete();
    }
    public void rename(String newName){
        System.out.println(entry.path);
        String directory=entry.path;
        String[] split=directory.split("/");
        int len=split[split.length-1].length();
        directory=directory.substring(0, directory.length()-len);
        try {
            client.move(entry.path, directory+newName);
        } catch (DbxException ex) {
            Logger.getLogger(DbxFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public String toString(){
        String zipPath=entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        return zipPath+" submitted on "+getSubmitDate(true,-1,-1);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.entry);
        hash = 29 * hash + Objects.hashCode(this.downloadedFile);
        hash = 29 * hash + Arrays.deepHashCode(this.javaFiles);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbxFile other = (DbxFile) obj;
        if (!Objects.equals(this.entry, other.entry)) {
            return false;
        }
        if (!Objects.equals(this.downloadedFile, other.downloadedFile)) {
            return false;
        }
        if (!Arrays.deepEquals(this.javaFiles, other.javaFiles)) {
            return false;
        }
        return true;
    }
    
}
