/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author 141lyonsm
 */
public class NameOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JButton submitButton;
    
    private String firstName;
    private String lastName;
    
    public NameOverlay(Gui gui){
        super("NameOverlay");
        this.gui=gui;
    }
    
    @Override
    public void setup() {
        if(getTitle().equals(""))
            setTitle("Edit Name");
        setLayout(new GridBagLayout());
        
        firstNameField=new JTextField(20);
        lastNameField=new JTextField(20);
        lastNameField.addActionListener(this);
        if(firstName!=null)
            firstNameField.setText(firstName);
        if(lastName!=null)
            lastNameField.setText(lastName);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints(); 
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        add(new JLabel("First Name: "),cons);
        cons.weighty=10;
        cons.gridx=1;
        add(firstNameField,cons);
        cons.weighty=1;
        cons.gridx=2;
        add(new JLabel("Last Name: "),cons);
        cons.weighty=10;
        cons.gridx=3;
        add(lastNameField,cons);
        cons.anchor=GridBagConstraints.SOUTHEAST;
        cons.gridy=1;
        add(submitButton,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setVisible(true);
    }

    @Override
    public void switchedTo() {}
    @Override
    public void isClosing(){
        actionPerformed(new ActionEvent(submitButton,0,null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(submitButton)||e.getSource().equals(lastNameField)){
            //validate data
            if(firstNameField.getText().equals("")||firstNameField.getText().equals(" ")){
                return;
            }
            if(lastNameField.getText().equals("")||lastNameField.getText().equals(" ")){
                return;
            }
            firstName=firstNameField.getText();
            lastName=lastNameField.getText();
            if(Character.isLowerCase(firstName.charAt(0))){
                firstName=Character.toUpperCase(firstName.charAt(0))+firstName.substring(1);
                firstNameField.setText(firstName);
            }
            if(Character.isLowerCase(lastName.charAt(0))){
                lastName=Character.toUpperCase(lastName.charAt(0))+lastName.substring(1);
                lastNameField.setText(lastName);
            }
            if(callback!=null){
                gui.getBackgroundThread().invokeLater(callback);
                dispose();
            }
        }
    }
    public String[] getNames(){
        return new String[]{firstName,lastName};
    }
    public void setCallback(Runnable callback){
        this.callback=callback;
    }
    public void setData(String firstName,String lastName){
        if(firstNameField!=null){
            firstNameField.setText(firstName);
            lastNameField.setText(lastName);
        }
        else{
            this.firstName=firstName;
            this.lastName=lastName;
        }
        setTitle("Edit Name: "+firstName+" "+lastName);
    }
}
