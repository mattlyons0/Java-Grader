/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscViews;

import DropboxGrader.Config;
import DropboxGrader.DbxSession;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

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
    private JLabel errorLabel;
    private JLabel jUnitJarLabel;
    private JLabel jUnitHamcrestLabel;
    private JButton setJUnitJar;
    private JButton setJUnitHamcrestJar;
    private JButton downloadJUnitButton;
    
    private String jUnitString="JUnit Jar Location:  ";
    private String jUnitHamcrestString="JUnit Hamcrest Jar Location: ";
    private FileFilter jarFilter;
    
    
    public ConfigView(Gui gui){
        super("ConfigView");
        
        this.gui=gui;
        
        jarFilter=new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.getName().toLowerCase().endsWith(".jar")||file.isDirectory())
                    return true;
                return false;
            }
            @Override
            public String getDescription() {
                return "*.jar";
            }
        };
    }
    @Override
    public void setup() {
        statusLabel=new JLabel("");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        errorLabel=new JLabel();
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        errorLabel.setForeground(Color.red);
        gradesFolder=new JTextField(25);
        gradesFolder.setText(Config.dropboxSpreadsheetFolder);
        gradesFolder.addFocusListener(this);
        dropboxFolder=new JTextField(25);
        dropboxFolder.setText(Config.dropboxFolder);
        dropboxFolder.addFocusListener(this);
        dropboxPeriod=new JTextField(3);
        dropboxPeriod.setText(Config.dropboxPeriod+"");
        dropboxPeriod.addFocusListener(this);
        runTimes=new JTextField(3);
        runTimes.setText(Config.runTimes+"");
        runTimes.addFocusListener(this);
        autoRun=new JCheckBox("AutoRun");
        autoRun.setSelected(Config.autoRun);     
        autoRun.addFocusListener(this);
        backToBrowser=new JButton("Back");
        backToBrowser.addActionListener(this);
        JLabel junitLabel=new JLabel("<html><b>JUnit</b> Download the Jar Files from </html>");
        junitLabel.setHorizontalAlignment(JLabel.RIGHT);
        downloadJUnitButton=new JButton("JUnit.org");
        downloadJUnitButton.addActionListener(this);
        jUnitJarLabel=new JLabel(jUnitString);
        setJUnitJar=new JButton("Browse");
        setJUnitJar.addActionListener(this);
        jUnitHamcrestLabel=new JLabel(jUnitHamcrestString);
        setJUnitHamcrestJar=new JButton("Browse");
        setJUnitHamcrestJar.addActionListener(this);
        JLabel creditsLabel=new JLabel(DbxSession.APPNAME+" V"+DbxSession.getVersion()+" Created by Matt Lyons");
        creditsLabel.setHorizontalTextPosition(JLabel.CENTER);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.NORTHWEST;
        cons.weighty=GridBagConstraints.RELATIVE;
        cons.weightx=1;
        cons.gridx=0;
        cons.gridy=0;
        add(backToBrowser,cons);
        cons.insets=new Insets(0,0,0,0);
        cons.anchor=GridBagConstraints.CENTER;
        cons.fill=GridBagConstraints.BOTH;
        cons.ipadx=5;
        cons.ipady=5;
        cons.gridy=1;
        cons.gridwidth=4;
        add(statusLabel,cons);
        cons.gridy=2;
        add(errorLabel,cons);
        cons.insets=new Insets(5,5,5,5);
        cons.fill=GridBagConstraints.NONE;
        cons.weighty=5;
        cons.gridwidth=1;
        cons.gridy=3;
        add(new JLabel("Grades Folder: "),cons);
        cons.gridx=1;
        add(gradesFolder,cons);
        cons.gridx=0;
        cons.gridy=4;
        add(new JLabel("Dropbox Folder: "),cons);
        cons.gridx=1;
        add(dropboxFolder,cons);
        cons.gridx=2;
        add(new JLabel("Class Period: "),cons);
        cons.gridx=3;
        add(dropboxPeriod,cons);
        cons.gridy=5;
        cons.gridx=0;
        add(new JLabel("Default Output Runs: "),cons);
        cons.gridx=1;
        add(runTimes,cons);
        cons.gridx=3;
        add(autoRun,cons);
        cons.gridy=6;
        cons.gridx=0;
        cons.gridwidth=2;
        cons.anchor=GridBagConstraints.EAST;
        add(junitLabel,cons);
        cons.gridx=2;
        cons.gridwidth=1;
        cons.anchor=GridBagConstraints.WEST;
        add(downloadJUnitButton,cons);
        cons.anchor=GridBagConstraints.EAST;
        cons.gridy=7;
        cons.gridx=0;
        add(jUnitJarLabel,cons);
        cons.gridx=1;
        cons.anchor=GridBagConstraints.WEST;
        add(setJUnitJar,cons);
        cons.gridx=2;
        cons.anchor=GridBagConstraints.EAST;
        add(jUnitHamcrestLabel,cons);
        cons.gridx=3;
        cons.anchor=GridBagConstraints.WEST;
        add(setJUnitHamcrestJar,cons);
        cons.anchor=GridBagConstraints.CENTER;
        cons.gridy=8;
        cons.gridx=0;
        cons.gridwidth=4;
        add(creditsLabel,cons);
        
        revalidate();
    }
        @Override
    public void switchedTo() {
        File junit=new File(Config.jUnitJarLocation);
        File jhamcrest=new File(Config.jUnitHamcrestJarLocation);
        if(!junit.exists()){
            Config.jUnitJarLocation="";
            jUnitJarLabel.setText(jUnitString);
        }
        else
            jUnitJarLabel.setText(jUnitString+junit.getName());
        if(!jhamcrest.exists()){
            Config.jUnitHamcrestJarLocation="";
            jUnitHamcrestLabel.setText(jUnitHamcrestString);
        }
        else
            jUnitHamcrestLabel.setText(jUnitHamcrestString+jhamcrest.getName());
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(backToBrowser)){
            gui.getGrader().refresh();
            gui.getManager().refresh();
            gui.refreshTable();

            gui.setupFileBrowserGui();
        }
        else if(e.getSource().equals(setJUnitJar)){
            JFileChooser fc=new JFileChooser(System.getProperty("user.dir"));
            fc.setFileFilter(jarFilter);
            int returnVal = fc.showOpenDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if(file.getName().toLowerCase().contains("junit")&&
                        file.getName().toLowerCase().endsWith(".jar")){
                    statusLabel.setText("");
                    Config.jUnitJarLocation=new File(System.getProperty("user.dir")).toURI().relativize(file.toURI()).getPath(); //relative location
                    jUnitJarLabel.setText(jUnitString+file.getName());
                    statusLabel.setForeground(Color.black);
                }
                else{
                    statusLabel.setText("The file '"+file.getName()+"' is not the JUnit Jar.");
                    statusLabel.setForeground(Color.red);
                }
            }
        }
        else if(e.getSource().equals(setJUnitHamcrestJar)){
            JFileChooser fc=new JFileChooser(System.getProperty("user.dir"));
            fc.setFileFilter(jarFilter);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if(file.getName().toLowerCase().contains("hamcrest")&&
                        file.getName().toLowerCase().endsWith(".jar")){
                    statusLabel.setText("");
                    Config.jUnitHamcrestJarLocation=new File(System.getProperty("user.dir")).toURI().relativize(file.toURI()).getPath(); //relative location
                    jUnitHamcrestLabel.setText(jUnitHamcrestString+file.getName());
                    statusLabel.setForeground(Color.black);
                }
                else{
                    statusLabel.setText("The file '"+file.getName()+"' is not the Hamcrest Jar.");
                    statusLabel.setForeground(Color.red);
                }
            }
        }
        else if(e.getSource().equals(downloadJUnitButton)){
            DbxSession.openWebsite("http://junit.org");
        }
    }
    public void saveData(){
        for(Component c:getComponents()){
            focusLost(new FocusEvent(c,FocusEvent.FOCUS_LOST));
            //Effectively validates all of the fields
        }
    }
    @Override
    public void focusGained(FocusEvent e) {
        statusLabel.setForeground(Color.black);
        if(e.getComponent().equals(autoRun)){
            statusLabel.setText("Determines if code will automatically run when opened.");
        }
        else if(e.getComponent().equals(dropboxFolder)){
            statusLabel.setText("Folder in dropbox containing assignments to grade. (May include slashes to indicate folders in another folder)");
        }
        else if(e.getComponent().equals(dropboxPeriod)){
            statusLabel.setText("Class period to show. (Must be a number)");
        }
        else if(e.getComponent().equals(gradesFolder)){
            statusLabel.setText("Folder in dropbox where grades will be stored. (Will be created if it doesn't already exist)");
        }
        else if(e.getComponent().equals(runTimes)){
            statusLabel.setText("Default times to run code when run is clicked (or autorun is checked). (Must be a number)");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        statusLabel.setText("");
        //validate input data
        if(e.getComponent().equals(autoRun)){
            Config.autoRun=autoRun.isSelected();
        }
        else if(e.getComponent().equals(dropboxFolder)){
            if(dropboxFolder.getText().endsWith("/")){ //cannot end with a slash
                dropboxFolder.setText(dropboxFolder.getText().substring(0,dropboxFolder.getText().length()-1));
            }
            if(dropboxFolder.getText().contains("\\")){ //should be slashes, not backslashes
                dropboxFolder.setText(dropboxFolder.getText().replace("\\", "/"));
            }
            Config.dropboxFolder=dropboxFolder.getText();
        }
        else if(e.getComponent().equals(dropboxPeriod)){
            Integer period=null;
            try{
                period=Integer.parseInt(dropboxPeriod.getText());
            } catch(NumberFormatException ex){
                
            }
            if(period==null){
                errorLabel.setText("Error: Class Period Must Be a Number");
                dropboxPeriod.setBackground(Color.red);
            }
            else{
                dropboxPeriod.setBackground(Color.white);
                errorLabel.setText("");
                Config.dropboxPeriod=period;
            }
        }
        else if(e.getComponent().equals(gradesFolder)){
            if(gradesFolder.getText().endsWith("/")){ //cannot end with a slash
                gradesFolder.setText(gradesFolder.getText().substring(0,gradesFolder.getText().length()-1));
            }
            if(gradesFolder.getText().contains("\\")){ //should be slashes, not backslashes
                gradesFolder.setText(gradesFolder.getText().replace("\\", "/"));
            }
            if(gradesFolder.getText().contains(dropboxFolder.getText())){
                errorLabel.setText("Note: It is highly recommended that grades are not stored in the same folder as assignments.");
                gradesFolder.setBackground(Color.orange);
            }
            else{
                errorLabel.setText("");
                gradesFolder.setBackground(Color.white);                
            }
            Config.dropboxSpreadsheetFolder=gradesFolder.getText();
        }
        else if(e.getComponent().equals(runTimes)){
            Integer times=null;
            try{
                times=Integer.parseInt(runTimes.getText());
            } catch(NumberFormatException ex){
                
            }
            if(times==null){
                errorLabel.setText("Error: Default Output Runs Must Be a Number");
                runTimes.setBackground(Color.red);
            }
            else{
                runTimes.setBackground(Color.white);
                errorLabel.setText("");
                Config.runTimes=times;
            }
        }
        
        Config.writeConfig();
    }
    
}
