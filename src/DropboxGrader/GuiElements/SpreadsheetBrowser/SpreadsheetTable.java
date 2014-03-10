/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.SpreadsheetBrowser;

import DropboxGrader.Config;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.AssignmentOverlay;
import DropboxGrader.GuiElements.NameOverlay;
import DropboxGrader.GuiHelper;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextName;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Matt
 */
public class SpreadsheetTable extends JTable implements MouseListener,ActionListener{
    public static final String[] MODES={"View","Copy"};
    private final String[] modeDescription={"","Copies selected grade to the clipboard. Left click for grade, Right click for comment."};
    private SpreadsheetData sheetData;
    private Gui gui;
    private TextSpreadsheet sheet;
    private Clipboard clipboard;
    private int mode;
    private JLabel statusLabel;
    
    public SpreadsheetTable(Gui gui,TextSpreadsheet sheet){
        super();
        this.sheet=sheet;
        this.gui=gui;
        
        statusLabel=new JLabel();
        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        mode=0;
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
        sheetData=new SpreadsheetData(sheet);
        
        init();
    }
    private void init(){
        setModel(sheetData);
        getSelectionModel().addListSelectionListener(this);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setDragEnabled(false); 
        getTableHeader().setReorderingAllowed(true);
        getColumnModel().addColumnModelListener(this);
        addMouseListener(this);
        getTableHeader().addMouseListener(this);
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
    public void dataChanged(){
        sheetData.fireTableStructureChanged();
        sheetData.fireTableDataChanged();
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if(mode==0){ //view mode
            if(e.getButton()==MouseEvent.BUTTON3){ //right click
                if(!(e.getSource() instanceof JTableHeader)){ //they clicked on a cell/rowHeader
                    selectAtPoint(e.getPoint());
                    JPopupMenu m=createRightClickMenu();
                    if(m!=null)
                        m.show(e.getComponent(), e.getX(), e.getY());
                }
                else{ //they clicked on a columnHeader
                    int col=getTableHeader().columnAtPoint(e.getPoint());
                    JPopupMenu m=createColHeaderRightClickMenu(col);
                    if(m!=null)
                        m.show(e.getComponent(),e.getX(),e.getY());
                }
            }
        }
        else if(mode==1){
            if(!(e.getSource() instanceof JTableHeader)){
                selectAtPoint(e.getPoint());
                copyCell(e.getButton());
            }
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
        if(c >= 0 && c < getColumnCount()){
            setColumnSelectionInterval(c,c);
        }
        else{
            clearSelection();
        }
    }
    private JPopupMenu createRightClickMenu(){
        int row=getSelectedRow();
        int col=getSelectedColumn();
        if(col!=0){
            TextGrade grade=sheet.getGradeAt(col-1, row);
            if(grade==null){
                return null;
            }
            JPopupMenu m=new JPopupMenu();
            JMenuItem m1=new JMenuItem("Toggle Gradebook Status");
            m1.setActionCommand("Toggle Gradebook Status"+row+","+col);
            m1.addActionListener(this);
            JMenuItem m2=new JMenuItem("Edit");
            m2.setActionCommand("Edit Grade"+row+","+col);
            m2.addActionListener(this);
            JMenuItem m3=new JMenuItem("Delete");
            m3.setActionCommand("Delete Grade"+row+","+col);
            m3.addActionListener(this);
            m.add(m1);
            m.add(m2);
            m.add(m3);
            return m;
        }
        else{ //if its a name
            TextName name=sheet.getNameAt(row);
            if(name==null){
                return null;
            }
            JPopupMenu m=new JPopupMenu();
            JMenuItem m1=new JMenuItem("Edit");
            m1.setActionCommand("Edit Name"+row+","+col);
            m1.addActionListener(this);
            JMenuItem m2=new JMenuItem("Delete");
            m2.setActionCommand("Delete Name"+row+","+col);
            m2.addActionListener(this);
            m.add(m1);
            m.add(m2);
            return m;
        }
    }
    private JPopupMenu createColHeaderRightClickMenu(int col){
        col--;
        if(col==-1){
            return null;
        }
        TextAssignment assign=sheet.getAssignmentAt(col);
        if(assign==null){
            return null;
        }
        JPopupMenu m=new JPopupMenu();
        JMenuItem m1=new JMenuItem("Edit");
        m1.setActionCommand("Edit Assignment"+col);
        m1.addActionListener(this);
        JMenuItem m2=new JMenuItem("Delete");
        m2.setActionCommand("Delete Assignment"+col);
        m2.addActionListener(this);
        m.add(m1);
        m.add(m2);
        
        return m;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().startsWith("Toggle Gradebook Status")){
            int[] coords=extractCoords("Toggle Gradebook Status",e.getActionCommand());
            TextGrade grade=sheet.getGradeAt(coords[1]-1,coords[0]);
            if(grade!=null){
               setInGradebook(coords[0],coords[1],!grade.inGradebook); 
            }
        }
        else if(e.getActionCommand().startsWith("Edit Grade")){
            int[] coords=extractCoords("Edit Grade",e.getActionCommand());
            changeGrade(coords[0],coords[1]);
        }
        else if(e.getActionCommand().startsWith("Delete Grade")){
            int[] coords=extractCoords("Delete Grade",e.getActionCommand());
            deleteGrade(coords[0],coords[1]);
        }
        else if(e.getActionCommand().startsWith("Edit Name")){
            int[] coords=extractCoords("Edit Name",e.getActionCommand());
            changeName(coords[0]);
        }
        else if(e.getActionCommand().startsWith("Delete Name")){
            int[] coords=extractCoords("Delete Name",e.getActionCommand());
            deleteName(coords[0]);
        }
        else if(e.getActionCommand().startsWith("Edit Assignment")){
            int col=extractNumber("Edit Assignment",e.getActionCommand());
            changeAssignment(col);
        }
        else if(e.getActionCommand().startsWith("Delete Assignment")){
            int col=extractNumber("Delete Assignment",e.getActionCommand());
            deleteAssignment(col);
        }
    }
    private void changeGrade(int row,int col){
        TextGrade grade=sheet.getGradeAt(col-1, row);
        if(grade!=null){
            final String gradeChoice=JOptionPane.showInputDialog("What would you like to change the grade to?",grade.grade);
            final String commentChoice=JOptionPane.showInputDialog("What would you like to change the comment to?",grade.comment);
            if(gradeChoice==null||commentChoice==null){
                return;
            }
            final String name=sheet.getNameAt(row).firstName+sheet.getNameAt(row).lastName;
            final int assignment=sheet.getAssignmentAt(col-1).number;
            gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getGrader().setGrade(name, assignment, gradeChoice, commentChoice, true);
                repaint();
                gui.fileBrowserDataChanged();
            }
        });
        }
    }
    private void changeName(final int row){
        final TextName name=sheet.getNameAt(row);
        final NameOverlay overlay=new NameOverlay(gui);
        overlay.setData(name.firstName,name.lastName);
        overlay.setCallback(new Runnable() {
            @Override
            public void run() {
                gui.getGrader().downloadSheet();
                String[] names=overlay.getNames();
                boolean success=sheet.changeName(name,names);
                if(success){
                    gui.getGrader().uploadTable();
                    revalidate();
                    repaint();
                    gui.fileBrowserDataChanged();
                }
                else{
                    gui.getGrader().forceDownloadSheet(); //revert our changes
                }
            }
        });
        gui.getViewManager().addOverlay(overlay);
    }
    private void changeAssignment(final int col){
        TextAssignment assign=sheet.getAssignmentAt(col);
        final AssignmentOverlay overlay=new AssignmentOverlay(gui);
        overlay.setData(assign.number, assign.name);
        overlay.setCallback(new Runnable() {
            @Override
            public void run() {
                gui.getGrader().downloadSheet();
                Object[] data=overlay.getData();
                TextAssignment assign=sheet.getAssignmentAt(col);
                assign.number=(int)data[0];
                assign.name=(String)data[1];
                gui.getGrader().uploadTable();
                dataChanged();
            }
        });
        gui.getViewManager().addOverlay(overlay);
    }
    private void deleteName(final int row){
        TextName name=sheet.getNameAt(row);
        int choice=GuiHelper.multiOptionPane("Are you sure you would like to delete "+
                name.firstName+" "+name.lastName+" and ALL their assignments?",new String[]{"Yes","No"});
        if(choice==0){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.getGrader().downloadSheet();
                    TextName name=sheet.getNameAt(row);
                    sheet.deleteName(name);
                    gui.getGrader().uploadTable();

                    dataChanged();
                }
            });
        }
    }
    private void deleteGrade(int row,int col){
        final TextAssignment assign=sheet.getAssignmentAt(col-1);
        final TextName name=sheet.getNameAt(row);
        final TextGrade grade=sheet.getGradeAt(col-1, row);
        int choice=GuiHelper.multiOptionPane("Are you sure you would like to delete grade of "+grade.grade+", "+grade.comment
                +" by "+name+" on Assignment "+assign,new String[]{"Yes","No"});
        if(choice==0){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.getGrader().downloadSheet();
                    sheet.deleteGrade(name, assign, grade);
                    gui.getGrader().uploadTable();
                    
                    dataChanged();
                    gui.fileBrowserDataChanged();
                }
            });
        }
    }
    private void deleteAssignment(final int col){
        final TextAssignment assign=sheet.getAssignmentAt(col);
        int choice=GuiHelper.multiOptionPane("Are you sure you would like to delete Assignment "+
                assign+"?",new String[]{"Yes","No"});
        if(choice==0){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.getGrader().downloadSheet();
                    boolean success=sheet.deleteAssignmentAt(col);
                    if(success){
                        gui.getGrader().uploadTable();
                        dataChanged();
                    }
                    else{
                        GuiHelper.alertDialog("You must delete all grades under an assignment before deleting that assignment.");
                    }
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
    private String[] extractValues(String key,String data){
        data=data.replace(key, "");
        String[] datas=data.split("÷"); //yea no ones name will contain that
        return datas;
    }
    private int extractNumber(String key,String data){
        data=data.replace(key, "");
        return Integer.parseInt(data);
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void columnMoved(TableColumnModelEvent e){
        super.columnMoved(e);
        int from=e.getFromIndex();
        int to=e.getToIndex();
        //System.out.println(from+" to "+to);
        from=convertColumnIndexToModel(to);
        to=convertColumnIndexToModel(e.getFromIndex());
        if(e.getFromIndex()==e.getToIndex()){
            return;
        }
        System.out.println(from+" to "+to);
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        super.columnMarginChanged(e);
        int cols=getColumnModel().getColumnCount();
        String width="";
        for(int x=0;x<cols;x++){
            width+=getColumnModel().getColumn(x).getPreferredWidth();
            if(x!=cols-1){
                width+=",";
            }
        }
        System.out.println(width);
    }
}
