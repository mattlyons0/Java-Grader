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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 *
 * @author 141lyonsm
 */
public class PrintOverlay extends ContentOverlay{
    private Gui gui;
    private BufferedImage image;
    private Print printer;
    private Runnable callback;
    
    private JButton print;
    
    public PrintOverlay(Gui gui){
        super("PrintOverlay");
        this.gui=gui;
        printer=gui.getPrinter();
    }
    @Override
    public void setup() {
        Dimension parentSize = gui.getSize();
        setSize((int)(parentSize.width*0.5),(int)(parentSize.height*0.25));
        PageFormat format=new PageFormat();
        image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
        printer.printPreview(image.getGraphics(),new PageFormat(), 0);
        
        print=new JButton("Print");
        print.addActionListener(this);
        ImageIcon icon=new ImageIcon(image);
        JLabel label=new JLabel(icon);
        
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.fill=GridBagConstraints.BOTH;
        add(print,cons);
        
        cons.gridy=1;
        cons.fill=GridBagConstraints.BOTH;
        cons.anchor=GridBagConstraints.NORTH;
        //add(label,cons);
        
        setTitle("Print Preview");
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
        setLocation((parentSize.width-getSize().width)/2,(parentSize.height-getSize().height)/2);
        setVisible(true);
    }
    @Override
    public void switchedTo() {
    }
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
    }
    
}
