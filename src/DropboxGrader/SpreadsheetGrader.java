/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Matt
 */
public class SpreadsheetGrader {
    private SpreadsheetService service;
    private HashMap<Integer,String> assignmentMap; //assignment number,columnHeader
    private HashMap<Integer,String> commentsMap; //assignment number,columnHeader
    private WorksheetEntry sheet;
    private ListFeed feed;
    private URL feedURL;
    private String sheetName;
    private Gui gui;
    public SpreadsheetGrader(String sheetName,SpreadsheetService service,Gui gui){
        this.sheetName=sheetName;
        this.service=service;
        this.gui=gui;
        init();
    }
    private void init(){
        sheetName=Config.spreadsheetName;
        try {
            feedURL=new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
            
            SpreadsheetFeed spreadsheets=service.getFeed(feedURL, SpreadsheetFeed.class);
            List<SpreadsheetEntry> sheets=spreadsheets.getEntries();
            SpreadsheetEntry sSheet=null;
            for(SpreadsheetEntry e:sheets){
                if(e.getTitle().getPlainText().equals(sheetName)){
                    sSheet=e;
                    break;
                }
            }
            if(sSheet==null){
                System.err.println("No spreadsheet with the name "+sheetName);
            }
            else{
                sheet=sSheet.getWorksheets().get(0); //if there is more than 1 worksheet it will always use the first.
                this.feed=service.getFeed(sheet.getListFeedUrl(), ListFeed.class);
            }
            initAssignmentNums();
        } catch (IOException|ServiceException ex) {
            if(ex instanceof UnknownHostException)
                GuiHelper.alertDialog("Error connecting to google. "+ex);
            Logger.getLogger(SpreadsheetGrader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initAssignmentNums(){
        assignmentMap=new HashMap();
        commentsMap=new HashMap();
        if(feed==null){
            return;
        }
        List<ListEntry> entries=feed.getEntries();
        ListEntry row=entries.get(0);
        int lastAssignment=-1;
        for (String tag : row.getCustomElements().getTags()) {
            String columnTitle=row.getCustomElements().getValue(tag);
            if(columnTitle!=null&&!columnTitle.equals("")){
                int index=columnTitle.indexOf("#")+1;
                if(index!=0){ 
                    String num="";
                    while(Character.isDigit(columnTitle.charAt(index))){
                        num+=columnTitle.charAt(index);
                        index++;
                        if(index>=columnTitle.length()){
                            break;
                        }
                    }
                    int number=Integer.parseInt(num);
                    assignmentMap.put(number, tag);
                    lastAssignment=number;
                    //System.out.println("Assignment "+number+" at "+tag);
                }
                    
            }
            else{
                commentsMap.put(lastAssignment, tag);
            }
        }
    }
    public void refresh(){
        init();
    }
    public boolean setGrade(String name,int assignment,String grade,String comment,JLabel statusLabel){
        init();
        
        if(grade==null||grade.equals("")){
            statusLabel.setText("Grade is not set.");
            return false;
        }
        
        String columnName=assignmentMap.get(assignment);
        String columnComment=commentsMap.get(assignment);
        if(columnName==null||columnComment==null){
            statusLabel.setText("Assignment "+assignment+" is not declared in the spreadsheet with a cell for the grade and a cell for the comments.");
        }
        else{
            boolean overwrite=false;
            List<ListEntry> entries=feed.getEntries();
            if(entries.isEmpty()){
                statusLabel.setText("No rows were found on the spreadsheet.");
            }
            for(ListEntry row:entries){
                if(nameEquals(name,row.getTitle().getPlainText(),statusLabel)){
                    String currentVal=row.getCustomElements().getValue(columnName);
                    String currentComment=row.getCustomElements().getValue(columnComment);
                    if(currentComment==null)
                        currentComment="";
                    if(currentVal!=null){
                        statusLabel.setText("Grade of "+currentVal+", "+currentComment+" has been changed and set as an resubmit.");
                        overwrite=true;
                    }
                    else{
                        statusLabel.setText("Graded");
                    }
                    if(overwrite&&!comment.contains(" Resubmitted")){
                        comment+=" Resubmitted";
                    }
                    if(!grade.contains("`")){
                        grade+="`";
                    }
                    //System.out.println("Found match with "+name+" at "+row.getTitle().getPlainText());
                    row.getCustomElements().setValueLocal(columnName, grade);
                    row.getCustomElements().setValueLocal(columnComment, comment);
                    //change color: http://stackoverflow.com/questions/3703676/google-spreadsheet-script-to-change-row-color-when-a-cell-changes-text
                    try {
                        row.update();
                        return true;
                    } catch (IOException|ServiceException ex) {
                        Logger.getLogger(SpreadsheetGrader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            statusLabel.setText(name+" was not found in the spreadsheet.");
        }
        return false;
    }
    public boolean gradeWritten(String name,int assignment,JLabel statusLabel){
        if(name==null||name.equals("")){
            statusLabel.setText("Name not set.");
            return false;
        }
        
        String columnName=assignmentMap.get(assignment);
        String columnComment=commentsMap.get(assignment);
        if(columnName==null){
            statusLabel.setText("Assignment "+assignment+" is not declared in the spreadsheet.");
        }
        else{
            List<ListEntry> entries=feed.getEntries();
            for(ListEntry row:entries){
                if(nameEquals(name,row.getTitle().getPlainText(),null)){
                    String currentVal=row.getCustomElements().getValue(columnName);
                    String currentComment=row.getCustomElements().getValue(columnComment);
                    if(currentVal!=null){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public String gradeAt(String name,int assignment,JLabel statusLabel){
        String[] entries=getEntryAt(name,assignment,statusLabel);
        if(entries==null)
            return null;
        return entries[0];
    }
    public String[] getEntryAt(String name,String assignment,JLabel statusLabel){
        int assign=DbxFile.safeStringToInt(assignment);
        return getEntryAt(name,assign,statusLabel);
    }
    /**
     * 
     * @param name
     * @param assignment
     * @param statusLabel
     * @return [0]=grade [1]=comment
     */
    public String[] getEntryAt(String name,int assignment,JLabel statusLabel){
        String[] entries=new String[2];
        
        if(name==null||name.equals("")){
            statusLabel.setText("Name not set.");
            return null;
        }
        
        String columnName=assignmentMap.get(assignment);
        String columnComment=commentsMap.get(assignment);
        if(columnName==null){
            statusLabel.setText("Assignment "+assignment+" is not declared in the spreadsheet.");
        }
        else{
            List<ListEntry> feedEntries=feed.getEntries();
            for(ListEntry row:feedEntries){
                if(nameEquals(name,row.getTitle().getPlainText(),null)){
                    String currentVal=row.getCustomElements().getValue(columnName);
                    String currentComment=row.getCustomElements().getValue(columnComment);
                    if(currentVal!=null){
                        entries[0]=currentVal;
                        if(entries[0]!=null)
                            entries[0]=entries[0].replace("`","");
                        entries[1]=currentComment;
                        if(entries[1]!=null)
                            entries[1]=entries[1].replace("Resubmitted", "Resubmit");
                        return entries;
                    }
                }
            }
        }
        return null;
    }
    private boolean nameEquals(String name,String rowTitle,JLabel statusLabel){
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
            if(statusLabel!=null){
                statusLabel.setText("Name "+name+" does not follow proper capitilization. Cannot find on spreadsheet because of that.");
            }
            return false;
        }
        firstName=name.substring(0, upercaseIndex);
        lastName=name.substring(upercaseIndex, name.length());
        
        if(rowTitle.contains(firstName)&&rowTitle.contains(lastName)){
            return true;
        }
        return false;
    }
}
