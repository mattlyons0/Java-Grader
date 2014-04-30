/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.Util;

/**
 *
 * @author 141lyonsm
 */
public abstract class NamedRunnable implements Runnable{

    @Override
    public abstract void run();
    public abstract String name();
    
}
