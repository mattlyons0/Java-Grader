/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.Color;
import java.awt.Insets;
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
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Matt
 */
public class JTerminal extends JTextPane implements KeyListener{
    private String lineTyped;
    private Gui gui;
    private PrintWriter writer;
    private File file;
    public JTerminal(Gui gui){
        super();
        lineTyped="";
        this.gui=gui;
        addKeyListener(this);
        EmptyBorder border=new EmptyBorder(new Insets(10,10,10,10));
        setBorder(border);
        setMargin(new Insets(5,5,5,5));
        try {
            file=new File("input.log");
            file.delete();
            file.createNewFile();

            writer=new PrintWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(JTerminal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void append(String s){
        append(s,Color.BLACK);
    }
    public void append(String s,Color c){
        addText(s,c);
    }
    private void addText(String s,Color c){
        StyleContext context= StyleContext.getDefaultStyleContext();
        AttributeSet set=context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        set=context.addAttribute(set, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int length=getDocument().getLength();
        setCaretPosition(length);
        setCharacterAttributes(set,false);
        replaceSelection(s);
        length=getDocument().getLength();
        setCaretPosition(length);
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
