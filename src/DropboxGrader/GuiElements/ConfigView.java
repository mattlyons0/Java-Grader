/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements;

import DropboxGrader.Config;
import DropboxGrader.DbxFile;
import DropboxGrader.FileManager;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import DropboxGrader.GuiHelper;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author Matt
 */
public class ConfigView extends ContentView{
    private Gui gui;
    
    private JTextField spreadsheetName;
    private JTextField dropboxFolder;
    private JTextField dropboxPeriod;
    private JTextField runTimes;
    private JCheckBox autoRun;
    private JButton backToBrowser;
    
    public ConfigView(Gui gui){
        super("ConfigView");
        
        this.gui=gui;
    }
    @Override
    public void setup() {
        spreadsheetName=new JTextField(25);
        spreadsheetName.setText(Config.spreadsheetName);
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
        JLabel creditsLabel=new JLabel("Created by Matt Lyons");
        creditsLabel.setHorizontalAlignment(JLabel.CENTER);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.NORTHWEST;
        cons.weighty=1;
        cons.weightx=1;
        cons.gridx=0;
        cons.gridy=0;
        add(backToBrowser,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.gridy=1;
        add(new JLabel("Spreadsheet Name: "),cons);
        cons.gridx=1;
        add(spreadsheetName,cons);
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
            Config.spreadsheetName=spreadsheetName.getText();
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
    
}
