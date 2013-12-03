/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.google.gdata.client.sidewiki.SidewikiEntryQuery;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
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
    public FileBrowser(FileBrowserData data,FileBrowserListener listener){
        super(data);
        listener.setTable(this);
        
        setAutoCreateRowSorter(true);
        getRowSorter().addRowSorterListener(listener);
        initSort();
        addMouseListener(listener);
        
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
    private void initSort(){
        RowSorter sorter=getRowSorter();
        ArrayList<RowSorter.SortKey> keys=new ArrayList();
        String order=Config.sortOrder;
        String column=Config.sortColumn;
        int col=DbxFile.safeStringToInt(column);
        keys.add(new SortKey(col,SortOrder.valueOf(order)));
        sorter.setSortKeys(keys);
    }
}
