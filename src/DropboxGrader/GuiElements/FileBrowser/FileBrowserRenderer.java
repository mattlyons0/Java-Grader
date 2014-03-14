/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import DropboxGrader.GuiElements.FileBrowser.FileBrowserData;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Matt
 */
public class FileBrowserRenderer extends DefaultTableCellRenderer{
    @Override
    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int col){
        JLabel l=(JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        Color c;
        try{
            col=table.convertColumnIndexToModel(col);
            FileBrowserData data=(FileBrowserData)table.getModel();
            String colName=data.getColumnName(col);
            c=data.getColorAt(new CellLocation(colName,table.convertRowIndexToModel(row)));
        } catch(ArrayIndexOutOfBoundsException ex){
            c=null;
            System.err.println("Determining color failed, "+ex); //tried to color something during a data refresh or something.
            ex.printStackTrace();
        }
        if(c!=null){
            l.setBackground(c);
            if(isSelected){
                l.setBackground(c.darker());
            }
        }
        
        return l;
    }
}
