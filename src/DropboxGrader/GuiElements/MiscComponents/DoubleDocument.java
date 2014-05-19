/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscComponents;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Matt
 */
public class DoubleDocument extends PlainDocument{
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        for(int i=0;i<str.length();i++){
            char c=str.charAt(i);
            if(Character.isDigit(c)||c=='.'){
                if(c=='.'){
                    if(getText().contains(".")){
                        return;
                    }
                }
                super.insertString(offs+i, c+"", a);
            }
        }
    }
    public String getText(){
        try {
            return getText(0,getLength());
        } catch (BadLocationException ex) {
            return null;
        }
    }
}
