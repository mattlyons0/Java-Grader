/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class FileManager {
    private String dropboxFolder;
    private DbxClient client;
    private Gui gui;
    private FileBrowserData tableData;
    private ArrayList<DbxFile> files;
    private String fileStartDelim;
    private final String downloadFolder="downloads";
    private final String[] attributes={"Assignment","Assignment Name","Submit Date","Name","Status"};
    
    public FileManager(String dropboxFolder,String fileDelim,DbxClient client,Gui gui){
        this.dropboxFolder=dropboxFolder;
        this.client=client;
        this.fileStartDelim=fileDelim;
        this.gui=gui;
        
        files=new ArrayList();
        
        File dlDirectory=new File(downloadFolder);
        dlDirectory.mkdir();
        File exDirectory=new File(downloadFolder);
        exDirectory.mkdir();
        
        init();
    }
    private void init(){
        try {
            DbxEntry.WithChildren folderList=client.getMetadataWithChildren("/"+dropboxFolder);
            DbxFile file;
            for(DbxEntry child: folderList.children){
                if(child.name.startsWith(fileStartDelim.toLowerCase())||child.name.startsWith(fileStartDelim.toUpperCase())){
                    if(child.asFile()!=null){
                        file=new DbxFile((DbxEntry.File)child,this,client);
                        files.add(file);
                        //System.out.println("Adding "+child.toString());
                    }
                    else{
                        System.out.println("There are folders and I was not written recursively so I can't pickup files from there.");
                    }
                }
            }
        } catch (DbxException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void download(int fileNum){
        files.get(fileNum).download();
    }
    public void downloadAll(){
        for(int x=0;x<files.size();x++){
            DbxFile f=files.get(x);
            f.download();
            gui.updateProgress(x+1/(double)files.size());
        }
    }
    public String getDownloadFolder(){
        return downloadFolder;
    }
    public String getDropboxFolder(){
        return dropboxFolder;
    }
    public String getUnzipFolder(){
        return "";
    }
    public ArrayList<DbxFile> getFiles(){
        return files;
    }
    public int getNumFiles(){
        return files.size();
    }
    public int getNumAttributes(){
        return attributes.length;
    }
    public String getFileInfo(int fileNum,int attributeNum){
        DbxFile file=files.get(fileNum);
        return getAttribute(file,fileNum,attributeNum);
    }
    private String getAttribute(DbxFile file,int fileNum,int attribute){
        String attrib=attributes[attribute];
        switch(attrib){
            case "Assignment": return file.getAssignmentNumber()+"";
            case "Assignment Name": return file.getAssignmentName();
            case "Submit Date": return file.getSubmitDate(true,fileNum,attribute);
            case "Name": return file.getFirstLastName();
            case "Status": return file.getStatus();
        }
        return null;
    }
    public String[] getAttributes(){
        return attributes;
    }
    public void setTableData(FileBrowserData d){
        tableData=d;
    }
    public FileBrowserData getTableData(){
        return tableData;
    }
    public void refresh(){
        files.clear();
        init();
        tableData.refresh();
    }
    public DbxFile getFile(int x){
        return files.get(x);
    }
}
