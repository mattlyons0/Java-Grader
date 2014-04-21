/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.GuiElements.MiscOverlays.ClosingOverlay;
import DropboxGrader.RunCompileJava.JavaRunner;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.io.File;

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
        File inputFolder=new File("runtimeFiles/");
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
            if(gui!=null&&gui.getRunner()!=null&&gui.getRunner().isRunning()){
                boolean close=GuiHelper.yesNoDialog("There is a program running.\n"
                        + "Are you sure you want to close?");
                if(!close){
                    return;
                }
            }
            if(gui!=null&&gui.getBackgroundThread()!=null&&gui.getBackgroundThread().hasWork()){
                gui.getBackgroundThread().setCloseAfterDone(true);
                ClosingOverlay overlay=new ClosingOverlay(gui);
                gui.getViewManager().addOverlay(overlay);
                return;
            }
            
            if(runner!=null){
                runner.stopProcess();
                runner.getRelay().stop();
                runner.getRelay().invalidate();
            }
            if(gui!=null&&gui.getTerminal()!=null){
                gui.getTerminal().stop();
            }
            if(gui!=null){
                Point coords=gui.getLocationOnScreen();
                Config.screenCoordX=coords.x;
                Config.screenCoordY=coords.y;
                Dimension size=gui.getSize();
                Config.screenWidth=size.width;
                Config.screenHeight=size.height;
                Config.screenState=gui.getExtendedState();
                gui.setVisible(false);
            }

            File inputFolder=new File("runtimeFiles/");
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
        } catch(Exception ex){
            System.err.println("Error doing closing tasks, the proccess probably terminated fast.");
            ex.printStackTrace();
        }
        gui.dispose();
        System.exit(0);
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
