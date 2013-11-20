/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import jsyntaxpane.DefaultSyntaxKit;

/**
 *
 * @author 141lyonsm
 */
public class JavaCodeBrowser extends Container{
    private JScrollPane [] scrolls;
    private JEditorPane[] browserArea;
    private JPanel[] fileWindows;
    private JTabbedPane tabPane;
    private DbxFile file;
    public JavaCodeBrowser(DbxFile f){
        file=f;
        
        init();
    }
    public void init(){  
        DefaultSyntaxKit.initKit();
        setLayout(new CardLayout(10,5));
        tabPane=new JTabbedPane();
        int numFiles=file.getJavaFiles().length;
        File[] files=file.getJavaFiles();
        String[] javaCode=file.getJavaCode();
        browserArea=new JEditorPane[numFiles];
        fileWindows=new JPanel[numFiles];
        scrolls=new JScrollPane [numFiles];
        GridBagConstraints constraints=new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH;
        constraints.weightx=1;
        constraints.weighty=1;
        for(int x=0;x<numFiles;x++){
            fileWindows[x]=new JPanel();
            fileWindows[x].setLayout(new GridBagLayout());
            
            browserArea[x]=new JEditorPane();
            browserArea[x].setEditable(false);
            scrolls[x]=new JScrollPane (browserArea[x]);
            browserArea[x].setContentType("text/java");
            browserArea[x].setText(javaCode[x]);
            fileWindows[x].add(scrolls[x],constraints);
            add(fileWindows[x],files[x].getName());
            
            tabPane.addTab(files[x].getName(), fileWindows[x]);
        }
        add(tabPane,BorderLayout.CENTER);
    }
}
