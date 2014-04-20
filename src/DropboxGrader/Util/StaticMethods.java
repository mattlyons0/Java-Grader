/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Util;

/**
 *
 * @author matt
 */
public class StaticMethods {
    private StaticMethods(){}
    
    public static void printStackTrace(){
        String out="";
        StackTraceElement[] elements=Thread.currentThread().getStackTrace();
        for(int i=2;i<elements.length;i++){
            out+="\t"+elements[i].toString()+"\n";
        }
        System.err.println(out);
    }
}
