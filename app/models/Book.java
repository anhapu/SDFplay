package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.Constraint;

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
    public String title;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String isbn;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String coverUrl;
    
    @Constraints.Required
    @Formats.NonEmpty
    public int year;
    
    public boolean exchangeable;
    
    public String comment;
    
    @ManyToOne
    public User owner;
    
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
        return find.where().like( "title", booktitle ).findList();
    }
    
    /**
     * Returns a list of books by a given author name. It's a like search.
     * @param nameOfAuthor The name of the author.
     * @return List of books can be empty.
     */
    public static List<Book> findByAuthor(final String nameOfAuthor)
    {
        return find.where().like( "author", nameOfAuthor ).findList();
    }
    
    /**
     * Returns a list of all swapable books.
     * @return
     */
    public static List<Book> findAllSwapableBooks()
    {
        return find.where().eq("exchangeable", true).findList();
    }
    
    /**
     * Returns all books of a given User.
     * @param user
     * @return List of books.
     */
    public static List<Book> findByUser(final User user)
    {
        return find.where().eq( "owner.id", user.id ).findList();
    }
    
    /**
     * Returns a list of swapable books by a given user.
     * @param user
     * @return List of books
     */
    public static List<Book> findSwapableBooksByUser(final User user)
    {
        return find.where().eq( "swapable", true ).eq( "owner.id", user.id ).findList();
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
     * Creates a book in the database. Default of exchangeable is false.
     * Sets the current user as owner.
     * @param book
     * @return Returns the saved book object.
     */
    public static Book create( Book book)
    {
        book.exchangeable = false;
        book.owner = Common.currentUser();
        book.save();
        return book;
    }
    
    /**
     * Creates a book in the database with the given user as owner of the book. Default of exchangeable is false.
     * @param book
     * @param owner
     * @return Returns the saved book object.
     */
    public static Book create(Book book, User owner)
    {
        book.exchangeable = false;
        book.owner = owner;
        book.save();
        return book;
    }
    
    /**
     * Marks a book as swapable.
     * @param book
     * @return Returns the updated book.
     */
    public static Book markAsSwapable(Book book)
    {
        book.exchangeable = true;
        book.owner = Common.currentUser();
        book.update();
        return book;
    }
}
