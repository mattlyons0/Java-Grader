/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

import DropboxGrader.FileManagement.Date;
import java.util.Objects;

/**
 *
 * @author Matt
 */
public class TextGrade {
    public double grade;
    public String comment="";
    public boolean inGradebook=false;
    public boolean unitTested=false;
    public Date dateGraded=null;
    
    public TextGrade(double grade,String comment){
        this.grade=grade;
        this.comment=comment;
        dateGraded=Date.currentDate();
    }
    public TextGrade(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        try{
            grade=Double.parseDouble(text[0]);
            comment=text[1];
            comment=comment.replaceAll(TextSpreadsheet.INDIVIDUALDELIMITER2, "\n");
            inGradebook=Boolean.parseBoolean(text[2]);
            unitTested=Boolean.parseBoolean(text[3]);
            dateGraded=text[4].equals("null")||text[4].equals("")?null:new Date(text[4]);
        } catch(Exception e){
            //System.err.println("Error reading grade from \""+fromText+"\": "+e);
            //it is entirely normal to get this error, so we don't need to output anything
            //it will be thrown if someone chooses not to write a comment for a grade
        }
    }
    public TextGrade(TextGrade g){
        comment=g.comment;
        dateGraded=new Date(g.dateGraded);
        grade=g.grade;
        inGradebook=g.inGradebook;
        unitTested=g.unitTested;
    }
    public String toText(){
        String text="";
        text+=grade+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=comment.replaceAll("\n", TextSpreadsheet.INDIVIDUALDELIMITER2).replaceAll("\r", TextSpreadsheet.INDIVIDUALDELIMITER2)
                +TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=inGradebook+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=unitTested+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=dateGraded==null?"null":dateGraded.toText();
        
        return text;
    }
    @Override
    public boolean equals(Object o){
        if(o==this){
            return true;
        }
        if(o instanceof TextGrade){
            TextGrade g=(TextGrade)o;
            if(g.grade==grade&&g.comment.equals(comment)&&g.inGradebook==inGradebook
                    &&unitTested==g.unitTested){
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
