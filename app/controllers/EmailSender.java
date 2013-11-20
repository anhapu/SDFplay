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

import models.User;
import controllers.SMTPAuthenticator;
import views.html.email.forgotPassword;
import views.html.email.registration;
import views.html.email.exchangeRequest;

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
	    Session mailSession = Session.getInstance(props, new SMTPAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"), true));
	    try {
		    Message msg = new MimeMessage(mailSession);
		    msg.setFrom(new InternetAddress(props.getProperty("mail.smtp.user")));
		    InternetAddress[] address = {new InternetAddress(to)};
		    msg.setRecipients(Message.RecipientType.TO, address);
		    msg.setSubject(subject);
		    msg.setSentDate(new Date());
		    msg.setContent(message, "text/plain; charset=utf-8");
		  //msg.setContent( message, "text/html; charset=utf-8" );
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

	/** Versendet eine E-Mail zur Bestaetigung der Registrierung an den Nutzer.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param user			Nutzer, der sich registriert hat.
	 */ 
	public static void sendRegistration(User user) {
		String message = registration.render(user.username).toString();
		send("Registrierung erfolgreich.", message, user.email);
	}
	
	/** Versendet eine E-Mail mit einem book exchange request an einen Nutzer.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static void sendBookExchangeRequest(User fromUser, User toUser) {
		String message = exchangeRequest.render(fromUser.username, toUser.username).toString();
		send("Neue Tauschanfrage von " + fromUser.username, message, toUser.email);
	}
	
	/** Sendet eine E-Mail mit einem Link zu Resetten des Passworts.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 * 
	 * @param user			Nutzer, der sein Passwort resetten moechte.
	 * @param linkAddress	Link, der auf eine Seite zum Reset des Passworts fuehrt.
	 */
	public static void sendForgotPassword(User user, String linkAddress) {
		String message = forgotPassword.render(user.username, linkAddress).toString();
		send("Reset Ihres Passworts..", message, user.email);
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
	    props.setProperty("mail.smtp.ssl.enable", "true");
	    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
       return props;
	}	
}
