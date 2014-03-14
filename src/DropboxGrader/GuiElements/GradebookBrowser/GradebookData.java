/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.GradebookBrowser;

import DropboxGrader.TextGrader.TextSpreadsheet;
import javax.swing.table.AbstractTableModel;

/**
 * Manages the data from the gradebook in a hacky way so that the first column
 * appears to be a header.
 * @author Matt
 */
public class GradebookData extends AbstractTableModel{
    private TextSpreadsheet data;
    
    
    public GradebookData(TextSpreadsheet data){
        super();
        this.data=data;
    }

    @Override
    public int getRowCount() {
        return data.numNames();
    }

    @Override
    public int getColumnCount() {
        return data.numAssignments()+1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex==0){
            return data.getNameAt(rowIndex);
        }
        return data.getGradeAt(columnIndex-1, rowIndex);
    }
    @Override
    public String getColumnName(int column){
        if(column==0){
            return "Name";
        }
        return data.getAssignmentAt(column-1).toString();
    }
}
