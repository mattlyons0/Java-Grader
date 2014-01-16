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
import java.util.ArrayList;
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
    private String code;
    public HTMLGrader(FileManager manager,DbxClient client){
        this.client=client;
        this.manager=manager;
        init();
    }
    private void init(){
        if(Config.dropboxSpreadsheetFolder==null){
            Config.reset(); //config corrupt...
        }
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
            code=DbxSession.readFromFile(sheet);
        } catch(DbxException | IOException e){
            System.err.println("An error occured while downloading the HTML spreadsheet. "+e);
        }
    }
    private void createSheet(){
        try{
            File sheet=new File(filenameLocal);
            sheet.createNewFile();
            //default html page
            code=""+
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
"                <!--names go here-->\n" +
"            </table>\n" +
"        </div>\n" +
"    </body>\n" +
"</html>";
            //write to file
            DbxSession.writeToFile(sheet, code);
            upload();
        } catch(IOException e){
            System.err.println("An error occured while creating HTML spreadsheet. "+e);
        }
    }
    private boolean upload(){
        try{
            //upload to dropbox
            FileInputStream sheetStream = new FileInputStream(sheet);
            client.uploadFile(filenameRemote, DbxWriteMode.force(), sheet.length(), sheetStream);
            sheetStream.close();
        } catch(DbxException | IOException e){
            System.err.println("Error uploading spredsheet to dropbox. "+e);
            return false;
        }
        return true;
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
    public static int lastIndexOf(String substring,String str){
        Integer[] index=allIndexOf(substring,str);
        if(index.length==0){
            return -1;
        }
        return index[index.length-1];
    }
    public static Integer[] allIndexOf(String substring,String str){
        boolean inSub=false;
        ArrayList<Integer> indexes=new ArrayList();
        int subIndex=0;
        int startIndex=-1;
        for(int x=0;x<str.length();x++){
            char c=str.charAt(x);
            if(substring.length()>subIndex&&substring.charAt(subIndex)==c){
                if(!inSub){
                    startIndex=x;
                    inSub=true;
                    subIndex=0;
                }
                if(subIndex==substring.length()-1){
                    indexes.add(startIndex);
                }
                subIndex++;
            }
            else{
                inSub=false;
                subIndex=0;
                startIndex=-1;
            }
        }
        Integer[] arr=new Integer[indexes.size()];
        return indexes.toArray(arr);
    }
    private void createAssignment(String assignmentNum){
        String substring="<!--assignments go below here-->";
        int assignmentIndex=indexOf(substring,code)+substring.length();
        code=code.substring(0, assignmentIndex)+"\n"
                + "                    <td>"+assignmentNum+"</td>\n"+
                code.substring(assignmentIndex, code.length());
    }
    private void createName(String name){
        String substring="<!--names go here-->";
        int nameIndex=indexOf(substring,code)+substring.length();
        code=code.substring(0,nameIndex)+"\n"+
                "                    <tr><td>"+name+"</td></tr>\n"+
                code.substring(nameIndex,code.length());
    }
    public boolean setGrade(String name,String assignmentNum,String gradeNum,String comment,JLabel statusLabel){
        downloadSheet();
        if(!nameInTable(name)){
            createName(name);
        }
        if(!assignmentInTable(assignmentNum)){ //need to create assignment in table
            createAssignment(assignmentNum);
        }
        //Isolate line
        int injectLineIndexStart=indexOf("<td>"+name+"</td>",code);
        String line=code.substring(injectLineIndexStart);
        int injectLineIndexEnd=lastIndexOf("</tr>",line);
        
        //count td tags
        String assignmentString="<td>"+assignmentNum;
        int assignCol=selectiveCountRepeats("<!--assignments go below here-->",assignmentNum,"</td>",code);
        
        //Determine which <td> block to put in
        int currentNumTags=selectiveCountRepeats(name+"</td>","</tr>","</td>",line);
        int tagsToAdd=assignCol-currentNumTags;
        int tagWriteIndex=lastIndexOf("</td>",line);
        for(int x=0;x<tagsToAdd;x++){
            line=line.substring(tagWriteIndex)+"<td></td>"+line.substring(tagWriteIndex,line.length());
        }
        int gradeIndex=lastIndexOf("</td>",line.substring(tagWriteIndex,line.length())); //this won't work for editing a "middle tag"
        line=line.substring(0,gradeIndex)+gradeNum+"\n"+comment+line.substring(gradeIndex,line.length());
        //Re-inject into code
        code=code.substring(0,injectLineIndexStart)+line+code.substring(injectLineIndexEnd,code.length());
        //write to file
        DbxSession.writeToFile(sheet, code);
        //upload
        upload();
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
    private boolean nameInTable(String name){
        return false;
    }
    public void reset(){
        init();
    }

    private int selectiveCountRepeats(String startString, String endString,String counted,String str) {
        int startIndex=indexOf(startString,str)+startString.length();
        str=str.substring(startIndex); //cut off the front part
        int endIndex=indexOf(endString,str);
        str=str.substring(0,endIndex); //cut off the end part
        
        Integer[] occurances=allIndexOf(counted,str);
        return occurances.length;
    }
}
