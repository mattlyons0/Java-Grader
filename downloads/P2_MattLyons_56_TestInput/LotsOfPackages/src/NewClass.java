import javax.swing.JOptionPane;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Matt
 */
public class NewClass {
    public static void main(String[] args) {
        try {
            //DROPBOXGRADERCODESTART
            System.setOut(new java.io.PrintStream(new java.io.FileOutputStream("output.log")));
            //DROPBOXGRADERCODEEND
            JOptionPane.showMessageDialog(null,"Test");
            Scanner s=new Scanner(System.in);
            System.out.println("How are you?");
            System.out.println("You are "+s.nextLine());
        } catch (java.io.FileNotFoundException ex) {
            Logger.getLogger(NewClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
