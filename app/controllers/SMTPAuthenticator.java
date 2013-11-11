package controllers;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/** Diese Objekt ist verantwortlich fuer die Authentifizierung.
 */
public class SMTPAuthenticator extends Authenticator{
	/** Name des Benutzers */
	private String username;
	/** Passwort des Benutzers */
    private String password;
    /** Flag, welches mittels true oder false angibt, ob eine Authetifizierung notwendig ist. */
    private boolean needAuth;

    /** Initialisiert ein Objekt, welches die STMP-Authentifizierung uebernimmt. 
     * 
     * @param username	Name des Benutzers
     * @param password	Passwort des Benutzers
     * @param needAuth	Flag, welches mittels true oder false angibt, ob eine Authetifizierung notwendig ist.
     */
    public SMTPAuthenticator(String username, String password,boolean needAuth)
    {
        this.username = username;
        this.password = password;
        this.needAuth = needAuth;
    }

    /** Liefert ein Objekt vom Typ PasswordAuthentication oder NULL, falls
     * 	keine Authentifizierung notwendig ist.
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        if (needAuth)
            return new PasswordAuthentication(username, password);
                else return null;
    }
}
