/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
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
    private DbxEntry entry;
    private DbxClient client;
    public DbxFile(DbxEntry entry,FileManager fileMan,DbxClient client){
        this.entry=entry;
        fileManager=fileMan;
        this.client=client;
    }
    public File download(){
        String zipPath=fileManager.getDownloadFolder()+"/"+entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        File file=new File(zipPath);
        try {
            FileOutputStream f = new FileOutputStream(fileManager.getDownloadFolder()+"/"+entry.name);
            DbxEntry.File downloadedFile=client.getFile(entry.path, null, f);
            f.close();
            Unzip.unzip(file.getPath()+".zip", fileManager.getUnzipFolder());
            new File(fileManager.getDownloadFolder()+"/"+entry.name).delete();
            return file;
        } catch (DbxException | IOException ex) {
            Logger.getLogger(DbxFile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
}
