/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.event.ActionEvent;
import javax.swing.JLabel;

/**
 *
 * @author 141lyonsm
 */
public class NameOverlay extends ContentOverlay{
    public NameOverlay(){
        super("NameOverlay");
    }
    @Override
    public void setup() {
        add(new JLabel("hello"));
        setTitle("Edit Name");
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        setSize(100,100);
        setVisible(true);
    }

    @Override
    public void switchedTo() {
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
    }
    
}
