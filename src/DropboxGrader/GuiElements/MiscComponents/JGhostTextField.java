/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscComponents;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

/**
 *
 * @author matt
 */
public class JGhostTextField extends JTextField implements FocusListener{
    private String ghostString;
    private Color regularColor;
    private final Color ghostColor=Color.GRAY;
    public JGhostTextField(int length,String ghostString){
        super(length);
        this.ghostString=ghostString;
        setText(ghostString);
        regularColor=getForeground();
        setForeground(ghostColor);
        setHorizontalAlignment(JTextField.CENTER);
        setToolTipText(ghostString);
        addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(super.getText().equals(ghostString)&&getForeground().equals(ghostColor)){
            setText("");
            setForeground(regularColor);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(getText().replaceAll(" ","").equals("")){
            setText(ghostString);
            setForeground(ghostColor);
        }
    }
    @Override
    public String getText(){
        if(super.getText().equals(ghostString)){
            return "";
        }
        return super.getText();
    }
    @Override
    public void setText(String text){
        if(!getForeground().equals(regularColor)){
            setForeground(regularColor);
        }
        super.setText(text);
    }
}
