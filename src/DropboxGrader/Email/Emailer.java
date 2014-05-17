/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Email;

import DropboxGrader.Config;
import DropboxGrader.Data.Data;
import DropboxGrader.Gui;
import DropboxGrader.TextGrader.TextName;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author matt
 */
public class Emailer {
    private Gui gui;
    
    public Emailer(Gui gui){
        this.gui=gui;
    }
    public void email(TextName name,String message,String subject){
        String email=name.email;
        
        try{
            Properties props=new Properties();
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable",true);
            props.put("mail.smtp.host", Data.MAIL_SMTP_SERVER);
            props.put("mail.smtp.port","587");
            
            Session session=Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(Data.MAIL_USERNAME,Data.MAIL_PASS);
                }
            });
            
            Message msg=new MimeMessage(session);
            msg.reply(false);
            msg.setFrom(new InternetAddress(Data.MAIL_USERNAME, Config.emailSentFrom));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email,name.firstName+" "+name.lastName));
            msg.setSubject(subject);
            msg.setText(message);
            
            Transport.send(msg);
        } catch(MessagingException|UnsupportedEncodingException e){
            System.err.println("Failed to send email.");
            e.printStackTrace();
        }
    }
}
