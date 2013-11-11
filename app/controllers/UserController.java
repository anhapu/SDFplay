package controllers;

import controllers.Common;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.index;
import views.html.profileForm;
import views.html.userProfile;
import views.html.userBookshelf;
import static play.data.Form.*;
import play.mvc.Http.Context;
import play.mvc.Security;
import play.api.mvc.Call;
import play.db.ebean.*;

@With(Common.class)
public class UserController extends Controller {

	public static Result login() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if(loginForm.hasErrors()) {
			Common.addToContext(Common.ContextIdent.loginForm, loginForm);
			return badRequest(index.render());
		} else {
			session().clear();
			User user = User.findByEmail(loginForm.get().email);
			session("id", user.id.toString());
			return redirect(routes.Application.index());
		}
	}
	
	public static Result logout() {
		session().clear();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public static Result editProfile(Long id) {
		final Form<User> form = form(User.class).bindFromRequest();
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			Secured.editUserProfile(searchedUser);
			return ok(profileForm.render(form.fill(searchedUser)));
		} else {
			return redirect(routes.Registration.index());
		}
	}

	@Transactional
	public static Result saveProfile(Long id) {
		User created = form(User.class).bindFromRequest().get();
		created.update();
		return redirect(routes.Application.index());
	}

	@Security.Authenticated(Secured.class)
	public static Result showProfile(Long id) {
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			Secured.showUserProfile(searchedUser);
			return ok(userProfile.render(searchedUser));
		}
		else {
			//ToDo redirect to something useful
			return redirect(routes.Application.index());
		}
	}
	
	@Security.Authenticated(Secured.class)
	public static Result showBookshelf(Long id) {
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			Secured.showUserProfile(searchedUser);
			return ok(userBookshelf.render());
		}
		else {
			//ToDo redirect to something useful
			return redirect(routes.Application.index());
		}
	}


	public static class Login {
		
		public String email;
		public String password;
		
		public String validate() {
			if(User.authenticate(email, password) == null) {
				return "Falscher Nutzername oder Passwort";
			} 
			return null;
		}
	}

}
