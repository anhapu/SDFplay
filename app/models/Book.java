package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import views.html.userProfile;

@Entity
@Table(name="books")
public class Book extends Model
{
    
    /**
     * Default
     */
    private static final long serialVersionUID = 1L;

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
    
    public String comment;
    
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
    
    public static List<Book> findByUser(final User user)
    {
        return find.where().eq( "user.id", user.id );
    }
}
