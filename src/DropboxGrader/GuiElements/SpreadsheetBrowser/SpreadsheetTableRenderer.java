/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class SpreadsheetTableRenderer extends DefaultTableCellRenderer{
    public SpreadsheetTableRenderer(){
        
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(column!=0){
            JLabel l=(JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return l;
        }
        else{ //First column, we are going to render this like a header cell
            TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
            JLabel l= (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, row, column);
            return l;
        }
        
    }
}
