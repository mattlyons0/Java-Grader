/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author 141lyonsm
 */
public class HTMLGrader {
    private DbxClient client;
    private FileManager manager;
    private String filename;
    public HTMLGrader(FileManager manager,DbxClient client){
        this.client=client;
        this.manager=manager;
        init();
    }
    private void init(){
        try{
            filename="Grades-Period"+Config.dropboxPeriod+".html";
            FileOutputStream f = new FileOutputStream(manager.getDownloadFolder()+"/"+filename);
            filename=manager.getDropboxFolder()+"/"+filename;
            client.getFile(filename, null, f);
        } catch(DbxException | IOException e){
            System.err.println("An error occured while initializing the HTML spreadsheet. "+e);
        }
    }
}
