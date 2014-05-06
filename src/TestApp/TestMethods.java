/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

//import DropboxGrader.UnitTesting.SimpleTesting.JavaMethod;
import DropboxGrader.GuiHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class TestMethods {
    public static void main (String[] args) throws ClassNotFoundException, InterruptedException {
//        Result r=JUnitCore.runClasses(Class.forName("JUnitTest"),Class.forName("TestApp.TestMethods"));
//        System.out.println(r.getFailures().get(0));
//        testJUnitJar();
        //Thread.sleep(1000);
        //System.out.println();
        testJUnitJar();
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
        String[] args={"java","-cp","\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\hamcrest-core-1.3.jar;"+
                "\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\junit-4.11.jar;\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader",
//                + "downloads\\JUnitTests\\;"
//                + "downloads\\P2_MattLyons_50_JUnitTest\\",
                "org.junit.runner.JUnitCore","JUnitTest"};
//                ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err,
//                        args[1],args[2],"/home/matt/NetBeansProjects/Java-Grader/test/"+args[4]+".java"
//                                ,"/home/matt/NetBeansProjects/Java-Grader/src/TestApp/TestMethods.java");
        ProcessBuilder builder=new ProcessBuilder(args);
        File dir=new File("\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\");
            //builder.directory(dir);
            builder.inheritIO();
            System.out.println("Running from: "+dir+"\nArgs: "+Arrays.toString(args));
        try {
            builder.start();
        } catch (IOException ex) {
            Logger.getLogger(TestMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void testJUnitJar(){
        String[] args={"java","-cp","\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\hamcrest-core-1.3.jar;"+
                "\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\junit-4.11.jar;\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\downloads\\JUnitTests;\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\downloads\\P2_MattLyons_50_JUnitTest",
//                + "downloads\\JUnitTests\\;"
//                + "downloads\\P2_MattLyons_50_JUnitTest\\",
                "org.junit.runner.JUnitCore","JUnitTest"};
//                ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err,
//                        args[1],args[2],"/home/matt/NetBeansProjects/Java-Grader/test/"+args[4]+".java"
//                                ,"/home/matt/NetBeansProjects/Java-Grader/src/TestApp/TestMethods.java");
        System.setProperty("java.class.path", args[2]);
        List<String> argList=new ArrayList();
        argList.add(args[0]);
        argList.add(args[3]);
        argList.add(args[4]);
        ProcessBuilder builder=new ProcessBuilder(argList);
        builder.environment().put("CLASSPATH", args[2]);
        File dir=new File("\\\\dist113\\dhs_stud\\DHSStudent14\\141lyonsm\\My Documents\\NetBeansProjects\\Java-Grader\\downloads\\JUnitTests");
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
