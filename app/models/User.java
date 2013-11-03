package models;


import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
@Table(name="account")
public class User extends Model
{

    @Id
    @Constraints.Required
    @Formats.NonEmpty
    public String email;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String username;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String postcode;
    
    
    
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
     * Authenticate a User via email and password.
     * Returns a user object
     */
    public static User authenticate(String email, String password) {
        return find.where()
            .eq("email", email)
            .eq("password", password)
            .findUnique();
    }
    
    
    /**
     * Overrides the to string methode.
     */
    @Override
    public String toString() {
        return "User(" + email + ")";
    }

    
    
    
    
}
