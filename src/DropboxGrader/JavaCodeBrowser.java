/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.CardLayout;
import java.awt.Container;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;

/**
 *
 * @author 141lyonsm
 */
public class JavaCodeBrowser extends Container{
    private JScrollBar scrollBar;
    private JTextArea[] browserArea;
    private JPanel[] fileWindows;
    private DbxFile file;
    public JavaCodeBrowser(DbxFile f){
        file=f;
        
        init();
    }
    public void init(){
        setLayout(new CardLayout(10,5));
        int numFiles=file.getJavaFiles().length;
        browserArea=new JTextArea[numFiles];
        for(int x=0;x<numFiles;x++){
            browserArea=new JTextArea();
        }
    }
}
