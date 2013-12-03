/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

/**
 *
 * @author 141lyonsm
 */
public class FileBrowserListener implements ActionListener,MouseListener,RowSorterListener{
    private JTable table;
    private Gui gui;
    private long lastClick;
    private int lastRow;
    private final long DOUBLECLICKDELAY=250;
    public FileBrowserListener(Gui gui){
        this.gui=gui;
        lastClick=DOUBLECLICKDELAY*-1;
        lastRow=-1;
    }
    public void setTable(JTable t){
        table=t;
    }
    private JPopupMenu createRightClickMenu(int row){
        JPopupMenu m=new JPopupMenu();
        JMenuItem m1=new JMenuItem("Rename");
        gui.getManager().getFile(row);
        m1.setActionCommand("Rename"+row);
        m1.addActionListener(this);
        m.add(m1);
        return m;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1){
            long currentClick=System.currentTimeMillis();
            if(currentClick-lastClick<=DOUBLECLICKDELAY&&lastRow==table.convertRowIndexToModel(table.getSelectedRow())){
                gui.gradeRows();
                lastClick=DOUBLECLICKDELAY*-1;
                lastRow=-1;
            }
            else{
                lastClick=currentClick;
                lastRow=table.convertRowIndexToModel(table.getSelectedRow());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
                table.setRowSelectionInterval(r, r);
            } else {
                table.clearSelection();
            }

            int rowindex = table.convertRowIndexToModel(table.getSelectedRow());
            if (rowindex < 0)
                return;
            if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                JPopupMenu popup = createRightClickMenu(r);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().contains("Rename")){
            int f=Integer.parseInt(e.getActionCommand().replace("Rename", ""));
            DbxFile file=gui.getManager().getFile(f);
            String choice=JOptionPane.showInputDialog("What would you like to name the file?",file.getFileName());
            if(choice!=null){
                file.rename(choice);
                gui.setupFileBrowserGui();
            }
        }
    }

    @Override
    public void sorterChanged(RowSorterEvent e) {
        Config.sortOrder=table.getRowSorter().getSortKeys().get(0).getSortOrder().toString();
        Config.sortColumn=table.getRowSorter().getSortKeys().get(0).getColumn()+"";
    }
    
}
