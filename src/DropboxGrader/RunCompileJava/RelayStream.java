/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import DropboxGrader.GuiElements.Grader.JTerminal;
import java.awt.Color;
import java.io.PrintStream;

/**
 *
 * @author Matt
 */
public class RelayStream extends PrintStream{
    private Evaluable output;
    private Evaluable error;
    public RelayStream(PrintStream origStream,Evaluable output,Evaluable error){
        super(origStream);
        this.output=output;
        this.error=error;
    }

    @Override
    public void println(String x) {
        output.evaluate(x+"\n");
        super.println(x);
    }
    @Override
    public void write(byte[] buff,int off,int len){
        error.evaluate(new String(buff,off,len));
        super.write(buff, off, len);
    }

}
