/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.TextGrader;

import DropboxGrader.Config;
import DropboxGrader.DbxSession;
import DropboxGrader.FileManagement.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.Assignment.AssignmentOverlay;
import DropboxGrader.GuiElements.MiscOverlays.NameOverlay;
import DropboxGrader.GuiHelper;
import DropboxGrader.Util.StaticMethods;
import DropboxGrader.WorkerThread;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class TextGrader {
    private String selectedPath;
    private String selectedRemotePath;
    
    private DbxClient client;
    private FileManager manager;
    private String filenameRemote;
    private String downloadedRevision;
    private String filenameLocal;
    private File sheet;
    private TextSpreadsheet data;
    private Gui gui;
    
    private boolean doneLoading=false;
    private boolean locked=false;
    public TextGrader(FileManager manager,DbxClient client,Gui gui){
        this.client=client;
        this.manager=manager;
        this.gui=gui;
        data=new TextSpreadsheet(gui);
        init();
    }
    private void init(){
        if(Config.dropboxSpreadsheetFolder==null){
            Config.reset(); //config corrupt
        }
        String text="";
        try {
            selectedPath=manager.getDownloadFolder()+"/.selected"+Config.dropboxPeriod;
            selectedRemotePath="/"+Config.dropboxSpreadsheetFolder+"/.selected"+Config.dropboxPeriod;
            FileOutputStream f = new FileOutputStream(selectedPath);
            DbxEntry e=client.getFile(selectedRemotePath, null, f);
            f.close();
            text=DbxSession.readFromFile(new File(selectedPath));
        } catch (IOException|DbxException ex) {
            System.err.println("Error determining selected gradebook. Using Default.");
            Logger.getLogger(TextGrader.class.getName()).log(Level.SEVERE, null, ex);
        }
        filenameLocal="Grades-Period"+Config.dropboxPeriod+text+".txt";
        filenameRemote="/"+Config.dropboxSpreadsheetFolder+"/"+filenameLocal;
        filenameLocal=manager.getDownloadFolder()+"/"+filenameLocal;
        sheet=new File(filenameLocal);
        if(!data.isInitialized())
            data.parse(null);
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                downloadSheet();
                gui.repaint();
                doneLoading=true;
            }
        });
    }
    public void forceDownloadSheet(){
        downloadSheet(true);
    }
    public void downloadSheet(){
        downloadSheet(false);
    }
    private void downloadSheet(boolean force){
        try{
            DbxEntry.File entry=(DbxEntry.File)client.getMetadata(filenameRemote);
            if(entry!=null){ //file has already been created
                if(!force&&downloadedRevision!=null&&downloadedRevision.equals(entry.rev)){ //current version is downloaded, no need to do it again
                    return;
                }
                FileOutputStream f = new FileOutputStream(filenameLocal);
                client.getFile(filenameRemote, null, f); //download file
                f.close();
                downloadedRevision=entry.rev;
            }
            else{ //no spreadsheet file found
                createSheet();
            }
            //parse sheet into a 2d array;
            data.parse(sheet);
            
        } catch(DbxException | IOException e){
            System.err.println("An error occured while downloading the HTML spreadsheet. "+e);
            e.printStackTrace();
            if(e instanceof ConnectException||e instanceof DbxException.NetworkIO){
                GuiHelper.alertDialog("Error, Connection Timed Out.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TextGrader.class.getName()).log(Level.SEVERE, null, ex);
                }
                downloadSheet(force);
            }
            else{
                GuiHelper.alertDialog("Error accessing files from dropbox.\nSee error log for more information.");
            }
        }
    }
    //make sheet, and save it to a file.
    private void createSheet(){
        try{
            File sheet=new File(filenameLocal);
            sheet.createNewFile();
            String code=TextSpreadsheet.COMMENTDELIMITER+"DO NOT EDIT THIS FILE MANUALLY.\n";
            DbxSession.writeToFile(sheet, code);
            upload();
        } catch(IOException e){
            System.err.println("An error occured while creating HTML spreadsheet. "+e);
            e.printStackTrace();
        }
    }
    public boolean uploadTable(){
        if(locked)
            return false;
        data.writeToFile(sheet);
        return upload();
    }
    private boolean upload(){
        if(locked)
            return false;
        try{
            DbxEntry.File entry=(DbxEntry.File)client.getMetadata(filenameRemote);
            if(entry!=null&&downloadedRevision!=null){ //file has already been created
                if(!downloadedRevision.equals(entry.rev)){
                    //If someone changes a grade between the time it takes to download the sheet
                    //proccess it, write the change to the sheet to disk then upload it.
                    System.err.println("A different revision was downloaded than was going to be uploaded.\n"
                            + "Someone changed a grade at the exact moment you changed a grade.");
                    StaticMethods.printStackTrace();
                    GuiHelper.alertDialog("Someone wrote a grade at the exact moment you did.\nTry again in a second.");
                    return false;
                }
            }
            //upload to dropbox
            FileInputStream sheetStream = new FileInputStream(sheet);
            client.uploadFile(filenameRemote, DbxWriteMode.force(), sheet.length(), sheetStream);
            sheetStream.close();
        } catch(DbxException | IOException e){
            System.err.println("Error uploading spreadsheet to dropbox. "+e);
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static int indexOf(String substring,String str){
        if(substring.length()==0||str.length()==0){
            return -1;
        }
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
    public boolean setGrade(String name,final int assignmentNum,final double gradeNum,final String comment,final boolean overwrite){
        if(!Thread.currentThread().getName().equals(WorkerThread.threadName)){
            throw new IllegalStateException("Attempted to grade from thread: "+Thread.currentThread().getName()+". "
                    + "Grading can only be done from the background thread.");
        }
        downloadSheet();
        if(!data.nameDefined(name)){ //need to put name in gradebook
            final String[] nameParts=splitName(name,assignmentNum,gradeNum,comment,overwrite);
            if(nameParts==null){
                return true;
            }
            data.addName(nameParts[0],nameParts[1],null);
            final NameOverlay overlay=new NameOverlay(gui);
            overlay.setData(nameParts[0], nameParts[1],null);
            overlay.setCallback(new Runnable() {
                @Override
                public void run() {
                    downloadSheet();
                    String[] strings=overlay.getNames();
                    TextSpreadsheet sheet=getSpreadsheet();
                    TextName name=sheet.getName(nameParts[0]+" "+nameParts[1]);
                    name.firstName=strings[0];
                    name.lastName=strings[1];
                    name.email=strings[2];
                    uploadTable();
                }
            });
            name=nameParts[0]+nameParts[1];
        }
        if(!data.assignmentDefined(assignmentNum)){ //need to create assignment in table
            final AssignmentOverlay overlay=new AssignmentOverlay(gui);
            overlay.setData(new TextAssignment(assignmentNum,"",overlay.getDate()));
            overlay.setCallback(new Runnable() {
                @Override
                public void run() {
                    downloadSheet();
                    Object[] returned=overlay.getData();
                    TextAssignment assign=data.getAssignment(assignmentNum);
                    assign.number=(int)returned[0];
                    assign.name=(String)returned[1];
                    assign.totalPoints=(Double)returned[2];
                    assign.simpleUnitTests=overlay.getUnitTest();
                    assign.junitTests=overlay.getJUnitTests();
                    assign.libraries=overlay.getLibraries();
                    uploadTable();
                    gui.gradebookDataChanged();
                }
            });
            gui.getViewManager().addOverlay(overlay);
            data.addAssignment(assignmentNum, "",null);
        }
        TextName n=data.getName(name);
        TextAssignment a=data.getAssignment(assignmentNum);
        if(gradeNum>a.totalPoints){
            int val=GuiHelper.multiOptionPane(gradeNum+" is larger than the total points"
                    + " for this assignment of "+a.totalPoints+". \nAre you sure you would to do this?", new String[]{"Yes","No"});
            if(val==1){
                downloadSheet(true);
                return false;
            }
        }
        TextGrade oldGrade=null;
        if(data.getGrade(n,a)!=null)
            oldGrade=new TextGrade(data.getGrade(n,a));
        
        boolean gradeSet=data.setGrade(data.getName(name),data.getAssignment(assignmentNum), gradeNum, comment,overwrite);
        if(!gradeSet){
            return false;
        }
        gui.getEmailer().emailGraded(a,n,data.getGrade(n,a), oldGrade);
        //convert to code, write and upload
        gui.gradebookDataChanged();
        return uploadTable();
    }
    public boolean setInGradebook(String name,int assignmentNum,boolean inGradebook){
        if(getGrade(name,assignmentNum).inGradebook==inGradebook){
            return true;
        }
        downloadSheet();
        TextGrade grade=getGrade(name,assignmentNum);
        grade.inGradebook=inGradebook;
        return uploadTable();
    }
    public TextGrade getGrade(String name,int assignmentNum){
        if(!doneLoading)
            return null;
        return data.getGrade(data.getName(name), data.getAssignment(assignmentNum));
    }
    public Double getGradeNum(String name,int assignmentNum){
        TextGrade grade=data.getGrade(data.getName(name), data.getAssignment(assignmentNum));
        return grade==null? null:grade.grade;
    }
    public String getComment(String name,int assignmentNum){
        TextGrade grade=data.getGrade(data.getName(name), data.getAssignment(assignmentNum));
        return grade==null? null:grade.comment;
    }
    public boolean gradeWritten(String name,int assignmentNum){
        return getGradeNum(name,assignmentNum)!=null;
    }
    
    public void refresh(){
        init();
        
    }
    public TextSpreadsheet getSpreadsheet(){
        return data;
    }
    public String[] splitName(String name){
        String firstName,lastName;
        int upercaseIndex=-1;
        char c;
        for(int x=0;x<name.length();x++){
            c=name.charAt(x);
            if(Character.isUpperCase(c)){
                if(x!=0){
                    upercaseIndex=x;
                    break;
                }
            }
        }
        if(upercaseIndex==-1){
            return null;
        }
        else{
            firstName=name.substring(0, upercaseIndex);
            lastName=name.substring(upercaseIndex, name.length());
        }
        
        return new String[] {firstName,lastName};
    }
    private String[] splitName(String name,final int assignmentNum,final double gradeNum,final String comment,final boolean overwrite){
        String[] split=splitName(name);
        if(split==null){
            final NameOverlay overlay=new NameOverlay(gui);
            overlay.setData(name, name,null);
            overlay.setCallback(new Runnable() {
                @Override
                public void run() {
                    String firstName=overlay.getNames()[0];
                    String lastName=overlay.getNames()[1];
                    String email=overlay.getNames()[2];
                    TextName name=data.getName(firstName+lastName);
                    if(name==null)
                        data.addName(firstName, lastName, email);
                    else
                        name.email=email;
                    TextAssignment a=data.getAssignment(assignmentNum);
                    TextName n=data.getName(firstName+lastName);
                    TextGrade oldGrade=data.getGrade(n,a)==null?null:new TextGrade(data.getGrade(n, a));
                    data.setGrade(n, a, gradeNum, comment, overwrite);
                    uploadTable();
                    
                    
                    gui.getEmailer().emailGraded(a,n,data.getGrade(n,a), oldGrade);
                }
            });
            gui.getViewManager().addOverlay(overlay);
            return null;
        }
        return split;
    }
    public String getSelectedPath(){
        return selectedPath;
    }    
    public String getSelectedRemotePath(){
        return selectedRemotePath;
    }
    public void lock(){
        locked=true;
    }
    public void unlock(){
        locked=false;
    }
}
