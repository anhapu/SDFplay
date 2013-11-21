package models;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import models.enums.Roles;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Email;
import play.db.ebean.Model;
import controllers.Common;

@Entity
@Table(name="account")
public class User extends Model
{
    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    @Email
    public String email;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String username;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String firstname;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String lastname;
    
    @Enumerated(EnumType.ORDINAL)
    public Roles role;
    
    
    @Constraints.Required
    @Formats.NonEmpty
    public String password;
  	
		/**
		 * This token is used for a password reset, if it's null the object will be inactive.
		 */
		public String token;
    
		/**
		 * This date is used to check if the token is valid or already expired.
		 */
		public Date tokenCreatedAt;
    
    public static Model.Finder<String,User> find = new Model.Finder<String,User>(String.class, User.class);
    
    /**
     * Retrieve all users.
     */
    public static List<User> findAll() {
        return find.all();
    }
    
    
    /**
     * Retrieve a User from email.
     */
    public static User findByEmail(String email) {
        return find.where().ieq("email", email).findUnique();
    }

    /**
     * Retrieve a User from username.
     */
    public static User findByUsername(String username) {
        return find.where().eq("username", username).findUnique();
    }
    
    /**
     * Retrieve a User from id.
     */
    public static User findById(Long id) {
        return find.byId(id.toString());
    }
    
    /**
     * Authenticate a User via email and password.
     * Returns a user object
     */
    public static User authenticate(String email, String password) {
        return find.where()
            .ieq("email", email.toLowerCase())
            .eq("password", Common.md5(password))
            .findUnique();
    }

		/**
		 * Check if the user is ative or not.
		 */
		public Boolean isActive() {
				boolean active = true;
				if (token != null) {
						active = false;
				}
				return active;
		}
    
    /**
     * Overrides the to string methode.
     */
    @Override
    public String toString() {
        return "User(" + email + ")";
    }  

    public String validate() {
        if (password.length() < 3) {
            return "Passwort zu kurz, min. 3 chars.";
        }
        return null;
    }
}
