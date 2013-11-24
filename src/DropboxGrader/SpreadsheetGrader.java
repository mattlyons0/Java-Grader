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
    public SpreadsheetGrader(String sheetName,SpreadsheetService service){
        try {
            feedURL=new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
            this.service=service;
            
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
                System.out.println("No spreadsheet with the name "+sheetName);
            }
            else{
                sheet=sSheet.getWorksheets().get(0); //if there is more than 1 worksheet it will always use the first.
                this.feed=service.getFeed(sheet.getListFeedUrl(), ListFeed.class);
            }
            initAssignmentNums();
        } catch (IOException|ServiceException ex) {
            Logger.getLogger(SpreadsheetGrader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void initAssignmentNums(){
        assignmentMap=new HashMap();
        commentsMap=new HashMap();
        
        List<ListEntry> entries=feed.getEntries();
        ListEntry row=entries.get(0);
        int lastAssignment=-1;
        for (String tag : row.getCustomElements().getTags()) {
            String columnTitle=row.getCustomElements().getValue(tag);
            if(columnTitle!=null){
                int index=columnTitle.indexOf("#")+1;
                if(index!=0){ 
                    String num="";
                    while(Character.isDigit(columnTitle.charAt(index))){
                        num+=columnTitle.charAt(index);
                        index++;
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
    public boolean setGrade(String name,int assignment,String grade,String comment,JLabel statusLabel){
        String columnName=assignmentMap.get(assignment);
        String columnComment=commentsMap.get(assignment);
        if(columnName==null){
            statusLabel.setText("Assignment "+assignment+" is not declared in the spreadsheet.");
        }
        List<ListEntry> entries=feed.getEntries();
        for(ListEntry row:entries){
            if(nameEquals(name,row.getTitle().getPlainText())){
                String currentVal=row.getCustomElements().getValue(columnName);
                if(currentVal==null){
                    //System.out.println("Found match with "+name+" at "+row.getTitle().getPlainText());
                    row.getCustomElements().setValueLocal(columnName, grade);
                    row.getCustomElements().setValueLocal(columnComment, comment);
                    try {
                        row.update();
                        return true;
                    } catch (IOException|ServiceException ex) {
                        Logger.getLogger(SpreadsheetGrader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    statusLabel.setText("A grade has already been set for this.");
                }
            }
        }
        
        return false;
    }
    private boolean nameEquals(String name,String rowTitle){
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
        firstName=name.substring(0, upercaseIndex);
        lastName=name.substring(upercaseIndex, name.length());
        
        if(rowTitle.contains(firstName)&&name.contains(lastName)){
            return true;
        }
        return false;
    }
}
