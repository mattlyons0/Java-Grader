/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JLabel;

/**
 *
 * @author 141lyonsm
 */
public class HTMLGrader {
    private DbxClient client;
    private FileManager manager;
    private String filenameRemote;
    private String filenameLocal;
    public HTMLGrader(FileManager manager,DbxClient client){
        this.client=client;
        this.manager=manager;
        init();
    }
    private void init(){
        try{
            filenameLocal="Grades-Period"+Config.dropboxPeriod+".html";
            filenameRemote="/"+manager.getDropboxFolder()+"/"+filenameLocal;
            filenameLocal=manager.getDownloadFolder()+"/"+filenameLocal;
            FileOutputStream f = new FileOutputStream(filenameLocal);
            DbxEntry entry=client.getMetadata(filenameRemote);
            if(entry!=null){ //file has already been created
                client.getFile(filenameRemote, null, f); //download file
            }
            else{ //need to create new spreadsheet
                File sheet=new File(filenameLocal);
                sheet.createNewFile();
                String code;
                
            }
        } catch(DbxException | IOException e){
            System.err.println("An error occured while initializing the HTML spreadsheet. "+e);
        }
    }
    public boolean setGrade(String name,String assignmentNum,String gradeNum,String comment,JLabel statusLabel){
        return false;
    }
    public String getGrade(String name,String assignmentNum,JLabel statusLabel){
        return "";
    }
    public String getComment(String name,String assignmentNum,JLabel statusLabel){
        return "";
    }
    public boolean gradeWritten(String name,String assignmentNum,JLabel statusLabel){
        return false;
    }
    public void reset(){
        init();
    }
}
