/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.GradebookBrowser;

import DropboxGrader.TextGrader.TextGrade;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class GradebookTableRenderer extends DefaultTableCellRenderer{
    public GradebookTableRenderer(){
        super();
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if(table.convertColumnIndexToModel(column)!=0){
            JLabel l=(JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(value instanceof TextGrade){
                TextGrade grade=(TextGrade)value;
                if(!grade.inGradebook){
                    l.setBackground(Color.ORANGE);
                    l.setForeground(Color.BLACK);
                    if(isSelected)
                        l.setBackground(l.getBackground().darker());
                }
            }
            l.setBorder(new MatteBorder(0,0,1,1,UIManager.getColor("Table.gridColor")));
            return l;
        }
        else{ //First column, we are going to render this like a header cell
            TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
            JLabel l= (JLabel) renderer.getTableCellRendererComponent(table, value, false, false, row, column);
            l.setBorder(BorderFactory.createEmptyBorder());
            return l;
        }
        
    }
}
