/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class FileBrowser extends JTable{
    public FileBrowser(FileBrowserData data){
        super(data);
        initOrder();
        
        initColumnChangeListener();
        initColWidth();
    }    
    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass){
        return new FileBrowserRenderer();
    }
    private void initOrder(){
        String order=Config.columnOrder;
        String[] index=order.split(",");
        for(int x=0;x<getColumnCount()-1;x++){
            moveColumn(x, Integer.parseInt(index[x]));
        }
    }
    private void initColWidth(){
        int cols=getColumnModel().getColumnCount();
        String[] widths=Config.columnWidth.split(",");
        for(int x=0;x<cols;x++){
            int w=Integer.parseInt(widths[x]);
            getColumnModel().getColumn(x).setPreferredWidth(w);
        }
    }
    private void initColumnChangeListener(){
        getColumnModel().addColumnModelListener(new FileBrowserColumnModelListener(this));
    }
}
