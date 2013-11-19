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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class DbxFile {
    private FileManager fileManager;
    private DbxEntry.File entry;
    private DbxClient client;
    private File downloadedFile;
    private File[] javaFiles;
    public DbxFile(DbxEntry.File entry,FileManager fileMan,DbxClient client){
        this.entry=entry;
        fileManager=fileMan;
        this.client=client;
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
    public int getAssignmentNumber(){
        String s=entry.name;
        return Integer.parseInt(s.split("_")[2]);//assignment number is the 3rd underscore
    }
    public String getAssignmentName(){
        String s=entry.name;
        s=s.split("_")[3];
        return s.substring(0, s.length()-4);//assignment name is 4th underscore, .zip is the last 4 characters
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
        if(checkRevisions&&entry.lastModified.after(entry.clientMtime)){
            fileManager.getTableData().setColorAt(new Color(230,120,120), row, col);
            return "Modified "+entry.lastModified+" Originally "+entry.clientMtime;
        }
        return entry.clientMtime.toString();
    }
    public String getFirstLastName(){
        String s=entry.name;
        return s.split("_")[1];
    }
    public String getStatus(){
        if(downloadedFile==null){
            return "On Server";
        }
        if(downloadedFile.exists()){
            return "Downloaded";
        }
        return "Unknown"; //downloaded but file doesnt exist.
    }
    private void setFile(File f){
        downloadedFile=f;
        searchJavaFiles();
    }
    private void searchJavaFiles(){
        javaFiles=searchForFiles(downloadedFile.getPath(),".java",null);
    }
    /**
     * Recursive file search
     * @param directory directory to search in
     * @param fileType files which end in this will be returned.
     * @return an array of files with specified ending characters.
     */
    private File[] searchForFiles(String directory,String fileType,ArrayList<File> currentFilesWithType){
        //System.out.println("Searching in "+directory);
        ArrayList<File> files=new ArrayList();
        ArrayList<File> filesWithType;
        if(currentFilesWithType==null){
            filesWithType=new ArrayList();
        }
        else{
            filesWithType=currentFilesWithType;
        }
        File folder=new File(directory);
        files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                if(f.getName().endsWith(fileType.toLowerCase())||f.getName().endsWith(fileType.toUpperCase())){
                    //if file is .Java it wont get added, but that is stupid capitalization that nothing would do anyway.
                    filesWithType.add(f);
                    //System.out.println("Adding "+f);
                }
            }
            else if(f.isDirectory()){
                files.addAll(Arrays.asList(searchForFiles(directory+"\\"+f.getName(),fileType,filesWithType)));
                break;
            }
        }
        
        File[] fileArr=new File[filesWithType.size()];
        return filesWithType.toArray(fileArr);
    }
    private String readFile(File f){
        try {
            Scanner reader=new Scanner(f);
            String read="";
            while(reader.hasNext()){
                read+=reader.next()+"\n";
            }
            return read;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public File[] getJavaFiles(){
        return javaFiles;
    }
    public String[] getJavaCode(){
        String[] code=new String[javaFiles.length];
        for(int x=0;x<javaFiles.length;x++){
            code[x]=readFile(javaFiles[x]);
        }
        return code;
    }
    
}
