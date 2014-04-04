/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

import DropboxGrader.UnitTesting.JavaMethod;

/**
 *
 * @author 141lyonsm
 */
public class TestMethods {
    public static void main (String[] args) {
        JavaMethod m=new JavaMethod("public static String toString(String[] args,   Object    o   ,String s,Object a){");
        System.out.println(m);
        JavaMethod m2=new JavaMethod("public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {");
        System.out.println(m2);
        
    }
    static void test(String s){
        s+="!";
    }
}
