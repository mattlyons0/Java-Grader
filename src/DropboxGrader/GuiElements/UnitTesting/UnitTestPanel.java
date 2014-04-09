/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.UnitTesting.UnitTest;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * All unit testing gui stuff should go in here
 * This will be the panel in the assignment overlay for tests
 * @author matt
 */
public class UnitTestPanel extends JPanel implements ActionListener{
    
    //Single Elements
    private JButton addTestsButton;
    private JButton addTestButton;
    
    //Multiple Elements
    private ArrayList<JTextField> methodNames;
    private ArrayList<JTextField> returnTypes;
    private ArrayList<JTextField> argumentTypes;
    private ArrayList<JTextField> arguments;
    private ArrayList<JTextField> expectedValues;
    private ArrayList<JButton> removeTestButtons;
    
    //Data
    private ArrayList<UnitTest> unitTests;
    public UnitTestPanel(UnitTest[] tests){
        super();
        setLayout(new GridBagLayout());
        
        unitTests=new ArrayList();
        if(tests!=null){
            unitTests.addAll(Arrays.asList(tests));
        }
        
        methodNames=new ArrayList();
        returnTypes=new ArrayList();
        argumentTypes=new ArrayList();
        arguments=new ArrayList();
        expectedValues=new ArrayList();
        removeTestButtons=new ArrayList();
        
        setup();
        setVisible(true);
    }
    
    private void setup(){
        removeAll();
        
        if(unitTests.isEmpty()){
            setupNoTests();
        }
        else{
            setupTests();
        }
        revalidate();
        repaint();
    }
    private void setupNoTests(){
        if(addTestsButton==null){
            addTestsButton=new JButton("Add Unit Test");
            addTestsButton.addActionListener(this);
        }

        GridBagConstraints cons=new GridBagConstraints();
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        
        add(addTestsButton,cons);
    }
    private void setupTests(){
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.fill=GridBagConstraints.NONE;
        cons.insets=new Insets(5,5,5,5);
        cons.weightx=1;
        cons.weighty=1;
        
        for(int i=0;i<unitTests.size();i++){
            if(i>=methodNames.size()){ //if we don't have stored fields for this index
                JTextField methodNameField=new JTextField(15);
                JTextField returnTypeField=new JTextField(10);
                JTextField argumentTypesField=new JTextField(15);
                JTextField argumentsField=new JTextField(10);
                JTextField expectedValueField=new JTextField(15);
                JButton removeButton=new JButton("-");
                removeButton.setActionCommand("RemoveUnitTest"+i);
                removeButton.addActionListener(this);
                removeButton.setToolTipText("Remove this Unit Test");
                
                UnitTest unitTest=unitTests.get(i);
                if(unitTest!=null){
                    methodNameField.setText(unitTest.getMethodName());
                    returnTypeField.setText(unitTest.getReturnTypeString());
                    String argsTypes=unitTest.getArgumentTypesString();
                    if(argsTypes!=null)
                        argumentTypesField.setText(argsTypes);
                    if(unitTest.getArgumentData()!=null)
                        argumentsField.setText(unitTest.getArgumentData());
                    if(unitTest.getExpectedReturnValue()!=null)
                        expectedValueField.setText(unitTest.getExpectedReturnValue());
                }
                methodNames.add(methodNameField);
                returnTypes.add(returnTypeField);
                argumentTypes.add(argumentTypesField);
                arguments.add(argumentsField);
                expectedValues.add(expectedValueField);
                removeTestButtons.add(removeButton);
            }
            if(addTestButton==null){
                addTestButton=new JButton("+");
                addTestButton.addActionListener(this);
                addTestButton.setToolTipText("Add Unit Test");
            }
            
            cons.gridx=0;
            cons.weightx=1;
            add(new JLabel("Method Name: "),cons);
            cons.gridx=1;
            cons.weightx=10;
            add(methodNames.get(i),cons);
            cons.gridx=2;
            cons.weightx=1;
            add(new JLabel("Return Type: "),cons);
            cons.gridx=3;
            cons.weightx=10;
            add(returnTypes.get(i),cons);
            cons.gridx=4;
            cons.weightx=1;
            add(new JLabel("Argument Types: "),cons);
            cons.gridx=5;
            cons.weightx=10;
            add(argumentTypes.get(i),cons);
            cons.gridx=6;
            cons.weightx=1;
            add(new JLabel("Argument Data: "),cons);
            cons.gridx=7;
            cons.weightx=10;
            add(arguments.get(i),cons);
            cons.gridx=8;
            cons.weightx=1;
            add(new JLabel("Expected Return Value: "),cons);
            cons.gridx=9;
            cons.weightx=10;
            add(expectedValues.get(i),cons);
            cons.gridx=11;
            cons.weightx=1;
            add(removeTestButtons.get(i),cons);
            if(i==unitTests.size()-1){
                cons.gridx=12;
                add(addTestButton,cons);
            }
            
            cons.gridy++;
        }
    }
    public void setUnitTests(UnitTest[] tests){
        if(tests!=null){
            unitTests.clear();
            unitTests.addAll(Arrays.asList(tests));
        }
    }
    public UnitTest[] getUnitTest(){
        return unitTests.toArray(new UnitTest[0]);
    }
    public void save(){
        for(int i=0;i<unitTests.size();i++){
            UnitTest test=unitTests.get(i);
            test.setMethodName(methodNames.get(i).getText());
            test.setReturnType(returnTypes.get(i).getText());
            test.setArgumentTypes(argumentTypes.get(i).getText().split(","));
            test.setArgumentData(arguments.get(i).getText().split(","));
            test.setExpectedReturnValue(expectedValues.get(i).getText());
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(addTestsButton)){
            unitTests.add(new UnitTest());
            setup();
        }
        else if(e.getSource().equals(addTestButton)){
            unitTests.add(new UnitTest());
            setup();
        }
        else if(e.getActionCommand().startsWith("RemoveUnitTest")){
            int testNum=GradebookTable.extractNumber("RemoveUnitTest", e.getActionCommand());
            unitTests.remove(testNum);
            setup();
        }
    }
}
