package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="book_comments")
public class Comment extends Model
{
    
    @Constraints.Required
    @Formats.NonEmpty
    public String conntent;
    
    @Constraints.Required
    @Formats.NonEmpty
    @ManyToOne
    public User author;
    
    @Constraints.Required
    @Formats.NonEmpty
    @ManyToOne
    public Book book;
    
    public static Model.Finder<String,Comment> find = new Model.Finder<String,Comment>(String.class, Comment.class);

    /**
     * Retrieve all comments.
     */
    public static List<Comment> findAll() {
        return find.all();
    }
    

    /**
     * Return all comments to a given isbn of book.
     * @param isbnOfBook the isbn of a book
     * @return List of comments. Can be empty.
     */
    public static List<Comment> findByBook(final String isbnOfBook){
        return find.where().eq( "book.isbn", isbnOfBook ).findList();
    }
}
