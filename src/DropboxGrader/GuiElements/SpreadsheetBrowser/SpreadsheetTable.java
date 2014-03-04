/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import DropboxGrader.Gui;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class SpreadsheetTable extends JTable implements MouseListener,ActionListener{
    public static final String[] MODES={"View","Copy"};
    private final String[] modeDescription={"","Copies selected grade to the clipboard. Left click for grade, Right click for comment."};
    private Gui gui;
    private TextSpreadsheet sheet;
    private Clipboard clipboard;
    private int mode;
    private JLabel statusLabel;
    
    public SpreadsheetTable(Gui gui,TextSpreadsheet sheet){        
        this.sheet=sheet;
        this.gui=gui;
        
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
        
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        addMouseListener(this);
    }
    private void copyCell(int mouseButton){
        int row=getSelectedRow();
        int col=getSelectedColumn();
        Object data=getModel().getValueAt(row, col);
        TextGrade grade=null;
        if(data==null||!(data instanceof TextGrade)){
            return;
        }
        grade=(TextGrade) data;
        String copyData;
        if(mouseButton==MouseEvent.BUTTON1){
            copyData=grade.grade;
        }
        else if(mouseButton==MouseEvent.BUTTON3){
            copyData=grade.comment;
        }
        else{
            return;
        }
        try{
            Object clipData=clipboard.getData(DataFlavor.stringFlavor);
            if(clipData.equals(copyData)){
                statusLabel.setText("Copied: "+copyData.toString());
                setInGradebook(row,col,true);
                return;
            }
        } catch(Exception e){ //its a risky operation to assume the data ont he clipboard is a string, so we are ok if that fails
            //well, now we know the data on the clipboard isn't the same as the data string
        }
        try{
            clipboard.setContents(new StringSelection(copyData.toString()), null);
            statusLabel.setText("Copied: "+copyData.toString());
            setInGradebook(row,col,true);
        } catch(IllegalStateException e){
            statusLabel.setText("Error accessing the clipboard.");
        }
    }
    private void setInGradebook(int row,int col,final boolean inBook){
        final TextGrader grader=gui.getGrader();
        final String name=sheet.getNameAt(row).firstName+sheet.getNameAt(row).lastName;
        final int assignment=sheet.getAssignmentAt(col-1).number;
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                grader.setInGradebook(name,assignment,inBook);
                repaint();
            }
        });
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
    public void mouseReleased(MouseEvent e) {
        if(mode==0){
            if(e.getButton()==MouseEvent.BUTTON3){
                selectAtPoint(e.getPoint());
                JPopupMenu m=createRightClickMenu();
                if(m!=null)
                    m.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        else if(mode==1){
            selectAtPoint(e.getPoint());
            copyCell(e.getButton());
        }
    }
    private void selectAtPoint(Point p){
        int r = rowAtPoint(p);
        if (r >= 0 && r < getRowCount()) {
            setRowSelectionInterval(r, r);
        } else {
            clearSelection();
        }
        int c=columnAtPoint(p);
        if(c >= 0 && r < getColumnCount()){
            setColumnSelectionInterval(c,c);
        }
        else{
            clearSelection();
        }
    }
    private JPopupMenu createRightClickMenu(){
        int row=getSelectedRow();
        int col=getSelectedColumn();
        TextGrade grade=sheet.getGradeAt(col-1, row);
        if(grade==null){
            return null;
        }
        JPopupMenu m=new JPopupMenu();
        JMenuItem m1=new JMenuItem("Toggle Gradebook Status");
        m1.setActionCommand("Toggle Gradebook Status"+row+","+col);
        m1.addActionListener(this);
        JMenuItem m2=new JMenuItem("Edit Grade");
        m2.setActionCommand("Edit Grade"+row+","+col);
        m2.addActionListener(this);
        m.add(m1);
        m.add(m2);
        return m;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().contains("Toggle Gradebook Status")){
            int[] coords=extractCoords("Toggle Gradebook Status",e.getActionCommand());
            TextGrade grade=sheet.getGradeAt(coords[1]-1,coords[0]);
            if(grade!=null){
               setInGradebook(coords[0],coords[1],!grade.inGradebook); 
            }
        }
        else if(e.getActionCommand().contains("Edit Grade")){
            int[] coords=extractCoords("Edit Grade",e.getActionCommand());
            changeGrade(coords[0],coords[1]);
        }
    }
    private void changeGrade(int row,int col){
        TextGrade grade=sheet.getGradeAt(col-1, row);
        if(grade!=null){
            final String gradeChoice=JOptionPane.showInputDialog("What would you like to change the grade to?",grade.grade);
            final String commentChoice=JOptionPane.showInputDialog("What would you like to change the comment to?",grade.comment);
            final String name=sheet.getNameAt(row).firstName+sheet.getNameAt(row).lastName;
            final int assignment=sheet.getAssignmentAt(col-1).number;
            gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getGrader().setGrade(name, assignment, gradeChoice, commentChoice, true);
                repaint();
            }
        });
        }
    }
    private int[] extractCoords(String key,String data){
        data=data.replace(key, "");
        String[] coordsData=data.split(",");
        int[] coords={Integer.parseInt(coordsData[0]),Integer.parseInt(coordsData[1])};
        return coords;
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
}
