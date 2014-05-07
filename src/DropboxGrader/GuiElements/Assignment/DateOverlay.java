/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.Assignment;

import DropboxGrader.FileManagement.Date;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import org.jdesktop.swingx.JXDatePicker;

/**
 *
 * @author matt
 */
public class DateOverlay extends ContentOverlay implements CaretListener{
    private AssignmentOverlay overlay;
    private Gui gui;
    
    private JXDatePicker datePicker;
    private JTextField hourSpinner; //spinners are awful, edit the text box and everything goes to hell
    private JTextField minuteSpinner;
    private JTextField secondSpinner;
    private JComboBox ampmSpinner;
    
    private Date date;
    
    public DateOverlay(AssignmentOverlay overlay,Gui gui){
        super("DateOverlay"+overlay.getID(),true);
        
        this.overlay=overlay;
        this.gui=gui;
        date=new Date();
    }
    @Override
    public void setup() {
        datePicker=new JXDatePicker();
        datePicker.setDate(Date.currentDate().toDate());
        datePicker.addActionListener(this);
        datePicker.setLightWeightPopupEnabled(false);
        
        hourSpinner=new JTextField(3);
        hourSpinner.setDocument(new NumberRangeDocument(0,13,hourSpinner));
        hourSpinner.addCaretListener(this);
        hourSpinner.repaint();
        minuteSpinner=new JTextField(3);
        minuteSpinner.setDocument(new NumberRangeDocument(-1,60,minuteSpinner));
        minuteSpinner.addCaretListener(this);
        secondSpinner=new JTextField(3);
        secondSpinner.setDocument(new NumberRangeDocument(-1,60,secondSpinner));
        secondSpinner.addCaretListener(this);
        ampmSpinner=new JComboBox(new String[]{"AM","PM"});
        ampmSpinner.addActionListener(this);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.fill=GridBagConstraints.NONE;
        
        JPanel timePanel=new JPanel();
        timePanel.setLayout(new GridBagLayout());
        timePanel.add(new JLabel("Time: "),cons);
        cons.gridx++;
        timePanel.add(hourSpinner,cons);
        cons.gridx++;
        timePanel.add(new JLabel(":"),cons);
        cons.gridx++;
        timePanel.add(minuteSpinner,cons);
        cons.gridx++;
        timePanel.add(new JLabel(":"),cons);
        cons.gridx++;
        timePanel.add(secondSpinner,cons);
        cons.gridx++;
        timePanel.add(ampmSpinner,cons);
        
        add(datePicker,cons);
        cons.gridx++;
        add(timePanel,cons);

        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
        setTitle("Set Due Date");
        setVisible(true);
    }

    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        try{
            date.hour=Integer.parseInt(hourSpinner.getText());
            date.minute=Integer.parseInt(minuteSpinner.getText());
            date.second=Integer.parseInt(secondSpinner.getText());
            actionPerformed(new ActionEvent(datePicker,0,null));
        } catch(NumberFormatException e){
            //some fields were blank
            overlay.setDate(null);
        }
        
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(datePicker)){
            if(datePicker.getDate()!=null){
                Date d=new Date(datePicker.getDate());
                date.year=d.year;
                date.month=d.month;
                date.day=d.day;
                overlay.setDate(date);
            }
        }
        else if(e.getSource().equals(ampmSpinner)){
            int hour=date.hour;
            if(hour>12&&ampmSpinner.getSelectedIndex()==0)
                hour-=12;
            else if(hour<13&&ampmSpinner.getSelectedIndex()==1)
                hour+=12;
            date.hour=hour;
            overlay.setDate(date);
        }
    }
    public void setDate(Date d){
        if(d!=null)
            date=d;
        if(d!=null&&datePicker!=null){
            datePicker.setDate(d.toDate());
            int hour=d.hour;
            if(hour>12){
                ampmSpinner.setSelectedIndex(1);
                hourSpinner.setText(hour-12+"");
            } else{
                ampmSpinner.setSelectedIndex(0);
                hourSpinner.setText(hour+"");
            }
            minuteSpinner.setText(d.minute+"");
            secondSpinner.setText(d.second+"");
        }
    }
    @Override
    public void caretUpdate(CaretEvent e) {
        if(e.getSource().equals(hourSpinner)){
            try{
                date.hour=Integer.parseInt(hourSpinner.getText());
                actionPerformed(new ActionEvent(ampmSpinner,0,null));
            } catch(NumberFormatException ex){
                //nothing has been entered yet
            }
        }
        else if(e.getSource().equals(minuteSpinner)){
            try{
                date.minute=Integer.parseInt(minuteSpinner.getText());
                overlay.setDate(date);
            } catch(NumberFormatException ex){
                //nothing has been entered yet
            }
        }
        else if(e.getSource().equals(secondSpinner)){
            try{
                date.second=Integer.parseInt(secondSpinner.getText());
                overlay.setDate(date);
            } catch(NumberFormatException ex){
                //nothing has been entered yet
            }
        }
    }
    
}
