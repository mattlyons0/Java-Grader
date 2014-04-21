/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodModifiers;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 *
 * @author matt
 */
public class MethodModifiersOverlay extends ContentOverlay{
    private final String[] states=MethodAccessOverlay.states;
    
    private Gui gui;
    private UnitTestPanel panel;
    private int index;
    private UnitTest test; //used for reading only
    
    private JComboBox staticMod;
    private JComboBox finalMod;
    private JComboBox abstractMod; //it occurs to me that you couldn't test it if it was abstract, but hell, i'll leave it in lol
    private JComboBox synchronizedMod;
    
    public MethodModifiersOverlay(Gui gui,UnitTestPanel panel,int testIndex){
        super("MethodModifiersOverlay"+testIndex,true);
        this.gui=gui;
        this.panel=panel;
        index=testIndex;
    }
    @Override
    public void setup() {
        staticMod=new JComboBox(states);
        staticMod.addActionListener(this);
        finalMod=new JComboBox(states);
        finalMod.addActionListener(this);
        abstractMod=new JComboBox(states);
        abstractMod.addActionListener(this);
        abstractMod.setToolTipText("Sometimes it occurs to me that you couldn't test abstract methods anyways...\n");
        synchronizedMod=new JComboBox(states);
        synchronizedMod.addActionListener(this);
        
        setTest(test);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        add(new JLabel("Static"),cons);
        cons.gridx=1;
        add(new JLabel("Final"),cons);
        cons.gridx=2;
        add(new JLabel("Abstract"),cons);
        cons.gridx=3;
        add(new JLabel("Synchronized"),cons);
        cons.gridy=1;
        cons.gridx=0;
        cons.weighty=2;
        add(staticMod,cons);
        cons.gridx=1;
        add(finalMod,cons);
        cons.gridx=2;
        add(abstractMod,cons);
        cons.gridx=3;
        add(synchronizedMod,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Method Modifiers");
        setVisible(true);
    }
    public void setTest(UnitTest unitTest){
        test=unitTest;
        
        if(test!=null&&staticMod!=null){
            staticMod.setSelectedIndex(test.modStatic.ordinal());
            finalMod.setSelectedIndex(test.modFinal.ordinal());
            abstractMod.setSelectedIndex(test.modAbstract.ordinal());
            synchronizedMod.setSelectedIndex(test.modSynchronized.ordinal());
        }
        
    }
    @Override
    public void switchedTo() {}
    @Override
    public boolean isClosing(){return true;}
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(staticMod)){
            panel.setModifierType(CheckboxStatus.values()[staticMod.getSelectedIndex()], 0, index);
        }
        else if(e.getSource().equals(finalMod)){
            panel.setModifierType(CheckboxStatus.values()[finalMod.getSelectedIndex()], 1, index);
        }
        else if(e.getSource().equals(abstractMod)){
            panel.setModifierType(CheckboxStatus.values()[abstractMod.getSelectedIndex()], 2, index);
        }
        else if(e.getSource().equals(synchronizedMod)){
            panel.setModifierType(CheckboxStatus.values()[synchronizedMod.getSelectedIndex()], 3, index);
        }
    }
    
}
