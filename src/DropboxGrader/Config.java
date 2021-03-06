/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import DropboxGrader.FileManagement.DbxFile;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author 141lyonsm
 */
public class Config {
    public static final File CONFIGFILE=new File("config.cfg");
    //Preinit
    public static String spreadsheetName;
    public static String dropboxFolder;
    public static int dropboxPeriod;
    public static String dropboxSpreadsheetFolder;
    public static int screenCoordX;
    public static int screenCoordY;
    public static int screenWidth;
    public static int screenHeight;
    public static int screenState;
    //FileBrowser
    public static String columnOrder;
    public static String columnWidth;
    public static String sortColumn;
    public static String sortOrder;
    public static boolean showModified;
    
    //GradingPanel
    public static boolean autoRun;
    public static int runTimes;
    public static int dividerLocation;
    public static int bottomDividerLocation;
    public static int codeSortMode;
    public static int codeSortOrder;
    
    //UnitTests
    public static String jUnitJarLocation;
    public static String jUnitHamcrestJarLocation;
    public static String jUnitTestsLocation="/JUnitTests";
    
    //Email
    public static String emailSentFrom;
    public static String emailTeacher;
    public static String emailTeacherName;
    public static boolean emailHowtoSubmit;
    public static String submitURL;
    
    //Misc
    public static String librariesLocation="/Libraries";
    public static boolean demoMode=false;
    
    public static void init(){
        //Preinit
        spreadsheetName="APCS Period 1 Assignments";
        dropboxFolder="/DROPitTOme";
        dropboxPeriod=1;
        dropboxSpreadsheetFolder="/Grades";
        screenCoordX=0;
        screenCoordY=0;
        screenWidth=(int)(Toolkit.getDefaultToolkit().getScreenSize().width*0.95);
        screenHeight=(int)(Toolkit.getDefaultToolkit().getScreenSize().height*0.9);
        screenState=JFrame.NORMAL;
        //FileBrowser
        columnOrder="0,1,2,3,4";
        columnWidth="75,75,75,75,75"; //75=default
        sortColumn="0";
        sortOrder="ASCENDING";
        showModified=true;

        //GradingPanel
        autoRun=false;
        runTimes=1;
        bottomDividerLocation=(int)(Toolkit.getDefaultToolkit().getScreenSize().height*19.0/20); //19/20ths
        dividerLocation=(int)(Toolkit.getDefaultToolkit().getScreenSize().width*2.0/3); //2/3rds
        codeSortMode=0;
        codeSortOrder=0;
        
        //UnitTests
        jUnitJarLocation="";
        jUnitHamcrestJarLocation="";
        
        //Email
        emailSentFrom="AP Computer Science Grader";
        emailTeacher=null;
        emailTeacherName=null;
        emailHowtoSubmit=true;
        submitURL=null;
    }
    public static void readConfig(){
        if(!CONFIGFILE.exists()){
            return;
        }
        try{
            String[] vars=splitVars(DbxSession.readFromFile(CONFIGFILE));
            spreadsheetName=vars[0];
            dropboxFolder=vars[1];
            dropboxPeriod=DbxFile.safeStringToInt(vars[2]);
            columnOrder=vars[3];
            columnWidth=vars[4];
            autoRun=Boolean.parseBoolean(vars[5]);
            runTimes=Integer.parseInt(vars[6]);
            sortColumn=vars[7];
            sortOrder=vars[8];
            dividerLocation=Integer.parseInt(vars[9]);
            dropboxSpreadsheetFolder=vars[10];
            codeSortMode=Integer.parseInt(vars[11]);
            codeSortOrder=Integer.parseInt(vars[12]);
            screenCoordX=Integer.parseInt(vars[13]);
            screenCoordY=Integer.parseInt(vars[14]);
            screenWidth=Integer.parseInt(vars[15]);
            screenHeight=Integer.parseInt(vars[16]);
            screenState=Integer.parseInt(vars[17]);
            jUnitJarLocation=vars[18];
            jUnitHamcrestJarLocation=vars[19];
            //jUnitTestsLocation=vars[20]; //no longer configurable
            bottomDividerLocation=Integer.parseInt(vars[21]);
            showModified=Boolean.parseBoolean(vars[22]);
            emailSentFrom=!vars[23].equals("null")?vars[23]:null;
            emailTeacher=!vars[24].equals("null")?vars[24]:null;
            emailTeacherName=!vars[25].equals("null")?vars[25]:null;
            emailHowtoSubmit=Boolean.parseBoolean(vars[26]);
            submitURL=!vars[27].equals("null")?vars[27]:null;
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
        config=append(config,autoRun);
        config=append(config,runTimes);
        config=append(config,sortColumn);
        config=append(config,sortOrder);
        config=append(config,dividerLocation);
        config=append(config,dropboxSpreadsheetFolder);
        config=append(config,codeSortMode);
        config=append(config,codeSortOrder);
        config=append(config,screenCoordX);
        config=append(config,screenCoordY);
        config=append(config,screenWidth);
        config=append(config,screenHeight);
        config=append(config,screenState);
        config=append(config,jUnitJarLocation);
        config=append(config,jUnitHamcrestJarLocation);
        config=append(config,jUnitTestsLocation); //no longer configurable, but we'll leave it in just to not screw up the order
        config=append(config,bottomDividerLocation);
        config=append(config,showModified);
        config=append(config,emailSentFrom);
        config=append(config,emailTeacher);
        config=append(config,emailTeacherName);
        config=append(config,emailHowtoSubmit);
        config=append(config,submitURL);
        
        DbxSession.writeToFile(CONFIGFILE, config);
    }
    private static String append(String string,String append){
        string+="\n"+append;
        return string;
    }
    private static String append(String string,Object append){
        return append(string,append.toString());
    }
    private static String[] splitVars(String s){
        return s.split("\n");
    }
    public static void reset(){
        init();
        writeConfig();
        GuiHelper.alertDialog("Corrupt Config was detected.\nAll settings have been reset.");
    }
}
