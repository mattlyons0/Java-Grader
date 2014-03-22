package DropboxGrader.Printing;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Modified from http://docs.oracle.com/javase/tutorial/2d/printing/examples/HelloWorldPrinter.java
 */ 


import DropboxGrader.Gui;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Date;

public class Print implements Printable {
    private Gui gui;
    private Pageable pageable;
    private PrinterJob job;
    
    public Print(final Gui gui){
        this.gui=gui;
        
        job=PrinterJob.getPrinterJob();
        pageable=new Pageable() {
            @Override
            public int getNumberOfPages() {
                return gui.getGrader().getSpreadsheet().numNames();
            }
            @Override
            public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
                return job.defaultPage();
            }
            @Override
            public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
                return Print.this;
            }
        };
    }
    @Override
    public int print(Graphics g,PageFormat pf,int page) throws PrinterException {
        return printData(g,pf,page);
    }
    public void printPreview(Graphics g,int page){
        try{
            printData(g,job.defaultPage(),page);
        } catch(PrinterException e){
            System.err.println("Error generating print preview.\n"+e);
        }
    }
    private int printData(Graphics g, PageFormat pf, int pageNum) throws PrinterException {
        g.setColor(Color.black);

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        TextGrader grader=gui.getGrader();
        int pages=grader.getSpreadsheet().numNames();
        if(pageNum>=pages||0>pageNum){
            return NO_SUCH_PAGE;
        }
        
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        /* Now we perform our rendering */
        renderStudent(g2d,pageNum,grader,pf);
        

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }
    private void renderStudent(Graphics2D g,int studentIndex,TextGrader grader,PageFormat pf){
        int marginY=10;
        double centerX=(int)(pf.getImageableWidth()/2.0)-pf.getImageableX()*0.5;
        TextSpreadsheet sheet=grader.getSpreadsheet();
        g.drawString(new Date().toString(),0, marginY);
        marginY+=25;
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        g.drawString(sheet.getNameAt(studentIndex).toString(), (int)centerX, (int)marginY);
        g.setFont(g.getFont().deriveFont(Font.PLAIN));
        marginY+=25;
        for(int i=0;i<sheet.numAssignments();i++){
            TextAssignment assign=sheet.getAssignmentAt(i);
            TextGrade grade=sheet.getGradeAt(i, studentIndex);
            
            g.drawString("Assignment "+assign.number+": "+assign.name,0,(int)marginY);
            marginY+=15;
            if(grade!=null){
                if(!grade.comment.equals("")){
                    String str="Grade: "+grade.grade+" Comment: "+grade.comment;
                    String[] stringLines=lineWrap(str,(int)pf.getImageableWidth(),g);
                    for(String s:stringLines){
                        g.drawString(s,0,(int)marginY);
                        marginY+=15;
                    }
                } else{
                    g.drawString("Grade: "+grade.grade,0,(int)marginY);
                }
            }
            else{
                g.setColor(Color.red);
                g.setFont(g.getFont().deriveFont(Font.BOLD));
                g.drawString("Missing",0,(int)marginY);
                g.setColor(Color.black);
                g.setFont(g.getFont().deriveFont(Font.PLAIN));
            }
            marginY+=25;
        }
    }
    private String[] lineWrap(String str,int width,Graphics2D g){
        int lineWidth=g.getFontMetrics().stringWidth(str);
        double lines=lineWidth/(double)width;
        if(lines<=1){
            return new String[]{str};
        }
        
        String lastString=null;
        String[] split=str.split(" ");
        for(int i=0;i<split.length;i++){
            String combined="";
            for(int index=0;index<i;index++){
                if(index!=0){
                    combined+=" ";
                }
                combined+=split[index];
            }
            int curWidth=g.getFontMetrics().stringWidth(combined);
            if(curWidth<=width){
                lastString=combined;
            }
            else{
                if(lastString==null){ //there were no spaces so we will just cut it off
                    lastString=str;
                }
                break;
            }
        }
        String[] otherLines=lineWrap(str.substring(lastString.length()),width,g);
        String[] merged=new String[otherLines.length+1];
        for(int i=0;i<merged.length;i++){
            if(i==0){
                merged[i]=lastString;
            }
            else{
                merged[i]=otherLines[i-1];
            }
        }
        return merged;
        
    }
    public boolean print(){
        job.setPrintable(this);
        job.setJobName("Grade Reports");
        job.setPageable(pageable);
        
        boolean accepted=job.printDialog();
        if(accepted){
            try {
                job.print();
            } catch (PrinterException ex) {
                  /* The job did not successfully complete */
                return false;
            }
        }
        else{
            return false;
        }
        return true;
        
    }
    public PageFormat getFormat(){
        return job.defaultPage();
    }
}