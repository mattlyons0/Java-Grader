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
    public static String spreadsheetName;
    public static String dropboxFolder;
    public static String dropboxPeriod;
    //FileBrowser
    public static String columnOrder;
    public static String columnWidth;
    public static String sortColumn;
    public static String sortOrder;
    
    //GradingPanel
    public static boolean autoRun;
    public static int runTimes;
    public static int dividerLocation;
    public static void init(){
        //Preinit
        spreadsheetName="APCS Period 1 Assignments";
        dropboxFolder="DROPitTOme";
        dropboxPeriod="P1";
        //FileBrowser
        columnOrder="0,1,2,3,4";
        columnWidth="75,75,75,75,75"; //75=default
        sortColumn="0";
        sortOrder="ASCENDING";

        //GradingPanel
        autoRun=false;
        runTimes=1;
        dividerLocation=Toolkit.getDefaultToolkit().getScreenSize().width*2/3; //2/3rds
    }
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
    public static void reset(){
        init();
        writeConfig();
    }
}
