/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

/**
 *
 * @author Matt
 */
public class TextGrade {
    public final String GRADE;
    public final String COMMENT;
    //public final Date DATEGRADED;
    //public final boolean INGRADEBOOK
    
    public TextGrade(String grade,String comment){
        GRADE=grade;
        COMMENT=comment;
    }
    public TextGrade(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        GRADE=text[0];
        COMMENT=text[1];
    }
    public String toText(){
        String text="";
        text+=GRADE+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=COMMENT;
        
        return text;
    }
    @Override
    public String toString(){
        return "Grade "+GRADE+" "+COMMENT;
    }
}
