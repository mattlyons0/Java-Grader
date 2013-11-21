import java.io.IOException;
import java.util.Arrays;
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
            java.io.PrintStream printStream=new java.io.PrintStream(new java.io.FileOutputStream("output.log"));
            System.setOut(printStream);
            System.setErr(printStream);
            java.io.File f=new java.io.File("input.log");
            int runNum=0;
            System.setIn(new java.io.FileInputStream(f){
                private int runNum=0;
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int read=super.read(b, off, len);
                    while(runNum%2==0&&read==-1){ //every 2nd call is for caching and doesnt matter
                        read=super.read(b, off, len);
                    }
                    runNum++;
                    return read;
                }
            });
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
