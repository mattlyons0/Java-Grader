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
public class TextName {
    public String firstName;
    public String lastName;
    //public String email;
    
    public TextName(String firstName,String lastName){
        this.firstName=firstName;
        this.lastName=lastName;
    }
    public TextName(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        //Pretty sure this should never happen, but just in case.
        try{
            firstName=text[0];
            lastName=text[1];
        } catch(Exception e){
            System.err.println("Error reading name from \""+fromText+"\": "+e);
        }
    }
    public String toText(){
        String text="";
        text+=firstName+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=lastName;
        
        return text;
    }
    @Override
    public boolean equals(Object o){
        if(o==this){
            return true;
        }
        if(o instanceof TextName){
            TextName n=(TextName)o;
            if(n.firstName.equals(firstName)&&n.lastName.equals(lastName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.firstName);
        hash = 59 * hash + Objects.hashCode(this.lastName);
        return hash;
    }
    @Override
    public String toString(){
        return firstName+" "+lastName;
    }
}
