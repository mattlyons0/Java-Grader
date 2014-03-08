/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import DropboxGrader.Data.Data;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Source;

/**
 *
 * @author 141lyonsm
 */
public class DbxSession {
    public static final String APPNAME="Matt Lyons Assignment Grader";
    public static final double APPVERSION=0.52;
    private final File KEYFILE;
    private boolean invalidToken=false;
    private DbxClient client;
    private DbxWebAuthNoRedirect webAuth;
    private Gui gui;
    
    public DbxSession(Gui gui){
        KEYFILE=new File("dbx.key");
        this.gui=gui;
        
        createSession();
    }
    public DbxClient getClient(){
        return client;
    }
    private void createSession(){
        DbxAppInfo appInfo = new DbxAppInfo(Data.APP_KEY, Data.APP_SECRET);
        DbxRequestConfig config = new DbxRequestConfig(
            APPNAME+" "+APPVERSION, Locale.getDefault().toString());
        webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        if(!KEYFILE.exists()||invalidToken){
            String token=getToken(true);
            if(token!=null)
            client=new DbxClient(config,token);
            else
                return; //return if there no key specified yet.
        }
        else{
            client=new DbxClient(config,getToken(false));
        }
        try {
            gui.goodKey(client.getAccountInfo().displayName,client);
        } catch (DbxException ex) {
            if(ex instanceof DbxException.InvalidAccessToken){
                invalidToken=true;
            }
            else if(ex instanceof DbxException.NetworkIO){
                GuiHelper.alertDialog("Error connecting to the network.");
            }
            else if(ex instanceof DbxException.RetryLater||ex instanceof DbxException.ServerError){
                GuiHelper.alertDialog("Dropbox is currently overloaded. We will keep trying to get through.");
            }
            else{
                GuiHelper.alertDialog("Unknown error connecting to dropbox. "+ex);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex1) {
                Logger.getLogger(DbxSession.class.getName()).log(Level.SEVERE, null, ex1);
            }
            createSession();
        }
    }
    private String getToken(boolean newKey){
        if(newKey){
            gui.promptKey();
            openWebsite(webAuth.start());

            return null;
        }
        else{
            return readFromFile(KEYFILE);
        }
    }
    public void setKey(String key){
        String val;
        try {
                val=webAuth.finish(key).accessToken;
            } catch (DbxException ex) {
                if(ex instanceof DbxException.BadRequest){
                    gui.badKey();
                    return;
                }
                val=null;
            }
            writeToFile(KEYFILE,val);
            createSession();
    }
    public static void writeToFile(File f,String s){
        try{
            if(!f.exists()){
                f.createNewFile();
            }
            OutputStreamWriter writer=new OutputStreamWriter(new FileOutputStream(f),Charset.forName("UTF-8"));
            writer.write(s);
            
            writer.close();
        } catch (IOException ex) {
            GuiHelper.alertDialog("Cannot read/write files. "+ex);
            System.exit(1); //this is um bad, because we made this into a static method.
        }
    }
    public static String readFromFile(File f){
        String read="";
        try {
            boolean firstRead=true;
            String line;
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF8"));
            while((line=reader.readLine())!=null){
                if(firstRead){
                    read+=line;
                    firstRead=false;
                }
                else{
                    read+="\n"+line;
                }
            }
            reader.close();
        } catch (IOException ex) {
            GuiHelper.alertDialog("Cannot read/write files. "+ex);
        }
        return read;
    }
    public static void openWebsite(String url){
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
    public static String getVersion(){
        String version=APPVERSION+"";
        String out="";
        for(int i=0;i<version.length();i++){
            if(i%2==0&&i!=0){
                if(version.charAt(i)!='.')
                    out+=version.charAt(i)+".";
            }
            else if(i%2==1){
                out+=version.charAt(i);
            }
            else{ //i==0
                out+=version.charAt(i);
            }
        }
        return out;
    }
}
