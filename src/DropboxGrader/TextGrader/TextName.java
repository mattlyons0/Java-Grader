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
public class TextName {
    public final String FIRSTNAME;
    public final String LASTNAME;
    //public final String EMAIL;
    
    public TextName(String firstName,String lastName){
        FIRSTNAME=firstName;
        LASTNAME=lastName;
    }
    public TextName(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        FIRSTNAME=text[0];
        LASTNAME=text[1];
    }
    public String toText(){
        String text="";
        text+=FIRSTNAME+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=LASTNAME;
        
        return text;
    }
    @Override
    public String toString(){
        return FIRSTNAME+" "+LASTNAME;
    }
}
