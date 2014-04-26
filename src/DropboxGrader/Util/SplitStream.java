/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Takes in output and relay.println's it as well as putting it into the specified stream
 * @author matt
 */
public class SplitStream extends OutputStream{
    private PrintStream orig;
    private PrintStream relay;
    public SplitStream(PrintStream ps,PrintStream relay){
        this.relay=relay;
        orig=ps;
    }

    @Override
    public void write(int b) {
        relay.write(b);
        orig.write(b);
    }
}
