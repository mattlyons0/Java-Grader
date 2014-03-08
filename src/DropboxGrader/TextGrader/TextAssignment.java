/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

import java.util.Objects;

/**
 *
 * @author Matt
 */
public class TextAssignment {
    public int number;
    public String name;
    //public Date dateDue;
    
    public TextAssignment(int number,String name){
        this.number=number;
        this.name=name;
    }
    public TextAssignment(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        try{
            number=Integer.parseInt(text[0]);
            name=text[1];
        } catch(Exception e){
            if(name==null){
                name="";
            }
            System.err.println("Error reading assignment from \""+fromText+"\": "+e);
        }
    }
    public String toText(){
        String text="";
        text+=number+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=name;
        
        return text;
    }
    @Override
    public boolean equals(Object o){
        if(o==this){
            return true; //interestingly enough this is insanely efficient to simply check if they are the same reference
        }
        if(o instanceof TextAssignment){
            TextAssignment a=(TextAssignment)o;
            if(a.name.equals(name)&&a.number==number){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.number;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }
    @Override
    public String toString(){
        return number+" "+name;
    }
}
