/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

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
        setup();
    }
    public String getViewName(){
        return viewName;
    }
    public boolean hasName(String otherName){
        if(otherName.equals(viewName)){
            return true;
        }
        return false;
    }
    public abstract void setup();
    
    @Override
    public String toString(){
        return viewName;
    }
}
