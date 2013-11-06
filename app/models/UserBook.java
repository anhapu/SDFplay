package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.ebean.Model;


@Entity
@Table(name="USER_BOOKS")
public class UserBook extends Model
{
    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;
    
    @ManyToOne
    @JoinColumn(name = "book_isbn")
    public Book book;
    
    @Column(name = "exchangeable")
    public boolean exchangeable;
}
