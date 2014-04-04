/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.UnitTesting.MethodData.CheckboxStatus;
import static DropboxGrader.UnitTesting.MethodData.CheckboxStatus.*;
import DropboxGrader.UnitTesting.MethodData.JavaClass;
import java.util.regex.Pattern;



/**
 *
 * @author matt
 */
public class UnitTest {
    //Access
    private CheckboxStatus accessPublic;
    private CheckboxStatus accessProtected;
    private CheckboxStatus accessPrivate;
    private CheckboxStatus accessPackagePrivate;
    //Modifiers
    private CheckboxStatus modStatic;
    private CheckboxStatus modFinal;
    private CheckboxStatus modAbstract;
    private CheckboxStatus modSynchronized;
    //Return Type
    private JavaClass returnType; //Must be a specific type
    //Method Name
    private String methodName;
    //Arguments
    private JavaClass[] argumentTypes; //types of arguments required in the method signature
    private String[] argumentData;
    //Expected Return Values
    private String expectedReturnValue;
    
    public UnitTest(){
        accessPublic=IGNORED;
        accessProtected=IGNORED;
        accessPrivate=IGNORED;
        accessPackagePrivate=IGNORED;
        
        modStatic=IGNORED;
        modFinal=IGNORED;
        modAbstract=IGNORED;
        modSynchronized=IGNORED;
    }
    public UnitTest(String fromText){
        super();
        String[] s=fromText.split(Pattern.quote("|"));
        try{
            accessPublic=valueOf(s[0]);
            accessProtected=valueOf(s[1]);
            accessPrivate=valueOf(s[2]);
            accessPackagePrivate=valueOf(s[3]);
            modStatic=valueOf(s[4]);
            modFinal=valueOf(s[5]);
            modAbstract=valueOf(s[6]);
            modSynchronized=valueOf(s[7]);
            if(!s[8].equals("null"))
                returnType=new JavaClass(s[8]);
            methodName=s[9];
            if(!s[10].equals("null")){
                String[] args=s[10].split(",");
                argumentTypes=new JavaClass[args.length];
                for(int i=0;i<args.length;i++){
                    argumentTypes[i]=new JavaClass(args[i]);
                }
            }
            if(!s[11].equals("null")){
                String[] argsData=s[11].split(",");
                argumentData=new String[argsData.length];
                for(int i=0;i<argsData.length;i++){
                    argumentData[i]=argsData[i];
                }
            }
            if(!s[12].equals("null"))
                expectedReturnValue=s[12];
        } catch(Exception e){
            
        }
    }
    public void setMethodName(String name){
        methodName=name;
    }
    public void setReturnType(String type){
        returnType=new JavaClass(type);
    }
    public void setArgumentData(String[] data){
        argumentData=data;
    }
    public void setArgumentTypes(String[] types){
        argumentTypes=new JavaClass[types.length]; 
        for(int i=0;i<types.length;i++){
            argumentTypes[i]=new JavaClass(types[i]);
        }
    }
    public void setExpectedReturnValue(String val){
        expectedReturnValue=val;
    }
    public String getMethodName(){
        return methodName;
    }
    public String getReturnType(){
        return returnType==null? null:returnType.toText();
    }
    public String getArgumentData(){
        if(argumentData==null)
            return null;
        String str="";
        for(int i=0;i<argumentData.length;i++){
            str+=argumentData[i];
            if(i!=argumentData.length-1)
                str+=", ";
        }
        return str;
    }
    public String getArgumentTypes(){
        if(argumentTypes==null)
            return null;
        String str="";
        for(int i=0;i<argumentTypes.length;i++){
            str+=argumentTypes[i].toText();
            if(i!=argumentTypes.length-1)
                str+=", ";
        }
        return str;
    }
    public String getExpectedReturnValue(){
        return expectedReturnValue;
    }
    public String toText(){
        String s="";
        s=append(s,accessPublic);
        s=append(s,accessProtected);
        s=append(s,accessPrivate);
        s=append(s,accessPackagePrivate);
        s=append(s,modStatic);
        s=append(s,modFinal);
        s=append(s,modAbstract);
        s=append(s,modSynchronized);
        if(returnType==null)
            s=append(s,returnType);
        else
            s=append(s,returnType.toText());
        s=append(s,methodName);
        if(argumentTypes==null||argumentTypes.length==0||(argumentTypes.length==1&&argumentTypes[0].className.equals("")))
            s=append(s,null);
        else{
            for(int i=0;i<argumentTypes.length;i++){
                s+=argumentTypes[i].toText();
                if(i!=argumentTypes.length-1)
                    s+=",";
            }
            s+="|";
        }
        if(argumentData==null||argumentData.length==0||(argumentData.length==1&&argumentData[0].equals("")))
            s=append(s,null);
        else{
            for(int i=0;i<argumentData.length;i++){
                s+=argumentData[i];
                if(i!=argumentData.length-1)
                    s+=",";
            }
            s+="|";
        }
        s=append(s,expectedReturnValue);
        
        return s;
    }
    private String append(String string,Object append){
        string+=append+"|";
        return string;
    }
}
