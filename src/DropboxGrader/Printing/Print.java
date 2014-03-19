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
http://docs.oracle.com/javase/tutorial/2d/printing/examples/HelloWorldPrinter.java
 */ 


import DropboxGrader.Gui;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.print.*;

public class Print implements Printable {
    private Gui gui;
    
    public Print(Gui gui){
        this.gui=gui;
    }
    @Override
    public int print(Graphics g,PageFormat pf,int page) throws PrinterException {
        return printData(g,pf,page);
    }
    public void printPreview(Graphics g,PageFormat pf,int page){
        try{
            printData(g,pf,page);
        } catch(PrinterException e){
            System.err.println("Error generating print preview.\n"+e);
        }
    }
    private int printData(Graphics g, PageFormat pf, int page) throws PrinterException {
        g.setColor(Color.black);
        if (page > 0) { /* We have only one page, and 'page' is zero-based */
            return NO_SUCH_PAGE;
        }

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        /* Now we perform our rendering */
        TextGrader grader=gui.getGrader();
        grader.getSpreadsheet().numNames();
//        for(int i=0;i<grader.getSpreadsheet().numNames();i++){
//            renderStudent(g2d,i,grader,pf);
//        }
        renderStudent(g2d,0,grader,pf);
        

        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }
    private void renderStudent(Graphics2D g,int studentIndex,TextGrader grader,PageFormat pf){
        int marginY=50;
        double centerX=(int)(pf.getImageableWidth()/2.0);
        TextSpreadsheet sheet=grader.getSpreadsheet();
        g.drawString(sheet.getNameAt(studentIndex).toString(), (int)centerX, (int)marginY);
        marginY+=75;
        for(int i=0;i<sheet.numAssignments();i++){
            g.drawString("Assignment: "+sheet.getAssignmentAt(i),(int)centerX,(int)marginY);
            marginY+=25;
            g.drawString("Grade: "+sheet.getGradeAt(i, studentIndex),(int)centerX,(int)marginY);
            marginY+=50;
        }
    }
    public boolean print(){
        PrinterJob job=PrinterJob.getPrinterJob();
        job.setPrintable(this);
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
    public static void main(String[] args) {
        
    }
}