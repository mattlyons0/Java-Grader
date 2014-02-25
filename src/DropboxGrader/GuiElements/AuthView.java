/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author 141lyonsm
 */
public class AuthView extends ContentView{
    private JLabel status;
    private JTextField keyField;
    private JButton submitButton;
    
    public AuthView(){
        super("AuthView");
    }
    
    @Override
    public void setup() {
        status=new JLabel("Connecting to Dropbox...");
        status.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(status);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}
