/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Matt
 */
public class JTerminal extends JTextArea implements KeyListener{
    private String lineTyped;
    private Gui gui;
    private PrintWriter writer;
    private File file;
    public JTerminal(Gui gui){
        super();
        this.setLineWrap(true);
        lineTyped="";
        this.gui=gui;
        addKeyListener(this);
        try {
            file=new File("input.log");
            file.delete();
            file.createNewFile();

            writer=new PrintWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(JTerminal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void append(String s){
        super.append(s);
        setCaretPosition(getText().length());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
       char c=e.getKeyChar();
       if(c==KeyEvent.VK_ENTER){
           String[] lines=getText().split("\n");
           String call=lines[lines.length-1].trim();
           try {
                writer=new PrintWriter(file);
                writer.append(call);
                writer.flush();
                writer.close();
           } catch (FileNotFoundException ex) {
               Logger.getLogger(JTerminal.class.getName()).log(Level.SEVERE, null, ex);
           }
           
       }
    }
    public void stop(){
        writer.close();
    }
}
