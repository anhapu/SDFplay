package controllers;

import java.util.List;

import models.User;
import play.api.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;
import views.html.error;
import views.html.denied;
import views.html.agb;
import views.html.help.faq;

@With(Common.class)
public class Application extends Controller {

	/**
	 * Start Page, where a lot of showcases of other users are presented
	 * 
	 * @return
	 */

	public static Result index() {
		return home(1);
	}
	
	public static Result home(int page) {
		List<User> users = null;
		if (Common.currentUser() != null) {
			if (!Common.currentUser().alreadyTradeABook) {
				flash("info",
						"Du hast noch keine Bücher getauscht. Um Bücher mit anderen Nutzern tauschen zu können, klicke auf den Showcase anderer Nutzer und wähle Bücher aus, die du tauschen möchtest.");
			}
			
			users = User.findPaginated(2, 1, Common.currentUser());
			//users = User.findAllBut(Common.currentUser());
		} else {
			flash("info",
					"Registriere dich auf der Seite, um mit anderen Benutzern Bücher zu tauschen.");
			users = User.findAll();
		}
		return ok(index.render(users));	
	}

	public static Result error() {
		return badRequest(error.render());
	}

	public static Result denied() {
		return badRequest(denied.render());
	}

	public static Result agb() {
		return ok(agb.render());
	}

    public static Result faq() {
        return ok(faq.render());
    }

}
