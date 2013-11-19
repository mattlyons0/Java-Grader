/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Matt
 */
public class FileBrowserData extends AbstractTableModel{
    private FileManager manager;
    private Color[][] cellColors;
    public FileBrowserData(FileManager f){
        super();
        manager=f;
        
        cellColors=new Color[getRowCount()][getColumnCount()];
    }
    @Override
    public int getRowCount() {
        return manager.getNumFiles();
    }

    @Override
    public int getColumnCount() {
        return manager.getNumAttributes();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return manager.getFileInfo(rowIndex, columnIndex);
    }
    @Override
    public String getColumnName(int col){
        return manager.getAttributes()[col];
    }
    public void setColorAt(Color c,int row,int col){
        cellColors[row][col]=c;
    }
    public Color getColorAt(int row,int col){
        return cellColors[row][col];
    }
    public void refresh(){
        cellColors=new Color[getRowCount()][getColumnCount()];
    }
    
}
