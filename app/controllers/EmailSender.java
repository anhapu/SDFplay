package controllers;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import controllers.SMTPAuthenticator;

/** Ein Objekt zum Versand von Emails.
 */
public class EmailSender {
	
	/** privater Constructor.
	 */
	private EmailSender() {
	}
		
	/** Versendet eine E-Mail mit dem angegebenen Betreff und Inhalt an die gewuenschte Ziel-E-Mail-Adresse.
	 *  Der Absender ist buecher.boerse@gmx.de. 
	 * 
	 * @param subject		Betreff der E-Mail
	 * @param message		Inhalt der E-Mail
	 * @param to			Ziel-E-Mail-Adresse
	 */
	public static void send(String subject, String message, String to) {
		Properties props = prepareProperties();
	    Session mailSession = Session.getDefaultInstance(props, new SMTPAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"), true));
	    try {
		    Message msg = new MimeMessage(mailSession);
		    msg.setFrom(new InternetAddress(props.getProperty("mail.smtp.user")));
		    InternetAddress[] address = {new InternetAddress(to)};
		    msg.setRecipients(Message.RecipientType.TO, address);
		    msg.setSubject(subject);
		    msg.setSentDate(new Date());
		    //( message, "text/html; charset=utf-8" );
		    msg.setText(message);		    
		    Transport.send(msg);
		} catch (MessagingException mex) {
		    mex.printStackTrace();
		    System.out.println();
		    Exception ex = mex;
		    do {
			if (ex instanceof SendFailedException) {
			    SendFailedException sfex = (SendFailedException)ex;
			    Address[] invalid = sfex.getInvalidAddresses();
			    if (invalid != null) {
				System.out.println("    ** Invalid Addresses");
				if (invalid != null) {
				    for (int i = 0; i < invalid.length; i++) 
					System.out.println("         " + invalid[i]);
				}
			    }
			    Address[] validUnsent = sfex.getValidUnsentAddresses();
			    if (validUnsent != null) {
				System.out.println("    ** ValidUnsent Addresses");
				if (validUnsent != null) {
				    for (int i = 0; i < validUnsent.length; i++) 
					System.out.println("         "+validUnsent[i]);
				}
			    }
			    Address[] validSent = sfex.getValidSentAddresses();
			    if (validSent != null) {
				System.out.println("    ** ValidSent Addresses");
				if (validSent != null) {
				    for (int i = 0; i < validSent.length; i++) 
					System.out.println("         "+validSent[i]);
				}
			    }
			}
			System.out.println();
			if (ex instanceof MessagingException)
			    ex = ((MessagingException)ex).getNextException();
			else
			    ex = null;
		    } while (ex != null);
		}
	}


	/** Liefert Objekt vom Typ Properties, welches mit den Nutzerdaten der E-Mail-Adresse von buecher.boerse@gmx.de 
	 * 	initialisiert wurde.
	 * 
	 * @return	Initialisiertes Objekt vom Properties
	 */
	private static Properties prepareProperties()
	{
		Properties props = new Properties();
	    props.setProperty("mail.smtp.host", "mail.gmx.net");
	    props.setProperty("mail.smtp.port", "465");
	    props.setProperty("mail.smtp.user", "buecher.boerse@gmx.de");
	    props.setProperty("mail.smtp.password", "#123#abc#");
	    props.setProperty("mail.smtp.auth", "true");
	    props.setProperty("mail.debug", "true");
	    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
       return props;
	}	
}
