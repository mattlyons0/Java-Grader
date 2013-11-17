/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

/**
 *
 * @author 141lyonsm
 */
public class Main {
    public static void main(String[] args) {
        DbxSession session=new DbxSession();
        FileManager man=new FileManager("DROPitTOme","P2",session.getClient());
        man.downloadAll();
    }
}
