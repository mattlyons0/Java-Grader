/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class GuiListener implements WindowListener{
    private Gui gui;
    private JavaRunner runner;
    public GuiListener(Gui gui){
        this.gui=gui;
    }
    public void setRunner(JavaRunner r){
        runner=r;
    }
    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if(runner!=null){
            runner.stopProcess();
            runner.getRelay().stop();
            runner.getRelay().invalidate();
        }
        if(gui.getTerminal()!=null){
            gui.getTerminal().stop();
        }
        gui.setVisible(false);

        File f1=new File("input.log");
        File f2=new File("output.log");
        if(f1.exists()||f2.exists()){
            f1.delete();
            f2.delete();
            if(f1.exists()){
                System.out.println("input.log is not being cleaned up properly.");
            }
            if(f2.exists()){
                System.out.println("output.log is not being cleaned up properly.");
            }
        }
        Config.writeConfig();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
    
}
