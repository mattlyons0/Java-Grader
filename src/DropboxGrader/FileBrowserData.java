/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Matt
 */
public class FileBrowserData extends AbstractTableModel{
    private FileManager manager;
    private HashMap<CellLocation,Color> cellColors;
    public FileBrowserData(FileManager f){
        super();
        manager=f;
        
        cellColors=new HashMap();
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
    public void setColorAt(Color c,CellLocation cell){
        cellColors.put(cell, c);
    }
    public Color getColorAt(CellLocation cell){
        return cellColors.get(cell);
    }
    public FileManager getManager(){
        return manager;
    }
    public void refresh(){
        cellColors.clear();
    }

    @Override
    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        super.fireTableRowsUpdated(firstRow, lastRow);
        System.out.println("Rows updated "+firstRow+"-"+lastRow);
    }    
}
