/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Email;

import DropboxGrader.Config;
import DropboxGrader.Data.Data;
import DropboxGrader.FileManagement.Date;
import DropboxGrader.Gui;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextName;
import DropboxGrader.UnitTesting.SimpleTesting.UnitTest;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Address;
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
    
    public void emailAssignment(TextAssignment assign, boolean created){
        TextName[] names=gui.getGrader().getSpreadsheet().getAllNames();
        for(TextName n:names){
            if(n.email!=null&&!n.email.equals("")){
                String subject="Assignment "+assign.number+(assign.name!=null?", "+assign.name:"")+" has been "+(created?"Created":"Modified");
                String days=Date.differenceBefore(Date.currentDate(),assign.dateDue);
                String message="<h3>"+subject+"</h3>"+("<b>Date Due:</b> "+assign.dateDue+(days!=null?" (In Approximately "+ //null if passed for some reason
                        Date.differenceBefore(Date.currentDate(),assign.dateDue)+")":"")+"\n");
                message+="<b>Total Points:</b> "+assign.totalPoints+"\n";
                if(assign.simpleUnitTests!=null&&assign.simpleUnitTests.length>0){
                    message+="\nThis assignment must contain the following methods:<ul>";
                    for(UnitTest t:assign.simpleUnitTests){
//                        String accessType=UnitTester.getAccesses(t); ///having these in makes the email too confusing to look at
//                        String modifiers=UnitTester.getModifiers(t);
                        message+="<li><ul>"+t.getMethodName()+"("+t.getArgumentTypesString()+")"
                                +"<li>Returns: "+(t.getReturnTypeString()==null||t.getReturnTypeString().equals("")?
                                "void ":t.getReturnTypeString()+" ")+"</li></ul></li>";
                    }
                    message+="</ul>";
                }
                if(assign.junitTests!=null&&assign.junitTests.length>0){
                    message+="\nThis assignment will be JUnit Tested. It requires certain methods to be defined. Your teacher will"
                            + " instruct you how to define those methods.";
                }
                if(Config.emailHowtoSubmit&&Config.submitURL!=null){
                    message+="\n<h4>How To Submit</h4>Go To <a href='"+Config.submitURL+"'>"+Config.submitURL+"</a>"
                            + " and upload a Zip File named '"+n.firstName+n.lastName+"_"+assign.number+"_<i>(A Breif Description of the Assignment)</i>.zip";
                }
                email(n,subject,message);
            }
        }
    }
    
    public void emailSet(TextName name){
        String subject="Email Confirmation";
        String message="This message has been sent to confirm your email is connected to the '"+
                Config.emailSentFrom+"' Homework Grading Program.\n\n";
        message+="If you believe this message was done in error, please contact "+
                (Config.emailTeacher!=null?
                    (Config.emailTeacherName!=null?(Config.emailTeacherName+" at "+Config.emailTeacher):Config.emailTeacher)
                    :"your Computer Science Teacher")+".";
        email(name,subject,message);
    }
    
    public void emailLateAssignment(TextAssignment assign,TextName name){
        String subject="Assignment "+assign.number+(assign.name!=null?", "+assign.name:"")
                +" is Missing";
        String message=subject+". It was due on "+assign.dateDue.toString()+".\n";
        
        email(name,subject,message);
    }
    
    public void emailGraded(TextAssignment assign,TextName name, TextGrade grade,TextGrade oldGrade){
        String subject="Assignment "+assign.number+(assign.name!=null?", "+assign.name:"")
                +" was Graded";
        String message="<h3>"+subject+"</h3>";
        message+=(oldGrade!=null?"<h4>New</h4>":"\n\n")+"<b>Grade:</b> "+grade.grade+(grade.comment!=null?"\n<b>Comment:</b> "+grade.comment:"")+"\n<b>Graded On:</b> "
                +grade.dateGraded+"\n\n";
        if(oldGrade!=null)
            message+="<h4>Old</h4><b>Grade:</b> "+oldGrade.grade+(oldGrade.comment!=null?"\n<b>Comment:</b> "+oldGrade.comment:"")+
                    "\n<b>Graded On:</b> "+oldGrade.dateGraded;
        
        email(name,subject,message);
    }
    
    public void email(TextName name,String subject,String message){
        if(name==null||name.email==null||message==null)
            return;
        if(subject==null)
            subject="(No Subject)";
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
            
            MimeMessage msg=new MimeMessage(session);
            if(Config.emailTeacher!=null){
                msg.reply(true);
                if(Config.emailTeacherName!=null)
                    msg.setReplyTo(new Address[]{new InternetAddress(Config.emailTeacher,Config.emailTeacherName)});
                else
                    msg.setReplyTo(new Address[]{new InternetAddress(Config.emailTeacher,Config.emailSentFrom)});
            }
            else
                msg.reply(false);
            msg.setFrom(new InternetAddress(Data.MAIL_USERNAME, Config.emailSentFrom));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email,name.firstName+" "+name.lastName));
            msg.setSubject(subject);

            message=message.replaceAll("\n","<br/>\r");
            msg.setText("<html><body>"+message+"</body></html>","utf-8","html");
            
            
            Transport.send(msg);
        } catch(MessagingException|UnsupportedEncodingException e){
            System.err.println("Failed to send email.");
            e.printStackTrace();
        }
    }
}
