/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.io.File;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 *
 * @author 141lyonsm
 */
public class JavaCodeBrowser extends Container{
    private JScrollPane [] scrolls;
    private JTextArea[] browserArea;
    private JPanel[] fileWindows;
    private JTabbedPane tabPane;
    private DbxFile file;
    public JavaCodeBrowser(DbxFile f){
        file=f;
        
        init();
    }
    public void init(){
        setLayout(new CardLayout(10,5));
        tabPane=new JTabbedPane();
        int numFiles=file.getJavaFiles().length;
        File[] files=file.getJavaFiles();
        String[] javaCode=file.getJavaCode();
        browserArea=new JTextArea[numFiles];
        fileWindows=new JPanel[numFiles];
        scrolls=new JScrollPane [numFiles];
        
        for(int x=0;x<numFiles;x++){
            fileWindows[x]=new JPanel();
            browserArea[x]=new JTextArea();
            browserArea[x].setText(javaCode[x]);
            browserArea[x].setEditable(false);
            scrolls[x]=new JScrollPane (browserArea[x]);
            fileWindows[x].add(scrolls[x]);
            add(fileWindows[x],files[x].getName());
            
            tabPane.addTab(files[x].getName(), fileWindows[x]);
        }
        add(tabPane,BorderLayout.CENTER);
    }
}
