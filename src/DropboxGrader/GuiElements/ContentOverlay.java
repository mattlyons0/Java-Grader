/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.EventListener;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 *
 * @author 141lyonsm
 */
public abstract class ContentOverlay extends JInternalFrame implements ActionListener,InternalFrameListener{
    protected String viewName;
    private boolean cached=false;
    private long id;
    public ContentOverlay(String name){
        super(name,true,true,true,false);
        this.viewName=name;
        
        setLayout(new GridBagLayout());
        addInternalFrameListener(this);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
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
    public abstract void isClosing();
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
    
    @Override
    public void internalFrameOpened(InternalFrameEvent ife){}

    @Override
    public void internalFrameClosing(InternalFrameEvent ife){
        isClosing();
        dispose();
        removeInternalFrameListener(this);
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent ife){}

    @Override
    public void internalFrameIconified(InternalFrameEvent ife){}

    @Override
    public void internalFrameDeiconified(InternalFrameEvent ife){}

    @Override
    public void internalFrameActivated(InternalFrameEvent ife){}

    @Override
    public void internalFrameDeactivated(InternalFrameEvent ife){}
}
