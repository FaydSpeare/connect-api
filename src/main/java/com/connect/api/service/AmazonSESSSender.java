package com.connect.api.service;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AmazonSESSSender {

    static final String FROM = "fspea123@gmail.com";
    static final String FROMNAME = "Fayd Speare";
    static final String SMTP_USERNAME = "AKIA2TPVUSGA5THJ35EY";
    static final String SMTP_PASSWORD = "BO7maRwsIFmF9sGWxX9n+B6uY52/YXfpu23+B/fTAP5O";
    static final String HOST = "email-smtp.ap-southeast-2.amazonaws.com";
    static final int PORT = 587;

    private static Properties createProperties() {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        return props;
    }

    public static void sendEmail(String subject, String to, String body) throws Exception {
        Properties props = createProperties();
        Session session = Session.getDefaultInstance(props);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setContent(body,"text/html");

        Transport transport = session.getTransport();
        try {
            System.out.println("Sending...");
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());
            System.out.println("Email sent!");
        }
        catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        finally {
            transport.close();
        }

    }

    public static void main(String[] args) throws Exception {
        AmazonSESSSender.sendEmail("this", "fayd.speare@gmail.com", "bruh");
    }
}
