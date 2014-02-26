/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import DropboxGrader.Gui;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author 141lyonsm
 */
public class AuthView extends ContentView{
    private Gui gui;
    
    private JLabel status;
    private JTextField keyField;
    private JButton submitButton;
    
    public AuthView(Gui gui){
        super("AuthView");
        this.gui=gui;
    }
    
    @Override
    public void setup() {
        setLayout(new FlowLayout());
        status=new JLabel("Connecting to Dropbox...");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(status);
    }
    public void promptKey(){
        status.setText("Please login and paste the code here: ");
        keyField=new JTextField(30);
        keyField.addActionListener(this);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        add(keyField);
        add(submitButton);

        revalidate();
    }
    public void badKey(){
        if(!status.getText().contains("Invalid Key, "))
        status.setText("Invalid Key, "+status.getText());
    }
    public void goodKey(String loginName){
        status.setText("Logged in as "+loginName+". Building File Directory...");
        if(keyField!=null){
            remove(keyField);
            keyField=null;
        }
        if(submitButton!=null){
            remove(submitButton);
            submitButton=null;
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(submitButton)){
            if(gui.getManager()==null){
                gui.getDbxSession().setKey(keyField.getText());
            }
        }
        else if(e.getSource().equals(keyField)){ //return throws the action event
            actionPerformed(new ActionEvent(submitButton,0,""));
        }
    }    

    @Override
    public void switchedTo() {

    }
}
