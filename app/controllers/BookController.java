package controllers;

import static play.data.Form.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Book;
import models.TradeTransaction;
import models.User;
import models.enums.States;
import play.Logger;
import play.api.templates.Html;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import utils.Utils;
import views.html.book.detailview;
import views.html.book.mybookshelf;
import views.html.book.modalview;

@With(Common.class)
public final class BookController extends Controller {

    private static Form<Book> bookForm = Form.form(Book.class);

    @Transactional
    public static Result index() {
        //List<Book> books = Book.findAll();

        // TODO Need a view for that stuff...
        return null;
    }
    
    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result getForm() {
        String navigation = "addBook";
        return ok(views.html.book.createBook.render(null, bookForm, navigation));
    }
    
    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result createBook() {
        String navigation = "addBook";
        
        Form<Book> filledForm = bookForm.bindFromRequest();
        
        filledForm.errors().remove("owner");
        filledForm.errors().remove("tradeable");
        
        if(filledForm.hasErrors()) {
            return badRequest(views.html.book.createBook.render(filledForm.data().get("coverUrl"), filledForm, navigation));
        } else {
            
            final SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
            final SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy");
            
            Book book = new Book();
            book.owner = Common.currentUser();
            book.tradeable = false;

            // Fill an and update the model manually
            // because its just a partial form
            book.title = filledForm.field("title").value();
            book.author = filledForm.field("author").value();
            book.isbn = Utils.isbnParser(filledForm.field("isbn").value());
            try{
                book.year = formatter.parse(filledForm.field("year").value());
            }catch (final ParseException e){
                Logger.error(e.getMessage());
                try {
                    book.year = formatter2.parse(filledForm.field("year").value());
                } catch ( final ParseException e2) {
                    Logger.error(e2.getMessage());
                    book.year = new Date();
                }
            }
            
            book.coverUrl = filledForm.field("coverUrl").value();
            book.comment = filledForm.field("comment").value();
            
            book.save();
            flash("success", "Dein Buch wurde deinem Bücherregal hinzugefügt!");
            return redirect(routes.BookController.getBook(book.id));


        }
    }
    
    @Transactional
    @Security.Authenticated(Secured.class)
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
                oriBook.isbn = Utils.isbnParser(book.isbn);
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
    @Security.Authenticated(Secured.class)
    public static Result addBook() {
        String navigation = "addBook";
        if (Secured.isAllowedToAddBook()) {
            return ok(views.html.book.addBook.render(form(SimpleProfile.class), navigation));
        } else {
            return forbidden();
        }
    }


    @Security.Authenticated(Secured.class)
    public static Result createBookByIsbn() {
        String navigation = "addBook";
        Form<SimpleProfile> pForm = form(SimpleProfile.class).bindFromRequest();
        if (pForm.hasErrors()) {
            Logger.error("Error in form");
            // TODO redirect to something useful
            flash( "error", "Die ISBN darf nur Zahlen enthalten" );
            return ok(views.html.book.addBook.render( form(SimpleProfile.class), navigation ));
        } else {
            Book book = Utils.getBookInformationFromAWS(Utils.isbnParser(pForm.get().isbn));
            if (book == null ) {
                flash("error", "Das Buch konnte nicht korrekt ermittelt werden, bitte füge es per Hand hinzu!");
               return ok(views.html.book.addBook.render(form(SimpleProfile.class), navigation));
            } else if( book.title == null) {
            	flash("error", "Das Buch konnte nicht korrekt ermittelt werden, bitte füge es per Hand hinzu!");
            	return ok(views.html.book.addBook.render(form(SimpleProfile.class), navigation));
            }else {
                flash("info", "Dein Buch wurde gefunden! Bitte überprüfe die Angaben und ergänze sie gegebenenfalls.");
            }
            return ok(views.html.book.createBook.render(book.coverUrl, bookForm.fill( book ), navigation));
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
    @Security.Authenticated(Secured.class)
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
    
    /** Delete a book. This action will also lead to a search for INVALID TradeTransactions. If this
     * 	book is used in a TradeTransaction, then this TradeTransaction will become INVALID. For each
     * 	invalid TradeTransaction one E-Mail is sent to the owner and another one is sent to the recipient.
     *
     * 	@param bookId	the id of the book that you want to delete
     */
    @Security.Authenticated(Secured.class)
    public static Result deleteBook(final Long bookId) {
        Book book = Book.findById(bookId);
        if (Secured.isOwnerOfBook(book)) {
            checkForInvalidTradeTransactions(book);
            book.delete();
            flash("info", "Buch erfolgreich gelöscht");
            return redirect(routes.BookController.myBookshelf());
        } else {
            return forbidden();
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result myBookshelf() {
         String navigation = "myBooks";
        User searchedUser = Common.currentUser();
        if (searchedUser != null) {
            List<Book> books = Book.findByUser(searchedUser);
            Logger.info("Found " + books.size() + " books for user "
                    + searchedUser.username);
            return ok(mybookshelf.render(Book.findByUser(searchedUser), "title", "asc", navigation));
        } else {
            // TODO redirect to something useful
            Logger.error("Current user is null.");
            return redirect(routes.Application.index(1));
        }
    }
    
    /**
     * Returns the showcase of a specific user
     *
     * @param id
     *            UserId
     * @return
     */
    @Security.Authenticated(Secured.class)
    public static Result getShowcase(final Long id) {
        final User searchedUser = User.findById(id);
        if (searchedUser != null) {
            List<Book> showcase = Book.getShowcaseForUser(searchedUser, 0);
            Logger.info("Found " + showcase.size()
                    + " books in showcase for user " + searchedUser.username);
            // TODO Redirect to something useful
            return ok(views.html.book.showcase.render(showcase));
        } else {
            // TODO Return to something useful
            Logger.error("Did not find any user for id: " + id);
            return redirect(routes.Application.index(1));
        }
    }

    /**
     * Marks a book tradeable. If it was successful there will be a redirect to
     * the bookshelf of the current user
     *
     * @param id
     *            ID of the book which should be marked as tradeable.
     */
    @Security.Authenticated(Secured.class)
    public static Result markAsTradeable(final Long id) {
        Book book = Book.findById(id);
        if (Secured.isOwnerOfBook(book)) {
            Book.markAsTradeable(book);
            Common.currentUser().setLastActivityToNow();
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
    @Security.Authenticated(Secured.class)
    public static Result unmarkAsTradeable(final Long bookId) {
        Book book = Book.findById(bookId);
        if (Secured.isOwnerOfBook(book)) {
            checkForInvalidTradeTransactions(book);
            Book.unmarkAsTradeable(book);
            return redirect(routes.BookController.myBookshelf());

        } else {

            Logger.error("User is not allowed to unmark book as tradeable");
            return forbidden();
        }
    }

    @Security.Authenticated(Secured.class)
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
    
    
    public static Result view(Long bookId) {
        Book book = Book.findById(bookId);
        return ok(modalview.render(book));
    }
    
    /*
     * @param field
     * @param term
     */
    public static Result searchBook() {
         String navigation = "searchBooks";
        List<Book> books = null;
        String sortAttribute = "title";
        String sortDirection = "asc";
        final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        for(Map.Entry< String, String[] > entry : entries){
             if(entry.getKey().equals("sorting")) {
                final String sortTerm = entry.getValue()[0];
                String[] data = sortTerm.split(":");
                sortAttribute = data[0];
                sortDirection = data[1];
             }
        }
        for(Map.Entry< String, String[] > entry : entries){
            if(entry.getKey().equals( "keyword" )){
                final String term = entry.getValue()[0];
                Logger.info("[BOOK-SEARCH] Looking up Database for term '" + term + "'");
                books = Book.findAllTradeableBooksBy(term, sortAttribute, sortDirection);
                Logger.info( "[BOOK-SEARCH] found " + books.size() );
                return ok(views.html.book.searchResults.render(books, term, sortAttribute, sortDirection, "all", navigation));
            }
        }

        return redirect(routes.Application.index(1));
    }

    public static class SimpleProfile {
        public String isbn;

        public SimpleProfile() {
        }

        public SimpleProfile(String isbn) {
            this.isbn = isbn;

        }
    }
    
    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result searchInMyBooks(){
         String navigation = "myBooks";
         List<Book> books = null;
         String sortAttribute = "title";
         String sortDirection = "asc";
         final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
         for(Map.Entry< String, String[] > entry : entries){
              if(entry.getKey().equals("sorting")) {
                   final String sortTerm = entry.getValue()[0];
                   String[] data = sortTerm.split(":");
                   sortAttribute = data[0];
                   sortDirection = data[1];
              }
         }
         for(Map.Entry< String, String[] > entry : entries){
              if(entry.getKey().equals( "keyword" )){
                   final String term = entry.getValue()[0];
                   Logger.info("[BOOK-SEARCH-IN-OWN-BOOKS] Looking up Database for term '" + term);
                   books = Book.findAllBooksFromBy(Common.currentUser(), term, sortAttribute, sortDirection);
                   Logger.info( "[BOOK-SEARCH-IN-OWN-BOOKS] found " + books.size() );
                   return ok(views.html.book.searchResults.render(books, term, sortAttribute, sortDirection, "", navigation));
              }
         }
         return redirect(routes.Application.index(1));
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result sortMyBooks(){
         String navigation = "myBooks";
         List<Book> books = null;
         String sortAttribute = "title";
         String sortDirection = "asc";
         final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
         for(Map.Entry< String, String[] > entry : entries){
              if(entry.getKey().equals("sorting")) {
                   final String sortTerm = entry.getValue()[0];
                   String[] data = sortTerm.split(":");
                   sortAttribute = data[0];
                   sortDirection = data[1];
              }
         }
         // "" to get all books
         books = Book.findAllBooksFromBy(Common.currentUser(), "", sortAttribute, sortDirection);
         return ok(views.html.book.mybookshelf.render(books, sortAttribute, sortDirection, navigation));
     }

    /** This method should be used, if a user deleted a book or removed it from his or her "showcase".
     * 	It will find all TradeTransaction involved and set them to "States.INVALID". It will also sent
     *  notifications to all users involved
     *
     * @param book A book that was deleted or set to to be "untradeable".
     */
    private static void checkForInvalidTradeTransactions(Book book) {
        List<TradeTransaction> invalidTradeTransactions = TradeTransaction.findListOfTradeTransactionInvolvedInBook(book);
        if (invalidTradeTransactions != null) {
            List<Email> emailList = new ArrayList<Email>();
            for (TradeTransaction invalidTradeTransaction : invalidTradeTransactions) {
                // set State to INVALID only, if trade is not finished yet (INIT or RESPONSE)
                if ((invalidTradeTransaction.state == States.INIT) || (invalidTradeTransaction.state == States.RESPONSE)) {
                    invalidTradeTransaction.state = States.INVALID;
                    invalidTradeTransaction.save();
                    emailList.addAll(EmailSender.getBookExchangeInvalid(invalidTradeTransaction.owner, invalidTradeTransaction.recipient));
                }
            }
            if (!emailList.isEmpty()) {
                EmailSender.send(emailList);
            }
        }
    }

}
