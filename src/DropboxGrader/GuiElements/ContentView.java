/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.event.ActionListener;
import javax.swing.JPanel;

/**
 *
 * @author 141lyonsm
 */
public abstract class ContentView implements ActionListener{
    protected String name;
    public ContentView(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public boolean hasName(String otherName){
        if(otherName.equals(name)){
            return true;
        }
        return false;
    }
    public abstract JPanel getContent();
    
    @Override
    public String toString(){
        return name;
    }
}
