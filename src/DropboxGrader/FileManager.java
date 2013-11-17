/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

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
    private String dropboxFolder;
    private DbxClient client;
    private ArrayList<DbxFile> files;
    private String fileStartDelim;
    private final String downloadFolder="downloads";
    private final String unzipedFolder="extracted";
    
    public FileManager(String dropboxFolder,String fileDelim,DbxClient client){
        this.dropboxFolder=dropboxFolder;
        this.client=client;
        this.fileStartDelim=fileDelim;
        
        files=new ArrayList();
        
        File dlDirectory=new File(downloadFolder);
        dlDirectory.mkdir();
        File exDirectory=new File(downloadFolder+"/"+unzipedFolder);
        exDirectory.mkdir();
        
        init();
    }
    private void init(){
        try {
            DbxEntry.WithChildren folderList=client.getMetadataWithChildren("/"+dropboxFolder);
            DbxFile file;
            for(DbxEntry child: folderList.children){
                if(child.name.startsWith(fileStartDelim.toLowerCase())||child.name.startsWith(fileStartDelim.toUpperCase())){
                    file=new DbxFile(child,this,client);
                    files.add(file);
                    System.out.println("Adding "+child.toString());
                }
            }
        } catch (DbxException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void downloadAll(){
        for(DbxFile f:files){
            f.download();
        }
    }
    public String getDownloadFolder(){
        return downloadFolder;
    }
    public String getDropboxFolder(){
        return dropboxFolder;
    }
    public String getUnzipFolder(){
        return downloadFolder+"/"+unzipedFolder;
    }
}
