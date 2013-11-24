/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

/**
 *
 * @author Matt
 */
public class FileBrowserColumnModelListener implements TableColumnModelListener{
    private JTable table;
    public FileBrowserColumnModelListener(JTable table){
        this.table=table;
    }
    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        int from=e.getFromIndex();
        int to=e.getToIndex();
        //System.out.println(from+" to "+to);
        from=table.convertColumnIndexToModel(to);
        to=table.convertColumnIndexToModel(e.getFromIndex());
        if(e.getFromIndex()==e.getToIndex()){
            return;
        }
        System.out.println(from+" to "+to);
        String[] columns=Config.columnOrder.split(",");
        columns[e.getToIndex()]=from+"";
        columns[e.getFromIndex()]=to+"";
        String s="";
        for(int x=0;x<columns.length;x++){
            s+=columns[x];
            if(x!=columns.length-1){
                s+=",";
            }
        }
        Config.columnOrder=s;
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        int cols=table.getColumnModel().getColumnCount();
        String width="";
        for(int x=0;x<cols;x++){
            width+=table.getColumnModel().getColumn(x).getPreferredWidth();
            if(x!=cols-1){
                width+=",";
            }
        }
        Config.columnWidth=width;
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }
    
    
}
