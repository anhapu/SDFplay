package controllers;

import java.util.Date;
import java.util.Map;

import models.Book;
import models.TradeBooks;
import models.TradeTransaction;
import models.User;
import models.enums.States;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import static play.data.Form.*;

@With(Common.class)
@Security.Authenticated(Secured.class)
public class TradeController extends Controller {

	
	/**
	 * Inits the trade transaction
	 * @return
	 */
    public static Result init(Long recipientId) {
    	// ToDo Security here
    	
    	User owner = Common.currentUser();
    	User recipient = User.findById(recipientId);
    	
    	// Create the transaction
    	TradeTransaction trade = new TradeTransaction();
    	trade.owner = owner;
    	trade.recipient = recipient;
    	trade.state = States.INIT;
    	trade.commentOwner = "bin owner";
    	trade.commentRecipient = "ich recipient";
    	trade.save();
    	
    	// Getting the selection
    	String[] bookSelection = request().body().asFormUrlEncoded().get("book_selection");
    	if(bookSelection == null) {
    		flash("error", "Bitte wähle min. ein Buch aus.");
    		return redirect(routes.BookController.showBookshelf(recipientId));
    	}
    	
 		Long bookId = null;
 		Book book = null;
 		for (String bookString : bookSelection) {
    		bookId = Long.parseLong(bookString);
    		book = Book.findById(bookId);
    		TradeBooks tradeBook = new TradeBooks();
    		tradeBook.book = book;
    		tradeBook.tradeTransaction = trade;
    		tradeBook.save();
			Logger.info("Added Book " + book.id.toString() + " to Transaction " + trade.id.toString());
		}
    	
 		flash("success", "Wunschzettel angelegt");
    	return redirect(routes.Application.index());
    }
	
}
