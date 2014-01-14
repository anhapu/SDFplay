package models;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import models.enums.Roles;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Email;
import play.db.ebean.Model;
import utils.Utils;
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
    
    public boolean alreadyTradeABook;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String password;

	@OneToMany(targetEntity = models.Book.class, cascade=CascadeType.ALL, mappedBy="owner")
	public List<Book> books;

	@OneToMany(targetEntity = models.TradeTransaction.class, cascade=CascadeType.ALL, mappedBy="owner")
	public List<TradeTransaction> tradeTransactionOwnerList;
	
	@OneToMany(targetEntity = models.TradeTransaction.class, cascade=CascadeType.ALL, mappedBy="recipient")
	public List<TradeTransaction> tradeTransactionRecipientList;
    
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
    
    public static List<User> findAllBut(User user) {
        return find.where().ne("id", user.id).findList();
    }
    
    public static List<User> findPaginated(int limit, int page, User user) {
    	int offset = (page * limit) - limit;
    	return find.where().ne("id", user.id).setMaxRows(limit).setFirstRow(offset).findList();
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
     * Retrieve a User from token.
     */
    public static User findByToken(String token) {
        return find.where().eq("token", token).findUnique();
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
            .eq("password", Utils.md5(password))
            .findUnique();
    }
    
    /**
     * Check if the user is active or not.
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

    public static boolean isAdmin(Long id) {
         User user = findById(id);
         boolean isAdmin = false;
         if(user.role == Roles.ADMIN) {
              isAdmin = true;
         }
         return isAdmin;
    }

}
