/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.io.File;

/**
 *
 * @author 141lyonsm
 */
public class Config {
    public static final File configFile=new File("config.cfg");
    //Preinit
    public static String spreadsheetName="APCS PERIOD 4 ASSIGNMENTS";
    public static String dropboxFolder="DROPitTOme";
    public static String dropboxPeriod="P2";
    //FileBrowser
    public static String columnOrder="0,1,2,3,4";
    public static String columnWidth="75,75,75,75,75"; //75=default
    public static String sortColumn="-1";
    public static String sortOrder="UNSORTED";
    
    //GradingPanel
    public static boolean autoRun=false;
    public static int runTimes=1;
    
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
        } catch(ArrayIndexOutOfBoundsException ex){
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
