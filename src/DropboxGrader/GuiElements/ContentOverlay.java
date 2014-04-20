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
    private boolean cached=false;
    private long id;
    public ContentOverlay(String name){
        super(name,true,true,true,false);
        this.viewName=name;
        
        setLayout(new GridBagLayout());
    }
    public ContentOverlay(String name,boolean cached){
        this(name);
        this.cached=cached;
    }
    public String getViewName(){
        return viewName;
    }
    public boolean hasName(String otherName){
        return otherName.equals(viewName);
    }
    public abstract void setup();
    public abstract void switchedTo();
    public boolean shouldBeCached(){
        return cached;
    }
    public long getID(){
        return id;
    }
    public void setID(long id){
        this.id=id;
    }
    @Override
    public String toString(){
        return viewName;
    }
}
