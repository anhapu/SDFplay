package controllers;

import controllers.Common;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;
import views.html.user.profileForm;
import views.html.user.userProfile;
import views.html.book.bookshelf;
import views.html.user.passwordForm;
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
		Form<SimpleProfile> form = form(SimpleProfile.class);
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			Secured.editUserProfile(searchedUser);
			return ok("Here be lions");
		} else {
			return redirect(routes.Registration.index());
		}
	}

	@Transactional
	public static Result saveProfile(Long id) {
		Form<SimpleProfile> pForm  = form(SimpleProfile.class).bindFromRequest();
		User created = form(User.class).bindFromRequest().get();
		if (pForm.hasErrors()) {
			Common.addToContext(Common.ContextIdent.loginForm, pForm);
			return badRequest(index.render());
		}
		else {
			created.update();
			return redirect(routes.Application.index());
		}
	}
	
	@Security.Authenticated(Secured.class)
	public static Result editPassword(Long id) {
		final Form form = form().bindFromRequest();
		User searchedUser = User.findById(id);
		if (searchedUser != null) {
			Secured.editUserProfile(searchedUser);
			return ok(passwordForm.render(searchedUser, form));
		} else {
			return redirect(routes.Registration.index());
		}
	}

	@Transactional
	public static Result savePassword(Long id) {
		Form<changePassword> pForm = form(changePassword.class).bindFromRequest();
		if (pForm.hasErrors()) {
			return badRequest(views.html.user.passwordForm.render(User.findById(id), pForm));
		}
		else {
			User user = User.findById(id);
			user.password = Common.md5(form().bindFromRequest().get("password"));
			user.update();
			return redirect(routes.Application.index());
		}
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
			return ok(bookshelf.render());
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

	public static class changePassword {
	
		public Long id;
		public String oldPassword;
		public String password;
		public String repeatPassword;
		
		public String validate() {
			User user = User.findById(id);
			if(user == null) {
				return "Du existierst nicht!";
			}
			if(!user.password.equals(Common.md5(oldPassword))) {
				return "Altes Passwort nicht korrekt";
			}
			if(password.length() < 3) {
				return "Neues Passwort zu kurz, min. 3 chars.";
			}
			if(!password.equals(repeatPassword)) {
				return "Passwörter nicht gleich";
			}
			return null;
		}
	}

	public static class SimpleProfile {
		public String email;
		public String username;
		public String lastname;
		public String firstname;

		public String validate() {
			if (email.length() == 0) {
				return "Email darf nicht leer sein!";
			}
			if (User.findByUsername(username) != null) {
				return "Username bereits vergeben!";
			}
			return null;
		}
	}
}
