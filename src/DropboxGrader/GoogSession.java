/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Google API doesnt support OAuth 2.0 with java, so OAuth 1.0 will work until 2015...
 * @author Matt
 */
public class GoogSession {
    private final String ClientID="382316958287-4fbaih27ri2hknvhcjeh44rg258n3m4n.apps.googleusercontent.com";
    private final String ClientSecret="W4xYMHvl74iwTk39URe1f_n8";
    private final String scope="https://spreadsheets.google.com/feeds https://docs.google.com/feeds"; //list of sites that can use this api
    private final String redirectUrl="urn:ietf:wg:oauth:2.0:oob";
    private final boolean useRSA=false;
    private final File tokenFile=new File("goog.key");
    private GoogleOAuthHelper oauthHelper;
    private GoogleOAuthParameters oauthParams;
    private SpreadsheetService service;
    public GoogSession(){
        
        createSession();
    }
    private void createSession(){
        try{
            oauthParams=new GoogleOAuthParameters();
            oauthParams.setOAuthConsumerKey(ClientID);
            OAuthSigner signer;
            if (useRSA){
                signer=new OAuthRsaSha1Signer(ClientSecret);
            }
            else{
                oauthParams.setOAuthConsumerSecret(ClientSecret);
                signer=new OAuthHmacSha1Signer();
            }
            oauthHelper=new GoogleOAuthHelper(signer);
            oauthParams.setScope(scope);
            oauthHelper.getUnauthorizedRequestToken(oauthParams);
            String token;
            if(!tokenFile.exists()){
                String requestURL=oauthHelper.createUserAuthorizationUrl(oauthParams);
                DbxSession.openWebsite(requestURL);
                GuiHelper.alertDialog("Please authenticate this program.");
                setToken(true);
            }
            else{
                setToken(false);
            }
        } catch (OAuthException ex) {
                Logger.getLogger(GoogSession.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    private void setToken(boolean newToken){
        String token;
        if(newToken){
            try {
                token=oauthHelper.getAccessToken(oauthParams);
                DbxSession.writeToFile(tokenFile, token);
            } catch (OAuthException ex) {
                createSession();
            }
        }
        service=new SpreadsheetService(DbxSession.appName);
    }
    public void newTokenAuthenticated(){
        setToken(true);
    }
    public SpreadsheetService getService(){
        return service;
    }
}
