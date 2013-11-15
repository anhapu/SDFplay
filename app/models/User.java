package models;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.management.relation.Role;
import javax.persistence.*;
import javax.validation.Constraint;

import org.jboss.logging.FormatWith;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;

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
    public Role role;
    
    
    @Constraints.Required
    @Formats.NonEmpty
    public String password;
  
    
    
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
        return find.where().eq("email", email).findUnique();
    }
    
    /**
     * Retrieve a User from email.
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
            .eq("email", email)
            .eq("password", User.md5(password))
            .findUnique();
    }
    
    
    /**
     * Overrides the to string methode.
     */
    @Override
    public String toString() {
        return "User(" + email + ")";
    }

    /**
     * Generates an md5 hash of a String.
     * @param input String value
     * @return Hashvalue of the String.
     */
    public static String md5(String input) {
        
        String md5 = null;
        if(null == input) return null;
        
            try {
            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);
    
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            
        return md5;
    }
    
}
