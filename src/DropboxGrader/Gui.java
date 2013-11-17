/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import javax.swing.JFrame;

/**
 *
 * @author Matt
 */
public class Gui extends JFrame{
    private FileManager fileManager;
    
    public Gui(FileManager fileMan){
        super("Dropbox Grader");
        fileManager=fileMan;
        
        init();
    }
    private void init(){
        setVisible(true);
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setSize(500,500);
    }
}
