/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author 141lyonsm
 */
public class StringStream extends PrintStream{
    private String output;
    public StringStream(PrintStream stream){
        super(stream);
        output="";
    }
    @Override
    public void println(String x) {
        output+=x+"\n";
        super.println(x);
    }
    @Override
    public void write(byte[] buff,int off,int len){
        output+=new String(buff,off,len);
        super.write(buff, off, len);
    }
    public String getOutput(){
        return output;
    }

    @Override
    public void print(Object obj) {
        if(obj!=null)
            output+=obj.toString();
        else
            output+=obj;
        super.print(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println(Object x) {
        if(x!=null)
            output+=x.toString()+"\n";
        else
            output+=x+"\n";
        super.println(x); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void print(char[] s) {
        output+=new String(s);
        super.print(new String(s)); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println() {
        output+="\n";
        super.println(); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void print(String s) {
        output+=s;
        super.print(s); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void write(byte[] b) throws IOException {
        output+=new String(b);
        super.write(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(int b) {
        output+=(char)b;
        super.write(b); //To change body of generated methods, choose Tools | Templates.
    }
}
