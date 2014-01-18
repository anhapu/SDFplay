package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Book;
import models.TradeTransaction;
import models.User;
import models.enums.States;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.trade.initOwner;
import views.html.trade.initRecipient;
import views.html.trade.refuseRecipient;
import views.html.trade.refuseOwner;
import views.html.trade.responseOwner;
import views.html.trade.responseRecipient;
import views.html.trade.finalRefuseOwner;
import views.html.trade.finalRefuseRecipient;
import views.html.trade.approveOwner;
import views.html.trade.approveRecipient;
import views.html.trade.invalidOwner;
import views.html.trade.invalidRecipient;
import views.html.trade.create;
import views.html.trade.showAll;

@With(Common.class)
@Security.Authenticated(Secured.class)
public class TradeController extends Controller {

     final static String navigation = "Trade";
	
	static Form<TradeTransaction> transactionForm = Form.form(TradeTransaction.class);
	
	/** Displays an overview of all book exchange offers, 
	 * 	in which the current user is involved in.
	 * 
	 * @return	html page with all TradeTransactions for the current user
	 */
	public static Result viewAllTrades() {
		User currentUser = Common.currentUser();
		if (currentUser != null) {
			List<TradeTransaction> tradeListOwner = TradeTransaction.findByOwner(currentUser);
			List<TradeTransaction> tradeListRecipient = TradeTransaction.findByRecipient(currentUser);			
			return ok(showAll.render(currentUser, tradeListOwner, tradeListRecipient, navigation));
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
		
		if(currentUser.equals(recipient)){
			return redirect(routes.Application.error());
		}
		
		if(recipient == null) {
			return redirect(routes.Application.error());
		}
		
		Logger.info("recipient = " + recipient.username);
		Logger.info("user = " + currentUser.username);
		
		TradeTransaction trade = TradeTransaction.exists(currentUser, recipient);
		if(trade == null) {
			// TODO Not the showcase books yet
			List<Book> books = Book.getShowcaseForUser(recipient);
			Logger.info("Found " + books.size() + " books for user " + recipient.username);
			return ok(create.render(books,recipient,transactionForm, navigation));
		} else {
			// There is already a Transaction, so forward it to the state machine
			return redirect(routes.TradeController.view(trade.id));
		}
	}
	
	/**
	 * TRADE STATE MACHINE
	 * 
	 * This is the main entry point for a trade!
	 * This method determines the current state of the transaction
	 * and redirects to the responsible method.
	 */
	public static Result view(Long id) {
		Logger.info("--- Processing Trade State Machine ---");
		TradeTransaction tradeTransaction = TradeTransaction.findById(id);
		if(tradeTransaction == null) {
			return redirect(routes.Application.error());
		}
		
		if(!Secured.viewTradeTransaction(tradeTransaction)){
			return redirect(routes.Application.denied());
		}
		
		Logger.info("TradeTransaction (id " + id + ") is in State " + tradeTransaction.state.name());
		switch (tradeTransaction.state) {
		 case INIT:			return viewInit(tradeTransaction);
		 case REFUSE:		return viewRefuse(tradeTransaction);
		 case RESPONSE:		return viewResponse(tradeTransaction);
		 case FINAL_REFUSE:	return viewFinalRefuse(tradeTransaction);
		 case APPROVE:		return viewApprove(tradeTransaction);
		 case INVALID:		return viewInvalid(tradeTransaction);
		 default:			Logger.info("Could not determine perspective");
		 					return redirect(routes.Application.error());
		}
	}
	
	private static Result viewInit(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewInit for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			List<Book> pickedBooks = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
			return ok(initOwner.render(pickedBooks,tradeTransaction, navigation));
			
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewInit for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
			List<Book> ownerBookList = Book.getShowcaseForUser(tradeTransaction.owner);
			return ok(initRecipient.render(recipientBookList, ownerBookList, tradeTransaction, transactionForm, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
	}
	
	private static Result viewRefuse(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewRefuse for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(refuseOwner.render(recipientBookList, tradeTransaction, navigation));
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewRefuse for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(refuseRecipient.render(recipientBookList, tradeTransaction, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
	}
	
	private static Result viewResponse(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
		List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewResponse for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(responseOwner.render(recipientBookList, ownerBookList, tradeTransaction, navigation));
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewResponse for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(responseRecipient.render(recipientBookList, ownerBookList, tradeTransaction, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
	}
	
	private static Result viewApprove(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
		List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewApprove for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(approveOwner.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewApprove for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(approveRecipient.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
	}
	
	
	private static Result viewFinalRefuse(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
		List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewFinalRefuse for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(finalRefuseOwner.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewFinalRefuse for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(finalRefuseRecipient.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
	}	
	
	private static Result viewInvalid(TradeTransaction tradeTransaction) {
		User currentUser = Common.currentUser();
		List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
		List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);		
		if (currentUser.equals(tradeTransaction.owner)) {
			Logger.info("viewInvalid for owner (" + tradeTransaction.owner.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(invalidOwner.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
			
		} else if (currentUser.equals(tradeTransaction.recipient)) {
			Logger.info("viewInvalid for recipient (" + tradeTransaction.recipient.username + ") of TradeTransaction (id " + tradeTransaction.id + ")");
			return ok(invalidRecipient.render(ownerBookList, recipientBookList, tradeTransaction, navigation));
		} else {
			return redirect(routes.Application.error());
		}	
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
    		flash("error", "Diese Tauschanfrage existiert bereits und konnte nicht neu angelegt werden.");
    	} else {
    		
    		Form<TradeTransaction> filledForm = transactionForm.bindFromRequest();
  
        	// Getting the selection
	    	String[] bookSelection = request().body().asFormUrlEncoded().get("book_selection");
	    	if(bookSelection == null) {
	    		flash("error", "Bitte wählen Sie mindestens ein Buch aus!");
	    		Logger.info("Error in Selection");
				List<Book> books = Book.getShowcaseForUser(recipient);
				return badRequest(create.render(books,recipient,filledForm, navigation));
	    	}
    		
	    	// Create the transaction
	    	TradeTransaction trade = new TradeTransaction();
	    	trade.owner = owner;
	    	trade.recipient = recipient;
	    	trade.state = States.INIT;
	    	trade.commentOwner = filledForm.data().get("comment");
	    	
	    	List<Book> books = new ArrayList<Book>();
	 		Long bookId = null;
	 		Book book = null;
	 		for (String bookString : bookSelection) {
	    		bookId = Long.parseLong(bookString);
	    		book = Book.findById(bookId);
	    		trade.bookList.add(book);
	    		books.add(book);
				Logger.info("Added Book " + book.id.toString());
			}
	 			 		
	    	trade.save();
	    	//send email
	    	EmailSender.sendBookExchangeRequest(trade, books);
	    	
	    	// Save that the user actually started a trade
	    	owner.alreadyTradeABook = true;
	    	owner.update();
	    	
	 		flash("success", "Tauschanfrage wurde angelegt!");
	    	return redirect(routes.TradeController.view(trade.id));
    	}
    	return redirect(routes.Application.index(1));
    }
    
    
    public static Result response(Long id) {
    	
    	TradeTransaction tradeTransaction = TradeTransaction.findById(id);
    	
		if(!Secured.responseTradeTransaction(tradeTransaction)){
			return redirect(routes.Application.denied());
		}
    	
		Form<TradeTransaction> filledForm = transactionForm.bindFromRequest();
		
		// Process the Response Button
		if(filledForm.data().get("response") != null) {
			
	       	// Getting the selection
	    	String[] bookSelection = request().body().asFormUrlEncoded().get("book_selection");
	    	
	    	Long bookId = null;
	 		Book book = null;
	 		
	 		if(bookSelection != null) {
		 		for (String bookString : bookSelection) {
		    		bookId = Long.parseLong(bookString);
		    		book = Book.findById(bookId);
		    		tradeTransaction.bookList.add(book);
					Logger.info("Added Book " + book.id.toString());
				}	
	 		}

	    	tradeTransaction.commentRecipient = filledForm.data().get("comment");
	    	tradeTransaction.state = States.RESPONSE;
	    	tradeTransaction.save();
	    	//send email
	    	List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);
			List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
	    	EmailSender.sendBookExchangeResponse(tradeTransaction, ownerBookList, recipientBookList);
	 		flash("success", "Tauschanfrage bestätigt");	
	 		
	 	// Process the Refuse Button
		} else if(filledForm.data().get("refuse") != null){

	    	tradeTransaction.state = States.REFUSE;
	    	tradeTransaction.commentRecipient = filledForm.data().get("comment");
	    	tradeTransaction.save();
	    	//send email
	    	EmailSender.sendBookExchangeRefuse(tradeTransaction.owner, tradeTransaction.recipient);
	 		flash("success", "Sie haben die Tauschanfrage abgelehnt.");
			
		}
		
    	return redirect(routes.TradeController.view(tradeTransaction.id));
    }
    
    public static Result approve(Long id) {
    	TradeTransaction tradeTransaction = TradeTransaction.findById(id);
		Form<TradeTransaction> filledForm = transactionForm.bindFromRequest();
		
		if(!Secured.approveTradeTransaction(tradeTransaction)){
			return redirect(routes.Application.denied());
		}
    	
	
		// Process the Approve Button
		if(filledForm.data().get("approve") != null) {
			tradeTransaction.state = States.APPROVE;
			tradeTransaction.save();
			
			List<Book> ownerBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.owner);
			List<Book> recipientBookList = Book.findByTransactionAndOwner(tradeTransaction, tradeTransaction.recipient);
				
	    	//exchange books owner
			for (Book book : ownerBookList) {
				book.owner = tradeTransaction.recipient;
				book.tradeable = false;
				book.save();
			}
			//exchange books recipient
			for (Book book : recipientBookList) {
				book.owner = tradeTransaction.owner;
				book.tradeable = false;
				book.save();
			}
			//set involved tradeTransactions to INVALID and send emails to all users involved
			List<Email> emailList = new ArrayList<Email>();
			List<TradeTransaction> invalidTradeTransactions = TradeTransaction.findListOfTradeTransactionInvolvedInTradeTransaction(tradeTransaction);
			for (TradeTransaction invalidTradeTransaction : invalidTradeTransactions) {
				// set State to INVALID only, if trade is not finished yet (INIT or RESPONSE)
				if ((invalidTradeTransaction.state == States.INIT) || (invalidTradeTransaction.state == States.RESPONSE)) {
					invalidTradeTransaction.state = States.INVALID;
					invalidTradeTransaction.save();
					// add emails to user whose tradeTransaction became invalid
					emailList.addAll(EmailSender.getBookExchangeInvalid(invalidTradeTransaction.owner, invalidTradeTransaction.recipient));
				}
			}
			//add emails of users whose tradeTransaction was APPROVED
			emailList.addAll(EmailSender.getBookExchangeApprove(tradeTransaction, ownerBookList, recipientBookList));
			//send emails
			EmailSender.send(emailList);
			flash("success", "Buchtausch erfolgreich!");
	 	// Process the Refuse Button
		} else if(filledForm.data().get("finalrefuse") != null) {
	    	tradeTransaction.state = States.FINAL_REFUSE;
	    	tradeTransaction.save();
	    	// send email
	    	EmailSender.sendBookExchangeFinalRefuse(tradeTransaction.owner, tradeTransaction.recipient);
	 		flash("success", "Sie haben die Tauschanfrage abgelehnt.");
		}
    	return redirect(routes.TradeController.view(tradeTransaction.id));
    }
    
    
    
    public static Result delete(Long id) {
    	TradeTransaction tradeTransaction = TradeTransaction.findById(id);
    	if(tradeTransaction == null) {
    		return redirect(routes.Application.error());
    	} else {
    	
			if (!Secured.deleteTradeTransaction(tradeTransaction)) {
				return redirect(routes.Application.denied());
			}
    		EmailSender.send(EmailSender.getBookExchangeDeleted(tradeTransaction.owner, tradeTransaction.recipient));
			User partner = tradeTransaction.recipient;
			tradeTransaction.delete();
			flash("success", "Die Tauschanfrage wurde gelöscht");
			return redirect(routes.TradeController.viewAllTrades());
    	}
    }
	
}
