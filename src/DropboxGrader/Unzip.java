/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;


/**
 *
 * @author http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class Unzip {
    public static void unzip(String source, String dest){
        dest+="\\"+source.substring(0, source.length()-4);
        System.out.println("Unzipping: "+source+" to "+dest);

    try {
         ZipFile zipFile = new ZipFile(source);
         zipFile.extractAll(dest);
    } catch (ZipException e) {
        e.printStackTrace();
    }

}
}
