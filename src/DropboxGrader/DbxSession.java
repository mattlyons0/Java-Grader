/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import static TestApp.Main.appName;
import static TestApp.Main.appVersion;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 141lyonsm
 */
public class DbxSession {
    private final String appName="Matt Lyons Assignment Grader";
    private final double appVersion=0.1;
    private final String APP_KEY = "681xzhh2nqu3hjc";
    private final String APP_SECRET = "k7e1pkfljgg1jdb";
    private final File appKey;
    private DbxClient client;
    
    public DbxSession(){
        appKey=new File("app.key");
        
        createSession();
    }
    public DbxClient getClient(){
        return client;
    }
    private void createSession(){
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxRequestConfig config = new DbxRequestConfig(
            appName+" "+appVersion, Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        DbxAuthFinish authFinish;
        if(!appKey.exists()){
            authFinish=getKey(true,webAuth);
        }
        else{
            authFinish=getKey(false,webAuth);
        }
        
        client = new DbxClient(config, authFinish.accessToken);
        try {
            System.out.println("Linked account: " + client.getAccountInfo().displayName);
        } catch (DbxException ex) {
            GuiHelper.alertDialog("Error accessing dropbox account: "+ex);
        }
    }
    private DbxAuthFinish getKey(boolean newKey,DbxWebAuthNoRedirect webAuth){
        String key;
        if(newKey){
            openWebsite(webAuth.start());
            key=GuiHelper.inputDialog("Please login and paste the code here:");
            writeToFile(appKey,key);
        }
        else{
            key=readFromFile(appKey);
        }
        try {
            return webAuth.finish(key);
        } catch (DbxException ex) {
            System.out.println("Dropbox session error: "+ex+"\nRetrying with new appkey.");
            return getKey(true,webAuth);
        }
    }
    private void writeToFile(File f,String s){
        try{
            if(!f.exists()){
                f.createNewFile();
            }
            BufferedWriter writer=new BufferedWriter(new FileWriter(f));
            writer.write(s);
            
            writer.close();
        } catch (IOException ex) {
            GuiHelper.alertDialog("Cannot read/write files. "+ex);
            System.exit(1);
        }
    }
    private String readFromFile(File f){
        String read="";
        try {
            Scanner reader=new Scanner(f);
            while(reader.hasNext()){
                read+=reader.next();
            }
        } catch (FileNotFoundException ex) {
            GuiHelper.alertDialog("Cannot read/write files. "+ex);
        }
        return read;
    }
    private void openWebsite(String url){
        Desktop desktop=Desktop.getDesktop();
        if(desktop!=null&&desktop.isSupported(Desktop.Action.BROWSE)){
            try{
                desktop.browse(new URL(url).toURI());
            }
            catch(URISyntaxException | IOException e){
                System.out.println("Error opening url. "+e);
            }
        }
        else{
            System.out.println("Opening urls is not supported.");
        }
    }
}
