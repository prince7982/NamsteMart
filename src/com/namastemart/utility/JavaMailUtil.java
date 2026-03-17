package com.namastemart.utility;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class JavaMailUtil {

    // 🔥 COMMON METHOD TO LOAD EMAIL & PASSWORD
    private static String[] getCredentials() {

        String emailId = null;
        String passWord = null;

        try {
            Properties props = new Properties();

            InputStream input = JavaMailUtil.class
                    .getClassLoader()
                    .getResourceAsStream("application.properties");

            if (input != null) {
                props.load(input);
                emailId = props.getProperty("mailer.email");
                passWord = props.getProperty("mailer.password");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 🔥 fallback for Render environment variables
        if (emailId == null) {
            emailId = System.getenv("mailer.email");
        }
        if (passWord == null) {
            passWord = System.getenv("mailer.password");
        }

        System.out.println("EMAIL: " + emailId);
        System.out.println("PASSWORD: " + passWord);

        return new String[]{emailId, passWord};
    }

    // ✅ MAIN SEND MAIL METHOD
    protected static void sendMail(String recipient, String subject, String htmlTextMessage)
            throws MessagingException {

        System.out.println("Preparing to send Mail...");

        String[] creds = getCredentials();
        String emailId = creds[0];
        String passWord = creds[1];

        if (emailId == null || passWord == null) {
            throw new MessagingException("Email credentials not found");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailId, passWord);
            }
        });

        Message message = prepareMessage(session, emailId, recipient, subject, htmlTextMessage);

        Transport.send(message);

        System.out.println("✅ Message Sent Successfully!");
    }

    // ✅ PREPARE MESSAGE
    private static Message prepareMessage(Session session, String myAccountEmail,
                                          String recipientEmail, String subject,
                                          String htmlTextMessage) {

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlTextMessage, "text/html");

            return message;

        } catch (Exception exception) {
            Logger.getLogger(JavaMailUtil.class.getName())
                    .log(Level.SEVERE, null, exception);
        }
        return null;
    }
}