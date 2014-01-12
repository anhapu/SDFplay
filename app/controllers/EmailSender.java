package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Exchanger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import play.Logger;
import models.User;
import controllers.SMTPAuthenticator;
import views.html.email.exchangeInvalidOwner;
import views.html.email.exchangeInvalidRecipient;
import views.html.email.forgotPassword;
import views.html.email.registration;
import views.html.email.exchangeInit;
import views.html.email.exchangeResponse;
import views.html.email.exchangeRefuse;
import views.html.email.exchangeApproveOwner;
import views.html.email.exchangeApproveRecipient;
import views.html.email.exchangeFinalRefuse;
import views.html.email.exchangeDeletedOwner;
import views.html.email.exchangeDeletedRecipient;

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
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(new Email(subject, message, to));
		send(emailList);
	}
	
	/** Versendet E-Mails mit dem angegebenen Betreff und Inhalt an die gewuenschte Ziel-E-Mail-Adresse.
	 *  Der Absender ist buecher.boerse@gmx.de. 
	 * 
	 * @param emailList		Eine Liste von Objekten vom Typ Email
	 */
	public static void send(List<Email> emailList) {
		Properties props = prepareProperties();
	    Session mailSession = Session.getInstance(props, new SMTPAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"), true));
	    try {
		    Message msg = new MimeMessage(mailSession);
		    msg.setFrom(new InternetAddress(props.getProperty("mail.smtp.user")));
		    
		    for (Email email : emailList) {
		    	InternetAddress[] address = {new InternetAddress(email.getTo())};
			    msg.setRecipients(Message.RecipientType.TO, address);
			    msg.setSubject(email.getSubject());
			    msg.setSentDate(new Date());
			    msg.setContent(email.getMessage(), "text/plain; charset=utf-8");
			  //msg.setContent( message, "text/html; charset=utf-8" );
			    Logger.info("Sending E-Mail : >>" + email.getSubject() + "<< to " + email.getTo());
			    Transport.send(msg);
		    }
  
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
		String message = exchangeInit.render(fromUser.username, toUser.username).toString();
		send("Neue Tauschanfrage von " + fromUser.username, message, toUser.email);
	}
	
	/** Versendet eine E-Mail mit Informationen zum Status RESPONSE an einen Nutzer.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static void sendBookExchangeResponse(User fromUser, User toUser) {
		String message = exchangeResponse.render(fromUser.username, toUser.username).toString();
		send("Reaktion auf Ihre Tauschanfrage an " + fromUser.username, message, fromUser.email);
	}
	
	
	/** Versendet eine E-Mail mit Informationen zum Status REFUSE an einen Nutzer.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static void sendBookExchangeRefuse(User fromUser, User toUser) {
		String message = exchangeRefuse.render(fromUser.username, toUser.username).toString();
		send("Ihre Tauschanfrage an " + fromUser.username + " wurde abgelehnt.", message, fromUser.email);
	}
	

	/** Versendet E-Mails an owner und recipient, dass eine Tauschanfrage abgeschlossen wurde. (State.APPROCE)
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static void sendBookExchangeApprove(User fromUser, User toUser) {
		String messageOwner = exchangeApproveOwner.render(fromUser.username, toUser.username).toString();
		String messageRecipient = exchangeApproveRecipient.render(fromUser.username, toUser.username).toString();
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(new Email("Ihre Tauschanfrage an " + toUser.username + " wurde erfolgreich abgeschlossen.", messageOwner, fromUser.email));
		emailList.add(new Email("Die Tauschanfrage von " + fromUser.username + " wurde erfolgreich abgeschlossen.", messageRecipient, toUser.email));
		send(emailList);
	}
	
	/** Liefert eine Liste von Email-Objekten, die dafür verwendet wird um Nutzern mitzuteilen, dass eine Tauschanfrage abgeschlossen wurde. (State.APPROVE)
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static List<Email> getBookExchangeApprove(User fromUser, User toUser) {
		String messageOwner = exchangeApproveOwner.render(fromUser.username, toUser.username).toString();
		String messageRecipient = exchangeApproveRecipient.render(fromUser.username, toUser.username).toString();
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(new Email("Ihre Tauschanfrage an " + toUser.username + " wurde erfolgreich abgeschlossen.", messageOwner, fromUser.email));
		emailList.add(new Email("Die Tauschanfrage von " + fromUser.username + " wurde erfolgreich abgeschlossen.", messageRecipient, toUser.email));
		return emailList;
	}
	
	/** Liefert eine Liste von Email-Objekten, die dafür verwendet wird um Nutzern mitzuteilen, dass eine Tauschanfrage ungültig wurde. (State.INVALID)
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static List<Email> getBookExchangeInvalid(User fromUser, User toUser) {
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(new Email("Ihre Tauschanfrage an " + toUser.username + " wurde für ungültig erklärt.", exchangeInvalidOwner.render(fromUser.username, toUser.username).toString(), fromUser.email));
		emailList.add(new Email("Die Tauschanfrage von " + fromUser.username + " an Sie wurde für ungültig erklärt.", exchangeInvalidRecipient.render(fromUser.username, toUser.username).toString(), toUser.email));
		return emailList;
	}
	
	/** Liefert eine Liste von Email-Objekten, die dafür verwendet wird um Nutzern mitzuteilen, dass eine Tauschanfrage gelöscht wurde.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static List<Email> getBookExchangeDeleted(User fromUser, User toUser) {
		List<Email> emailList = new ArrayList<Email>();
		emailList.add(new Email("Die Tauschanfrage an " + toUser.username + " wurde gelöscht.", exchangeDeletedOwner.render(fromUser.username, toUser.username).toString(), fromUser.email));
		emailList.add(new Email("Die Tauschanfrage von " + fromUser.username + " wurde gelöscht.", exchangeDeletedRecipient.render(fromUser.username, toUser.username).toString(), toUser.email));
		return emailList;
	}
	
	/** Versendet eine E-Mail mit Informationen zum Status FINALREFUSE an einen Nutzer.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param fromUser		Nutzer, welcher den exchange request gestellt hat.
	 * @param toUser		Nutzer, an den dieser exchange request gerichtet ist.
	 */
	public static void sendBookExchangeFinalRefuse(User fromUser, User toUser) {
		String message = exchangeFinalRefuse.render(fromUser.username, toUser.username).toString();
		send("Die Tauschanfrage von " + fromUser.username + " an Sie wurde abgelehnt.", message, toUser.email);
	}
	
	/** Versendet E-Mails mit Informationen zu dem Status INVALID an eine Reihe von Nutzern.
	 *  Der Absender ist buecher.boerse@gmx.de.
	 *  
	 * @param emailListe		Eine Liste von zu versendenden E-Mails
	 */
	public static void sendBookExchangeInvalid(List<Email> emailListe) {
		send(emailListe);
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
	    // set debug to true, if you want mailing output :)
	    props.setProperty("mail.debug", "false");
	    props.setProperty("mail.smtp.timeout", "1000");
	    props.setProperty("mail.smtp.ssl.enable", "true");
	    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
       return props;
	}	
}
