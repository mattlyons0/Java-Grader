/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import java.io.IOException;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *http://stackoverflow.com/questions/16140031/how-to-use-runtime-getruntime-to-execute-a-java-file
 * @author 141lyonsm
 */
public class RunJava {
    public static void main(String[] args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // creating a file, but you could load an existing one
        FileOutputStream javaFile = new FileOutputStream("Test.java");
        javaFile.write("public class Test { public static void main(String[] args) { javax.swing.JOptionPane.showMessageDialog(null, \"I'm here!\",\"Test\", 1);}}"
                .getBytes());

        // compiling it
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, System.out, System.err, "Test.java");

        // running it    
        Runtime.getRuntime().exec("java Test");
    }
}
