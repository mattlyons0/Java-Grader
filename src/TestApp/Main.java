/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TestApp;

import DropboxGrader.Data.Data;
import DropboxGrader.DbxSession;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

/**
 *https://www.dropbox.com/developers/core/start/java
 * @author 141lyonsm
 */
public class Main {
    public final static String appName="Java Grader";
    public final static double appVersion=DbxSession.APPVERSION;
    public static void main(String[] args) throws IOException, DbxException {
        final String APP_KEY = Data.APP_KEY;
        final String APP_SECRET = Data.APP_SECRET;

        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
            appName+" "+appVersion, Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        openWebsite(webAuth.start());
        String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
        DbxAuthFinish authFinish = webAuth.finish(code);
        
        DbxClient client = new DbxClient(config, authFinish.accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/DROPitTOme");
        System.out.println("Files in the root path:");
        for (DbxEntry child : listing.children) {
            System.out.println("	" + child.name + ": " + child.toString());
    }
    }
    private static void openWebsite(String url){
        Desktop desktop=Desktop.getDesktop();
        if(desktop!=null&&desktop.isSupported(Desktop.Action.BROWSE)){
            try{
                desktop.browse(new URL(url).toURI());
            }
            catch(URISyntaxException | IOException e){
                System.out.println("Error opening url. "+e);
                e.printStackTrace();
            }
        }
        else{
            System.out.println("Opening urls is not supported.");
        }
    }
}
