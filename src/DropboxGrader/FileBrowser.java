/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class FileBrowser extends JTable{
    private FileBrowserData data;
    public FileBrowser(FileBrowserData data){
        super(data);
    }    
    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass){
        return new FileBrowserRenderer();
    }
}