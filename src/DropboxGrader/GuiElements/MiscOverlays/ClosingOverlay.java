/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author matt
 */
public class ClosingOverlay extends ContentOverlay{
    private Gui gui;
    
    private JLabel statusLabel;
    private JButton cancelButton;
    
    public ClosingOverlay(Gui gui){
        super("ClosingOverlay");
        this.gui=gui;
    }
    @Override
    public void setup() {
        if(getTitle().equals(""))
            setTitle("Edit Grade");
        setLayout(new GridBagLayout());
        
        statusLabel=new JLabel("Finishing Background Tasks...");
        cancelButton=new JButton("Cancel Closing");
        cancelButton.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints(); 
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=90;
        cons.fill=GridBagConstraints.NONE;
        add(statusLabel,cons);
        cons.gridy=1;
        cons.weighty=10;
        add(cancelButton,cons);
        
        setClosable(false);
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setVisible(true);
    }

    @Override
    public void switchedTo() {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(cancelButton)){
            if(gui!=null){
                gui.getBackgroundThread().setCloseAfterDone(false);
                dispose();
            }
        }
    }
    
}
