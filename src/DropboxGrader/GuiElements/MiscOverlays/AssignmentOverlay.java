/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.UnitTesting.UnitTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private JPanel unitTestPanel;
    private JScrollPane unitTestScroll;
    private JButton submitButton;
    
    private JButton addTestsButton;
    private JTextField testMethodName;
    private JTextField testReturnType;
    private JTextField testArgumentTypes;
    private JTextField testArguments;
    private JTextField testExpectedValue;
    private JButton removeTestsButton;
    
    private Integer assignmentNum;
    private String assignmentName;
    private Double assignmentPoints;
    private UnitTest unitTest;
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
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        if(unitTestPanel==null){
            unitTestPanel=new JPanel();
            unitTestPanel.setLayout(new GridBagLayout());
        }
        unitTestScroll=new JScrollPane(unitTestPanel);
        setupTestsGui();
        
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
        cons.gridy=1;
        cons.gridx=0;
        cons.gridwidth=6;
        add(unitTestScroll,cons);
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.SOUTHEAST;
        cons.gridy=2;
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
    private void setupTestsGui(){
        if(unitTestPanel==null){
            unitTestPanel=new JPanel();
            unitTestPanel.setLayout(new GridBagLayout());
        }
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.fill=GridBagConstraints.NONE;
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        if(unitTest==null){
            if(testMethodName!=null){
                unitTestPanel.remove(testMethodName);
                testMethodName=null;
                unitTestPanel.remove(testReturnType);
                testReturnType=null;
                unitTestPanel.remove(testArgumentTypes);
                testArgumentTypes=null;
                unitTestPanel.remove(removeTestsButton);
                removeTestsButton=null;
                unitTestPanel.remove(testArguments);
                testArguments=null;
                unitTestPanel.remove(testExpectedValue);
                testExpectedValue=null;
            }
            if(addTestsButton==null){
                addTestsButton=new JButton("Add Unit Test");
                addTestsButton.addActionListener(this);

                unitTestPanel.add(addTestsButton,cons);
            }
        }
        else{
            if(addTestsButton!=null){
                unitTestPanel.remove(addTestsButton);
                addTestsButton=null;
            }
            if(testMethodName==null){
                testMethodName=new JTextField(15);
                testReturnType=new JTextField(10);
                testArgumentTypes=new JTextField(15);
                testArguments=new JTextField(10);
                testExpectedValue=new JTextField(15);
                if(unitTest!=null){
                    testMethodName.setText(unitTest.getMethodName());
                    testReturnType.setText(unitTest.getReturnType());
                    String argsTypes=unitTest.getArgumentTypes();
                    if(argsTypes!=null)
                        testArgumentTypes.setText(argsTypes);
                    if(unitTest.getArgumentData()!=null)
                        testArguments.setText(unitTest.getArgumentData());
                    if(unitTest.getExpectedReturnValue()!=null)
                        testExpectedValue.setText(unitTest.getExpectedReturnValue());
                }
                removeTestsButton=new JButton("Remove All Tests");
                removeTestsButton.addActionListener(this);
                unitTestPanel.add(new JLabel("Method Name: "),cons);
                cons.gridx=1;
                unitTestPanel.add(testMethodName,cons);
                cons.gridx=2;
                unitTestPanel.add(new JLabel("Return Type: "),cons);
                cons.gridx=3;
                unitTestPanel.add(testReturnType,cons);
                cons.gridx=4;
                unitTestPanel.add(new JLabel("Argument Types: "),cons);
                cons.gridx=5;
                unitTestPanel.add(testArgumentTypes,cons);
                cons.gridx=6;
                unitTestPanel.add(new JLabel("Argument Data: "),cons);
                cons.gridx=7;
                unitTestPanel.add(testArguments,cons);
                cons.gridx=8;
                unitTestPanel.add(new JLabel("Expected Return Value: "),cons);
                cons.gridx=9;
                unitTestPanel.add(testExpectedValue,cons);
                cons.gridy=1;
                cons.gridx=1;
                cons.gridwidth=6;
                unitTestPanel.add(removeTestsButton,cons);
            }
        }
        revalidate();
    }
    @Override
    public void switchedTo() {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(submitButton)||e.getSource().equals(assignmentNumField)||e.getSource().equals(assignmentNameField)||
                e.getSource().equals(assignmentPointsField)){
            //validate data
            try{
                assignmentNum=Integer.parseInt(assignmentNumField.getText().replace(" ", ""));
            } catch(NumberFormatException ex){
                GuiHelper.alertDialog("Assignment Number must be a number.");
                return;
            }
            assignmentName=assignmentNameField.getText();
            if(assignmentPointsField.getText().equals("")){
                GuiHelper.alertDialog("Total points need to be specified.");
                return;
            }
            assignmentPoints=Double.parseDouble(assignmentPointsField.getText());
            if(testMethodName!=null){
                unitTest.setMethodName(testMethodName.getText());
                unitTest.setReturnType(testReturnType.getText());
                unitTest.setArgumentTypes(testArgumentTypes.getText().split(","));
                unitTest.setArgumentData(testArguments.getText().split(","));
                unitTest.setExpectedReturnValue(testExpectedValue.getText());
            }
            
            if(callback!=null){
                gui.getBackgroundThread().invokeLater(callback);
                dispose();
            }
        }
        else if(e.getSource().equals(addTestsButton)){
            unitTest=new UnitTest();
            setupTestsGui();
        }
        else if(e.getSource().equals(removeTestsButton)){
            unitTest=null;
            setupTestsGui();
        }
    }
    public Object[] getData(){
        return new Object[]{assignmentNum,assignmentName,assignmentPoints};
    }
    public UnitTest getUnitTest(){
        return unitTest;
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
        unitTest=assign.unitTest;
        setupTestsGui();
        setTitle("Edit Assignment: "+assign.number+" "+assign.name);
    }
    
}
