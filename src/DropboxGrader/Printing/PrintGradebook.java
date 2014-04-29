/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Printing;

import DropboxGrader.Gui;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 *
 * @author matt
 */
public class PrintGradebook implements Printable{
    private Gui gui;
    private JTable table;
    private JTableHeader columns;
    private boolean landscapeMode;
    
    private PrinterJob job;
    public PrintGradebook(Gui gui,JTable gradebook){
        this.gui=gui;
        table=gradebook;
        columns=gradebook.getTableHeader();
        job=PrinterJob.getPrinterJob();
    }
    public void printPreview(Graphics g,int pageNum){
        BufferedImage columnsImage=new BufferedImage(columns.getBounds().width,columns.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        table.getTableHeader().paint(columnsImage.getGraphics());
        BufferedImage tableImage=new BufferedImage(table.getBounds().width,table.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        table.paint(tableImage.getGraphics());
        
        BufferedImage combinedImage=new BufferedImage(columnsImage.getWidth(),columnsImage.getHeight()+tableImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=(Graphics2D)combinedImage.getGraphics();
        g2.drawImage(columnsImage, null, null);
        g2.drawImage(tableImage, 0,columnsImage.getHeight(), null);

        int lastHorizontal=pageNum==0?0:getNumHorizontalCells(pageNum-1)+1;
        int lastVertical=pageNum==0?0:getNumVerticalCells(pageNum-1)+1;
        Rectangle oldHorizontalCell=table.getCellRect(0, lastHorizontal, true);
        Rectangle oldVerticalCell=table.getCellRect(0, lastVertical, true);
        Rectangle horizontalCell=table.getCellRect(0, getNumHorizontalCells(pageNum), true);
        Rectangle verticalCell=table.getCellRect(0, getNumVerticalCells(pageNum), true);

        g2.fillRect(horizontalCell.x+horizontalCell.width, 0, combinedImage.getWidth(), combinedImage.getHeight());
        //g2.clearRect(0, verticalCell.height+verticalCell.y, combinedImage.getWidth(), combinedImage.getHeight());
        
        g.translate(-oldHorizontalCell.x, -0);
        if(landscapeMode){
            ((Graphics2D)g).rotate(Math.toRadians(90));
            g.drawImage(combinedImage, 0, 0, null);
        } else
            g.drawImage(combinedImage, 0, 0, null);
        g.translate(oldHorizontalCell.x,0);
    }
    public void print(){
        
    }
    private int getNumHorizontalCells(int page){
        if(page<0)
            return 0;
        int width=(int)job.defaultPage().getImageableWidth();
        int totalWidth=0;
        int startCell=0;
        if(page>0){
            startCell=getNumHorizontalCells(page-1);
            //totalWidth+=table.getCellRect(0, startCell, true).width;
        }
        for(int i=startCell+1;i<table.getModel().getColumnCount();i++){
            totalWidth+=table.getCellRect(0, i, true).width;
            if(totalWidth>width){//one lower than our current index, but since we return size we don't subtract one
                return i-1;
            }
            if(totalWidth==width)
                return i; //current one, but since we are returning the number not the index add one
        }
        return table.getModel().getColumnCount();
    }
    private int getNumVerticalCells(int page){
        if(page<0)
            return 0;
        int height=(int)job.defaultPage().getImageableHeight();
        
        int lastHeight;
        int totalHeight=columns.getHeight();
        for(int i=0;i<table.getModel().getRowCount();i++){
            lastHeight=totalHeight;
            totalHeight+=table.getCellRect(i,0,true).height;
            if(totalHeight>height)
                return lastHeight; //one lower than our current index, but since we return size we don't subtract one
            if(totalHeight==height)
                return totalHeight; //current one, but since we are returning the number not the index add one
        }
        return totalHeight;
    }
    public void setLandscape(boolean b){
        landscapeMode=b;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        printPreview(graphics,pageIndex);
        return PAGE_EXISTS;
    }
}
