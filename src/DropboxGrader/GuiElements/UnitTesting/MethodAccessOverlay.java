/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * I considered using a 3 stage checkbox for this, but it was a bit confusing which meant ignored and which meant forced
 * @author matt
 */
public class MethodAccessOverlay extends ContentOverlay{
    
    public static final String[] states={"Required","Disallowed","Optional"}; //must have same ordering is CheckboxStatus
    
    private Gui gui;
    private UnitTestPanel panel;
    private int testIndex;
    private UnitTest test; //only used for reading, unitTestPanel writes modifications
    
    private JComboBox publicAccess;
    private JComboBox protectedAccess;
    private JComboBox privateAccess;
    private JComboBox packagePrivateAccess;
    
    public MethodAccessOverlay(Gui gui,UnitTestPanel panel,int testIndex){
        super("MethodAccessOverlay"+testIndex,true);
        this.gui=gui;
        this.panel=panel;
        this.testIndex=testIndex;
    }
    public void setTest(UnitTest unitTest){
        test=unitTest;
        if(test!=null&&publicAccess!=null){
            publicAccess.setSelectedIndex(test.accessPublic.ordinal());
            protectedAccess.setSelectedIndex(test.accessProtected.ordinal());
            privateAccess.setSelectedIndex(test.accessPrivate.ordinal());
            packagePrivateAccess.setSelectedIndex(test.accessPackagePrivate.ordinal());
        }
    }
    @Override
    public void setup() {
        publicAccess=new JComboBox(states);
        publicAccess.addActionListener(this);
        protectedAccess=new JComboBox(states);
        protectedAccess.addActionListener(this);
        privateAccess=new JComboBox(states);
        privateAccess.addActionListener(this);
        packagePrivateAccess=new JComboBox(states);
        packagePrivateAccess.addActionListener(this);
        
        setTest(test);

        GridBagConstraints cons=new GridBagConstraints();
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        add(new JLabel("Public"),cons);
        cons.gridx=1;
        add(new JLabel("Protected"),cons);
        cons.gridx=2;
        add(new JLabel("Private"),cons);
        cons.gridx=3;
        add(new JLabel("Package-Private"),cons);
        cons.weighty=2;
        cons.gridy=1;
        cons.gridx=0;
        add(publicAccess,cons);
        cons.gridx=1;
        add(protectedAccess,cons);
        cons.gridx=2;
        add(privateAccess,cons);
        cons.gridx=3;
        add(packagePrivateAccess,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Method Access");
        setVisible(true);
    }
    @Override
    public void switchedTo() {}
    @Override
    public void isClosing(){}
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(publicAccess))
            panel.setAccessType(MethodAccessType.PUBLIC, CheckboxStatus.values()[publicAccess.getSelectedIndex()], testIndex);
        else if(e.getSource().equals(protectedAccess))
            panel.setAccessType(MethodAccessType.PROTECTED, CheckboxStatus.values()[protectedAccess.getSelectedIndex()], testIndex);
        else if(e.getSource().equals(privateAccess))
            panel.setAccessType(MethodAccessType.PRIVATE, CheckboxStatus.values()[privateAccess.getSelectedIndex()], testIndex);
        else if(e.getSource().equals(packagePrivateAccess))
            panel.setAccessType(MethodAccessType.PACKAGEPRIVATE, CheckboxStatus.values()[packagePrivateAccess.getSelectedIndex()], testIndex);
    }
    
}
