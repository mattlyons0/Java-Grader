/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.RunCompileJava.JavaRunner;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class GuiListener implements WindowListener,ComponentListener,WindowStateListener{
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
        File inputFolder=new File("runtimeFiles\\");
        File[] inputFiles=inputFolder.listFiles();
        if(inputFiles!=null){
            for(File f:inputFiles){
                f.delete();
            }
        }
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try{
            if(runner!=null){
                runner.stopProcess();
                runner.getRelay().stop();
                runner.getRelay().invalidate();
            }
            if(gui!=null&&gui.getTerminal()!=null){
                gui.getTerminal().stop();
            }
            if(gui!=null)
                gui.setVisible(false);

            File inputFolder=new File("runtimeFiles\\");
            File[] inputFiles=inputFolder.listFiles();
            if(inputFiles!=null){
                for(File f:inputFiles){
                    f.delete();
                }
            }
            inputFolder.delete();
            if(gui!=null)
                gui.isClosing();

            Config.writeConfig();

            //delete downloads
            File downloads=new File("downloads\\");
            File[] downloaded=downloads.listFiles();
            if(downloaded!=null){
                for(File f:downloaded){
                    f.delete();
                }
            }
        } catch(Exception ex){
            System.err.println("Error doing closing tasks, someone probably terminated the proccess really fast.\n "+ex);
            ex.printStackTrace();
        }
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

    @Override
    public void componentResized(ComponentEvent e) {
        //gui.resized();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void windowStateChanged(WindowEvent e) {
    }
    
}
