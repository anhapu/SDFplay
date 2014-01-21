package controllers;

import java.util.Collections;
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

	public static Result index(int page) {
		List<User> users = null;
		int limit = 5;
		int maxShowcases;
		
		if (Common.currentUser() != null) {
			
			if (!Common.currentUser().alreadyTradeABook) {
				flash("info", "Du hast noch keine Tauschanfrage angelegt! " + 
					"Um Bücher mit anderen Nutzern tauschen zu können, klicke auf ihren Showcase oder finde Deine Lieblingsbücher über die Suche. "
					+ "Weiter Informationen findest Du unter dem Menüpunkt Hilfe.");
			}
			
			maxShowcases = User.countWithShowcases(Common.currentUser());
			users = User.findPaginated(limit, page, Common.currentUser());
		} else {
			if(!flash().containsKey("success")){
				flash("info", "Registriere Dich auf der Seite, um mit anderen Benutzern Bücher zu tauschen.");
			}
			maxShowcases = User.countWithShowcases(null);
			users = User.findPaginated(limit, page, null);
		}

		int maxPage = (int)Math.ceil(maxShowcases/(double)limit);
		return ok(index.render(users, page, maxPage));
	}
	

	public static Result error() {
		return badRequest(error.render());
	}

	public static Result denied() {
		return badRequest(denied.render());
	}

	public static Result agb() {
          String navigation = "Help";
		return ok(agb.render(navigation));
	}

    public static Result faq() {
        String navigation = "Help";
        return ok(faq.render(navigation));
    }

}
