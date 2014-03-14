/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.GradebookBrowser;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Matt
 */
public class GradebookView extends ContentView{
    private Gui gui;
    
    private GradebookTable gradebookTable;
    private JScrollPane gradebookScroll;
    private JButton backToFileBrowser;
    private JComboBox gradebookMode;
    
    public GradebookView(Gui gui){
        super("GradebookView");
        
        this.gui=gui;
    }
    @Override
    public void setup() {
        gradebookTable=new GradebookTable(gui,gui.getGrader().getSpreadsheet());
        gradebookScroll=new JScrollPane(gradebookTable);
        backToFileBrowser=new JButton("Back");
        backToFileBrowser.addActionListener(this);
        JLabel statusLabel=gradebookTable.getStatusLabel();
        JPanel modeSelector=new JPanel();
        modeSelector.setLayout(new GridBagLayout());
        GridBagConstraints cons=new GridBagConstraints();
        modeSelector.add(new JLabel("Mode: "),cons);
        gradebookMode=new JComboBox(GradebookTable.MODES);
        gradebookMode.addActionListener(this);
        cons.gridx=1;
        modeSelector.add(gradebookMode,cons);
        
        
        cons=new GridBagConstraints();
        cons.anchor=GridBagConstraints.WEST;
        cons.gridy=0;
        cons.gridx=0;
        cons.weighty=GridBagConstraints.RELATIVE;
        cons.weightx=1;
        cons.insets=new Insets(5,5,5,5);
        add(backToFileBrowser,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.gridx=1;
        cons.weightx=2;
        add(statusLabel,cons);
        cons.anchor=GridBagConstraints.EAST;
        cons.gridx=2;
        cons.weightx=1;
        add(modeSelector,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridy=1;
        cons.gridx=0;
        cons.weighty=95;
        cons.gridwidth=3;
        cons.gridheight=1;
        cons.insets=new Insets(0,5,5,5);
        add(gradebookScroll,cons);
        
        revalidate();
    }

    @Override
    public void switchedTo() {
        gradebookTable.dataChanged();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(backToFileBrowser)){
            gui.setupFileBrowserGui();
        }
        else if(e.getSource().equals(gradebookMode)){
            gradebookTable.setMode(gradebookMode.getSelectedIndex());
        }
    }
    public void dataChanged(){
        gradebookTable.dataChanged();
    }
    
}
