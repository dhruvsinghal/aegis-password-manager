package main.java.server.util;

import org.jetbrains.annotations.NotNull;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;

/**
 * A server that sends emails
 */
public class EmailServer {
    private final String from;
    private final String password;
    private final String host;


    public EmailServer(String from, String password, Optional<String> host) {
        this.from = from;
        this.password = password;
        this.host = host.orElse("localhost");
    }

    public synchronized boolean sendEmail(Email email) {
        //Get System Properties
        Properties properties = System.getProperties();

        //setup mail server
        //properties.setProperty("mail.smtp.host", this.host);
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");

        //get the default session object
        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            //Create a default mime message object
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email.to));
            message.setSubject(email.subject);
            message.setContent(email.body, "text/html");

            //Send the message
            Transport.send(message);
            return true;

        } catch (MessagingException mex) {
            mex.printStackTrace();
            return false;
        }

    }

    public static class Email {
        /**
         * Who this email is to (email address)
         */
        @NotNull
        public final String to;

        /**
         * Subject of the email
         */
        @NotNull
        public final String subject;

        /**
         * Body of the email
         */
        @NotNull
        public final String body;


        public Email(@NotNull String to, @NotNull String subject, @NotNull String body) {
            this.subject = subject;
            this.body = body;
            this.to = to;
        }
    }
}
