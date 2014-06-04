/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.GradebookBrowser;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.GuiElements.MiscOverlays.PrintOverlay;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextSpreadsheet;
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
    private JButton printButton;
    private JButton markGraded;
    private JButton changeSpreadsheetButton;
    
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
        printButton=new JButton("Print");
        printButton.addActionListener(this);
        changeSpreadsheetButton=new JButton("Change Gradebook");
        changeSpreadsheetButton.addActionListener(this);
        
        
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
        cons.gridx=3;
        cons.weightx=0.1;
        add(changeSpreadsheetButton,cons);
        cons.gridx=4;
        add(modeSelector,cons);
        cons.gridx=5;
        cons.weightx=0.1;
        add(printButton,cons);
        cons.weightx=1;
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridy=1;
        cons.gridx=0;
        cons.weighty=95;
        cons.gridwidth=6;
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
            if(GradebookTable.MODES[gradebookMode.getSelectedIndex()].equals("Copy")&&markGraded==null){
                markGraded=new JButton("Mark All Graded");
                markGraded.addActionListener(this);
                GridBagConstraints cons=new GridBagConstraints();
                cons.anchor=GridBagConstraints.EAST;
                cons.gridx=2;
                cons.gridy=0;
                cons.weightx=1;
                add(markGraded,cons);
                revalidate();
            }
            else if(!GradebookTable.MODES[gradebookMode.getSelectedIndex()].equals("Copy")&&markGraded!=null){
                remove(markGraded);
                markGraded=null;
                revalidate();
            }
        }
        else if(e.getSource().equals(printButton)){
            PrintOverlay overlay=new PrintOverlay(gui,this);
            gui.getViewManager().addOverlay(overlay);
        }
        else if(e.getSource().equals(markGraded)){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    TextGrader grader=gui.getGrader();
                    grader.downloadSheet();
                    TextSpreadsheet sheet=grader.getSpreadsheet();
                    for(int student=0;student<sheet.numNames();student++){
                        for(int assign=0;assign<sheet.numAssignments();assign++){
                            TextGrade grade=sheet.getGradeAt(assign, student);
                            if(grade!=null){
                                grade.inGradebook=true;
                            }
                        }
                    }
                    grader.uploadTable();
                    repaint();
                }
            });
        }
        else if(e.getSource().equals(changeSpreadsheetButton)){
            gui.getViewManager().removeOverlay("ChangeGradebookOverlay");
            ChangeGradebookOverlay overlay=new ChangeGradebookOverlay(this);
            gui.getViewManager().addOverlay(overlay);
        }
    }
    public void dataChanged(){
        gradebookTable.dataChanged();
    }
    public GradebookTable getGradebookTable(){
        return gradebookTable;
    }
    public JScrollPane getScroll(){
        return gradebookScroll;
    }
    public Gui getGui(){
        return gui;
    }
    
}
