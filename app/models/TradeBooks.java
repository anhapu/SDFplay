package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

/** An entity, which keeps track of books, which are associated  with a  certain list of wishes. (see TradeTransaction)
 *  This list of wishes contains books, which one user wants to trade to another user.
 */
@Entity
@Table(name="tradebooks")
public class TradeBooks extends Model{

	@Id
    public Long id;

	@ManyToOne
	public TradeTransaction tradeTransaction;
	
	@OneToOne
	public Book book;
}