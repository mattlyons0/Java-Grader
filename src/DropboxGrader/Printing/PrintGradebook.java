/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Printing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
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
    private JTable table;
    private JTableHeader columns;
    private boolean landscape;
    private boolean wrapCells;
    private float scale=0.9f;
    private boolean verticalPage;
    
    private PrinterJob job;
    private Pageable pageable;
    private Integer pages;
    public PrintGradebook(JTable gradebook){
        table=gradebook;
        columns=gradebook.getTableHeader();
        job=PrinterJob.getPrinterJob();
        wrapCells=true;
        pageable=new Pageable() {
            @Override
            public int getNumberOfPages() {
                if(pages!=null)
                    return pages;
                else{
                    calculatePages();
                    return pages;
                }
            }
            @Override
            public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
                return job.defaultPage();
            }
            @Override
            public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
                return PrintGradebook.this;
            }
        };
    }
    public int printPreview(Graphics g,int pageNum,Color clearColor){
        table.clearSelection();
        BufferedImage columnsImage=new BufferedImage(columns.getBounds().width,columns.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        table.getTableHeader().paint(columnsImage.getGraphics());
        BufferedImage tableImage=new BufferedImage(table.getBounds().width,table.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        table.paint(tableImage.getGraphics());
        
        BufferedImage combinedImage=new BufferedImage((int)(columnsImage.getWidth()*scale),(int)(columnsImage.getHeight()+tableImage.getHeight()*scale),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=(Graphics2D)combinedImage.getGraphics();
        g2.drawImage(columnsImage.getScaledInstance((int)(columnsImage.getWidth()*scale),
                (int)(columnsImage.getHeight()*scale), BufferedImage.SCALE_SMOOTH), 0,0,null);
        g2.drawImage(tableImage.getScaledInstance((int)(tableImage.getWidth()*scale),
                (int)(tableImage.getHeight()*scale), BufferedImage.SCALE_SMOOTH), 0,(int)(columnsImage.getHeight()*scale), null);

        int lastHorizontal=pageNum==0?0:getNumHorizontalCells(pageNum-1)+1;
        int lastVertical=pageNum==0?0:getNumVerticalCells(pageNum-1)+1;
        Rectangle oldHorizontalCell=table.getCellRect(0, lastHorizontal, true);
        Rectangle oldVerticalCell=table.getCellRect(lastVertical,0, true);
        Rectangle horizontalCell=table.getCellRect(0, getNumHorizontalCells(pageNum), true);
        Rectangle verticalCell=table.getCellRect(getNumVerticalCells(pageNum)-1,0, true); //-1 for the col header
        boolean pageVertical=false;
        if(!wrapCells){
            int width=(int)job.defaultPage().getImageableWidth();
            int height=(int)job.defaultPage().getImageableHeight();
            if(landscape){
                int temp=width;
                width=height;
                height=temp;
            }
            oldHorizontalCell=new Rectangle((int)(width*pageNum*(2-scale)),0,0,0);
            horizontalCell=new Rectangle((int)(width*(pageNum+1)*(2-scale)),0,0,0);
            oldVerticalCell=new Rectangle(0,(int)(height*scale),0,0);
            verticalCell=new Rectangle(0,(int)(height*scale),0,0);            
        }
        if((wrapCells&&oldHorizontalCell.x==horizontalCell.x)||(!wrapCells&&oldHorizontalCell.x>=combinedImage.getWidth())){
//            if(oldVerticalCell.y==verticalCell.y)
                return NO_SUCH_PAGE;
//            else{
//                pageVertical=true;
//            }
                    }
        if(clearColor!=null)
            g2.setColor(clearColor);
        if(landscape)
            ((Graphics2D)g).rotate(Math.toRadians(90));
        //g2.clearRect(0, verticalCell.height+verticalCell.y, combinedImage.getWidth(), combinedImage.getHeight());
        int marginX=clearColor!=null?0:(int)job.defaultPage().getImageableX();
        int marginY=clearColor!=null?0:(int)job.defaultPage().getImageableY();
        g.translate((int)marginX+(int)(-oldHorizontalCell.x*scale),
                (int)marginY);
        if(!pageVertical)
            g2.fillRect((int)((horizontalCell.x+horizontalCell.width)*scale), 0, combinedImage.getWidth(), combinedImage.getHeight());
        else
            g2.fillRect(marginY, marginY, marginY, marginY);
        if(landscape)
            g.drawImage(combinedImage, 0, -(int)job.defaultPage().getImageableWidth(), null);
        else //portrait
            g.drawImage(combinedImage, 0, 0, null);
        
        return PAGE_EXISTS;
    }
    private int getNumHorizontalCells(int page){
        if(page<0)
            return 0;
        int width=(int)job.defaultPage().getImageableWidth();
        if(landscape)
            width=(int)job.defaultPage().getImageableHeight();
        
        int totalWidth=0;
        int startCell=-1;
        if(page>0){
            startCell=getNumHorizontalCells(page-1);
            //totalWidth+=table.getCellRect(0, startCell, true).width;
        }
        for(int i=startCell+1;i<table.getModel().getColumnCount();i++){
            totalWidth+=table.getCellRect(0, i, true).width*scale;
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
        if(landscape)
            height=(int)job.defaultPage().getImageableWidth();
        int totalHeight=(int)(columns.getHeight()*scale);
        int startCell=0;
        if(page>0){
            startCell=getNumVerticalCells(page-1);
        }
        for(int i=startCell+1;i<table.getModel().getRowCount();i++){
            totalHeight+=table.getCellRect(i,0,true).height*scale;
            if(totalHeight>height)
                return i-1; //one lower than our current index, but since we return size we don't subtract one
            if(totalHeight==height)
                return i; //current one, but since we are returning the number not the index add one
        }
        return table.getModel().getRowCount();
    }
    public void setLandscape(boolean b){
        landscape=b;
        calculatePages();
    }
    public boolean print(){
        job.setPrintable(this);
        job.setJobName("Gradebook Table");
        job.setPageable(pageable);
        
        boolean accepted=job.printDialog();
        if(accepted){
            try{
                job.print();
            } catch(PrinterException ex){
                ex.printStackTrace();
                return false;
            }
        } else
            return false;
        return true;
    }
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        printPreview(graphics,pageIndex,null);
        return PAGE_EXISTS;
    }
    private void calculatePages(){
        BufferedImage i=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        int pages=0;
        boolean done=false;
        boolean oldVPage=verticalPage;
        verticalPage=false;
        while(!done){
            int val=printPreview(i.getGraphics(),pages,null);
            if(val==NO_SUCH_PAGE)
                done=true;
            else
                pages++;
        }
        this.pages=pages;
    }
    public int getNumPages(){
        if(pages==null)
            calculatePages();
        return pages;
    }
    public void setWrap(boolean wrap){
        wrapCells=wrap;
        calculatePages();
    }
    public void setScale(float scale){
        this.scale=scale;
        calculatePages();
    }
    public float getScale(){
        return scale;
    }
    public boolean getLandscape(){
        return landscape;
    }
    public boolean getWrapMode(){
        return wrapCells;
    }
    public boolean getPageVertical(){
        return verticalPage;
    }
    public void setPageVertical(boolean b){
        verticalPage=b;
    }
}
