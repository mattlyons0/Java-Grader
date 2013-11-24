/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class Main {
    public static void main(String[] args) {
        try {
            PrintStream s=new PrintStream(new File("error.log"));
            //System.setErr(s); //log exceptions to error.log file.
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Gui gui=new Gui();
    }
}
