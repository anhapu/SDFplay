package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.annotation.CreatedTimestamp;

import models.enums.States;
import models.TradeBooks;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/** An entity, which takes care of the list of wishes, which one user shows to another one.
 * 	If both users agree, they will be able to exchange books. If one user does not agree with
 * 	the deal, he or she will be able to send an alternative offer or refue the deal.
 */
@Entity
@Table(name="tradetransaction", uniqueConstraints=@UniqueConstraint(columnNames={"owner_id", "recipient_id"}))
public class TradeTransaction extends Model{

	@Id
    public Long id;
	
	@OneToOne(targetEntity = models.User.class)
	@Required
	public User owner;
	
	@OneToOne(targetEntity = models.User.class)
	@Required
	public User recipient;

	@Enumerated(EnumType.STRING)  //If you have EnumType.ORDINAL set, you would run into problems when updating your enum.
	public States state;

	@OneToMany(targetEntity = models.TradeBooks.class, cascade=CascadeType.ALL, mappedBy="tradeTransaction")
	public List<TradeBooks> tradeBooks;
	
	public String commentOwner;
	
	public String commentRecipient;
	
	@CreatedTimestamp
	public Timestamp initTime;
	
	public static Model.Finder<String,TradeTransaction> find = new Model.Finder<String,TradeTransaction>(String.class, TradeTransaction.class);
	
    /**	Retrieve all trade transactions.
     * 
     * @return a list of all trade transactions
     */
    public static List<TradeTransaction> findAll() {    	
        return find.all();        
    }
        
    /** Returns a trade transaction for a given id.
     * 
     * @param id Id of the trade transaction.
     * @return an object, whose type is 'TradeTransaction'
     */
    public static TradeTransaction findById(Long id) {
        return find.where().eq( "id", id ).findUnique();
    }
    
    /** Returns true, if a TradeTransaction with this owner and this recipient 
     * 	already exists. Otherwise it returns false.
     * 
     * @param owner		user, who owns this TradeTransaction
     * @param recipient	user, who is recipient in this TradeTransaction
     * @return			Returns true, if a TradeTransaction with this owner and this recipient 
     * 					already exists. Otherwise it returns false.
     */
    public static Boolean exists(final User owner, final User recipient) {
    	Boolean exists = (find.where(Expr.and(Expr.eq("owner", owner), Expr.eq("recipient", recipient))).findRowCount() >= 1) ? true : false;
    	return exists;
    }
    
    /** Returns a list of trade transactions, where a given user is the owner
     * 	of these trade transactions.
     * 
     * @param owner user
     * @return List of trade transactions (can be empty).
     */
    public static List<TradeTransaction> findByOwner(final User owner) {
        return find.where().eq("owner", owner).findList();
    }
    
    /** Returns a list of trade transactions, where a given user is the recipient
     * 	of these trade transactions.
     * 
     * @param  recipient user
     * @return List of trade transactions (can be empty).
     */
    public static List<TradeTransaction> findByRecipient(final User recipient) {
        return find.where().eq("recipient", recipient).findList();
    } 	    
}
