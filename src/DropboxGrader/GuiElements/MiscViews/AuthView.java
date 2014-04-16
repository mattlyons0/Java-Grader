/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.MiscViews;

import DropboxGrader.DbxSession;
import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentView;
import com.dropbox.core.DbxClient;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author 141lyonsm
 */
public class AuthView extends ContentView{
    private Gui gui;
    private DbxSession session;
    private Clipboard clipboard;
    
    private JLabel descriptionLabel;
    private JLabel statusLabel;
    private JLabel demoLabel;
    private JLabel helpLabel;
    private JTextField keyField;
    private JButton authButton;
    private JButton keySubmitButton;
    private JButton submitKeyField;
    private JButton demoButton;
    private JButton openBrowserButton;
    private JLabel urlFieldLabel;
    private JTextField urlField;
    
    private GridBagConstraints cons;
    
    public AuthView(Gui gui){
        super("AuthView");
        this.gui=gui;
        this.session=new DbxSession(this);
        gui.setDbxSession(session);
        clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
        
        cons=new GridBagConstraints();
        cons.weightx=1;
        cons.weighty=1;
    }
    
    @Override
    public void setup() {
        descriptionLabel=new JLabel("Connecting to Dropbox...");
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel=new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        
        cons.gridx=0;
        cons.gridy=0;
        add(descriptionLabel,cons);
        cons.gridy=1;
        add(statusLabel,cons);
    }
    public void needsKey(){
        descriptionLabel.setText("<html><center>In order to use "+DbxSession.APPNAME+" must authenticate with your Dropbox account."
                + "<br/>A webpage will open asking you to authenticate, then it will give you a code."
                + "<br/>Copy that code.</center></html>"); //funny java supports html in jlabels because the center tag is really outdated
        statusLabel.setText("Click to open Dropbox authentication in your browser.");
        authButton=new JButton("Open Authentication Request Page");
        authButton.addActionListener(this);
        demoLabel=new JLabel("Alernatively if you just want to try "+DbxSession.APPNAME+", you can try demo mode.");
        demoButton=new JButton("Demo Mode");
        demoButton.setEnabled(false); //NYI
        demoButton.setToolTipText("Not Yet Implemented");
        
        cons.gridy=2;
        add(authButton,cons);
        cons.gridy=3;
        add(demoLabel,cons);
        cons.gridy=4;
        add(demoButton,cons);
        revalidate();
        repaint();
    }
    public void promptKey(){
        if(authButton!=null){
            remove(authButton);
            authButton=null;
        }
        if(demoLabel!=null){
            remove(demoLabel);
            demoLabel=null;
        }
        if(demoButton!=null){
            remove(demoButton);
            demoButton=null;
        }
        descriptionLabel.setText("A webpage will now open to authenticate you, once you login it will give you a code.");
        statusLabel.setText("Copy the Code you are given.");
        statusLabel.setForeground(Color.black);
        if(keySubmitButton==null){
            keySubmitButton=new JButton("I have copied the code");
            keySubmitButton.addActionListener(this);

            cons.gridy=2;
            add(keySubmitButton,cons);
        }
        revalidate();
        repaint();
    }
    public void badKey(){
        if(!statusLabel.getText().contains("Invalid Key, "))
            statusLabel.setText("Invalid Key, "+statusLabel.getText());
        statusLabel.setForeground(Color.red);
        if(helpLabel==null){
            helpLabel=new JLabel("You can also type the code in the box.");
            cons.gridy=3;
            add(helpLabel,cons);
        }
        if(keyField==null){
            keyField=new JTextField(20);
            keyField.addActionListener(this);
            cons.gridy=4;
            add(keyField,cons);
        }
        if(submitKeyField==null){
            submitKeyField=new JButton("I have typed the code in the box");
            submitKeyField.addActionListener(this);
            cons.gridy=5;
            add(submitKeyField,cons);
        }
        if(openBrowserButton==null){
            openBrowserButton=new JButton("Open Website Again");
            openBrowserButton.addActionListener(this);
            cons.gridy=6;
            add(openBrowserButton,cons);
        }
        if(urlFieldLabel==null){
            urlFieldLabel=new JLabel("Or you can type the url in your browser manually.");
            cons.gridy=7;
            add(urlFieldLabel,cons);
        }
        if(urlField==null){
            urlField=new JTextField(session.getURL().length()/2);
            urlField.setText(session.getURL());
            urlField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    urlField.setSelectionStart(0);
                    urlField.setSelectionEnd(urlField.getText().length());
                }

                @Override
                public void focusLost(FocusEvent e) {}
            });
            cons.gridy=8;
            add(urlField,cons);
        }
        
        repaint();
    }
    public void goodKey(String loginName,final DbxClient client){
        descriptionLabel.setText("Logged in as "+loginName+".");
        statusLabel.setText("Building File Directory...");
        statusLabel.setForeground(Color.black);
        if(keyField!=null){
            remove(keyField);
            keyField=null;
        }
        if(keySubmitButton!=null){
            remove(keySubmitButton);
            keySubmitButton=null;
        }
        if(demoLabel!=null){
            remove(demoLabel);
            demoLabel=null;
        }
        if(helpLabel!=null){
            remove(helpLabel);
            helpLabel=null;
        }
        if(openBrowserButton!=null){
            remove(openBrowserButton);
            openBrowserButton=null;
        }
        if(submitKeyField!=null){
            remove(submitKeyField);
            submitKeyField=null;
        }
        if(urlFieldLabel!=null){
            remove(urlFieldLabel);
            urlFieldLabel=null;
        }
        if(urlField!=null){
            remove(urlField);
            urlField=null;
        }
        repaint();
        gui.getBackgroundThread().invokeLater(new Runnable() {

            @Override
            public void run() {
                gui.goodKey(client);
            }
        });
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(keySubmitButton)){
            if(gui.getManager()==null){
                try{
                    Object clipData=clipboard.getData(DataFlavor.stringFlavor);
                    if(clipData instanceof String){
                        session.setKey((String)clipData);
                    }
                } catch(Exception ex){ //its a risky operation to assume the data ont he clipboard is a string, so we are ok if that fails
                    badKey();
                }
            }
        }
        else if(e.getSource().equals(submitKeyField)){
            if(gui.getManager()==null){
                session.setKey(keyField.getText());
                if(keyField!=null)
                    keyField.setText("");
            }
        }
        else if(e.getSource().equals(keyField)){ //return throws the action event
            actionPerformed(new ActionEvent(submitKeyField,0,""));
        }
        else if(e.getSource().equals(authButton)){
            session.promptKey();
        }
        else if(e.getSource().equals(openBrowserButton)){
            session.promptKey();
        }
    }    

    @Override
    public void switchedTo() {
        if(session.getClient()==null){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    session.createSession();
                }
            });
        }
    }
}
