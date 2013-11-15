package controllers;

import models.Book;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

@With(Common.class)
public final class BookController extends Controller
{

    private static  Form<Book> bookForm = Form.form(Book.class);
    @Transactional
    public static Result addBook()
    {
        if(Secured.isAllowedToAddBook())
        {
            Form<Book> filledForm = bookForm.bindFromRequest();
            if(filledForm.hasErrors())
            {
                return badRequest();
            }
            Book book = filledForm.get();
            Book.create( book );
            return ok();
        }
        else
        {
            return forbidden();
        }
    }
    
   // public static Result addComment(Long)
    @Transactional
    public static Result editBook(final Long bookId)
    {
        Book book = Book.findById(bookId);
        if(book != null)
        {
            if(Secured.isAllowedToEditBook( book ))
            {
                Form<Book> filledForm = bookForm.bindFromRequest( );
                book = filledForm.get();
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
}
