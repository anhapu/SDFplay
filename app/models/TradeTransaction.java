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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import models.enums.States;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.mvc.With;

/** An entity, which takes care of the list of wishes, which one user shows to another user.
 * 	If both users agree, they will be able to exchange books. If one user does not agree with
 * 	the deal, he or she will be able to send an alternative offer or refuse the deal.
 */
@Entity
@Table(name="tradetransaction", uniqueConstraints=@UniqueConstraint(columnNames={"owner_id", "recipient_id"}))
public class TradeTransaction extends Model{

	@Id
    public Long id;
	
	@ManyToOne(targetEntity = models.User.class)
	@Required
	public User owner;
	
	@ManyToOne(targetEntity = models.User.class)
	@Required
	public User recipient;

	@Enumerated(EnumType.STRING)  //If you have EnumType.ORDINAL set, you would run into problems when updating your enum.
	public States state;
	
	public String commentOwner;
	
	public String commentRecipient;
	
	@CreatedTimestamp
	public Timestamp initTime;
	
	@UpdatedTimestamp
	public Timestamp updatedTime;

	
	@JoinTable(name = "tradetransaction_book", 
	        joinColumns = { @JoinColumn(name = "tradetransaction_id", referencedColumnName = "id")}, 
	        inverseJoinColumns = { @JoinColumn(name = "book_id", referencedColumnName = "id")})
	@ManyToMany(cascade=CascadeType.ALL)
	public List<Book> bookList;
	
	public static Model.Finder<String,TradeTransaction> find = new Model.Finder<String,TradeTransaction>(String.class, TradeTransaction.class);
	
    /**	Retrieve all trade transactions.
     * 
     * @return a list of all trade transactions
     */
    public static List<TradeTransaction> findAll() {    	
        return find.orderBy("updated_time desc").findList();        
    }
        
    /** Returns a trade transaction for a given id.
     * 
     * @param id Id of the trade transaction.
     * @return an object, whose type is 'TradeTransaction'
     */
    public static TradeTransaction findById(Long id) {
        return find.where().eq( "id", id ).findUnique();
    }
    
    /** Returns a TradeTransaction, if a TradeTransaction with this owner and this recipient 
     * 	already exists. Otherwise it returns null.
     * 
     * @param owner		user, who owns this TradeTransaction
     * @param recipient	user, who is recipient in this TradeTransaction
     * @return			Returns true, if a TradeTransaction with this owner and this recipient 
     * 					already exists. Otherwise it returns false.
     */
    public static TradeTransaction exists(final User owner, final User recipient) {
    	return find.where(Expr.and(Expr.eq("owner", owner), Expr.eq("recipient", recipient))).findUnique();
    }
    
    /** Returns a list of trade transactions, where a given user is the owner
     * 	of these trade transactions.
     * 
     * @param owner user
     * @return List of trade transactions (can be empty).
     */
    public static List<TradeTransaction> findByOwner(final User owner) {
        return find.where().eq("owner", owner).orderBy("updated_time desc").findList();
    }
    
    /** Returns a list of trade transactions, where a given user is the recipient
     * 	of these trade transactions.
     * 
     * @param  recipient user
     * @return List of trade transactions (can be empty).
     */
    public static List<TradeTransaction> findByRecipient(final User recipient) {
        return find.where().eq("recipient", recipient).orderBy("updated_time desc").findList();
    }
    
    public static int countForUser(User user) {
    	return find.where().or(Expr.eq("owner", user), Expr.eq("recipient", user)).findRowCount();
    }
    
    public static int countForUserAsOwner(User user) {
    	return find.where().eq("owner", user).findRowCount();
    }
    
    public static int countForUserAsRecipient(User user) {
    	return find.where().eq("recipient", user).findRowCount();
    }
        
    public static List<TradeTransaction> findListOfTradeTransactionInvolvedInTradeTransaction(TradeTransaction tradeTransaction) {
    	return find.where(Expr.and(Expr.ne("id", tradeTransaction.id), Expr.in("bookList", tradeTransaction.bookList))).findList();
//    	List<Book> books = tradeTransaction.bookList;
//    	List<TradeTransaction> trades = new ArrayList<TradeTransaction>();
//    	for(Book book : books) {
//    		List<TradeTransaction> list = find.where(Expr.and(Expr.ne("id", tradeTransaction.id), Expr.eq("bookList", book))).findList();
//    		if ((!list.isEmpty()) && (list != null)) {
//    			trades.addAll(list);
//    		}
//    				
//    	}
//    	return trades;
    }
    
    public static List<TradeTransaction> findListOfTradeTransactionInvolvedInBook(Book book) {
    	return find.where().eq("bookList", book).findList();
    }
    
    public void save() {
    	this.updatedTime = new java.sql.Timestamp(new java.util.Date().getTime());
    	super.save();
    }
}
