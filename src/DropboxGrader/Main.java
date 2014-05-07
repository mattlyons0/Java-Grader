/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import DropboxGrader.Util.SplitStream;
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
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if(UIManager.getSystemLookAndFeelClassName().equals("javax.swing.plaf.metal.MetalLookAndFeel")){
                if(System.getProperty("os.name").contains("Linux")){
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        try {
            File errLog=new File("error.log");
            errLog.createNewFile();
            PrintStream s=new PrintStream(errLog);
            SplitStream es=new SplitStream(s,System.err);  
            System.setErr(new PrintStream(es));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Config.init();
        Config.readConfig();
        Gui gui=new Gui();
    }
}
