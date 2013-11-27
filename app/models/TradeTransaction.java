package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.enums.States;
import play.db.ebean.Model;

@Entity
@Table(name="tradetransaction")
public class TradeTransaction extends Model{

	@Id
    public Long id;
	
	@OneToOne
	public User ower;
	
	@OneToOne
	public User recipient;

	@Enumerated(EnumType.STRING)  //If you have EnumType.ORDINAL set, you would run into problems when updating your enum.
	public States state;

	@OneToMany(mappedBy = "tradetransaction")
	public List<Book> books = new ArrayList<Book>();
	
	public String commentOwner;
	
	public String commentRecipient;
	
	public Date initTime;
	
	public static Model.Finder<String,TradeTransaction> find = new Model.Finder<String,TradeTransaction>(String.class, TradeTransaction.class);

    /**	Retrieve all trade transactions.
     * 
     * @return a list of all trade transactions
     */
    public static List<TradeTransaction> findAll() {    	
        return find.all();        
    }
	
    /** Returns a trade transaction for a given id.
     * @param id Id of the trade transaction.
     * @return an object, whose type is 'TradeTransaction'
     */
    public static TradeTransaction findById(Long id)
    {
        return find.where().eq( "id", id ).findUnique();
    }

    
    /** Returns a list of trade transactions, where a given user is owner
     * 	of these trade transactions.
     * 
     * @param owner user
     * @return List of trade transactions (can be empty).
     */
    public static List<TradeTransaction> findByOwner(final User owner)
    {
        return find.where().eq("owner", owner).findList();
    }
    
    /** Returns a list of trade transactions, where a given user is recipient
     * 	of these trade transactions.
     * 
     * @param booktitle The title of the book.
     * @return List of books can be empty.
     */
    public static List<TradeTransaction> findByRecipient(final User recipient)
    {
        return find.where().eq("owner", recipient).findList();
    } 
	    
    
    /** Persists the given book in the database.
     * @param book
     * @return Returns the persisted book object.
     */
    public static TradeTransaction create(TradeTransaction tradetransaction)
    {
    	tradetransaction.save();
        return tradetransaction;
    }	    
}
