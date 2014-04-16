/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author Matt
 */
public class TextAssignment {
    public int number;
    public String name;
    public int perferredWidth; //perferred width in gradebook table
    public UnitTest[] simpleUnitTests;
    public String[] junitTests;
    public double totalPoints;
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
            if(!text[3].equals("null")){
                String[] tests=text[3].split(Pattern.quote(TextSpreadsheet.INDIVIDUALDELIMITER2));
                if(tests.length!=0&&!(tests.length==1&&tests[0].equals(""))){
                    simpleUnitTests=new UnitTest[tests.length];
                    for(int i=0;i<tests.length;i++){
                        simpleUnitTests[i]=new UnitTest(tests[i]);
                    }
                }
            }
            totalPoints=Double.parseDouble(text[4]);
            if(!text[5].equals("null")){
                String[] tests=text[5].split(TextSpreadsheet.INDIVIDUALDELIMITER2);
                if(tests.length!=0&&!(tests.length==1&&tests[0].equals(""))){
                    junitTests=new String[tests.length];
                    for(int i=0;i<tests.length;i++){
                        junitTests[i]=tests[i];
                    }
                }
            }
        } catch(Exception e){ //catch numberformat and indexoutofbounds
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
        text+=perferredWidth+TextSpreadsheet.INDIVIDUALDELIMITER;
        if(simpleUnitTests!=null){
            for(int i=0;i<simpleUnitTests.length;i++){
                if(simpleUnitTests[i]!=null)
                    text+=simpleUnitTests[i].toText();
                if(i!=simpleUnitTests.length-1)
                    text+=TextSpreadsheet.INDIVIDUALDELIMITER2;
            }
        }
        else
            text+=null;
        text+=TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=totalPoints+TextSpreadsheet.INDIVIDUALDELIMITER;
        if(junitTests!=null)
            for(int i=0;i<junitTests.length;i++){
                text+=junitTests[i];
                text+=TextSpreadsheet.INDIVIDUALDELIMITER2;
            }
        else
            text+=null;
        //text+=TextSpreadsheet.INDIVIDUALDELIMITER;
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
