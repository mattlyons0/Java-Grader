/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.Date;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.Assignment.DateOverlay;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.FileBrowser.BulkFilterComponent;
import DropboxGrader.GuiElements.MiscComponents.IntegerDocument;
import DropboxGrader.TextGrader.TextSpreadsheet;
import DropboxGrader.Util.DateComparator;
import DropboxGrader.Util.SettableDate;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author Matt
 */
public class FilterOverlay extends ContentOverlay implements SettableDate,CaretListener{
    private Gui gui;
    private BulkFilterComponent comp;
    
    private JTextField assignmentField;
    private JTextField assignmentNField;
    private JCheckBox assignmentNameCheck;
    private JButton dateButton;
    private JComboBox beforeAfterDate;
    private JTextField nameField;
    private JCheckBox caseNameCheck;
    private JComboBox statusField;
    private JButton removeFiltersButton;
    
    private RowFilter filter;
    private Date date;
    
    public FilterOverlay(BulkFilterComponent comp,Gui gui){
        super("FilterOverlay",true);
        
        this.gui=gui;
        this.comp=comp;
        
        filter=new RowFilter() {

            @Override
            public boolean include(RowFilter.Entry entry) {
                if(assignmentField==null) //the gui hasnt been initialized yet
                    return true;
                if(!assignmentField.getText().equals("")){
                    if(!entry.getValue(0).equals(assignmentField.getText()))
                        return false;
                }
                if(!assignmentNField.getText().equals("")){
                    String value=entry.getStringValue(1);
                    String text=assignmentNField.getText();
                    if(assignmentNameCheck.isSelected()){
                        value=value.toLowerCase();
                        text=text.toLowerCase();
                    }
                    if(!value.contains(text))
                        return false;
                }
                if(date!=null){
                    DateComparator compare=new DateComparator();
                    int val=compare.compare((String)entry.getValue(2), date.toString());
                    if(beforeAfterDate.getSelectedIndex()==0){
                        if(val>=0)
                            return false;
                    }
                    else if(beforeAfterDate.getSelectedIndex()==1){
                        if(val<=0)
                            return false;
                    }
                    else if(beforeAfterDate.getSelectedIndex()==2){
                        if(val!=0)
                            return false;
                    }
                }
                if(!nameField.getText().equals("")){
                    String value=(String)entry.getValue(3);
                    String text=nameField.getText();
                    if(caseNameCheck.isSelected()){
                        value=value.toLowerCase();
                        text=text.toLowerCase();
                    }
                    
                    if(!value.contains(nameField.getText()))
                        return false;
                }
                if(statusField.getSelectedIndex()!=0){
                    int index=statusField.getSelectedIndex();
                    String value=(String)entry.getValue(4);
                    
                    if(index==1){
                        if(!value.contains("Graded: "))
                            return false;
                    }
                    else if(index==2){
                        if(!value.contains("Downloaded"))
                            return false;
                    }
                    else if(index==3){
                        if(!value.contains("On Server"))
                            return false;
                    }
                    else if(index==4){
                        if(!value.contains("Invalid"))
                            return false;
                    }
                }
                return true;
            }
        };
        comp.setFilter(filter);
    }
    @Override
    public void setup() {
        if(assignmentField!=null){
            setVisible(true);
            return;
        }
        assignmentField=new JTextField(5);
        assignmentField.setDocument(new IntegerDocument());
        assignmentField.addCaretListener(this);
        assignmentNField=new JTextField(15);
        assignmentNField.addCaretListener(this);
        assignmentNameCheck=new JCheckBox("Ignore Case");
        assignmentNameCheck.setSelected(true);
        assignmentNameCheck.addActionListener(this);
        beforeAfterDate=new JComboBox(new String[]{"Before","After","Exactly"});
        beforeAfterDate.addActionListener(this);
        dateButton=new JButton("No Date Set");
        dateButton.addActionListener(this);
        nameField=new JTextField(15);
        nameField.addCaretListener(this);
        caseNameCheck=new JCheckBox("Ignore Case");
        caseNameCheck.setSelected(true);
        caseNameCheck.addActionListener(this);
        statusField=new JComboBox(new String[]{"Any","Graded","Downloaded","On Server","Invalid File"});
        statusField.addActionListener(this);
        removeFiltersButton=new JButton("Remove All Filters");
        removeFiltersButton.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        
        add(new JLabel("Assignment Number"),cons);
        cons.gridx++;
        add(new JLabel("Assignment Name"),cons);
        cons.gridx++;
        add(new JLabel("Submission Date"),cons);
        cons.gridx++;
        add(new JLabel("Name"),cons);
        cons.gridx++;
        add(new JLabel("Status"),cons);
        
        cons.gridy++;
        cons.gridx=0;
        add(assignmentField,cons);
        cons.gridx++;
        add(assignmentNField,cons);
        cons.gridx++;
        add(beforeAfterDate,cons);
        cons.gridx++;
        add(nameField,cons);
        cons.gridx++;
        add(statusField,cons);
        
        cons.gridy++;
        cons.gridx=1;
        add(assignmentNameCheck,cons);
        cons.gridx++;
        add(dateButton,cons);
        cons.gridx++;
        add(caseNameCheck,cons);
        
        cons.gridx=0;
        cons.gridy++;
        cons.gridwidth=5;
        add(removeFiltersButton,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.75),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setVisible(true);
    }

    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        gui.getViewManager().removeOverlay("DateOverlay"+getID());
        
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(dateButton)){
            gui.getViewManager().removeOverlay("DateOverlay"+getID());
            
            DateOverlay overlay=new DateOverlay(this,gui,getID());
            
            gui.getViewManager().addOverlay(overlay);
            overlay.setDate(date);
        }
        else if(e.getSource().equals(removeFiltersButton)){
            assignmentField.setText("");
            assignmentNField.setText("");
            assignmentNameCheck.setSelected(true);
            beforeAfterDate.setSelectedIndex(0);
            caseNameCheck.setSelected(true);
            setDate(null);
            nameField.setText("");
            statusField.setSelectedIndex(0);
        }
        updateFilter();
    }

    @Override
    public void setDate(Date d) {
        date=d;
        if(d!=null){
            dateButton.setText("Date: "+d.toString());
        }
        else{
            dateButton.setText("No Date Set");
        }
        updateFilter();
    }
    private void updateFilter(){
        comp.setFilter(filter);
        
        String initialText="Filtering by ";
        ArrayList<String> filters=new ArrayList();
        
        if(!assignmentField.getText().equals(""))
            filters.add("Assignment Number");
        if(!assignmentNField.getText().equals(""))
            filters.add("Assignment Name");
        if(date!=null)
            filters.add("Date");
        if(!nameField.getText().equals(""))
            filters.add("Name");
        if(statusField.getSelectedIndex()!=0)
            filters.add("Status");
        
        if(filters.isEmpty()){
            comp.setFilterText("Filter");
            return;
        }
        String filterString="";
        for(int i=0;i<filters.size();i++){
            filterString+=filters.get(i);
            if(i!=filters.size()-1)
                filterString+=", ";
        }
        comp.setFilterText(initialText+filterString);
        comp.setFilterData(getData());
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        updateFilter();
    }
    public String getData(){
        String s="";
        s+=assignmentField.getText()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=assignmentNField.getText()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=assignmentNameCheck.isSelected()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=beforeAfterDate.getSelectedIndex()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=caseNameCheck.isSelected()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=(date==null?"null":date.toText())+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=nameField.getText()+TextSpreadsheet.INDIVIDUALDELIMITER;
        s+=statusField.getSelectedIndex();
        
        return s;
    }
    public void setData(String data){
        String[] s=data.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        
        assignmentField.setText(s[0]);
        assignmentNField.setText(s[1]);
        assignmentNameCheck.setSelected(Boolean.parseBoolean(s[2]));
        beforeAfterDate.setSelectedIndex(Integer.parseInt(s[3]));
        caseNameCheck.setSelected(Boolean.parseBoolean(s[4]));
        if(!s[5].equals("null"))
            setDate(new Date(s[5]));
        nameField.setText(s[6]);
        statusField.setSelectedIndex(Integer.parseInt(s[7]));
    }
    
}
