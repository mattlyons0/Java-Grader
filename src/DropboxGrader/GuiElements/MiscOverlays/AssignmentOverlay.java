/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.UnitTesting.UnitTestPanel;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Matt
 */
public class AssignmentOverlay extends ContentOverlay{
    private Gui gui;
    private Runnable callback;
    
    private JTextField assignmentNumField;
    private JTextField assignmentNameField;
    private JTextField assignmentPointsField;
    private UnitTestPanel unitTestPanel;
    private JScrollPane unitTestScroll;
    
    private Integer assignmentNum;
    private String assignmentName;
    private Double assignmentPoints;
    private UnitTest[] tests;
    private String[] jtests;
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
        assignmentPointsField=new JTextField(5);
        assignmentPointsField.addActionListener(this);
        assignmentPointsField.setDocument(new PlainDocument(){
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                for(int i=0;i<str.length();i++){
                    char c=str.charAt(i);
                    if(Character.isDigit(c)||c=='.'){
                        if(c=='.'){
                            if(assignmentPointsField.getText().contains(".")){
                                return;
                            }
                        }
                        super.insertString(offs, str, a);
                    }
                }
            }
        });
        if(assignmentNum!=null)
            assignmentNumField.setText(assignmentNum+"");
        if(assignmentName!=null)
            assignmentNameField.setText(assignmentName);
        if(assignmentPoints!=null){
            assignmentPointsField.setText(assignmentPoints+"");
        }
        if(unitTestPanel==null){
            unitTestPanel=new UnitTestPanel(tests,jtests,assignmentName,assignmentNum,gui);
        }
        unitTestScroll=new JScrollPane(unitTestPanel);
        
        GridBagConstraints cons=new GridBagConstraints(); 
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        add(new JLabel("Number: "),cons);
        cons.weightx=10;
        cons.gridx=1;
        add(assignmentNumField,cons);
        cons.weightx=1;
        cons.gridx=2;
        add(new JLabel("Name: "),cons);
        cons.weightx=10;
        cons.gridx=3;
        add(assignmentNameField,cons);
        cons.gridx=4;
        cons.weightx=1;
        add(new JLabel("Total Points: "),cons);
        cons.gridx=5;
        cons.weightx=10;
        add(assignmentPointsField,cons);
        
        cons.fill=GridBagConstraints.BOTH;
        cons.weighty=98;
        cons.weightx=1;
        cons.gridy=1;
        cons.gridx=0;
        cons.gridwidth=6;
        add(unitTestScroll,cons);
        
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
        try{
            assignmentNum=Integer.parseInt(assignmentNumField.getText().replace(" ", ""));
        } catch(NumberFormatException ex){
            GuiHelper.alertDialog("Assignment Number must be a number.");
            return false;
        }
        assignmentName=assignmentNameField.getText();
        if(assignmentPointsField.getText().equals("")){
            GuiHelper.alertDialog("Total points need to be specified.");
            return false;
        }
        assignmentPoints=Double.parseDouble(assignmentPointsField.getText());
        unitTestPanel.save();

        if(callback!=null){
            gui.getBackgroundThread().invokeLater(callback);
        }
        if(unitTestPanel.hasTests())
            gui.getTestManager().test();
        return true;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(assignmentNumField)||e.getSource().equals(assignmentNameField)||
                e.getSource().equals(assignmentPointsField)){
            if(save())
                dispose();
        }
    }
    public Object[] getData(){
        return new Object[]{assignmentNum,assignmentName,assignmentPoints};
    }
    public UnitTest[] getUnitTest(){
        if(unitTestPanel!=null)
            return unitTestPanel.getUnitTest();
        return tests;
    }
    public String[] getJUnitTests(){
        if(unitTestPanel!=null)
            return unitTestPanel.getJUnitTests();
        return jtests;
    }
    public void setCallback(Runnable callback){
        this.callback=callback;
    }
    public void setData(TextAssignment assign){
        if(assignmentNameField!=null){
            assignmentNumField.setText(assign.number+"");
            assignmentNameField.setText(assign.name);
            assignmentPointsField.setText(assign.totalPoints+"");
        }
        else{
            assignmentNum=assign.number;
            assignmentName=assign.name;
            assignmentPoints=assign.totalPoints;
        }
        tests=assign.simpleUnitTests;
        if(unitTestPanel!=null)
            unitTestPanel.setUnitTests(tests);
        jtests=assign.junitTests;
        if(unitTestPanel!=null)
            unitTestPanel.setJUnitTests(jtests);
        setTitle("Edit Assignment: "+assign.number+" "+assign.name);
    }
    
}
