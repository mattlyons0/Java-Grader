/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
    private InputStream inStream;
    public JTerminal(Gui gui){
        super();
        lineTyped="";
        this.gui=gui;
        addKeyListener(this);
    }
    @Override
    public void append(String s){
        super.append(s);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c=e.getKeyChar();
        if(c==KeyEvent.VK_ENTER){
            if(inStream!=null){
                try {
                    inStream.read(lineTyped.getBytes());
                } catch (IOException ex) {
                    Logger.getLogger(JTerminal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            lineTyped="";
        }
        else if(c==KeyEvent.VK_BACK_SPACE){
            lineTyped=lineTyped.substring(0, lineTyped.length()-1);
        }
        else if(!Character.isISOControl(c)){
            lineTyped+=e.getKeyChar();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    public void setInputStream(InputStream i){
        inStream=i;
    }
}
