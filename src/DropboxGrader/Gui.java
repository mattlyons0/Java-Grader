/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.dropbox.core.DbxClient;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

/**
 *
 * @author Matt
 */
public class Gui extends JFrame implements ActionListener{
    //Raw Data Instance Vars
    private FileManager fileManager;
    private DbxSession session;
    private DbxClient client;
    private Config config;
    
    //First Stage (Authentication) Instance Vars
    private JLabel status;
    private JTextField keyField;
    private JButton submitButton;
    
    //Second Stage (File Browser) Instance Vars
    private FileBrowserData fileBrowserData;
    private JTable fileBrowserTable;
    private JScrollPane fileBrowserScroll;
    private GridBagConstraints constraints;
    private JButton refreshButton;
    private JButton downloadAllButton;
    private JButton configButton;
    private JProgressBar progressBar;
    private JButton gradeButton;
    private JLabel statusText;
    
    //Third Stage (Grader) Instance Vars
    private JavaCodeBrowser javaCode;
    public Gui(){
        super("Dropbox Grader");
        config=new Config(); //TODO: make config
        
        UIManager.put("ProgressBar.foreground", new Color(120,200,55)); //color the progressbar green.
        
        init();
    }
    private void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(375,130);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        
        status=new JLabel("Connecting to Dropbox...");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(status);
        
        setVisible(true);
        
        session=new DbxSession(this);
    }
    private void createSession(){
        if(client!=null||session!=null){
            fileManager=new FileManager("DROPitTOme","P2",client,this);
            setupFileBrowserGui();
        }
    }
    
    public void setupFileBrowserGui(){
        if(status!=null){
            remove(status);
            status=null;
        }
        setLayout(new GridBagLayout());
        constraints=new GridBagConstraints();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        fileBrowserData=new FileBrowserData(fileManager);
        fileManager.setTableData(fileBrowserData);
        fileBrowserTable=new FileBrowser(fileBrowserData);
        fileBrowserScroll=new JScrollPane(fileBrowserTable);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        fileBrowserScroll.setBounds(0, 0, screenSize.width,screenSize.height);
        
        refreshButton=new JButton("Refresh");
        refreshButton.addActionListener(this);
        downloadAllButton=new JButton("Download All");
        downloadAllButton.addActionListener(this);
        statusText=new JLabel("");
        configButton=new JButton("Settings");
        configButton.addActionListener(this);
        progressBar=new JProgressBar(0,0,100);
        gradeButton=new JButton("Grade");
        gradeButton.addActionListener(this);
        
        constraints.anchor=GridBagConstraints.WEST;
        constraints.ipady=5;
        constraints.ipadx=10;
        constraints.gridx=0;
        constraints.gridy=0;
        //constraints.weightx=0.05;
        constraints.weighty=0.01;
        add(refreshButton,constraints);
        constraints.gridx=1;
        add(downloadAllButton,constraints);
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.gridx=3;
        add(statusText,constraints);
        constraints.anchor=GridBagConstraints.EAST;
        constraints.gridx=4;
        //constraints.weightx=0.9;
        add(configButton,constraints);
        constraints.fill=GridBagConstraints.BOTH;
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.gridx=0;
        constraints.gridy=1;
        constraints.gridwidth=5;
        constraints.weightx=100;
        constraints.weighty=0.9;
        add(fileBrowserScroll,constraints);
        constraints.gridwidth=4;
        constraints.gridy=2;
        constraints.fill=GridBagConstraints.HORIZONTAL;
        constraints.weighty=0.01;
        constraints.weightx=1;
        add(progressBar,constraints);
        constraints.weightx=0.01;
        constraints.gridwidth=1;
        constraints.gridx=4;
        constraints.gridwidth=1;
        constraints.anchor=GridBagConstraints.EAST;
        constraints.fill=GridBagConstraints.NONE;
        add(gradeButton,constraints);
        revalidate();
    }
    public void setupGraderGui(int fileNum){
        refreshButton.setEnabled(false);
        downloadAllButton.setEnabled(false);
        remove(fileBrowserScroll);
        remove(progressBar);
        remove(gradeButton);
        
        javaCode=new JavaCodeBrowser(fileManager.getFile(fileNum));
        
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.fill=GridBagConstraints.BOTH;
        constraints.gridheight=1;
        constraints.gridwidth=5;
        constraints.gridx=0;
        constraints.gridy=1;
        constraints.weightx=50;
        constraints.weighty=0.9;
        
        add(javaCode,constraints);
        
        revalidate();
    }
    public void promptKey(){
        status.setText("Please login and paste the code here: ");
        keyField=new JTextField(30);
        keyField.addActionListener(this);
        submitButton=new JButton("Submit");
        submitButton.addActionListener(this);
        
        add(keyField);
        add(submitButton);

        revalidate();
    }
    public void badKey(){
        if(!status.getText().contains("Invalid Key, "))
        status.setText("Invalid Key, "+status.getText());
    }
    public void goodKey(String loginName,DbxClient client){
        this.client=client;
        status.setText("Logged in as "+loginName+". Building File Directory...");
        if(keyField!=null)
            remove(keyField);
        if(submitButton!=null)
            remove(submitButton);
        repaint();
        createSession();
    }
    public void updateProgress(double val){
        progressBar.setValue((int)val*100);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(statusText!=null){
            statusText.setText("");
        }
        
        if(e.getSource().equals(submitButton)){
            if(fileManager==null){
                session.setKey(keyField.getText());
            }
        }
        else if(e.getSource().equals(keyField)){ //return throws the action event
            actionPerformed(new ActionEvent(submitButton,0,""));
        }
        else if(e.getSource().equals(refreshButton)){
            fileManager.refresh();
            fileBrowserTable.revalidate();
        }
        else if(e.getSource().equals(downloadAllButton)){
            fileManager.downloadAll();
            fileBrowserTable.repaint();
        }
        else if(e.getSource().equals(configButton)){
            //do config code soon
        }
        else if(e.getSource().equals(gradeButton)){
            int selected=fileBrowserTable.getSelectedRow();
            if(selected==-1){
                statusText.setText("You must select an assignment to grade.");
                return;
            }
            fileManager.download(selected);
            fileBrowserTable.repaint();
            setupGraderGui(selected);
        }
    }
}
