/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.MiscComponents.DoubleDocument;
import DropboxGrader.GuiHelper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 *
 * @author matt
 */
public class GradeOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField gradeField;
    private JTextPane commentField;
    
    private Double grade;
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
        gradeField.setDocument(new DoubleDocument());
        gradeField.addActionListener(this);
        commentField=new JTextPane();
        JScrollPane scrollComment=new JScrollPane(commentField);
        scrollComment.setMinimumSize(new Dimension(200,100));
        if(grade!=null)
            gradeField.setText(grade+"");
        if(comment!=null)
            commentField.setText(comment);
        
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
        add(scrollComment,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setVisible(true);
    }

    @Override
    public void switchedTo() {}
    @Override
    public boolean isClosing(){
        return save();
    }
    private boolean save(){
        //validate data
        if(gradeField.getText().equals("")&&commentField.getText().equals(""))// nothin entered
            return true;
        if(gradeField.getText().replaceAll(" ","").equals("")){
            gradeField.setText("");
            GuiHelper.alertDialog("Grade cannot be empty.");
            return false;
        }
        grade=Double.parseDouble(gradeField.getText());
        comment=commentField.getText();

        if(callback!=null){
            gui.getBackgroundThread().invokeLater(callback);
        }
        return true;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(gradeField)||e.getSource().equals(commentField)){
            if(save())
                dispose();
        }
    }
    public Object[] getData(){
        return new Object[]{grade,comment};
    }
    public void setCallback(Runnable callback){
        this.callback=callback;
    }
    public void setData(Double grade,String comment,String name,int assignment){
        if(gradeField!=null){
            gradeField.setText(grade+"");
            commentField.setText(comment);
        }
        else{
            this.grade=grade;
            this.comment=comment;
        }
        setTitle("Edit "+name+" Assignment "+assignment);
    }
    
}
