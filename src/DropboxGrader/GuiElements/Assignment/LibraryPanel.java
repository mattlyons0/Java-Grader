/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.Assignment;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Handles the gui for adding a library to be run with an assignment
 * @author 141lyonsm
 */
public class LibraryPanel extends JPanel implements ActionListener{
    private Gui gui;
    
    private ArrayList<String> libraries;
    
    private ArrayList<JButton> browseButtons;
    private ArrayList<JLabel> statuses;
    private ArrayList<JButton> removeLibraries;
    private JButton plusButton;
    private JButton addLibraryButton;
    
    public LibraryPanel(Gui gui){
        this.gui=gui;
        
        setLayout(new GridBagLayout());
        
        browseButtons=new ArrayList();
        statuses=new ArrayList();
        removeLibraries=new ArrayList();
        
        libraries=new ArrayList();
        
        plusButton=new JButton("+");
        plusButton.setToolTipText("Add Library");
        plusButton.addActionListener(this);
        addLibraryButton=new JButton("Add Library");
        addLibraryButton.addActionListener(this);
        
        setup();
    }
    private void setup(){
        removeAll();
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        for(int i=0;i<libraries.size();i++){
            if(browseButtons.size()<=i){
                JLabel status=new JLabel("Select A Library");
                JButton browse=new JButton("Browse");
                browse.setActionCommand("BrowseLibrary"+i);
                browse.addActionListener(this);
                JButton remove=new JButton("-");
                remove.setToolTipText("Remove This Library");
                remove.setActionCommand("RemoveLibrary"+i);
                remove.addActionListener(this);
                statuses.add(status);
                browseButtons.add(browse);
                removeLibraries.add(remove);
            }
            cons.gridx=0;
            add(statuses.get(i),cons);
            cons.gridx++;
            add(browseButtons.get(i),cons);
            cons.gridx++;
            add(removeLibraries.get(i),cons);
            
            if(i==libraries.size()-1){
                cons.gridx++;
                add(plusButton,cons);
            }
            cons.gridy++;            
        }
        if(libraries.isEmpty()){
            add(addLibraryButton);
        }
        
        revalidate();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(plusButton)||e.getSource().equals(addLibraryButton)){
            libraries.add(null);
            setup();
        }
        else if(e.getActionCommand().startsWith("RemoveLibrary")){
            int num=GradebookTable.extractNumber("RemoveLibrary", e.getActionCommand());
            
            libraries.remove(num);
            
            browseButtons.remove(num);
            removeLibraries.remove(num);
            statuses.remove(num);
            setup();
        }
        else if(e.getActionCommand().startsWith("BrowseLibrary")){
            int num=GradebookTable.extractNumber("BrowseLibrary", e.getActionCommand());
            
        }
    }
}
