/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting.SimpleTesting;

import DropboxGrader.UnitTesting.SimpleTesting.MethodData.JavaClass;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodAccessType;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.MethodModifiers;
import java.util.Arrays;

/**
 *
 * @author matt
 */
public class JavaMethod {
    public MethodAccessType accessType=MethodAccessType.PACKAGEPRIVATE; //default access type
    public MethodModifiers modifiers;
    public JavaClass returnType; //null==void
    public String methodName;
    public JavaClass[] arguments; //need to think about how to deal with objects created in provided classes
    private String methodString;
    
    /**
     * Takes in the source code declaring a method and populates data structures based on that
     * @param method source code of a method to extract data from
     * String must only contain the method declaration and nothing past the opening brace
     */
    public JavaMethod(String method){
        method=" "+method;
        extractData(method);
        methodString=method;
    }
    private void extractData(String method){
        //misc cleanup
        String s=readNextWord(method,'@',' '); //if it has an annotation remove it
        if(s!=null){
            method=method.replace("@"+s, "");
        }
        //accesstype
        if(method.contains(" public ")){
            accessType=MethodAccessType.PUBLIC;
            method=replaceData(" public ",method);
        }
        else if(method.contains(" protected ")){
            accessType=MethodAccessType.PROTECTED;
            method=replaceData(" protected ",method);
        }
        else if(method.contains(" private ")){
            accessType=MethodAccessType.PRIVATE;
            method=replaceData(" private ",method);
        }
        //modifiers
        modifiers=new MethodModifiers();
        if(method.contains(" static ")){
            modifiers.staticMod=true;
            method=replaceData(" static ",method);
        }
        if(method.contains(" final ")){
            modifiers.finalMod=true;
            method=replaceData(" final ",method);
        }
        if(method.contains(" abstract ")){
            modifiers.abstractMod=true;
            method=replaceData(" abstract ",method);
        }
        if(method.contains(" synchronized ")){
            modifiers.synchronizedMod=true;
            method=replaceData(" synchronized ",method);
        }
        //return type
        if(method.contains(" void ")){
            returnType=null;
            method=replaceData(" void ",method);
        }
        else{ //now it starts getting harder to determine
            String read=readNextWord(method,' ',' ');
                returnType=new JavaClass(read);
            
            method=replaceData(" "+read+" ",method);
        }
        //methodName
        String read=readNextWord(method,' ','(');
        if(read.contains(" ")){
            int index=method.indexOf(read);
            method=method.substring(0,index)+method.substring(index+read.length(),method.length());
            read=read.replaceAll(" ", "");
        }
        else{
            method=replaceData(" "+read+"(",method);
        }
        methodName=read;
        //arguments
        int start=method.indexOf("(");
        int end=method.lastIndexOf(")"); //we shouldn't have anything but spaces outside the (, but just to make sure
        method=method.substring(start+1,end); //now we only have the argument string without the ( or )
        if(method.contains(",")){ //there are more than 1 arguments
            String[] args=method.split(",");
            arguments=new JavaClass[args.length];
            for(int i=0;i<args.length;i++){
                arguments[i]=new JavaClass(args[i]);
            }
        }
        else{
            if(method.replaceAll(" ", "").length()==0){ //there are no arguments
                arguments=new JavaClass[0];
            }
            else{ //there is 1 argument
                arguments=new JavaClass[1];
                arguments[0]=new JavaClass(method);
            }
        }
        
    }
    /**
     * Replaces data using the leading and trailing spaces to search, but not to replace
     * @param usedData data containing leading and trailing spaces which was used
     * @param data data to be replaced 
     * @return the string with the data removed
     */
    private String replaceData(String usedData,String data){
        int index=data.indexOf(usedData);
        if(index==-1){
            return data;
        }
        String newString;
        newString=data.substring(0,index+1);
        newString+=data.substring(index+usedData.length()-1);
        return newString;
    }
    /**
     * Parses the next sequence of characters between specified characters
     * @param data data to parse from
     * @param start the character to be at the start of the sequence
     * @param end the character to be at the end of the sequence
     * @return the substring between the first set of spaces with something other than the starting character between them
     */
    public static String readNextWord(String data,char start,char end){
        String read=null;
            for(int i=0;i<data.length();i++){
                char c=data.charAt(i);
                if((c==end||c==start)&&read!=null){
                    if(read!=null){
                        break;
                    }
                }
                else if(read!=null||(read==null&&c==start)){
                    if(read==null){
                        if(i!=data.length()-1){
                            if(data.charAt(i+1)!=end&&data.charAt(i+1)!=start)
                                read="";
                        }
                    }
                    else{
                        read+=c;
                    }
                }
            }
        return read;
    }
    public String[] getArguments(){
        if(arguments==null)
            return null;
        String[] args=new String[arguments.length];
        for(int i=0;i<arguments.length;i++){
            args[i]=arguments[i].toText();
        }
        return args;
    }
    public String getMethodString(){
        return methodString;
    }
    @Override
    public String toString(){
        return "Access: "+accessType+" Modifiers: "+modifiers+"Returns: "+returnType+" Name: "+methodName+" Args: "+Arrays.toString(arguments);
    }
}
