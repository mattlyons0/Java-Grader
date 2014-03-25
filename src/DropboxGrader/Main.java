/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import DropboxGrader.RunCompileJava.RelayStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 *
 * @author 141lyonsm
 */
public class Main {
    public static void main(String[] args) {
        //Use operating system look. Basically makes it look better overall. Metal theme sucks
        try { 
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //uncommenting this causes the overlays to make the rest of the screen black when they are exited
//            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        
        try {
            File errLog=new File("error.log");
            errLog.createNewFile();
            PrintStream s=new PrintStream(errLog);
            SplitStream es=new SplitStream(s,System.err);  
            System.setErr(es);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Config.init();
        Config.readConfig();
        Gui gui=new Gui();
    }
}
