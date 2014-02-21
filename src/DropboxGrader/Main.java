/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author 141lyonsm
 */
public class Main {
    public static void main(String[] args) {
        //Use operating system look. Basically makes it look better overall. Metal theme sucks
//        try { 
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
        try {
            File errLog=new File("error.log");
            errLog.createNewFile();
            PrintStream s=new PrintStream(errLog);
            //System.setErr(s); //log exceptions to error.log file.
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Config.init();
        Config.readConfig();
        Gui gui=new Gui();
    }
}
