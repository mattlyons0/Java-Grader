/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import static DropboxGrader.Config.configFile;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.io.File;
import java.io.FileInputStream;
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
    private File sheet;
    public HTMLGrader(FileManager manager,DbxClient client){
        this.client=client;
        this.manager=manager;
        init();
    }
    private void init(){
        filenameLocal="Grades-Period"+Config.dropboxPeriod+".html";
        filenameRemote="/"+Config.dropboxSpreadsheetFolder+"/"+filenameLocal;
        filenameLocal=manager.getDownloadFolder()+"/"+filenameLocal;
        sheet=new File(filenameLocal);
        downloadSheet();
    }
    private void downloadSheet(){
        try{
            DbxEntry entry=client.getMetadata(filenameRemote);
            if(entry!=null){ //file has already been created
                FileOutputStream f = new FileOutputStream(filenameLocal);
                client.getFile(filenameRemote, null, f); //download file
                f.close();
            }
            else{ //no spreadsheet file found
                createSheet();
            }
        } catch(DbxException | IOException e){
            System.err.println("An error occured while downloading the HTML spreadsheet. "+e);
        }
    }
    private void createSheet(){
        try{
            File sheet=new File(filenameLocal);
            sheet.createNewFile();
            //default html page
            String code="" +
"<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <title>Period "+Config.dropboxPeriod+" Grades</title>\n" +
"        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
"    </head>\n" +
"    <body>\n" +
"        <h2><h2>Period "+Config.dropboxPeriod+" Grades</h2>\n"+
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
            //write to file
            DbxSession.writeToFile(sheet, code);
            //upload to dropbox
            FileInputStream sheetStream = new FileInputStream(sheet);
            client.uploadFile(filenameRemote, DbxWriteMode.add(), sheet.length(), sheetStream);
            sheetStream.close();
        } catch(IOException | DbxException e){
            System.err.println("An error occured while creating and uploading HTML spreadsheet. "+e);
        }
    }
    public static int indexOf(String substring,String str){
        boolean inSub=false;
        int subIndex=0;
        int startIndex=-1;
        for(int x=0;x<str.length();x++){
            char c=str.charAt(x);
            if(substring.charAt(subIndex)==c){
                if(!inSub){
                    startIndex=x;
                    inSub=true;
                    subIndex=0;
                }
                if(subIndex==substring.length()-1){
                    return startIndex; //will return at the first instance of substring
                }
                subIndex++;
            }
            else{
                inSub=false;
                subIndex=0;
                startIndex=-1;
            }
        }
        return -1;
    }
    private void createAssignment(String assignmentNum){
        String html=DbxSession.readFromFile(sheet);
        String substring="<!--assignments go below here-->";
        int assignmentIndex=indexOf(substring,html)+substring.length();
        html=html.substring(0, assignmentIndex)+"\n"
                + "                    <td>"+assignmentNum+"</td>\n"+
                html.substring(assignmentIndex, html.length());
        DbxSession.writeToFile(sheet, html);
    }
    public boolean setGrade(String name,String assignmentNum,String gradeNum,String comment,JLabel statusLabel){
        downloadSheet();
        if(!assignmentInTable(assignmentNum)){ //need to create assignment in table
            createAssignment(assignmentNum);
        }
        //need to write data
        try{
            //upload to dropbox
            FileInputStream sheetStream = new FileInputStream(sheet);
            client.uploadFile(filenameRemote, DbxWriteMode.add(), sheet.length(), sheetStream);
            sheetStream.close();
        } catch(DbxException | IOException e){
            System.err.println("Error uploading spredsheet to dropbox. "+e);
        }
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
    private boolean assignmentInTable(String assignmentNum){
        return false;
    }
    public void reset(){
        init();
    }
}
