/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Util;

import java.util.Comparator;

/**
 *
 * @author Matt
 */
public class NumberComparator implements Comparator<String>{
    @Override
    public int compare(String o1, String o2) {
        int i1,i2;
        try{
            i1=Integer.parseInt(o1);
        } catch(NumberFormatException e){
            return -1;
        }
        try{
            i2=Integer.parseInt(o2);
        } catch(NumberFormatException e){
            return 1;
        }
        
        return i1-i2;
    }
    
}
