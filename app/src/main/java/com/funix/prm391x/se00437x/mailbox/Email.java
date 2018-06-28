package com.funix.prm391x.se00437x.mailbox;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

class Email {
    //Set pattern for date format
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("dd/MMM", Locale.US);

    private InternetAddress fromAddress; //the sender information
    private String subject; //the subject of mail received
    private String receivedDate;    //date of received day
    private int messageNumber; //mark the message number to load 10 first mail when opening mail box and...

    Email(Message message) throws MessagingException {
        this.fromAddress = (InternetAddress) message.getFrom()[0];
        this.subject = message.getSubject() == null ? "[No subject]" : message.getSubject();
        this.receivedDate = FORMAT.format(message.getReceivedDate());
        this.messageNumber = message.getMessageNumber();
    }

    @Nullable
    static String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) { //Test the text type of content to return
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    String getAddress() {
        return fromAddress.getAddress();
    }

    String getFrom() {
        String personal = fromAddress.getPersonal();
        return (personal == null ? "" : personal + " ") + "<" + fromAddress.getAddress() + ">";
    }

    String getSubject() {
        return subject;
    }

    String getReceivedDate() {
        return receivedDate;
    }

    int getMessageNumber() {
        return messageNumber;
    }
}
