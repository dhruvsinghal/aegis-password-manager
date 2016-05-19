package main.java.server.util;

import main.java.server.Server;

import java.net.InetAddress;

/**
 * Various templates for helping generate emails
 * <p>
 * Note that the "to" parameter is the email address to send the message to
 */
public class EmailTemplates {
    public static EmailServer.Email generateVerificationEmail(String to, String code) {
        String subject = "Aegis Sign Up Verification Code";

        String body = "<p>Welcome to Aegis " + to + "! </p>";
        body += "<p>Please enter the following code on the linked page and complete your sign up process.</p>";
        body += "<p>Code: " + code + "</p>";

        String addr = "";
        int port = Server.getPort();
        try {
            InetAddress i = InetAddress.getLocalHost();
            addr = i.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        body += "<p>Link: <a href=\"https://" + addr + ":" + port + "/index.html#/signup2\">Click here</a></p>";
        body += "<p>Regards,</p>";
        body += "<p>Team Aegis</p>";

        return new EmailServer.Email(to, subject, body);
    }

    public static EmailServer.Email generateLoginVerificationEmail(String to, String code) {
        String subject = "Aegis Login Verification Code";

        String body = "<p>Welcome back to Aegis " + to + "! </p>";
        body += "<p>Please enter the following code to login.</p>";
        body += "<p>Code: " + code + "</p>";

        body += "<p>Regards,</p>";
        body += "<p>Team Aegis</p>";

        return new EmailServer.Email(to, subject, body);
    }

    public static EmailServer.Email generateSignUpConfirmation(String to, String name) {
        String subject = "Welcome to Aegis!";

        String body = "<p>Hi " + name + "</p>";
        body += "<p>Welcome to Aegis! Your sign up was successful. Your username is: " + to + "</p>";

        String addr = "";
        int port = Server.getPort();
        try {
            InetAddress i = InetAddress.getLocalHost();
            addr = i.getHostAddress();

        } catch (Exception e) {
            e.printStackTrace();
        }

        body += "<p>To start using your Aegis account, visit this <a href=\"https://" + addr + ":" + port + "/index.html#/login\">link</a></p>";
        body += "<p>Regards,</p>";
        body += "<p>Team Aegis</p>";

        return new EmailServer.Email(to, subject, body);
    }

}
