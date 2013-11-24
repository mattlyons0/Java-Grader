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
    
    //GradingPanel
    public static boolean autoRun=false;
    public static int runTimes=1;
    
    public static void readConfig(){
        
    } 
    public static void writeConfig(){
        
    }    
}
