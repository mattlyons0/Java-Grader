/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.FileManagement;

import DropboxGrader.TextGrader.TextSpreadsheet;
import java.text.DecimalFormat;
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
    public Date(Date d){
        day=d.day;
        hour=d.hour;
        minute=d.minute;
        month=d.month;
        second=d.second;
        year=d.year;
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
        java.util.Date d=new java.util.Date(year-1900,month,day,hour,minute,second); //minus 1900... wow, just wow
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
        Calendar c1=Calendar.getInstance();
        c1.setTime(first.toDate());
        Calendar c2=Calendar.getInstance();
        c2.setTime(last.toDate());
        
        return c1.before(c2);
    }
    public static String differenceBefore(Date first,Date last){
        java.util.Date dt1 = first.toDate();
        java.util.Date dt2 = last.toDate();

        long diff = dt2.getTime() - dt1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        int diffInDays = (int) ((dt2.getTime() - dt1.getTime()) / (1000 * 60 * 60 * 24));

        if (diffInDays > 0) {
            return diffInDays+" Days";
        } else if (diffHours > 1) {
            return diffHours+" Hours";
        } else if (diffMinutes > 1) {
            return diffMinutes+" Minutes";
        } else if (diffSeconds > 0)
            return diffSeconds+" Seconds";
        return null;
    }
    
    @Override
    public String toString(){
        DecimalFormat format=new DecimalFormat("#00");
        DecimalFormat format2=new DecimalFormat("#0000");
        return (format.format(month+1))+"/"+format.format(day)+"/"+format2.format(year)+
                " "+format.format(hour)+":"+format.format(minute)+":"+format.format(second);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.month;
        hash = 79 * hash + this.day;
        hash = 79 * hash + this.year;
        hash = 79 * hash + this.hour;
        hash = 79 * hash + this.minute;
        hash = 79 * hash + this.second;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Date){
            Date d=(Date)obj;
            if(d.day==day&&d.hour==hour&&d.minute==minute&&d.second==second&&d.year==year)
                return true;
        }
        return false;
    }    
}
