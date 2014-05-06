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
        month=c.get(Calendar.MONTH);
        day=c.get(Calendar.DAY_OF_MONTH);
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);
        second=c.get(Calendar.SECOND);
    }
    public Date(String dateString){
        try{
            String[] split=dateString.split(Pattern.quote(TextSpreadsheet.INDIVIDUALDELIMITER4));
            if(split.length==1)
                toCurrentDate();
            else
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
        return str+TextSpreadsheet.INDIVIDUALDELIMITER4+append;
    }
    public java.util.Date toDate(){
        java.util.Date d=new java.util.Date(year,month,hour,minute,second);
        return d;
    }
    private void toCurrentDate(){
        Calendar c=Calendar.getInstance();
        year=c.get(Calendar.YEAR);
        month=c.get(Calendar.MONTH);
        day=c.get(Calendar.DAY_OF_MONTH);
        hour=c.get(Calendar.HOUR_OF_DAY);
        minute=c.get(Calendar.MINUTE);
        second=c.get(Calendar.SECOND);
    }
    public static Date currentDate(){
        Date d=new Date();
        d.toCurrentDate();
        return d;
    }
    public static boolean before(Date first,Date last){
        if(last.year>first.year)
            return false;
        else if(first.year>last.year)
            return true;
        if(last.month>first.month)
            return false;
        else if(first.month>last.month)
            return true;
        if(last.day>first.day)
            return false;
        else if(first.day>last.day)
            return true;
        if(last.hour>first.hour)
            return false;
        else if(first.hour>last.hour)
            return true;
        if(last.minute>first.minute)
            return false;
        else if(first.minute>last.minute)
            return true;
        if(last.second>first.second)
            return false;
        else if(first.second>last.second)
            return true;
        return true; //exact same time (to the second)
    }
    @Override
    public String toString(){
        return month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second;
    }
    
}
