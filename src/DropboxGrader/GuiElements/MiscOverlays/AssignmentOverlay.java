/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiHelper;
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
 * @author Matt
 */
public class AssignmentOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField assignmentNumField;
    private JTextField assignmentNameField;
    private JButton submitButton;
    
    private Integer assignmentNum;
    private String assignmentName;
    public AssignmentOverlay(Gui gui) {
        super("AssignmentOverlay");
        this.gui=gui;
    }

    @Override
    public void setup() {
        if(getTitle().equals(""))
            setTitle("Edit Assignment");
        setLayout(new GridBagLayout());
        
        assignmentNumField=new JTextField(10);
        assignmentNumField.addActionListener(this);
        assignmentNameField=new JTextField(20);
        assignmentNameField.addActionListener(this);
        if(assignmentNum!=null)
            assignmentNumField.setText(assignmentNum+"");
        if(assignmentName!=null)
            assignmentNameField.setText(assignmentName);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints(); 
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        add(new JLabel("Number: "),cons);
        cons.weighty=10;
        cons.gridx=1;
        add(assignmentNumField,cons);
        cons.weighty=1;
        cons.gridx=2;
        add(new JLabel("Name: "),cons);
        cons.weighty=10;
        cons.gridx=3;
        add(assignmentNameField,cons);
        cons.anchor=GridBagConstraints.SOUTHEAST;
        cons.gridy=1;
        add(submitButton,cons);
        
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
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
        if(e.getSource().equals(submitButton)||e.getSource().equals(assignmentNumField)||e.getSource().equals(assignmentNameField)){
            //validate data
            try{
                assignmentNum=Integer.parseInt(assignmentNumField.getText().replace(" ", ""));
            } catch(NumberFormatException ex){
                GuiHelper.alertDialog("Assignment Number must be a number.");
                return;
            }
            assignmentName=assignmentNameField.getText();
            
            if(callback!=null){
                gui.getBackgroundThread().invokeLater(callback);
                dispose();
            }
        }
    }
    public Object[] getData(){
        return new Object[]{assignmentNum,assignmentName};
    }
    public void setCallback(Runnable callback){
        this.callback=callback;
    }
    public void setData(int number,String name){
        if(assignmentNameField!=null){
            assignmentNumField.setText(number+"");
            assignmentNameField.setText(name);
        }
        else{
            assignmentNum=number;
            assignmentName=name;
        }
        setTitle("Edit Assignment: "+number+" "+name);
    }
    
}
