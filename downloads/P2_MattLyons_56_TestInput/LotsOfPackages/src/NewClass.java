import javax.swing.JOptionPane;
import java.util.Scanner;

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
JOptionPane.showMessageDialog(null,"Test");
        Scanner s=new Scanner(System.in);
        System.out.println("How are you?");
        System.out.println("You are "+s.nextLine());
    }
}
