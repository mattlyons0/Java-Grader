/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

import DropboxGrader.TextGrader;

/**
 *
 * @author 141lyonsm
 */
public class TestMethods {
    public static void main(String[] args) {
        String str="hello there my name is matt";
        int index=TextGrader.indexOf("my name", str);
        System.out.println(index);
    }
}
