/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class InputRelayer implements Runnable{
    private File file;
    private BufferedReader stream;
    private JTerminal terminal;
    private boolean looping;
    public InputRelayer(JTerminal t){
        terminal=t;
        looping=true;
        try {
            file=new File("output.log");
            file.delete();
            file.createNewFile();
            stream=new BufferedReader(new FileReader(file));
            new Thread(this).start();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    public void stop(){
        boolean looping=false;
    }
    @Override
    public void run() {
        while(looping&&file.exists()){
            try {
                String line=stream.readLine();
                if(line!=null){
                    terminal.append(line+"\n");
                }
            } catch (IOException ex) {
                Logger.getLogger(InputRelayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
