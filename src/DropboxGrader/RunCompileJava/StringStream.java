/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author 141lyonsm
 */
public class StringStream extends OutputStream{
    private String output;
    public StringStream(){
        super();
        output="";
    }
    @Override
    public void write(int b) {
        output+=(char)b;
    }
    public String getOutput(){
        return output;
    }
}
