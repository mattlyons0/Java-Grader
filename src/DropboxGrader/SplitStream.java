/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Takes in output and relay.println's it as well as putting it into the specified stream
 * @author matt
 */
public class SplitStream extends PrintStream{
    private PrintStream relay;
    public SplitStream(PrintStream ps,PrintStream relay){
        super(ps);
        this.relay=relay;
    }
    @Override
    public void println(String x) {
        relay.println(x);
        super.println(x);
    }

    @Override
    public void print(Object obj) {
        relay.print(obj);
        super.print(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println(Object x) {
        relay.println(x);
        super.println(x); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void print(char[] s) {
        relay.print(s);
        super.print(s); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println() {
        relay.println();
        super.println(); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void print(String s) {
        relay.print(s);
        super.print(s); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void write(byte[] buff,int off,int len){
        relay.println(new String(buff,off,len));
        super.write(buff, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        relay.println(new String(b));
        super.write(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int b) {
        relay.print((char)b+"");
        super.write(b); //To change body of generated methods, choose Tools | Templates.
    }
}
