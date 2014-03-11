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
    public int perferredWidth; //perferred width in gradebook table
    //public Date dateDue;
    
    public TextAssignment(int number,String name){
        this.number=number;
        this.name=name;
        this.perferredWidth=75;
    }
    public TextAssignment(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        try{
            number=Integer.parseInt(text[0]);
            name=text[1];
            perferredWidth=Integer.parseInt(text[2]);
        } catch(Exception e){
            if(name==null){
                name="";
            }
            if(perferredWidth==0){
                perferredWidth=75;
            }
            //System.err.println("Error reading assignment from \""+fromText+"\": "+e);
            //That just means the comment was null really, not important
        }
    }
    public String toText(){
        String text="";
        text+=number+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=name+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=perferredWidth;
        
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
