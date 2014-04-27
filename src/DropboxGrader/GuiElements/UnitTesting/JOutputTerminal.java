/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Matt
 */
public class JOutputTerminal extends JTextPane{
    private boolean errors;
    
    public JOutputTerminal(){
        super();
    }    
    public void append(String s,Color c){
        if(c.equals(Color.RED))
            errors=true;
        StyleContext context= StyleContext.getDefaultStyleContext();
        AttributeSet set=context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        StyledDocument doc=getStyledDocument();
        try {
            doc.insertString(0, s, set);
        } catch (BadLocationException ex) {
            Logger.getLogger(JOutputTerminal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean errorsOccured(){
        return errors;
    }
}
