/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.Toolkit;
import java.io.File;

/**
 *
 * @author 141lyonsm
 */
public class Config {
    public static final File configFile=new File("config.cfg");
    //Preinit
    public static String spreadsheetName="APCS Period 1 Assignments";
    public static String dropboxFolder="DROPitTOme";
    public static String dropboxPeriod="P1";
    //FileBrowser
    public static String columnOrder="0,1,2,3,4";
    public static String columnWidth="75,75,75,75,75"; //75=default
    public static String sortColumn="0";
    public static String sortOrder="ASCENDING";
    
    //GradingPanel
    public static boolean autoRun=false;
    public static int runTimes=1;
    public static int dividerLocation=Toolkit.getDefaultToolkit().getScreenSize().width*2/3; //2/3rds
    
    public static void readConfig(){
        if(!configFile.exists()){
            return;
        }
        try{
            String[] vars=splitVars(DbxSession.readFromFile(configFile));
            spreadsheetName=vars[0];
            dropboxFolder=vars[1];
            dropboxPeriod=vars[2];
            columnOrder=vars[3];
            columnWidth=vars[4];
            autoRun=Boolean.parseBoolean(vars[5]);
            runTimes=Integer.parseInt(vars[6]);
            sortColumn=vars[7];
            sortOrder=vars[8];
            dividerLocation=Integer.parseInt(vars[9]);
        } catch(Exception ex){
            //cool, we got all we wanted, defaults will work for the rest.
        }
    } 
    public static void writeConfig(){
        String config=spreadsheetName;
        config=append(config,dropboxFolder);
        config=append(config,dropboxPeriod);
        config=append(config,columnOrder);
        config=append(config,columnWidth);
        config=append(config,autoRun+"");
        config=append(config,runTimes+"");
        config=append(config,sortColumn);
        config=append(config,sortOrder);
        config=append(config,dividerLocation+"");
        
        DbxSession.writeToFile(configFile, config);
    }
    private static String append(String s,String append){
        s+="\n"+append;
        return s;
    }
    private static String[] splitVars(String s){
        return s.split("\n");
    }
}
