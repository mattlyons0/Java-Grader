/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import java.io.IOException;
import java.util.Locale;

/**
 *https://www.dropbox.com/developers/core/start/java
 * @author 141lyonsm
 */
public class Main {
    public final static String appName="Matt Lyons Assignment Grader";
    public final static double appVersion=0.1;
    public static void main(String[] args) throws IOException, DbxException {
        // Get your app key and secret from the Dropbox developers website.
        final String APP_KEY = "681xzhh2nqu3hjc";
        final String APP_SECRET = "k7e1pkfljgg1jdb";

        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
            appName+" "+appVersion, Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
    }
}
