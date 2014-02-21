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
    public String toString(){
        return "Assignment "+number+" "+name;
    }
}
