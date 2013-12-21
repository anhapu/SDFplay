package controllers;

import static play.data.Form.form;

import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Book;
import models.TradeTransaction;
import models.User;
import models.enums.States;
import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import utils.Utils;
import views.html.book.detailview;
import views.html.book.mybookshelf;

@With(Common.class)
@Security.Authenticated(Secured.class)
public final class BookController extends Controller {

    private static Form<Book> bookForm = Form.form(Book.class);

    @Transactional
    public static Result index() {
        List<Book> books = Book.findAll();

        // TODO Need a view for that stuff...
        return null;
    }
    
    @Transactional
    public static Result getForm() {
        
        return ok(views.html.book.createBook.render(bookForm));
    }
    
    @Transactional
    public static Result createBook() {
        
        Form<Book> filledForm = bookForm.bindFromRequest();
        
        filledForm.errors().remove("owner");
        filledForm.errors().remove("tradeable");
        
        if(filledForm.hasErrors()) {
            return badRequest(views.html.book.createBook.render(filledForm));
        } else {
            
            Book book = new Book();
            book.owner = Common.currentUser();
            book.tradeable = false;

            // Fill an and update the model manually
            // because the its just a partial form
            book.title = filledForm.field("title").value();
            book.author = filledForm.field("author").value();
            book.isbn = filledForm.field("isbn").value();
            book.year = Long.parseLong(filledForm.field("year").value());
            book.coverUrl = filledForm.field("coverUrl").value();
            book.comment = filledForm.field("comment").value();
            
            book.save();
            return redirect(routes.BookController.getBook(book.id));


        }
    }
    
    @Transactional
    public static Result updateBook(Long bookId) {
        Form<Book> filledForm = bookForm.bindFromRequest();
        Book oriBook = Book.findById(bookId);
        
        if(filledForm.hasErrors()){
            return badRequest(views.html.book.editBook.render(filledForm, oriBook));
        } else {
            
            if(Secured.isOwnerOfBook( oriBook )){
                Book book = filledForm.get();
                oriBook.author = book.author;
                oriBook.title = book.title;
                oriBook.comment = book.comment;
                oriBook.coverUrl = book.coverUrl;
                oriBook.isbn = book.isbn;
                oriBook.year = book.year;
                oriBook.update();
                return redirect( routes.BookController.getBook( oriBook.id ) );
            } else {
                return forbidden();
            }
        }
    }
    

    /**
     * Persists a book in the database.
     *
     * @return
     */
    @Transactional
    public static Result addBook() {
        if (Secured.isAllowedToAddBook()) {
            return ok(views.html.book.addBook.render(form(SimpleProfile.class)));
        } else {
            return forbidden();
        }
    }


    public static Result createBookByIsbn() {
        Form<SimpleProfile> pForm = form(SimpleProfile.class).bindFromRequest();
        if (pForm.hasErrors()) {
            Logger.error("Error in form");
            // TODO redirect to something useful
            flash( "error", "Die ISBN darf nur Zahlen enthalten" );
            return ok(views.html.book.addBook.render( form(SimpleProfile.class) ));
        } else {
                Book book = Utils.getBookInformationFromAWS(pForm.get().isbn);
                return ok(views.html.book.createBook.render(bookForm.fill( book )));
        }
    }
 

    
    /**
     * Edit a book.
     *
     * @param bookId
     *            the id of a book which should be edit.
     * @return
     */
    @Transactional
    public static Result editBook(final Long bookId) {
        Book book = Book.findById(bookId);
        if (book != null) {
            if (Secured.isOwnerOfBook(book)) {
                return ok(views.html.book.editBook.render( bookForm.fill( book ), book ));
            } else {
                return forbidden();
            }
        } else {
            return badRequest();
        }
    }
    
    /**
     * Delete a book.
     */
    public static Result deleteBook(final Long bookId) {
        Book book = Book.findById(bookId);
        if (Secured.isOwnerOfBook(book)) {
        	//find TradeTransaction involved and set them INVALID
        	List<TradeTransaction> invalidTradeTransactions = TradeTransaction.findListOfTradeTransactionInvolvedInBook(book);
        	if (invalidTradeTransactions != null) {
    			for (TradeTransaction trade : invalidTradeTransactions) {
					trade.state = States.INVALID;
					trade.save();
    			}
        	}
            book.delete();
            return redirect(routes.BookController.myBookshelf());
        } else {
            return forbidden();
        }
    }

    public static Result myBookshelf() {
        User searchedUser = Common.currentUser();
        if (searchedUser != null) {
            List<Book> books = Book.findByUser(searchedUser);
            Logger.info("Found " + books.size() + " books for user "
                    + searchedUser.username);
            return ok(mybookshelf.render(Book.findByUser(searchedUser)));
        } else {
            // TODO redirect to something useful
            Logger.error("Current user is null.");
            return redirect(routes.Application.index());
        }
    }
    
    /**
     * Returns the showcase of a specific user
     *
     * @param id
     *            UserId
     * @return
     */
    public static Result getShowcase(final Long id) {
        final User searchedUser = User.findById(id);
        if (searchedUser != null) {
            List<Book> showcase = Book.getShowcaseForUser(searchedUser);
            Logger.info("Found " + showcase.size()
                    + " books in showcase for user " + searchedUser.username);
            // TODO Redirect to something useful
            return ok(views.html.book.showcase.render(showcase));
        } else {
            // TODO Return to something useful
            Logger.error("Did not find any user for id: " + id);
            return redirect(routes.Application.index());
        }
    }

    /**
     * Marks a book tradeable. If it was successful there will be a redirect to
     * the bookshelf of the current user
     *
     * @param id
     *            ID of the book which should be marked as tradeable.
     */
    public static Result markAsTradeable(final Long id) {
        Book book = Book.findById(id);
        if (Secured.isOwnerOfBook(book)) {
            Book.markAsTradeable(book);
            Logger.info("Marked book as tradeable");
            return redirect(routes.BookController.myBookshelf());
        } else {
            Logger.error("User is not allowed to mark book as tradeable");
            return forbidden();
        }
    }

    /**
     * Removes a book from the showcase. If it was successful there will be a
     * redirect to the bookshelf of the user.
     *
     * @param bookId
     * @return
     */
    public static Result unmarkAsTradeable(final Long bookId) {
        Book book = Book.findById(bookId);
        if (Secured.isOwnerOfBook(book)) {
            Book.unmarkAsTradeable(book);
            return redirect(routes.BookController.myBookshelf());

        } else {

            Logger.error("User is not allowed to unmark book as tradeable");
            return forbidden();
        }
    }

    public static Result getBook(final Long bookId) {
        Book book = Book.findById(bookId);
        if (book != null) {
            Logger.info("Got book with id " + book.id);
            return ok(detailview.render(book));
        } else {
            Logger.error("No results for request!");
            return badRequest();
        }
    }
    
    /*
     * @param field
     * @param term
     */
    public static Result searchBook() {
        List<Book> books = null;
        final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        for(Map.Entry< String, String[] > entry : entries){
            if(entry.getKey().equals( "keyword" )){
                final String term = entry.getValue()[0];
                Logger.info("[BOOK-SEARCH] Looking up Database for term '" + term);
                books = Book.findAllTradeableBooksByTitle(term);
                Logger.info( "[BOOK-SEARCH] found " + books.size() );
                return ok(views.html.book.searchResults.render(books));
            }
        }

        return redirect(routes.Application.index());
    }

    public static class SimpleProfile {
        public long isbn;

        public SimpleProfile() {
        }

        public SimpleProfile(long isbn) {
            this.isbn = isbn;

        }
    }
}
