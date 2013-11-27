package models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name="tradebooks")
public class TradeBooks extends Model{

	@Id
    public Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transId")
	public TradeTransaction tradeTransaction;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Id")
	public Book book;
}
