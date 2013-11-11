package controllers;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import controllers.SMTPAuthenticator;

/** Ein Objekt zum Versand von Emails.
 */
public class EmailSender {
	
	/** Der Name des SMTP Servers */
	private String smtpServer;
	/** Die Port des SMTP Servers */ 
	private String port;
	/** Benutzername des E-Mail-Accounts */
	private String user;
	/** Passwort des E-Mail-Accounts */
	private String password;
	/** Flag, welches mittels true oder false angibt, ob eine Authetifizierung notwendig ist. */
	private String auth;
	/** E-Mail-Adresse des Absenders */
	private String from;
	
	/** Initialisiert das EmailSender-Objekt mit allen passenden Daten zur EmailAdresse buecher.boerse@gmx.de.
	 */
	public EmailSender() {
		this.smtpServer = "mail.gmx.net";
		this.port = "465";
		this.user = "buecher.boerse@gmx.de";
		this.password = "#123#abc#";
		this.auth = "true";
		this.from = "buecher.boerse@gmx.de";
	}
	
	/** Initialisiert ein EmailSender-Objekt mit der Adresse eines SMTP-Server, Portnummer, Nutzernamen,
	 * 	Passwort, Absenderadresse und einem Flag, welches mittels true oder false angibt, ob eine Authentifizierung
	 * 	notwendig ist. 
	 * @param smtpServer	Adresse des SMTP Servers 
	 * @param port			Port des SMTP Servers
	 * @param user			Benutzername des E-Mail-Accounts
	 * @param password		Passwort des E-Mail-Accounts
	 * @param auth			Authentifizierung notwendig? true / false
	 * @param from			E-Mail-Adresse des Absenders
	 */
	public EmailSender (String smtpServer, String port, String user, String password, String auth, String from) {
		this.smtpServer = smtpServer;
		this.port = port;
		this.user = user;
		this.password = password;
		this.auth = auth;
		this.from = from;
	}

	/** Versendet eine E-Mail mit dem angegebenen Betreff und Inhalt an alle im Array enthaltenen E-Mail-Adressen. 
	 * 
	 * @param subject		Betreff der E-Mail
	 * @param HtmlMessage	Inhalt der E-Mail
	 * @param to			Array aus E-Mail-Adressen, welche diese E-Mail erhalten sollen.
	 */
	public void sendEmail(String subject,String HtmlMessage,String[] to)
    {
        Transport transport = null;
        try {
            Properties props = prepareProperties();
            Session mailSession = Session.getDefaultInstance(props, new SMTPAuthenticator(from, password, true));
            transport =  mailSession.getTransport("smtp"); // SSL false 
            //transport =  mailSession.getTransport("smtps"); // SSL true
            MimeMessage message = prepareMessage(mailSession, "ISO-8859-2", from, subject, HtmlMessage, to);
            transport.connect();
            Transport.send(message);
        } catch (Exception ex) {    
        }
        finally{
            try {
                transport.close();
            } catch (MessagingException ex) {
                Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

	/** Liefert Objekt vom Typ Properties, welches mit den Nutzerdaten initialisiert wurde.
	 * 
	 * @return	Initialisiertes Objekt vom Properties
	 */
	private Properties prepareProperties()
	{
	   Properties props = new Properties();
       props.setProperty("mail.smtp.host", smtpServer);
       props.setProperty("mail.smtp.port", port);
       props.setProperty("mail.smtp.user", user);
       props.setProperty("mail.smtp.password", password);
       props.setProperty("mail.smtp.auth", auth);
       props.setProperty("mail.debug", "true");
       props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
       return props;
	}
	
	/** Liefert ein MIME-Objekt.
	 * 
	 * @param mailSession	Ein aktive Session
	 * @param charset		Zeichensatz
	 * @param from			E-Mail-Adresse des Absenders
	 * @param subject		Betreff der E-Mail
	 * @param HtmlMessage	Inhalt der E-Mail
	 * @param recipient		Array aus E-Mail-Adressen, welche diese E-Mail erhalten sollen.
	 * @return				Ein mit den o.g. Daten initialisierten MIME-Objekt.
	 */
	private MimeMessage prepareMessage(Session mailSession,String charset, String from, String subject, String HtmlMessage,String[] recipient) {
		MimeMessage message = null;
		try {
			message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			for (int i=0;i<recipient.length;i++)
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient[i]));
				message.setContent(HtmlMessage, "text/html; charset=\""+charset+"\"");
		} catch (Exception ex) {
			Logger.getLogger(EmailSender.class.getName()).log(Level.SEVERE, null, ex);
		}
		return message;
	}	
}
