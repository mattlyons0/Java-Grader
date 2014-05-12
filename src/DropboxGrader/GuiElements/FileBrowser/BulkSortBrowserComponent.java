/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.FileBrowser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 *
 * @author 141lyonsm
 */
public class BulkSortBrowserComponent extends JComponent{
    private String[] bulkOperations={"Bulk Actions","Change Assignment Number","Change Assignment Name",
        "Delete", "Forcibly Delete"};
    
    private JComboBox bulkActionsBox;
    private JButton applyBulkActionsButton;
    
    public BulkSortBrowserComponent(){
        
        init();
    }
    
    private void init(){
        bulkActionsBox=new JComboBox(bulkOperations);
        applyBulkActionsButton=new JButton("Apply");
        
        setLayout(new GridBagLayout());
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        
        add(bulkActionsBox,cons);
        cons.gridx++;
        add(applyBulkActionsButton,cons);
    }
}
