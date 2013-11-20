/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Matt
 */
public class TextOutputStream extends OutputStream{
    private final JTerminal area;
    public TextOutputStream(JTerminal a){
        area=a;
    }

    @Override
    public void write(byte[] buffer,int offset,int length){
        String text=new String(buffer,offset,length);
        area.append(text);
        System.out.println("Writing");
    }
    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte)b}, 0,1);
    }
    
}
