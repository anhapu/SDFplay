package controllers;

import controllers.Common;
import controllers.Secured;
import models.TradeTransaction;
import models.User;
import models.Book;
import models.enums.Roles;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;

import java.util.List;

import play.db.ebean.Transactional;

@With(Common.class)
public class UserAdministrationController extends Controller {
     final static String navigation = "UserAdministration";

     @Security.Authenticated(Secured.class)
     public static Result index() {
          if (Secured.isAllowedToAccessUserAdminInterface(Common.currentUser())) {
               List<User> users = null;
               users = User.findAllBut(Common.currentUser());
               return ok(views.html.userAdministration.render(users, navigation));
          } else {
               flash("error", "Zugriff nicht gestattet!");
               return redirect(routes.Application.index(1));
          }
     }

     /**
     * Changes the role. Admin -> User or User -> Admin.
     */
     @Security.Authenticated(Secured.class)
     @Transactional
	public static Result toggleRole(Long id) {
          User user = User.findById(id);
          if (Secured.isAllowedToAccessUserAdminInterface(Common.currentUser())) {
               if (User.isAdmin(user.id)) {
                    user.role = Roles.USER;
                    user.save();
                    flash("success", "Benutzerrolle zu Benutzer geändert!");
               }
               else {
                    user.role = Roles.ADMIN;
                    user.save();
                    flash("success", "Benutzerrolle zu Admin geändert!");
               }
               return redirect(routes.UserAdministrationController.index());
          }
          else {
               flash("error", "Zugriff nicht gestattet!");
               return redirect(routes.Application.index(1));
          }
     }

     /**
     * Activate and deactive a user, this is possible by changing the value of token to null or
     * something else.
     */
     @Security.Authenticated(Secured.class)
     @Transactional
     public static Result toogleActive(Long id) {
         User user = User.findById(id);
         if (Secured.isAllowedToAccessUserAdminInterface(Common.currentUser())) {
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
         else {
              flash("error", "Zugriff nicht gestattet!");
              return redirect(routes.Application.index(1));
         }
    }

    @Security.Authenticated(Secured.class)
    @Transactional
    /**
     * Deletes the user and all of it's books.
     */
    public static Result deleteUser(Long id) {
		User user = User.findById(id);
          if (Secured.isAllowedToAccessUserAdminInterface(Common.currentUser())) {
               if(user != Common.currentUser()) {
                    String userName = user.username;
                    
                    //delete all of his books
                    for (Book book : user.books) {
                    	book.delete();
                    }
                    //delete all TradeTransaction where he is owner
                    List<TradeTransaction> ownerList = TradeTransaction.findByOwner(user);
                    for (TradeTransaction tradetransaction : ownerList) {
                    	tradetransaction.delete();
                    }
                  //delete all TradeTransaction where he is recipient
                    List<TradeTransaction> recipientList = TradeTransaction.findByRecipient(user);
                    for (TradeTransaction tradetransaction : recipientList) {
                    	tradetransaction.delete();
                    }
                    
                    user.delete();
                    flash("success", "Benutzer " + userName + " gelöscht!");
               } else {
                    flash("error", "Man kann sich nicht selbst löschen!");
               }
               return redirect(routes.UserAdministrationController.index());
          } else {
               flash("error", "Zugriff nicht gestattet!");
               return redirect(routes.Application.index(1));
          }
     }
    
}
