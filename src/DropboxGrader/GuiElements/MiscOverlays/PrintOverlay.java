/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.Printing.Print;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

/**
 *
 * @author 141lyonsm
 */
public class PrintOverlay extends ContentOverlay{
    private Gui gui;
    private Print printer;
    private Runnable callback;
    private int currentPage;
    private PageFormat format;
    
    private JLabel iconLabel;
    private JButton print;
    private JButton backButton;
    private JButton forwardButton;
    private JLabel pageLabel;
    private JScrollPane scroll;
    
    public PrintOverlay(Gui gui){
        super("PrintOverlay");
        this.gui=gui;
        printer=gui.getPrinter();
    }
    @Override
    public void setup() {
        format=new PageFormat();
        BufferedImage image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
        printer.printPreview(image.getGraphics(),new PageFormat(), currentPage);
        
        JPanel panel=new JPanel();
        panel.setLayout(new GridBagLayout());
        ImageIcon icon=new ImageIcon(image);
        iconLabel=new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        print=new JButton("Print");
        print.addActionListener(this);
        backButton=new JButton("Back");
        backButton.setEnabled(false);
        backButton.addActionListener(this);
        forwardButton=new JButton("Forward");
        forwardButton.addActionListener(this);
        pageLabel=new JLabel("Page "+currentPage);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.insets=new Insets(5,5,5,5);
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=1;
        cons.weighty=1;
        cons.gridwidth=4;
        cons.fill=GridBagConstraints.NONE;
        cons.anchor=GridBagConstraints.NORTH;
        panel.add(iconLabel,cons);
        cons.gridwidth=1;
        cons.gridy=1;
        cons.weighty=10;
        cons.anchor=GridBagConstraints.NORTHWEST;
        cons.weightx=5;
        panel.add(print,cons);
        cons.anchor=GridBagConstraints.NORTH;
        cons.weightx=1;
        cons.gridx=1;
        panel.add(backButton,cons);
        cons.gridx=2;
        panel.add(pageLabel,cons);
        cons.gridx=3;
        panel.add(forwardButton,cons);
        
        scroll=new JScrollPane(panel);
        cons.gridx=0;
        cons.gridy=0;
        cons.fill=GridBagConstraints.BOTH;
        add(scroll,cons);
        
        setTitle("Print Preview");
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        Dimension parentSize = gui.getSize();
        setSize((int)(format.getWidth()*1.1f),(int)(format.getHeight()*1.1f));
        setLocation((parentSize.width-getSize().width)/2,(parentSize.height-getSize().height)/2);
        setVisible(true);
    }
    private void changePage(int newPage){
        currentPage=newPage;
        BufferedImage image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
        printer.printPreview(image.getGraphics(),new PageFormat(), currentPage);
        iconLabel.setIcon(new ImageIcon(image));
        pageLabel.setText("Page "+currentPage);
        if(currentPage==0){
            backButton.setEnabled(false);
        }
        else{
            backButton.setEnabled(true);
        }
    }
    @Override
    public void switchedTo() {}
    public void setCallback(Runnable r){
        callback=r;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(print)){
            if(callback!=null){
                gui.getBackgroundThread().invokeLater(callback);
            }
        }
        else if(e.getSource().equals(backButton)){
            changePage(currentPage-1);
        }
        else if(e.getSource().equals(forwardButton)){
            changePage(currentPage+1);
        }
    }
    
}
