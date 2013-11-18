/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author Matt
 */
public class Gui extends JFrame{
    private FileManager fileManager;
    private Config config;
    
    private JLabel status;
    private JTextField keyField;
    public Gui(){
        super("Dropbox Grader");
        
        config=new Config(); //TODO: make config
        
        init();
    }
    private void init(){
        setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500,500);
        setLayout(new GridLayout(2,1));
        
        status=new JLabel("Connecting to Dropbox...");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(status);
        
        setVisible(true);
        
        createSession();
    }
    private void createSession(){
        DbxSession session=new DbxSession(this);
        fileManager=new FileManager("DROPitTOme","P2",session.getClient());
    }
    public void promptKey(){
        status.setText("Please login and paste the code here:");
        keyField=new JTextField("Code");
        add(keyField);
    }
}
