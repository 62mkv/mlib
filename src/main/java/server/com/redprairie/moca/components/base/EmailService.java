/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005-2007
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.components.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaTrace;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.exceptions.InvalidArgumentException;
import com.sun.mail.smtp.SMTPTransport;

public class EmailService {

    /**
     * Sends emails to one or more recipients through SMTP.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param host the hostname or IP address of the SMTP server. This argument
     *                cannot be null.
     * @param port the port on which the SMTP server is listening. The default
     *                value is 25.
     * @param user the user to login as to the smtp server. This argument CAN be
     *                null.
     * @param password the password for the user login. If user is not null then
     *                this value cannot be null.
     * @param TLS Encrypt the connection to the smtp server immediately. Both
     *                Authorization exchanges and message will be transferred
     *                over an encrypted tunnel.
     * @param fromAddr the email address of the sender. This argument cannot be
     *                null.
     * @param toAddr the email address of the recipient/recipients.This argument
     *                cannot be null.
     * @param replyTo the email address to which the reply need to be sent.
     * @param fromName the sender's name.
     * @param toName the recipients's name.
     * @param subject the subject of the email.This argument cannot be null.
     * @param msg the message to be sent via email.This argument cannot be null.
     * @param document the document that contains the contents to be sent.This
     *                can be in addition to the message(msg)
     * @param attachments the attachments to be sent along with the email.
     * @throws EmailServiceException if an SMTP error occurs.
     * @throws InvalidArgumentException if an invalid attachment path is given
     */
    public MocaResults sendMail(MocaContext moca, String host, int port,
                                String user, String password, int enableTLS,
                                String fromAddr, String toAddr, String replyTo,
                                String fromName, String toName, String subject,
                                String msg, String document, String attachments)
            throws EmailServiceException, InvalidArgumentException {
        boolean useTLS = (enableTLS != 0) ? true : false;

        try {
            // We must first obtain a session object. We'll need
            // to configure it with SMTP properties.
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", host);
            props.setProperty("mail.smtp.port", String.valueOf(port));
            
            // MOCA-6939 - there's some scenarios where the connection
            // with the SMTP server may be severed but we're stuck waiting
            // on a response on the socket forever during sendMessage(). To prevent
            // this from hanging forever introduce some generous timeout defaults.
            // These can also be configured via Java system properties.
            props.setProperty("mail.smtp.connectiontimeout", 
                System.getProperty("mail.smtp.connectiontimeout", "60000"));
            props.setProperty("mail.smtp.timeout", 
                System.getProperty("mail.smtp.timeout", "60000"));
            Session session = Session.getInstance(props, null);

            // Now, create a new message to send.
            MimeMessage message = new MimeMessage(session);
            InternetAddress from = new InternetAddress(fromAddr);
            message.setFrom(from);

            // Set the content-type header
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");

            // Determine the addressee(s)
            InternetAddress[] addressTo = splitAddressList(toAddr);
            message.setRecipients(Message.RecipientType.TO, addressTo);

            // Set the message subject
            message.setSubject(subject, "UTF-8");

            // Set the reply-to address, if passed.
            if (replyTo != null) {
                message.setReplyTo(splitAddressList(replyTo));
            }

            // first, create the message body. Then, add any attachments.
            MimeBodyPart mbpMsg = new MimeBodyPart();

            // If they specified a document file, send that file
            // as the body
            if (document != null) {
                moca.trace(MocaTrace.FLOW, "Adding document text from "
                        + document);

                StringBuilder tmp = new StringBuilder(msg);
                tmp.append("\n\n");
                tmp.append(document);
                tmp.append(':');
                tmp.append("\n\n");
                tmp.append(getDocContents(new File(document)));
                msg = tmp.toString();
            }

            mbpMsg.setText(msg, "UTF-8");

            Multipart content = new MimeMultipart();
            content.addBodyPart(mbpMsg);

            // Add any attachments.
            if (attachments != null) {
                String[] attach = attachments.split(";");

                for (int i = 0; i < attach.length; i++) {
                    final String[] name = attach[i].split(",");
                    final String sourceFilePathName = name[0];
                    final File attachFile = new File(sourceFilePathName);
                    final String destFileName;
                    
                    if (!attachFile.isFile()) {
                        throw new InvalidArgumentException("Invalid attachment path: " + name[0]);
                    }
                    
                    if (name.length == 1) {
                        destFileName = attachFile.getName();
                    }
                    else if (name.length == 2) {
                        destFileName = name[1];
                    }
                    else {
                        throw new EmailServiceException("Attachment argument invalid: " + attach[i]);
                    }

                    moca.trace(MocaTrace.FLOW, "Adding attachment "+ sourceFilePathName);
                    
                    MimeBodyPart attachment = new MimeBodyPart();
                    DataSource source = new FileDataSource(sourceFilePathName);
                    attachment.setDataHandler(new DataHandler(source));
                    attachment.setFileName(destFileName);
                    content.addBodyPart(attachment);
                }
            }

            message.setContent(content);

            // send the message
            moca.trace(MocaTrace.FLOW,
                "Sending message along with attachents, if any");
            Transport transport = session.getTransport("smtp");
            ((SMTPTransport) transport).setStartTLS(useTLS);
            if (user != null) {
                transport.connect(user, password);
            }
            else {
                transport.connect();
            }
            transport.sendMessage(message, addressTo);
            transport.close();

            // Set up result
            EditableResults res = moca.newResults();
            res.addColumn("status", MocaType.STRING);
            res.addRow();
            res.setStringValue("status", "Email to " + toAddr + " succeeded");
            
            return res;
        }
        catch (MessagingException e) {
            throw new EmailServiceException("Error sending email: " + e, e);
        }
    }

    /*
     * Method used to extract the contents of "document" it fetches each line
     * and returns the contents as a string.
     */
    private String getDocContents(File doc) throws EmailServiceException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                doc), "UTF-8"));
            String eachLine = null;

            StringBuilder docContents = new StringBuilder();
            while ((eachLine = in.readLine()) != null) {
                docContents.append(eachLine);
                docContents.append(System.getProperty("line.separator"));
            }
            return docContents.toString();
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new EmailServiceException("Error reading document: " + e, e);
        }
        finally {
            try {
                if (in != null) in.close();
            }
            catch (IOException e) {
                _logger.debug("There was an issue closing document file", e);
            }
        }
    }

    /*
     * Split an address list of the form: addr1@host;addr2@host;... into an
     * array of address objects.
     */
    private InternetAddress[] splitAddressList(String addresses)
            throws AddressException {
        String[] nameList = addresses.split(";");
        InternetAddress[] addList = new InternetAddress[nameList.length];
        for (int i = 0; i < nameList.length; i++) {
            addList[i] = new InternetAddress(nameList[i]);
        }
        return addList;

    }
    
    private static final Logger _logger = LogManager.getLogger(EmailService.class);
}
