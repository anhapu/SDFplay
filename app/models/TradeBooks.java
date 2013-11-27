package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name="tradebooks")
public class TradeBooks extends Model{

	@Id
    public Long id;

	@ManyToOne
	public TradeTransaction tradeTransaction;
	
	@ManyToOne
	public Book book;
}
