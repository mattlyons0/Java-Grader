/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Matt
 */
public class TextOutputStream extends OutputStream{
    private final JTerminal area;
    private final OutputStream stream;
    public TextOutputStream(JTerminal a,OutputStream s){
        area=a;
        stream=s;
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        for(byte by:b){
            write((char)by);
        }
    }
    @Override
    public void write(int b) throws IOException {
        stream.write(b);
        stream.flush();
        
        area.append((char)b+"");
        System.out.println("Test");
    }
    @Override
    public void close() throws IOException{
        flush();
        
        stream.close();
    }
    @Override
    public void flush() throws IOException{
        stream.flush();
    }

}
