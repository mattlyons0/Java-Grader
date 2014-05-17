/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.MiscComponents.IntegerDocument;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author Matt
 */
public class ChangeAssignmentOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField assignmentField;
    private JTextField assignmentNameField;
    
    public ChangeAssignmentOverlay(Gui gui){
        super("ChangeAssignmentOverlay");
        
        this.gui=gui;
    }
    @Override
    public void setup() {
        assignmentField=new JTextField(5);
        assignmentField.setDocument(new IntegerDocument());
        assignmentField.addActionListener(this);
        assignmentNameField=new JTextField(15);
        assignmentNameField.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        add(new JLabel("Submitted Assignment Number"),cons);
        cons.gridx++;
        add(new JLabel("Submitted Assignment Name"),cons);
        cons.gridy++;
        cons.gridx=0;
        add(assignmentField,cons);
        cons.gridx++;
        add(assignmentNameField,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setVisible(true);
    }

    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        save();
        return true;
    }

    public void setCallback(Runnable r){
        callback=r;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(assignmentField)||e.getSource().equals(assignmentNameField)){
            save();
            dispose();
        }
    }
    public Integer getAssignment(){
        if(assignmentField.getText().equals(""))
            return null;
        return Integer.parseInt(assignmentField.getText());
    }
    public String getAssignmentName(){
        if(assignmentNameField.getText().replaceAll(" ","").equals(""))
            return null;
        return assignmentNameField.getText();
    }
    public void save(){
        if(callback!=null)
            gui.getBackgroundThread().invokeLater(callback);
    }
    
}
