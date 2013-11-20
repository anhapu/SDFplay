package controllers;

import java.util.List;

import models.Book;
import models.User;
import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.book.bookshelf;

@With( Common.class )
@Security.Authenticated( Secured.class )
public final class BookController extends Controller
{

    private static Form< Book > bookForm = Form.form( Book.class );

    @Transactional
    public static Result index()
    {
        List< Book > books = Book.findAll();

        // TODO Need a view for that stuff...
        return null;
    }

    /**
     * Persists a book in the database.
     *
     * @return
     */
    @Transactional
    public static Result addBook()
    {
        if ( Secured.isAllowedToAddBook() )
        {
            Form< Book > filledForm = bookForm.bindFromRequest();
            if ( filledForm.hasErrors() )
            {
                return badRequest();
            }
            Book book = filledForm.get();

            book.owner = Common.currentUser();
            book.exchangeable = false;
            book.comment = "";
            Book.create( book );
            return ok();
        }
        else
        {
            return forbidden();
        }
    }

    // public static Result addComment(Long)
    /**
     * Edit a book.
     *
     * @param bookId
     *            the id of a book which should be edit.
     * @return
     */
    @Transactional
    public static Result editBook( final Long bookId )
    {
        Book book = Book.findById( bookId );
        if ( book != null )
        {
            if ( Secured.isOwnerOfBook( book ) )
            {
                Form< Book > filledForm = bookForm.bindFromRequest();
                book = filledForm.get();
                book.owner = Common.currentUser();
                book.update();
                return ok();

            }
            else
            {

                return forbidden();
            }
        }
        else
        {
            return badRequest();
        }
    }

    /**
     * Delete a book.
     */
    public static Result deleteBook( final Long bookId )
    {
        Book book = Book.findById( bookId );
        if ( Secured.isOwnerOfBook( book ) )
        {
            book.delete();
            return ok();
        }
        else
        {
            return forbidden();
        }
    }

    
    public static Result showBookshelf( Long id )
    {
        User searchedUser = User.findById( id );
        if ( searchedUser != null )
        {
            List< Book > books = Book.findByUser( searchedUser );
            Logger.info( "Found " + books.size() + " books for user " + searchedUser.username );
            return ok( bookshelf.render( Book.findByUser( searchedUser ) ) );
        }
        else
        {
            // ToDo redirect to something useful
            Logger.error( "Did not find any user for id: " + id );
            return redirect( routes.Application.index() );
        }
    }
    
    /**
     * Returns the showcase of a specific user
     * @param id UserId
     * @return
     */
    public static Result getShowcase(Long id)
    
    
}
