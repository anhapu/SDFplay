package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import controllers.Common;

@Entity
@Table(name="books")
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
    
    @Id
    @Constraints.Required
    @Formats.NonEmpty
    public String isbn;
    
    @Constraints.Required
    @Formats.NonEmpty
    public String coverUrl;
    
    @Constraints.Required
    @Formats.NonEmpty
    public int year;
    
    public boolean swabable;
    
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
        return find.where().eq("swapable", true).findList();
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
    
    public static Book create( Book book)
    {
        book.owner = Common.currentUser();
        book.save();
        return book;
    }
}
