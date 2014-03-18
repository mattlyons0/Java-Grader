/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Printing;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author 141lyonsm
 */
public class Print {
    private String test="Hello";
        private void load(JTextArea comp, String fileName) {
        try {
            comp.read(
                new InputStreamReader(
                    getClass().getResourceAsStream(fileName)),
                null);
        } catch (IOException ex) {
            // should never happen with the resources we provide
            ex.printStackTrace();
        }
    }
     
    private void print(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_print
        MessageFormat header = createFormat(headerField);
        MessageFormat footer = createFormat(footerField);
        boolean interactive = interactiveCheck.isSelected();
        boolean background = backgroundCheck.isSelected();
 
        PrintingTask task = new PrintingTask(header, footer, interactive);
        if (background) {
            task.execute();
        } else {
            task.run();
        }
    }//GEN-LAST:event_print
     
    private class PrintingTask extends SwingWorker<Object, Object> {
        private final MessageFormat headerFormat;
        private final MessageFormat footerFormat;
        private final boolean interactive;
        private volatile boolean complete = false;
        private volatile String message;
         
        public PrintingTask(MessageFormat header, MessageFormat footer,
                            boolean interactive) {
            this.headerFormat = header;
            this.footerFormat = footer;
            this.interactive = interactive;
        }
         
        @Override
        protected Object doInBackground() {
            try {
                complete = text.print(headerFormat, footerFormat,
                        true, null, null, interactive);
                message = "Printing " + (complete ? "complete" : "canceled");
            } catch (PrinterException ex) {
                message = "Sorry, a printer error occurred";
            } catch (SecurityException ex) {
                message =
                    "Sorry, cannot access the printer due to security reasons";
            }
            return null;
        }
         
        @Override
        protected void done() {
            message(!complete, message);
        }
    }
}
