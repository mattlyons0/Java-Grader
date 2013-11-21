/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Matt
 */
public class TextInputStream extends FilterInputStream{
    private JTerminal terminal;
    public TextInputStream(JTerminal t,InputStream in) {
        super(in);
        terminal=t;
        terminal.setInputStream(this);
    } 
}
