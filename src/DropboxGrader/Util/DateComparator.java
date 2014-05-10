/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Util;

import DropboxGrader.FileManagement.Date;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 *
 * @author Matt
 */
public class DateComparator implements Comparator<String>{

    @Override
    public int compare(String o1, String o2) {
        o1=o1.replaceAll("Modified ", "");
        o2=o2.replaceAll("Modified ", "");
        
        Date d1=new Date();
        try{
            String[] segments=o1.split(" ");
            String[] dates=segments[0].split(Pattern.quote("/"));
            d1.month=Integer.parseInt(dates[0]);
            d1.day=Integer.parseInt(dates[1]);
            d1.year=Integer.parseInt(dates[2]);
            String[] times=segments[1].split(Pattern.quote(":"));
            d1.hour=Integer.parseInt(times[0]);
            d1.minute=Integer.parseInt(times[1]);
            d1.second=Integer.parseInt(times[2]);
        } catch(Exception e){
            return -1;
        }
        
        Date d2=new Date();
        try{
            String[] segments=o2.split(" ");
            String[] dates=segments[0].split(Pattern.quote("/"));
            d2.month=Integer.parseInt(dates[0]);
            d2.day=Integer.parseInt(dates[1]);
            d2.year=Integer.parseInt(dates[2]);
            String[] times=segments[1].split(Pattern.quote(":"));
            d2.hour=Integer.parseInt(times[0]);
            d2.minute=Integer.parseInt(times[1]);
            d2.second=Integer.parseInt(times[2]);
        } catch(Exception e){
            return 1;
        }
        return d1.toDate().compareTo(d2.toDate());
    }
    
}
