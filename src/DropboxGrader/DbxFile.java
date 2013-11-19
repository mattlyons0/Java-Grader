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
    }
    private void searchJavaFiles(){
        
    }
    public File[] getJavaFiles(){
        return javaFiles;
    }
    
}
