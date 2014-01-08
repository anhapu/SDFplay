package models;

import java.sql.Timestamp;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Constraint;

import com.avaje.ebean.annotation.CreatedTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import controllers.Common;

@Entity
@Table(name="book")
public class Book extends Model
{
    @Id
    public Long id;

    @Constraints.Required
    @Formats.NonEmpty
    public String author;
    
    @Constraints.Required
    @Formats.NonEmpty
    @JsonProperty("title")
    public String title;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String isbn;
    
    @Constraints.Required
    @Formats.NonEmpty
    @JsonProperty("thumbnail")
    public String coverUrl;
    
    @Constraints.Required
    @Formats.NonEmpty
    public long year;
    
    public boolean tradeable;
    
    public String comment;
    
    @ManyToOne
    public User owner;
    
    @CreatedTimestamp
    public Timestamp initTime;

    // If you delete a book, the entries of this book in a TradeTransaction will be removed as well.
    @ManyToMany(mappedBy = "bookList", cascade=CascadeType.REMOVE)
    public List<TradeTransaction> tradeTransactionList;
    
    
    public static Model.Finder<String,Book> find = new Model.Finder<String,Book>(String.class, Book.class);
    
    /**
     * Retrieve all books.
     */
    public static List<Book> findAll() {
        return find.all();
    }
    
    /**
     * Returns a list of books by a given title. It's a like search
     * @param booktitle The title of the book.
     * @return List of books can be empty.
     */
    public static List<Book> findByTitle(final String booktitle)
    {
        return find.where().ilike( "title", "%" + booktitle + "%" ).orderBy("title asc").findList();
    }
    
    /**
     * Returns a list of books by a given author name. It's a like search.
     * @param nameOfAuthor The name of the author.
     * @return List of books can be empty.
     */
    public static List<Book> findByAuthor(final String nameOfAuthor) {
        return find.where().like( "author", nameOfAuthor ).orderBy( "title asc" ).findList();
    }
    
    /**
     * Returns a list of all swapable books.
     * @return
     */
    public static List<Book> findAllTradeableBooks() {
        return find.where().eq( "tradeable", true ).orderBy( "title asc" ).findList();
    }
    
    public static List<Book> findAllTradeableBooksByTitle(final String title) {
        return find.where().eq( "tradeable", true ).ilike( "title", "%" + title + "%" ).orderBy( "title asc" ).findList();
    }

    /**
     * Find all tradeable books for a searchterm of a book attr, result is sorted by
     * searchDirection.
     * @param searchString
     * @param searchAttribute
     * @param searchDirection
     */
    public static List<Book> findAllTradeableBooksBy(String searchString, String searchAttribute, String searchDirection) {
        return find.where().eq( "tradeable", true ).ilike( "title", "%" + searchString + "%" ).orderBy( searchAttribute + " " + searchDirection ).findList();
    }
    
    /**
     * Returns all books of a given User.
     * @param user
     * @return List of books.
     */
    public static List<Book> findByUser(final User user) {
        return find.where().eq( "owner.id", user.id ).orderBy( "title asc" ).findList();
    }
    
    /**
     * Returns the showcase for a specific user. In the showcase are only the tradeable books of a user.
     * @param user
     * @return List of books
     */
    public static List<Book> getShowcaseForUser(final User user) {
        return find.where().eq( "tradeable", true ).eq( "owner.id", user.id ).orderBy( "title asc" )
                .findList();
    }
    /**
     * Returns a book by a given id.
     * @param id Id of the book.
     * @return Book object.
     */
    public static Book findById(Long id)
    {
        return find.where().eq( "id", id ).findUnique();
    }
    
    /**
     * Persists the given book in the database.
     * @param book
     * @return Returns the persisted book object.
     */
    public static Book create( Book book)
    {
        book.save();
        return book;
    }
    
    
    /**
     * Marks a book as tradeable. So it is visible in the showcase.
     * @param book
     * @return Returns the updated book.
     */
    public static Book markAsTradeable(Book book)
    {
        book.tradeable = true;
        book.update();
        return book;
    }

    /**
     * Unmark a book as tradeable. So it is not visible in the showcase.
     * @param book
     */
    public static Book unmarkAsTradeable( Book book )
    {
        book.tradeable = false;
        book.update();
        return book;
    }

    
    public static List<Book> findByTransactionAndOwner(TradeTransaction trans, User owner) {
        return find.where().eq("tradeTransactionList", trans).eq("owner", owner).findList();
    }
    
    public static List<Book> findByUserAndTitle(final User owner, final String title) {
    	return find.where().eq("owner", owner).ilike( "title", "%" + title + "%" ).orderBy("title asc").findList();
    }

    /**
     * Find all books of a user for a searchterm of a book attr, result is sorted by
     * searchDirection.
     * @param user
     * @param searchString
     * @param searchAttribute
     * @param searchDirection
     */
    public static List<Book> findAllBooksFromBy(final User owner, String searchString, String searchAttribute, String searchDirection) {
        return find.where().eq("owner", owner).ilike( "title", "%" + searchString + "%" ).orderBy( searchAttribute + " " + searchDirection ).findList();
    }
}
