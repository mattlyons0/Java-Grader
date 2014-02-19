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
public class TextAssignment {
    public final int NUMBER;
    public final String NAME;
    //public final Date DATEDUE;
    
    public TextAssignment(int number,String name){
        NUMBER=number;
        NAME=name;
    }
    public TextAssignment(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        NUMBER=Integer.parseInt(text[0]);
        NAME=text[1];
    }
    public String toText(){
        String text="";
        text+=NUMBER+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=NAME;
        
        return text;
    }
    @Override
    public String toString(){
        return "Assignment "+NUMBER+" "+NAME;
    }
}
