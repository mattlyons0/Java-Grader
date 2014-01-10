/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import static DropboxGrader.Config.configFile;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
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
                f.close();
            }
            else{ //need to create new spreadsheet
                f.close();
                File sheet=new File(filenameLocal);
                sheet.createNewFile();
                //default html page
                String code="<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <title>Period "+Config.dropboxPeriod+" Grades Spreadsheet</title>\n" +
"        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
"    </head>\n" +
"    <body>\n" +
"        <div>\n" +
"            <table border=\"1\">\n" +
"                <tr>\n" +
"                    <td></td> <!--assignments go below here-->\n" +
"                </tr>\n" +
"                <tr>\n" +
"                    <!--names go here-->\n" +
"                </tr>\n" +
"            </table>\n" +
"        </div>\n" +
"    </body>\n" +
"</html>";
                DbxSession.writeToFile(sheet, code);
                
            }
        } catch(DbxException | IOException e){
            System.err.println("An error occured while initializing the HTML spreadsheet. "+e);
        }
    }
    private int indexOf(String substring,String str){
        boolean inSub=false;
        int subIndex=0;
        int startIndex=-1;
        for(int x=0;x<str.length();x++){
            char c=str.charAt(x);
            if(substring.charAt(subIndex)==c){
                if(!inSub){
                    startIndex=x;
                    inSub=true;
                }
                if(subIndex==substring.length()-1){
                    return startIndex;
                }
            }
            else{
                inSub=false;
                subIndex=0;
                startIndex=-1;
            }
        }
        return -1;
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
