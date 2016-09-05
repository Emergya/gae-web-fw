/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emergya.spring.gae.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utility to easily send mail from other code.
 *
 * @author lroman
 */
public final class MailSender {

    private MailSender() {

    }

    /**
     * Method to send an email.
     *
     * @param to Direction that received the message.
     * @param from Direction that send the message.
     * @param fromAlias Alias for the from address.
     * @param subject Message subject.
     * @param message Email message.
     */
    public static void sendEmail(
            String to, String from, String fromAlias,
            String subject, String message) {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, fromAlias));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setContent(message, "text/html");
            Transport.send(msg);
        } catch (AddressException e) {
            Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, e);
        } catch (MessagingException | UnsupportedEncodingException e) {
            Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
