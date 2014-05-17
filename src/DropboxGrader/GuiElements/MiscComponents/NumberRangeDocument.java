/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscComponents;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 *
 * @author matt
 */
public class NumberRangeDocument extends PlainDocument{
    private int min;
    private int max;
    private JTextField field;
    private Color regularColor;
    public NumberRangeDocument(int min,int max,JTextField field){
        this.min=min;
        this.max=max;
        this.field=field;
        regularColor=field.getForeground();
    }
    @Override
    public void insertString(int offset, String string, AttributeSet a) throws BadLocationException {
        if(field.getForeground().equals(regularColor)){ //adds support for ghost field
            String text=field.getText();
            for(int i=0;i<string.length();i++){
                char c=string.charAt(i);
                if(!Character.isDigit(c)){
                    return;
                }
            }
            if(offset>0)
                text=text.substring(0,offset)+string+(text.length()>offset?text.substring(offset):"");
            else
                text=string+text;
            int sum=Integer.parseInt(text);
            if(sum>=max||sum<=min)
                return;
        }
        super.insertString(offset, string, a); //To change body of generated methods, choose Tools | Templates.
    }
}
