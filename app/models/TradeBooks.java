package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.avaje.ebean.Expr;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

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
	
	public static Model.Finder<String,TradeBooks> find = new Model.Finder<String,TradeBooks>(String.class, TradeBooks.class);
	
	/** Will return a list of TradeBooks, which a user owns.
	 * 
	 * @param user	user, who owns these books.
	 * @return		a list of TradeBooks, which a user owns.
	 */
    public static List<TradeBooks> findByUser(final User user) {
    	String sqlString = "SELECT tradebooks.id, tradebooks.trade_transaction_id, tradebooks.book_id FROM tradebooks INNER JOIN book ON tradebooks.book_id = book.id WHERE book.owner_id = " + user.id;
    	RawSql rawSql = RawSqlBuilder.parse(sqlString).columnMapping("tradebooks.id", "id").columnMapping("tradebooks.trade_transaction_id", "tradeTransaction.id").columnMapping("tradebooks.book_id", "book.id").create();
    	return find.query().setRawSql(rawSql).findList();
    	//return find.fetch("book", new FetchConfig().query()).where(Expr.eq("owner_id", user.id)).findList();
    }
}