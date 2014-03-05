/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JInternalFrame;

/**
 *
 * @author 141lyonsm
 */
public abstract class ContentOverlay extends JInternalFrame implements ActionListener{
    protected String viewName;
    public ContentOverlay(String name){
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
    //public abstract boolean shouldBeCached();
    
    @Override
    public String toString(){
        return viewName;
    }
}
