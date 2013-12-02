/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author 141lyonsm
 */
public class TestInject {
    private ArrayList<String> coins;
    
    /**
     * Constructs an empty purse
     */
    public TestInject() {
        coins = new ArrayList();
    }
    
    /**
     * Constructs a new purse with a coin in it
     * @param newCoin The first coin in the purse
     */
    public TestInject(String newCoin) {
        coins = new ArrayList();
        (this.coins).add(newCoin);
    }
    
    /**
     * Adds a new coin to the purse
     * @param newCoin The next coin in the purse
     */
    public void addCoin(String newCoin) {
        (this.coins).add(newCoin);
    }
    
    /**
     * Allows you to Print out the reversed contents of the purse
     * @return A string containing the reversed contents of the purse
     */
    public String reverse() {
        ArrayList<String> pReverse = new ArrayList();
        for(int i=0; i<(this.coins).size(); i++) {
            pReverse.add(i, (this.coins).get(this.coins.size()-1-i));
        }
        return pReverse.toString();
    }
    
    /**
     * Transfers the contents of a purse to the purse on which the method
     * was called, leaving the other purse empty and leaving the third purse
     * quite confused as to where the fourth purse went.
     * @param other The purse from which the transfer is made
     */
    public void transfer(TestInject other) {
        for(int i=0; i<other.coins.size(); i++) {
            this.coins.add(other.coins.get(i));
        }
        other.coins = new ArrayList <String>();
        other.coins.add("");
    }
    
    /**
     * Checks if two purses have the same contents, regardless of order
     * @param other The "other" purse to check.
     * @return True/False depending on whether they have the same coins or not
     */
    public boolean sameCoins(TestInject other) {
        ArrayList<String> P1Compare = copy(this.coins);
        ArrayList<String> P2Compare = copy(other.coins);
        for(int i=0; i<P1Compare.size(); i++) {
            if(!P1Compare.isEmpty() && !P2Compare.isEmpty() && 
                    P2Compare.get(0).equalsIgnoreCase(P1Compare.get(i))) {
                P2Compare.remove(0);
                P1Compare.remove(i);
                i = -1; //start checking again
            }
        }
        if(P1Compare.isEmpty() && P1Compare.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private static ArrayList<String> copy(ArrayList<String> x) {
        ArrayList<String> newAL = new ArrayList<String>();
        for(int i=0; i<x.size(); i++) {
            newAL.add(x.get(i));
        }
        return newAL;
    }
    
    /**
     * Creates a string detailing the contents of this purse
     * @return The contents of the purse in a string
     */
    public String toString() {
        String contents = "TestInject[";
        for (int i=0; i<this.coins.size(); i++) {
            contents += this.coins.get(i) + ",";
        }
        return contents.substring(0,contents.lastIndexOf(",")) + "]";
    }
    
    /**
     * The main class
     * @param args Who knows?
     */
    public static void main(String[] args) {
	//DROPBOXGRADERCODESTART
        java.io.FileInputStream iDropbox=null;
        java.io.PrintStream printDropbox=null;
        try{	
            int x=0;
            java.io.File f=new java.io.File("inputFiles\\input"+x+".log");
            while(f.exists()){
                f=new java.io.File("inputFiles\\input"+x+".log");
                x++;
            }
            x-=2;
            f=new java.io.File("inputFiles\\input"+x+".log");
            iDropbox=new java.io.FileInputStream(f){
            //int runNum=0; //requires everything to be written twice, for stupid reasons.
                @Override
                public int read(byte[] b, int off, int len) throws java.io.IOException {
                    int read=super.read(b, off, len);
                    while(read==-1){ //every 2nd call is for caching and doesnt matter
                        read=super.read(b, off, len);
                    }
                    return read;
                }
            };
        System.setIn(iDropbox);
        printDropbox=new java.io.PrintStream(new java.io.FileOutputStream("inputFiles\\output"+x+".log"));
        System.setOut(printDropbox);
        System.setErr(printDropbox);
        } catch(java.io.IOException e){
            System.out.println("Injection code has failed. "+e);
        }
        //DROPBOXGRADERCODEEND

        Scanner reader = new Scanner(System.in);
        TestInject purse1 = new TestInject();
        System.out.println("First purse (enter \"next\" to proceed)");
        String nextCoin = "";
        nextCoin = reader.nextLine();
        while(!nextCoin.equalsIgnoreCase("next")) {
            purse1.addCoin(nextCoin);
            nextCoin = reader.nextLine();
            System.out.println("You put in "+nextCoin);
        }
        TestInject purse2 = new TestInject();
        System.out.println("Second purse (enter \"quit\" to finish)");
        nextCoin = "";
        nextCoin = reader.nextLine();
        while(!nextCoin.equalsIgnoreCase("Quit")) {
            purse2.addCoin(nextCoin);
            nextCoin = reader.nextLine();
            System.out.println("You put in "+nextCoin);
        }
        System.out.println("Contents of purse1: " + purse1);
        System.out.println("Contents of purse2: " + purse2);
        System.out.println("pusre1 and purse2 have the same contents: " + 
                purse1.sameCoins(purse2));
        System.out.println("Transferring from 2 to 1");
        purse1.transfer(purse2);
        System.out.println("Contents of purse1: " + purse1);
        System.out.println("Contents of purse2: " + purse2);
        System.out.println("That final purse flipped is " + purse1.reverse());
    }
}
