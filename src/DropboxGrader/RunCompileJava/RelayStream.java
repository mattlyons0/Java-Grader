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
    private PrintStream originalStream;
    private JTerminal terminal;
    public RelayStream(PrintStream origStream,JTerminal t){
        super(origStream);
        originalStream=origStream;
        terminal=t;
    }

    @Override
    public void println(String x) {
        terminal.append(x+"\n");
        super.println(x);
    }
    @Override
    public void write(byte[] buff,int off,int len){
        terminal.append(new String(buff,off,len),Color.RED);
        super.write(buff, off, len);
    }

}
