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
 * @author matt
 */
public class GradeOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField gradeField;
    private JTextField commentField;
    private JButton submitButton;
    
    private String grade;
    private String comment;
    
    public GradeOverlay(Gui gui){
        super("GradeOverlay");
        this.gui=gui;
    }
    @Override
    public void setup() {
        if(getTitle().equals(""))
            setTitle("Edit Grade");
        setLayout(new GridBagLayout());
        
        gradeField=new JTextField(10);
        gradeField.addActionListener(this);
        commentField=new JTextField(20);
        commentField.addActionListener(this);
        if(grade!=null)
            gradeField.setText(grade);
        if(comment!=null)
            commentField.setText(comment);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints(); 
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        add(new JLabel("Grade: "),cons);
        cons.weighty=10;
        cons.gridx=1;
        add(gradeField,cons);
        cons.weighty=1;
        cons.gridx=2;
        add(new JLabel("Comment: "),cons);
        cons.weighty=10;
        cons.gridx=3;
        add(commentField,cons);
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
        if(e.getSource().equals(submitButton)||e.getSource().equals(gradeField)||e.getSource().equals(commentField)){
            //validate data
            grade=gradeField.getText();
            comment=commentField.getText();
            if(grade.replaceAll(" ","").equals("")){
                gradeField.setText("");
                GuiHelper.alertDialog("Grade cannot be empty.");
                return;
            }
            
            if(callback!=null){
                gui.getBackgroundThread().invokeLater(callback);
                dispose();
            }
        }
    }
    public String[] getData(){
        return new String[]{grade,comment};
    }
    public void setCallback(Runnable callback){
        this.callback=callback;
    }
    public void setData(String grade,String comment,String name,int assignment){
        if(gradeField!=null){
            gradeField.setText(grade);
            commentField.setText(comment);
        }
        else{
            this.grade=grade;
            this.comment=comment;
        }
        setTitle("Edit "+name+" Assignment "+assignment);
    }
    
}
