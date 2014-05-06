/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting.SimpleTesting;

import DropboxGrader.FileManagement.Date;
import DropboxGrader.TextGrader.TextSpreadsheet;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus;
import static DropboxGrader.UnitTesting.SimpleTesting.MethodData.CheckboxStatus.*;
import DropboxGrader.UnitTesting.SimpleTesting.MethodData.JavaClass;
import java.util.regex.Pattern;



/**
 *
 * @author matt
 */
public class UnitTest {
    //Access
    public CheckboxStatus accessPublic;
    public CheckboxStatus accessProtected;
    public CheckboxStatus accessPrivate;
    public CheckboxStatus accessPackagePrivate;
    //Modifiers
    public CheckboxStatus modStatic;
    public CheckboxStatus modFinal;
    public CheckboxStatus modAbstract;
    public CheckboxStatus modSynchronized;
    //Return Type
    private JavaClass returnType; //Must be a specific type
    //Method Name
    private String methodName;
    //Arguments
    private JavaClass[] argumentTypes; //types of arguments required in the method signature
    private String[] argumentData;
    //Expected Return Values
    private String expectedReturnValue;
    //description
    private String testDescription;
    //date
    public Date updateDate;
    
    public UnitTest(){
        accessPublic=ALLOWED;
        accessProtected=ALLOWED;
        accessPrivate=ALLOWED;
        accessPackagePrivate=ALLOWED;
        
        modStatic=ALLOWED;
        modFinal=ALLOWED;
        modAbstract=ALLOWED;
        modSynchronized=ALLOWED;
        updateDate=Date.currentDate();
    }
    public UnitTest(String fromText){
        this();
        String[] s=fromText.split(Pattern.quote(TextSpreadsheet.INDIVIDUALDELIMITER3));
        try{
            accessPublic=valueOf(s[0])==null?accessPublic:valueOf(s[0]);
            accessProtected=valueOf(s[1])==null?accessProtected:valueOf(s[1]);
            accessPrivate=valueOf(s[2])==null?accessPrivate:valueOf(s[2]);
            accessPackagePrivate=valueOf(s[3])==null?accessPackagePrivate:valueOf(s[3]);
            modStatic=valueOf(s[4])==null?modStatic:valueOf(s[4]);
            modFinal=valueOf(s[5])==null?modFinal:valueOf(s[5]);
            modAbstract=valueOf(s[6])==null?modAbstract:valueOf(s[6]);
            modSynchronized=valueOf(s[7])==null?modSynchronized:valueOf(s[7]);
            if(!s[8].equals("null"))
                returnType=new JavaClass(s[8]);
            if(!s[9].equals("null"))
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
            if(!s[13].equals("null"))
                testDescription=s[13];
            updateDate=s[14].equals("null")||s[14].equals("")?null:new Date(s[14]);
        } catch(Exception e){
            
        }
        
    }
    public void setMethodName(String name){
        methodName=validate(name);
    }
    public void setReturnType(String type){
        returnType=new JavaClass(validate(type));
    }
    public void setArgumentData(String[] data){
        argumentData=data;
    }
    public void setArgumentTypes(JavaClass[] types){
        argumentTypes=types;
    }
    public void setArgumentTypes(String[] types){
        argumentTypes=new JavaClass[types.length]; 
        for(int i=0;i<types.length;i++){
            argumentTypes[i]=new JavaClass(types[i]);
        }
    }
    public void setExpectedReturnValue(String val){
        expectedReturnValue=validate(val);
    }
    public String getMethodName(){
        return methodName;
    }
    public JavaClass getReturnType(){
        return returnType;
    }
    public String getReturnTypeString(){
        return returnType==null? null:returnType.toText();
    }
    public String[] getArguments(){
        return argumentData;
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
    public JavaClass[] getArgumentTypes(){
        return argumentTypes;
    }
    public String getArgumentTypesString(){
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
    public String getDescription(){
        return testDescription;
    }
    public void setDescription(String s){
        if(!s.equals(""))
            testDescription=validate(s);
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
            s+=TextSpreadsheet.INDIVIDUALDELIMITER3;
        }
        if(argumentData==null||argumentData.length==0||(argumentData.length==1&&argumentData[0].equals("")))
            s=append(s,null);
        else{
            for(int i=0;i<argumentData.length;i++){
                s+=argumentData[i];
                if(i!=argumentData.length-1)
                    s+=",";
            }
            s+=TextSpreadsheet.INDIVIDUALDELIMITER3;
        }
        s=append(s,expectedReturnValue);
        s=append(s,testDescription);
        s=append(s,updateDate==null?"null":updateDate.toText());
        
        return s;
    }
    private String append(String string,Object append){
        string+=append+TextSpreadsheet.INDIVIDUALDELIMITER3;
        return string;
    }
    private String validate(String s){
        return TextSpreadsheet.validateString(s);
    }

    public CheckboxStatus[] getAccess() {
        return new CheckboxStatus[]{accessPublic,accessProtected,accessPrivate,accessPackagePrivate};
    }
    public CheckboxStatus[] getModifiers(){
        return new CheckboxStatus[]{modStatic,modFinal,modAbstract,modSynchronized};
    }
}
