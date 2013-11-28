package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.util.List;

import controllers.UserController.SimpleProfile;

import models.Book;
import models.User;
import play.Logger;
import play.data.Form;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.book.mybookshelf;
import views.html.book.bookshelf;
import views.html.book.detailview;

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

	public static Result createBook() {
		Form<SimpleProfile> pForm = form(SimpleProfile.class).bindFromRequest();
		if (pForm.hasErrors()) {
			Logger.error("Error in form");
			// TODO redirect to something useful
			return badRequest();
		} else {

			try {
				return ok(Common.getGoogleBooksContent(pForm.get().isbn));
			} catch (IOException e) {
				Logger.error(e.getMessage());
				return badRequest();
			}

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
	public static Result editBook(final Long bookId) {
		Book book = Book.findById(bookId);
		if (book != null) {
			if (Secured.isOwnerOfBook(book)) {
				Form<Book> filledForm = bookForm.bindFromRequest();
				book = filledForm.get();
				book.owner = Common.currentUser();
				book.update();
				return ok();

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
			book.delete();
			return redirect(routes.BookController.showBookshelf(book.owner.id));
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
			Logger.error("Did not find any user for id: " + searchedUser.id);
			return redirect(routes.Application.index());
		}
	}
	
	
	public static Result showBookshelf(Long id) {
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			List<Book> books = Book.findByUser(searchedUser);
			Logger.info("Found " + books.size() + " books for user " + searchedUser.username);
			return ok(bookshelf.render(Book.findByUser(searchedUser),searchedUser));
		} else {
			// TODO redirect to something useful
			Logger.error("Did not find any user for id: " + id);
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
			return redirect(routes.BookController.showBookshelf(book.owner.id));
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
			return redirect(routes.BookController.showBookshelf(book.owner.id));

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

	public static class SimpleProfile {
		public String isbn;

		public SimpleProfile() {
		}

		public SimpleProfile(String isbn) {
			this.isbn = isbn;

		}
	}
}
