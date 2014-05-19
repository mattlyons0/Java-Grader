/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.Assignment;

import DropboxGrader.FileManagement.Date;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.MiscComponents.DoubleDocument;
import DropboxGrader.GuiElements.MiscComponents.IntegerDocument;
import DropboxGrader.GuiElements.UnitTesting.UnitTestPanel;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import DropboxGrader.Util.SettableDate;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author Matt
 */
public class AssignmentOverlay extends ContentOverlay implements SettableDate{
    private Gui gui;
    private Runnable callback;
    
    private JTextField assignmentNumField;
    private JTextField assignmentNameField;
    private JTextField assignmentPointsField;
    private DateOverlay dateOverlay;
    private JButton dateButton;
    private UnitTestPanel unitTestPanel;
    private JScrollPane unitTestScroll;
    private LibraryPanel libraryPanel;
    
    private Integer assignmentNum;
    private String assignmentName;
    private Double assignmentPoints;
    private UnitTest[] tests;
    private String[] jtests;
    private Date date;
    private String[] libs;
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
        assignmentNumField.setDocument(new IntegerDocument());
        assignmentNumField.addActionListener(this);
        assignmentNameField=new JTextField(20);
        assignmentNameField.addActionListener(this);
        assignmentPointsField=new JTextField(5);
        assignmentPointsField.addActionListener(this);
        assignmentPointsField.setDocument(new DoubleDocument());
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
        dateOverlay=new DateOverlay(this,gui,getID());
        dateButton=new JButton("Due Date");
        dateButton.setToolTipText("Due Date");
        dateButton.addActionListener(this);
        setDate(date);
        libraryPanel=new LibraryPanel(libs,assignmentNum,assignmentName,gui);
        JScrollPane libraryScroll=new JScrollPane(libraryPanel);
        
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
        cons.gridx=6;
        add(dateButton,cons);
        
        cons.fill=GridBagConstraints.BOTH;
        cons.weighty=98;
        cons.weightx=1;
        cons.gridy=1;
        cons.gridx=0;
        cons.gridwidth=7;
        add(unitTestScroll,cons);
        cons.gridy++;
        add(libraryScroll,cons);
        
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
        gui.getViewManager().removeOverlay(dateOverlay.getViewName()); //save and remove the overlay
        if(assignmentNameField.getText().equals("")&&assignmentNumField.getText().equals("")&&assignmentPointsField.getText().equals("")&&
                !unitTestPanel.hasTests()&&!libraryPanel.hasLibraries()) //no data entered
            return true;
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
        libraryPanel.save();
        
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
        else if(e.getSource().equals(dateButton)){
            gui.getViewManager().removeOverlay(dateOverlay.getViewName());
            dateOverlay=new DateOverlay(this,gui,getID());
            gui.getViewManager().addOverlay(dateOverlay);
            dateOverlay.setDate(date);
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
        if(dateOverlay!=null)
            dateOverlay.setDate(assign.dateDue);
        date=assign.dateDue;
        libs=assign.libraries;
        if(libraryPanel!=null)
            libraryPanel.setLibs(libs);
        setTitle("Edit Assignment: "+assign.number+" "+assign.name);
    }
    @Override
    public void setDate(Date d){
        if(d==null){
            dateButton.setText("Due Date");
        } else{
            dateButton.setText("Due Date: "+d.toString());
        }
        date=d;
    }
    public Date getDate(){
        return date;
    }
    public String[] getLibraries(){
        return libraryPanel.getLibs();
    }
}
