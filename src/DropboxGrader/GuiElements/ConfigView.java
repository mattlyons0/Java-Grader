/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.DbxSession;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.GuiHelper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author Matt
 */
public class ConfigView extends ContentView implements FocusListener{
    private Gui gui;
    
    private JTextField gradesFolder;
    private JTextField dropboxFolder;
    private JTextField dropboxPeriod;
    private JTextField runTimes;
    private JCheckBox autoRun;
    private JButton backToBrowser;
    private JLabel statusLabel;
    
    public ConfigView(Gui gui){
        super("ConfigView");
        
        this.gui=gui;
    }
    @Override
    public void setup() {
        statusLabel=new JLabel("");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        gradesFolder=new JTextField(25);
        gradesFolder.setText(Config.dropboxSpreadsheetFolder);
        dropboxFolder=new JTextField(25);
        dropboxFolder.setText(Config.dropboxFolder);
        dropboxPeriod=new JTextField(3);
        dropboxPeriod.setText(Config.dropboxPeriod+"");
        runTimes=new JTextField(3);
        runTimes.setText(Config.runTimes+"");
        autoRun=new JCheckBox("AutoRun");
        autoRun.setSelected(Config.autoRun);     
        backToBrowser=new JButton("Back");
        backToBrowser.addActionListener(this);
        JLabel creditsLabel=new JLabel(DbxSession.APPNAME+" V"+DbxSession.APPVERSION+" Created by Matt Lyons");
        creditsLabel.setHorizontalAlignment(JLabel.CENTER);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.NORTHWEST;
        cons.weighty=1;
        cons.weightx=1;
        cons.gridx=0;
        cons.gridy=0;
        add(backToBrowser,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.NONE;
        cons.ipadx=5;
        cons.ipady=5;
        cons.gridx=1;
        cons.gridwidth=2;
        add(statusLabel,cons);
        cons.gridwidth=1;
        cons.gridx=0;
        cons.gridy=1;
        add(new JLabel("Grades Folder: "),cons);
        cons.gridx=1;
        add(gradesFolder,cons);
        cons.gridx=0;
        cons.gridy=2;
        add(new JLabel("Dropbox Folder: "),cons);
        cons.gridx=1;
        add(dropboxFolder,cons);
        cons.gridx=2;
        add(new JLabel("Class Period: "),cons);
        cons.gridx=3;
        add(dropboxPeriod,cons);
        cons.gridy=3;
        cons.gridx=0;
        add(new JLabel("Default Output Runs: "),cons);
        cons.gridx=1;
        add(runTimes,cons);
        cons.gridx=3;
        add(autoRun,cons);
        cons.gridy=4;
        cons.gridx=0;
        cons.gridwidth=4;
        add(creditsLabel,cons);
        
        revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(backToBrowser)){
            Config.dropboxSpreadsheetFolder=gradesFolder.getText();
            Config.dropboxFolder=dropboxFolder.getText();
            Config.dropboxPeriod=DbxFile.safeStringToInt(dropboxPeriod.getText());
            try{
                Config.runTimes=Integer.parseInt(runTimes.getText());
            } catch(NumberFormatException ex){
                GuiHelper.alertDialog("Default Output Runs was set to a invalid number.");
            }
            Config.autoRun=autoRun.isSelected();
            Config.writeConfig();
            gui.getGrader().refresh();
            gui.getManager().refresh();

            gui.setupFileBrowserGui();
        }
    }

    @Override
    public void switchedTo() {

    }

    @Override
    public void focusGained(FocusEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void focusLost(FocusEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
