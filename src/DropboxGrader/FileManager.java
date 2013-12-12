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
    private SpreadsheetGrader grader;
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
        if(grader!=null){
            grader.refresh();
        }
        try {
            DbxEntry.WithChildren folderList=client.getMetadataWithChildren("/"+dropboxFolder);
            DbxFile file;
            if(folderList==null){
                return;
            }
            for(DbxEntry child: folderList.children){
                if(child.name.startsWith(fileStartDelim.toLowerCase())||child.name.startsWith(fileStartDelim.toUpperCase())){
                    if(child.isFile()&&child.asFile()!=null){
                        file=new DbxFile((DbxEntry.File)child,this,client);
                        files.add(file);
                        //System.out.println("Adding "+child.toString());
                    }
                    else{
                        System.err.println("There are folders and I was not written recursively so I can't pickup files from there.");
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
            gui.updateProgress((int)(x+1.0/files.size()));
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
        if(fileNum>=files.size())
            return null; //wrong size when deleting files
        DbxFile file=files.get(fileNum);
        return getAttribute(file,fileNum,attributeNum);
    }
    private String getAttribute(DbxFile file,int fileNum,int attribute){
        String attrib=attributes[attribute];
        switch(attrib){
            case "Assignment": return file.getAssignmentNumber()+"";
            case "Assignment Name": return file.getAssignmentName(fileNum,attribute);
            case "Submit Date": return file.getSubmitDate(true,fileNum,attribute);
            case "Name": return file.getFirstLastName();
            case "Status": return file.getStatus(fileNum,attribute);
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
        
        dropboxFolder=Config.dropboxFolder;
        fileStartDelim=Config.dropboxPeriod;
        
        init();
        if(tableData!=null)
            tableData.refresh();
    }
    public int getFileNum(DbxFile f){
        return files.indexOf(f);
    }
    public DbxFile getFile(int x){
        if(x>=files.size()){
            return null; //thrown when deleting files
        }
        return files.get(x);
    }
    public SpreadsheetGrader getGrader(){
        return grader;
    }
    public void setGrader(SpreadsheetGrader g){
        grader=g;
    }
    public void delete(DbxFile file){
        files.remove(file);
    }
    public Gui getGui(){
        return gui;
    }
}
