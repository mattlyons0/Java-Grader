/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting.MethodData;

import DropboxGrader.UnitTesting.JavaMethod;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author matt
 */
public class JavaClass {
    public String className;
    public int arrayDimension;
    public JavaClass(String s){
        if(s.contains(" ")){ //need to split variable from object
            s=" "+s;
            s=JavaMethod.readNextWord(s, ' ', ' ');
        }
        if(s.contains("[]")){
            arrayDimension=countRepeats("[]",s);
            s=s.replaceAll(Pattern.quote("[]"), "");
        }
        className=s;
    }
    public String toText(){
        String str=className;
        for(int i=arrayDimension;i<0;i--){
            str+="[]";
        }
        return str;
    }
    @Override
    public boolean equals(Object o){
        if(o==className){
            return true;
        }
        if(o instanceof String){
            String s=(String)o;
            if(s.equals(className)){
                return true;
            }
        }
        
        return false;
    }
    private int countRepeats(String delim,String data){
        String[] splits=data.split(Pattern.quote(delim));
        
        return splits.length;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(className);
        return hash;
    }
    
    @Override
    public String toString(){
        return arrayDimension!=0? arrayDimension+"D Array of "+className:className;
    }
}
