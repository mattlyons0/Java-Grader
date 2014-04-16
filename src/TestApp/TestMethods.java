/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

//import DropboxGrader.UnitTesting.SimpleTesting.JavaMethod;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.ToolProvider;

/**
 *
 * @author 141lyonsm
 */
public class TestMethods {
    public static void main (String[] args) {
        testJUnit();
    }
    public static String test(String s){
        s+="!";
        return s;
    }
//    public static void testUnitTest(){
//        JavaMethod m=new JavaMethod("public static String toString(String[] args,   Object    o   ,String s,Object a){");
//        System.out.println(m);
//        JavaMethod m2=new JavaMethod("public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {");
//        System.out.println(m2);
//    }
    public static void testJUnit(){
        String[] args={"java","-cp","/home/matt/Downloads/junit-4.11.jar:"
                + "/home/matt/Downloads/hamcrest-core-1.3.jar:/home/matt/NetBeansProjects/Java-Grader/test/:"
                + "/home/matt/NetBeansProjects/Java-Grader/build/classes/",
                "org.junit.runner.JUnitCore","JUnitTest"};
                ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err,
                        args[1],args[2],"/home/matt/NetBeansProjects/Java-Grader/test/"+args[4]+".java"
                                ,"/home/matt/NetBeansProjects/Java-Grader/src/TestApp/TestMethods.java");
        ProcessBuilder builder=new ProcessBuilder(args);
        File dir=new File("/home/matt/NetBeansProjects/Java-Grader/test/");
            builder.directory(dir);
            builder.inheritIO();
            System.out.println("Running from: "+dir+"\nArgs: "+Arrays.toString(args));
        try {
            builder.start();
        } catch (IOException ex) {
            Logger.getLogger(TestMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
