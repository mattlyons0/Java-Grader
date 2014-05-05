/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.FileManagement;

import DropboxGrader.TextGrader.TextSpreadsheet;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 *
 * @author 141lyonsm
 */
public class Date {
    public int month;
    public int day;
    public int year;
    public int hour;
    public int minute;
    public int second;
    
    public Date(){
        
    }
    public Date(java.util.Date d){
        Calendar c=Calendar.getInstance();
        c.setTime(d);
        year=c.get(Calendar.YEAR);
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);
        second=c.get(Calendar.SECOND);
    }
    public Date(String dateString){
        try{
            String[] split=dateString.split(Pattern.quote(TextSpreadsheet.INDIVIDUALDELIMITER2));
            month=Integer.parseInt(split[0]);
            day=Integer.parseInt(split[1]);
            year=Integer.parseInt(split[2]);
            hour=Integer.parseInt(split[3]);
            minute=Integer.parseInt(split[4]);
            second=Integer.parseInt(split[5]);            
        } catch(Exception e){
            //defaults will do
        }
    }
    public String toText(){
        String str=month+"";
        str=append(str,day);
        str=append(str,year);
        str=append(str,hour);
        str=append(str,minute);
        str=append(str,second);
        return str;
    }
    private String append(String str,Object append){
        return str+TextSpreadsheet.INDIVIDUALDELIMITER2+append;
    }
    public java.util.Date toDate(){
        java.util.Date d=new java.util.Date(year,month,hour,minute,second);
        return d;
    }
    public static Date currentDate(){
        Calendar c=Calendar.getInstance();
        Date d=new Date();
        d.year=c.get(Calendar.YEAR);
        d.hour=c.get(Calendar.HOUR_OF_DAY);
        d.minute=c.get(Calendar.MINUTE);
        d.second=c.get(Calendar.SECOND);
        return d;
    }
    public static boolean before(Date first,Date last){
        Calendar firstC=Calendar.getInstance();
        firstC.setTime(first.toDate());
        Calendar lastC=Calendar.getInstance();
        lastC.setTime(last.toDate());
        
        return firstC.before(lastC);
    }
    
}
