/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.GuiElements.MiscComponents.JGhostTextField;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.JavaClass;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author matt
 */
public class MethodArgumentsOverlay extends ContentOverlay implements CaretListener{
    
    private Gui gui;
    private UnitTestPanel panel;
    private int testIndex;
    private UnitTest test;
    
    private ArrayList<JavaClass> argumentsT;
    private ArrayList<String> argumentsV;
    private ArrayList<JTextField> argumentTypes;
    private ArrayList<JTextField> argumentValues;
    private ArrayList<JButton> removeArguments;
    private JButton addArgument;
    
    public MethodArgumentsOverlay(Gui gui,UnitTestPanel panel,int testIndex){
        super("MethodArgumentsOverlay"+testIndex,true);
        this.gui=gui;
        this.panel=panel;
        this.testIndex=testIndex;
        
        argumentTypes=new ArrayList();
        argumentValues=new ArrayList();
        removeArguments=new ArrayList();
        argumentsT=new ArrayList();
        argumentsV=new ArrayList();
    }
    @Override
    public void setup() {
        setupArgs();
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Method Arguments");
        setVisible(true);
    }
    private void setupArgs(){
        if(addArgument==null){
            addArgument=new JButton("+");
            addArgument.setToolTipText("Add Argument");
            addArgument.addActionListener(this);
        }
        Component[] comps=getComponents();
        for(int i=0;i<comps.length;i++){ //cant use removeAll because it crashes, because we need to keep the first 2 components
            if(i>1){
                remove(comps[i]);
            }
        }
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        for(int i=0;i<argumentsT.size();i++){
            if(argumentTypes.size()<=i){ //need to add it to the arraylists
                JTextField type=new JGhostTextField(10,"Parameter Type");
                type.setActionCommand("ObjectType"+i);
                type.addCaretListener(this);
                type.addActionListener(this);
                //type.addKeyListener(this);
                JTextField data=new JGhostTextField(25,"Parameter Value");
                data.setActionCommand("DataType"+i);
                data.addCaretListener(this);
                data.addActionListener(this);
                //data.addKeyListener(this);
                JButton remove=new JButton("-");
                remove.setToolTipText("Remove This Argument");
                remove.setActionCommand("RemoveArgument"+i);
                remove.addActionListener(this);
                 
                argumentTypes.add(type);
                argumentValues.add(data);
                removeArguments.add(remove);
            }
            else{
                argumentTypes.get(i).setActionCommand("ObjectType"+i);
                argumentValues.get(i).setActionCommand("DataType"+i);
                removeArguments.get(i).setActionCommand("RemoveArgument"+i);
            }
            if(argumentsT.get(i)!=null)
                argumentTypes.get(i).setText(argumentsT.get(i).toText());
            if(argumentsV.get(i)!=null)
                argumentValues.get(i).setText(argumentsV.get(i));
            cons.gridx=0;
            add(argumentTypes.get(i),cons);
            cons.gridx++; //why the hell haven't I been doing this the whole time...
            add(argumentValues.get(i),cons);
            cons.gridx++;
            add(removeArguments.get(i),cons);
            
            cons.gridy++;
        }
        cons.gridx++;
        cons.gridy--;
        add(addArgument,cons);
        
        revalidate();
        repaint();
    }

    @Override
    public void switchedTo() {}
    @Override
    public boolean isClosing(){return true;}
    public void setUnitTest(UnitTest unitTest){
        test=unitTest;
        
        JavaClass[] types=test.getArgumentTypes();
        String[] data=test.getArguments();
        argumentsT.clear();
        argumentsV.clear();
        if(types!=null){
            for(int i=0;i<types.length;i++){
                argumentsT.add(types[i]);
                argumentsV.add(data[i]);
            }
        } else{
            argumentsT.add(null);
            argumentsV.add(null);
        }
        
        setupArgs();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(addArgument)){
            argumentsT.add(null);
            argumentsV.add(null);
            setupArgs();
        }
        else if(e.getActionCommand().startsWith("RemoveArgument")){
            int index=GradebookTable.extractNumber("RemoveArgument", e.getActionCommand());
            argumentsT.remove(index);
            argumentsV.remove(index);
            
            remove(argumentTypes.get(index));
            remove(argumentValues.get(index));
            remove(removeArguments.get(index));
            
            argumentTypes.remove(index);
            argumentValues.remove(index);
            removeArguments.remove(index);
            setupArgs();       
        }
        else if(e.getActionCommand().startsWith("ObjectType")){
            int index=GradebookTable.extractNumber("ObjectType", e.getActionCommand());
            argumentsT.set(index, new JavaClass(((JTextField)e.getSource()).getText()));
            panel.setArgs(argumentsT.toArray(new JavaClass[0]), argumentsV.toArray(new String[0]), testIndex);
        }
        else if(e.getActionCommand().startsWith("DataType")){
            int index=GradebookTable.extractNumber("DataType", e.getActionCommand());
            argumentsV.set(index, ((JTextField)e.getSource()).getText());
            panel.setArgs(argumentsT.toArray(new JavaClass[0]), argumentsV.toArray(new String[0]), testIndex);
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if(e.getSource() instanceof JTextField)
            ((JTextField)e.getSource()).postActionEvent();
    }    
}
