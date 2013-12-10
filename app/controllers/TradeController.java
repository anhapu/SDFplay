package controllers;

import java.util.ArrayList;
import java.util.List;

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
import views.html.trade.init;
import views.html.trade.create;
import views.html.trade.showAll;

@With(Common.class)
@Security.Authenticated(Secured.class)
public class TradeController extends Controller {

	/** Displays an overview of all book exchange offers, 
	 * 	in which the current user is involved in.
	 * 
	 * @return	html page with all TradeTransactions for the current user
	 */
	public static Result viewAllTrades() {
		// ---- ToDo ---- 
		// distinguish between books which are from owner and books which are from recipient 
		// e.g. in state RESPONSE both user have offered books and they are mixed at the moment
		User currentUser = Common.currentUser();
		if (currentUser != null) {
			List<TradeTransaction> tradeListOwner = TradeTransaction.findByOwner(currentUser);
			List<TradeTransaction> tradeListRecipient = TradeTransaction.findByRecipient(currentUser);			
			return ok(showAll.render(currentUser, tradeListOwner, tradeListRecipient));
    	} else {
    		return redirect(routes.Application.error());
    	}
    }
	
	/**
	 * This is a entry point for starting or viewing a transaction
	 * between the current user and another user.
	 */
	public static Result viewForUser(Long id) {
		User currentUser = Common.currentUser();
		User recipient = User.findById(id);
		Logger.info("recipient = " + recipient.username);
		Logger.info("user = " + currentUser.username);
		
		if(recipient == null) {
			return redirect(routes.Application.error());
		}
		
		TradeTransaction trade = TradeTransaction.exists(currentUser, recipient);
		if(trade == null) {
			// TODO Not the showcase books yet
			List<Book> books = Book.findByUser(recipient);
			Logger.info("Found " + books.size() + " books for user " + recipient.username);
			return ok(create.render(books,recipient));
		} else {
			// There is already a Transaction, so forward it to the state machine
			return redirect(routes.TradeController.view(trade.id));
		}
	}
	
	/**
	 * TRADE STATE MACHINE
	 * 
	 * This is the main entry point for a trade!
	 * This method determins the current state of the transaction
	 * and redirects to the responsible method.
	 */
	public static Result view(Long id) {
		Logger.info("Processing Trade State Machine");
		TradeTransaction tradeTransaction = TradeTransaction.findById(id);
		User currentUser = Common.currentUser();
		
		// Get the right perspective - which role has the current user?
		if(tradeTransaction.owner.equals(currentUser)) {
			if(tradeTransaction.state == States.INIT) {
				return viewInit(tradeTransaction);
			} else {
				return null;
			}			
		} else if (tradeTransaction.recipient.equals(currentUser)) {
			return null;
		} else {
			Logger.info("Could not determine perspective");
			return redirect(routes.Application.error());
		}

	}
	
	private static Result viewInit(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		User recipient = tradeTransaction.recipient;
		
		List<Book> pickedBooks = new ArrayList<Book>();		
		List<Book> restBooks = Book.findByUser(recipient);
		
		// Separating the already picked books from all books from the partner
		List<TradeBooks> tradeBooks = tradeTransaction.tradeBooks;
		for (TradeBooks tradeBook : tradeBooks) {
			Book book = tradeBook.book;
			if(restBooks.contains(book)){
				restBooks.remove(book);
				pickedBooks.add(book);
				
			}
		}
		return ok(init.render(restBooks,tradeTransaction,recipient));	
	}
	
	
	/**
	 * Inits the trade transaction
	 * @return
	 */
    public static Result init(Long recipientId) {
    	// ToDo Security here
    	
    	User owner = Common.currentUser();
    	User recipient = User.findById(recipientId);

    	//Check if transaction already exists.
    	if (TradeTransaction.exists(owner, recipient) != null) {
    		Logger.info("A TradeTransaction for (owner = " + owner.username + " ,recipient = " + recipient.username + ") already exists? ");
    		flash("error", "Dieser Wunschzettel existiert bereits und konnte nicht neu angelegt werden.");
    	} else {
    		
        	// Getting the selection
	    	String[] bookSelection = request().body().asFormUrlEncoded().get("book_selection");
	    	if(bookSelection == null) {
	    		flash("error", "Bitte wähle min. ein Buch aus.");
	    		Logger.info("Error in Selection");
	    		return redirect(routes.TradeController.viewForUser(recipientId));
	    	}
    		
    		
	    	// Create the transaction
	    	TradeTransaction trade = new TradeTransaction();
	    	trade.owner = owner;
	    	trade.recipient = recipient;
	    	trade.state = States.INIT;
	    	trade.commentOwner = "Hallo! Wie waere es mit einem Buchtausch?";
	    	trade.commentRecipient = "Ok. Geht klar.";
	    	trade.save();
	    	
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
	    	return redirect(routes.TradeController.view(trade.id));
    	}
    	return redirect(routes.Application.index());
    }
    
    public static Result delete(Long id){
    	// Security here!!!
    	
    	TradeTransaction trade = TradeTransaction.findById(id);
    	if(trade == null) {
    		return redirect(routes.Application.error());
    	}
    	
    	User partner = trade.recipient;
    	trade.delete();
    	flash("success", "Wunschzettel wurde gelöscht");
    	return redirect(routes.TradeController.viewForUser(partner.id));
    	
    }
	
}
