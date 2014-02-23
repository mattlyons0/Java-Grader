/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import DropboxGrader.TextGrader.TextGrader;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import javax.swing.JScrollPane;

/**
 *
 * @author Matt
 */
public class SpreadsheetBrowser extends Container{
    //Data
    private TextGrader grader;
    //GUI Elements
    private SpreadsheetTable table;
    private JScrollPane scrollPane;
    
    public SpreadsheetBrowser(TextGrader grader){
        this.grader=grader;
        
        init();
    }
    private void init(){
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        table=new SpreadsheetTable(grader.getSpreadsheet());
        scrollPane=new JScrollPane(table);
        
        add(scrollPane);
    }
}
