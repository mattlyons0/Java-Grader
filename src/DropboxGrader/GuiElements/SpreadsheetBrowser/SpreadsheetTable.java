/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class SpreadsheetTable extends JTable{
    public static final String[] MODES={"View","Copy"};
    private final String[] modeDescription={"","Copies selected grade to the clipboard."};
    private TextSpreadsheet sheet;
    private Clipboard clipboard;
    private int mode;
    private JLabel statusLabel;
    
    public SpreadsheetTable(TextSpreadsheet sheet){        
        this.sheet=sheet;
        
        statusLabel=new JLabel();
        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mode=0;
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
        
        init();
    }
    private void init(){
        setModel(new SpreadsheetData(sheet));
        getSelectionModel().addListSelectionListener(this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //this.get
    }
    private void copyCell(){
        int row=getSelectedRow();
        int col=getSelectedColumn();
        Object data=getModel().getValueAt(row, col);
        if(data!=null){
            try{
                Object clipData=clipboard.getData(DataFlavor.stringFlavor);
                if(clipData.equals(data.toString())){
                    return;
                }
            } catch(Exception e){ //its a risky operation to assume the data ont he clipboard is a string, so we are ok if that fails
                //well, now we know the data on the clipboard isn't the same as the data string
            }
            try{
                clipboard.setContents(new StringSelection(data.toString()), null);
                statusLabel.setText("Copied: "+data.toString());
            } catch(IllegalStateException e){
                statusLabel.setText("Error accessing the clipboard.");
            }
        }
    }
    public void setMode(int modeIndex){
        mode=modeIndex;
        statusLabel.setText(modeDescription[modeIndex]);
    }
    public JLabel getStatusLabel(){
        return statusLabel;
    }
    @Override
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
        return new SpreadsheetTableRenderer();
    }
    @Override
    public void valueChanged(ListSelectionEvent e){
        super.valueChanged(e);
        if(mode==1){
            if(!e.getValueIsAdjusting()){
                copyCell();
            }
        }
    }
    @Override
    public void columnSelectionChanged(ListSelectionEvent e){
        super.columnSelectionChanged(e);
        if(mode==1){
            if(!e.getValueIsAdjusting()){
                copyCell();
            }
        }
    }
}
