/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.FileManagement;

import DropboxGrader.Config;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.FileBrowser.FileBrowserData;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextName;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class FileManager {
    private TextGrader grader;
    private String dropboxFolder;
    private DbxClient client;
    private Gui gui;
    private FileBrowserData tableData;
    private ArrayList<DbxFile> files;
    private int classPeriod;
    private final String DOWNLOADFOLDER="downloads";
    private final String[] ATTRIBUTES={"Assignment","Assignment Name","Submit Date","Name","Status"};
    
    public FileManager(String dropboxFolder,int period,DbxClient client,Gui gui){
        this.dropboxFolder=dropboxFolder;
        this.client=client;
        this.classPeriod=period;
        this.gui=gui;
        
        files=new ArrayList();
        
        File dlDirectory=new File(DOWNLOADFOLDER);
        dlDirectory.mkdir();
        new File(DOWNLOADFOLDER+Config.librariesLocation).mkdir();
        
    }
    public void postInit(){
        try {
            DbxEntry.WithChildren folderList=client.getMetadataWithChildren("/"+dropboxFolder);
            DbxFile file;
            if(folderList==null){
                return;
            }
            for(DbxEntry child: folderList.children){
                if(child!=null){
                    String[] splitChild=child.name.split("_");
                    if(splitChild[0].contains(classPeriod+"")){
                        if(child.isFile()&&child.asFile()!=null){
                            file=new DbxFile((DbxEntry.File)child,this,client);
                            files.add(file);
                        }
                        else{
                            System.err.println("There are folders and I was not written recursively so I can't pickup files from "+child.name+".");
                        }
                    }
                }
            }
            gui.fileBrowserDataChanged();
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
        return DOWNLOADFOLDER;
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
        return ATTRIBUTES.length;
    }
    public String getFileInfo(int fileNum,int attributeNum){
        if(fileNum>=files.size())
            return null; //wrong size when deleting files
        DbxFile file=files.get(fileNum);
        return getAttribute(file,fileNum,attributeNum);
    }
    private String getAttribute(DbxFile file,int fileNum,int attribute){
        String attrib=ATTRIBUTES[attribute];
        switch(attrib){
            case "Assignment": return file.getAssignmentNumber()==-1?"":file.getAssignmentNumber()+"";
            case "Assignment Name": return file.getAssignmentName(fileNum,attribute);
            case "Submit Date": return file.getSubmitDate(fileNum,attribute);
            case "Name": return file.getFirstLastName();
            case "Status": return file.getStatus(fileNum,attribute);
        }
        return null;
    }
    public String[] getAttributes(){
        return ATTRIBUTES;
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
        classPeriod=Config.dropboxPeriod;
        
        postInit();
        if(tableData!=null)
            tableData.refresh();
    }
    public void refreshCellColors(){
        if(tableData!=null){
            tableData.refresh();
        }
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
    public TextGrader getGrader(){
        return grader;
    }
    public void setGrader(TextGrader g){
        grader=g;
    }
    public void delete(DbxFile file){
        files.remove(file);
    }
    public Gui getGui(){
        return gui;
    }
    public boolean fileExists(int assignmentNumber,TextName name){
        for(DbxFile f:files){
            if(f!=null){
                if(f.getAssignmentNumber()==assignmentNumber){
                    String submittedName=f.getFirstLastName();
                    TextName foundName=gui.getGrader().getSpreadsheet().getName(submittedName);
                    if(name.equals(foundName))
                        return true;
                }
            }
        }
        return false;
    }
}
