package controllers;

import controllers.Common;
import models.User;
import models.Book;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import java.util.List;
import play.db.ebean.Transactional;

@With(Common.class)
public class UserAdministrationController extends Controller {

     public static Result index() {
          List<User> users = null;
          users = User.findAllBut(Common.currentUser());
          return ok(views.html.userAdministration.render(users));
     }

     /**
     * Activate and deactive a user, this is possible by changing the value of token to null or
     * something else.
     */
     @Transactional
     public static Result toogleActive(Long id) {
         User user = User.findById(id);
         // deactivate
         if (user.isActive()) {
              user.token = "123";
              user.save();
              flash("success", "Benutzer " + user.username + " deaktiviert!");
         }
         // activate
         else {
              user.token = null;
              user.save();
              flash("success", "Benutzer " + user.username + " aktiviert!");
         }
         return redirect(routes.UserAdministrationController.index());
    }

    @Transactional
    /**
     * Deletes the user and all of it's books.
     */
    public static Result deleteUser(Long id) {
		User user = User.findById(id);
		if(user != Common.currentUser()) {
			String userName = user.username;
			List<Book> books = Book.findByUser(user);
			for (int i = 0; i < books.size(); i++) {
				books.get(i).delete();
			}
			user.delete();
			flash("success", "Benutzer " + userName + " gelöscht!");
		} else {
			flash("error", "Man kann sich nicht selbst löschen!");
		}
		return redirect(routes.UserAdministrationController.index());
	}
}
