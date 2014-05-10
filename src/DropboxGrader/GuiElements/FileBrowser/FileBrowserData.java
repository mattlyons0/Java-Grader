/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import DropboxGrader.FileManagement.FileManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Matt
 */
public class FileBrowserData extends AbstractTableModel{
    private FileManager manager;
    private HashMap<CellLocation,Color> cellColors;
    private HashMap<CellLocation,String> tooltips;
    private ArrayList<Integer> hiddenCols;
    public FileBrowserData(FileManager f){
        super();
        manager=f;
        
        cellColors=new HashMap();
        tooltips=new HashMap();
        hiddenCols=new ArrayList();
    }
    @Override
    public int getRowCount() {
        int count=manager.getNumFiles();
        if(count==0){
            count=1;
        }
        return count;
    }

    @Override
    public int getColumnCount() {
        int count=manager.getNumAttributes();
        
        return count;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(hiddenCols.contains(columnIndex)){
            return "Hiden";
        }
        String value=manager.getFileInfo(rowIndex, columnIndex);
        if(value==null)
            value="No files found.";
        return value;
    }
    @Override
    public String getColumnName(int col){
        return manager.getAttributes()[col];
    }
    public void setColorAt(Color c,CellLocation cell){
        cellColors.put(cell, c);
    }
    public void removeColorAt(CellLocation cell){
        cellColors.remove(cell);
    }
    public Color getColorAt(CellLocation cell){
        return cellColors.get(cell);
    }
    public void setTooltipAt(String tooltip,CellLocation cell){
        tooltips.put(cell,tooltip);
    }
    public String getTooltipAt(CellLocation cell){
        return tooltips.get(cell);
    }
    public FileManager getManager(){
        return manager;
    }
    public void refresh(){
        cellColors.clear();
        tooltips.clear();
    }
    public void hideCol(int col){
        if(!hiddenCols.contains(col)){
            hiddenCols.add(col);
        }
    }
    public boolean isHidden(int col){
        return hiddenCols.contains(col);
    }
    public void unhideCol(int col){
        hiddenCols.remove((Integer)col);
    }
}
