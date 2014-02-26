/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

/**
 *
 * @author 141lyonsm
 */
public abstract class ContentView extends JPanel implements ActionListener{
    protected String viewName;
    public ContentView(String name){
        this.viewName=name;
        
        setLayout(new GridBagLayout());
    }
    public String getViewName(){
        return viewName;
    }
    public boolean hasName(String otherName){
        return otherName.equals(viewName);
    }
    public abstract void setup();
    public abstract void switchedTo();
    @Override
    public String toString(){
        return viewName;
    }
}
