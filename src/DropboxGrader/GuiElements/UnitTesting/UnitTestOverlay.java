/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.UnitTesting;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Matt
 */
public class UnitTestOverlay extends ContentOverlay{
    private Gui gui;
    
    private JLabel statusLabel;
    private JLabel currentTestInfo;
    private JButton cancelButton;
    private JOutputTerminal testOutput;
    
    private int closingSeconds;
    private boolean canceled;
    private boolean cancelClose;
    private Timer timer;
    public UnitTestOverlay(Gui gui){
        super("UnitTestOverlay");
        this.gui=gui;
        
        canceled=false;
        cancelClose=false;
    }

    @Override
    public void setup() {
        statusLabel=new JLabel("");
        currentTestInfo=new JLabel("");
        cancelButton=new JButton("Cancel Unit Testing");
        cancelButton.addActionListener(this);
        testOutput=new JOutputTerminal();
        testOutput.setEditable(false);
        JScrollPane outputPane=new JScrollPane(testOutput);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        add(statusLabel,cons);
        cons.gridy++;
        add(currentTestInfo,cons);
        cons.gridy++;
        add(cancelButton,cons);
        cons.gridy++;
        cons.weighty=10;
        cons.fill=GridBagConstraints.BOTH;
        add(outputPane,cons);
        
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        Dimension size=getSize();
        setLocation((parentSize.width-size.width)/2,(parentSize.height-size.height)/2);
    }

    @Override
    public void switchedTo() {}

    @Override
    public boolean isClosing() {
        if(timer!=null)
            timer.cancel();
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(cancelButton)){
            if(timer==null){
                canceled=true;
                cancelButton.setEnabled(false);
                cancelButton.setText("Canceling...");
                cancelButton.setToolTipText("Finishing Current Test.");
            } else{
                cancelClose=true;
                remove(cancelButton);
                cancelButton=null;
                revalidate();
                repaint();
            }
        }
    }
    public boolean isCanceled(){
        return canceled;
    }
    public void setStatus(String status){
        if(statusLabel!=null)
            statusLabel.setText(status+"...");
    }
    public void setDescription(String desc){
        if(currentTestInfo!=null)
            currentTestInfo.setText(desc);
    }
    public void finished(){
        if(testOutput!=null&&testOutput.getText().equals("")){//no text has been printed
            isClosing();
            dispose();
        }
        if(timer!=null)
            return;
        if(testOutput!=null&&testOutput.errorsOccured()){
            statusLabel.setText("Errors Running Tests");
            statusLabel.setForeground(Color.RED);
            currentTestInfo.setText("Errors logged below.");
            remove(cancelButton);
            cancelButton=null;
            revalidate();
            repaint();
            return;
        }
        if(cancelButton!=null){
            cancelButton.setEnabled(true);
            cancelButton.setText("Cancel Closing");
            cancelButton.setToolTipText("");
        }        
        
        closingSeconds=5;
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(cancelClose){
                    timer.cancel();
                    setDescription("");
                    return;
                }
                setDescription("Closing in "+closingSeconds+" second"+(closingSeconds==1?"":"s")+".");
                closingSeconds--;
                if(closingSeconds==-1){
                    isClosing();
                    dispose();
                }
            }
        }, 0,1000);
    }
    public void append(String s){
        append(s,Color.BLACK);
    }
    public void append(String s,Color c){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
        testOutput.append(s+"\n",c);
    }
    
}
