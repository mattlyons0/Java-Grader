/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.Grader;

import DropboxGrader.Gui;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.OutputStream;
import java.io.PrintWriter;
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
    private Gui gui;
    private PrintWriter writer;
    public JTerminal(Gui gui){
        super();
        this.gui=gui;
        addKeyListener(this);
        setMargin(new Insets(5,5,5,5));
    }
    public void setInputStream(OutputStream out){
        if(writer!=null){
            writer.close();
            writer=null;
        }
        writer=new PrintWriter(out);
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
           if(writer==null){
               return;
           }
           writer.append(call+"\n");
           writer.flush();
           //writer.write("");
           //writer.flush();
       }
    }
    public void stop(){
        if(writer!=null)
            writer.close();
    }
}
