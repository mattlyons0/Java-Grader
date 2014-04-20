/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import java.io.OutputStream;

/**
 *
 * @author Matt
 */
public class RelayStream extends OutputStream{
    private Evaluable error;
    public RelayStream(Evaluable error){
        this.error=error;
    }
    @Override
    public void write(int b){
        error.evaluate((char)b+"");
    }

}
