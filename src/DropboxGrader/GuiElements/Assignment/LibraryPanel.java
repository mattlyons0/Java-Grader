/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.Assignment;

import DropboxGrader.Gui;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Handles the gui for adding a library to be run with an assignment
 * @author 141lyonsm
 */
public class LibraryPanel extends JPanel{
    private Gui gui;
    
    private ArrayList<JButton> browseButtons;
    private ArrayList<JLabel> statuses;
    
    public LibraryPanel(Gui gui){
        this.gui=gui;
        
        browseButtons=new ArrayList();
        statuses=new ArrayList();
    }
}
