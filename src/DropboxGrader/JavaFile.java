/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.io.File;

/**
 *
 * @author Matt
 */
public class JavaFile extends File{
    private boolean mainMethod;
    private String packageFolder;
    public JavaFile(String location,boolean containsMain,String packageF){
        super(location);
        mainMethod=containsMain;
        packageFolder=packageF;
    }
    public JavaFile(File f){
        super(f.getPath());
        mainMethod=false;
    }
    public boolean containsMain(){
        return mainMethod;
    }
    public String packageFolder(){
        return packageFolder;
    }
    public boolean hasPackage(){
        if(packageFolder==null){
            return false;
        }
        return true;
    }
    public void setPackage(String packageF){
        packageFolder=packageF;
    }
    public void setMainMethod(boolean hasMain){
        mainMethod=hasMain;
    }
}
