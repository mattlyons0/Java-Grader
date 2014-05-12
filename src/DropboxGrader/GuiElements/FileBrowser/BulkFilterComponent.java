/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 *
 * @author 141lyonsm
 */
public class BulkFilterComponent extends JComponent{
    private String[] bulkOperations={"Bulk Actions","Change Assignment Number","Change Assignment Name",
        "Delete", "Forcibly Delete"};
    private BrowserView view;
    
    private JComboBox bulkActionsBox;
    private JButton applyBulkActionsButton;
    
    private JButton filterButton;
    
    public BulkFilterComponent(BrowserView view){
        this.view=view;
        
        init();
    }
    
    private void init(){
        bulkActionsBox=new JComboBox(bulkOperations);
        applyBulkActionsButton=new JButton("Apply");
        
        filterButton=new JButton("Filter");
        
        setLayout(new GridBagLayout());
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        
        add(bulkActionsBox,cons);
        cons.gridx++;
        add(applyBulkActionsButton,cons);
        
        cons.gridx++;
        add(filterButton,cons);
    }
}
