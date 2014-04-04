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
public class TextGrade {
    public double grade;
    public String comment="";
    //public final Date DATEGRADED;
    public boolean inGradebook=false;
    
    public TextGrade(double grade,String comment){
        this.grade=grade;
        this.comment=comment;
        inGradebook=false;
    }
    public TextGrade(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        try{
            grade=Double.parseDouble(text[0]);
            comment=text[1];
            inGradebook=Boolean.parseBoolean(text[2]);
        } catch(Exception e){
            //System.err.println("Error reading grade from \""+fromText+"\": "+e);
            //it is entirely normal to get this error, so we don't need to output anything
            //it will be thrown if someone chooses not to write a comment for a grade
        }
    }
    public String toText(){
        String text="";
        text+=grade+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=comment+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=inGradebook;
        
        return text;
    }
    @Override
    public boolean equals(Object o){
        if(o==this){
            return true;
        }
        if(o instanceof TextGrade){
            TextGrade g=(TextGrade)o;
            if(g.grade==grade&&g.comment.equals(comment)&&g.inGradebook==inGradebook){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.grade);
        hash = 73 * hash + Objects.hashCode(this.comment);
        hash = 73 * hash + (this.inGradebook ? 1 : 0);
        return hash;
    }
    @Override
    public String toString(){
        String comment=this.comment==null? "":this.comment;
        return grade+" "+comment;
    }
}
