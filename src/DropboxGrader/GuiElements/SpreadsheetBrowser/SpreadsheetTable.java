/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class SpreadsheetTable extends JTable{
    private TextSpreadsheet sheet;
    
    public SpreadsheetTable(TextSpreadsheet sheet){        
        this.sheet=sheet;
        
        init();
    }
    private void init(){
        setModel(new SpreadsheetData(sheet));
    }
    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        return new SpreadsheetTableRenderer();
    }
}
