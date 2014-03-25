/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import DropboxGrader.GuiElements.Grader.JTerminal;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class InputRelayer implements Runnable{
    private BufferedReader output;
    private BufferedReader error;
    private JTerminal terminal;
    private boolean looping;
    public InputRelayer(JTerminal t){
        terminal=t;
        looping=false;
        new Thread(this).start();
    }
    public void stop(){
        looping=false;
    }
    public void invalidate(){
        try {
            if(output!=null)
                output.close();
            if(error!=null)
                error.close();
            output=null;
            error=null;
        } catch (IOException ex) {
            Logger.getLogger(InputRelayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void changeReadProccess(InputStream out,InputStream err){
        stop();
        invalidate();
        output=new BufferedReader(new InputStreamReader(out));
        error=new BufferedReader(new InputStreamReader(err));
        looping=true;
    }
    @Override
    public void run() {
        while(true){
            while(looping&&output!=null&&error!=null){
                try {
                    String line=null;
                    int out=-2;
                    while(output.ready()&&out!=-1){
                        out=output.read();
                        if(line==null)
                            line="";
                        if(out!=-1||out!=-2)
                            line+=(char)out;
                        if(out==-2)
                            out=-1;
                    }
                    if(line!=null){
                        terminal.append(line);
                    }
                    line=null;
                    out=-2;
                    while(error.ready()&&out!=-1){
                        out=error.read();
                        if(line==null)
                            line="";
                        if(out!=-1||out!=-2)
                            line+=(char)out;
                        if(out==-2)
                            out=-1;
                    }
                    if(line!=null){
                        terminal.append(line,Color.RED);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(InputRelayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try{
                if(output!=null&&error!=null&&!looping){
                    output.close();
                    error.close();
                    output=null;
                    error=null;
                }
                Thread.sleep(1000);
            } catch (IOException|InterruptedException ex) {
                Logger.getLogger(InputRelayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
