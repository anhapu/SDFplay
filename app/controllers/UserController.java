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
import static play.data.Form.*;
import play.mvc.Http.Context;

@With(Common.class)
public class UserController extends Controller {

	public static Result login() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if(loginForm.hasErrors()) {
			Common.addToContext(Common.ContextIdent.loginForm, loginForm);
			return badRequest(index.render(loginForm));
		} else {
			session().clear();
			session("email", loginForm.get().email);
			return redirect(routes.Application.index());
		}
	}
	
	public static Result logout() {
		session().clear();
		return redirect(routes.Application.index());
	}

	public static Result editProfile() {
		final Form<User> form = form(User.class);
		if (!"".equals(Common.currentUser())) {
			return ok(profileForm.render(form.fill(User.findByEmail(Common.currentUser()))));
		} else {
			//ToDo redirect to register page
			return redirect(routes.Application.index());
		}
	}

	public static Result saveProfile() {
		return redirect(routes.Application.index());
	}

	public static Result showProfile() {
		if (!"".equals(Common.currentUser())) {
			return ok(userProfile.render(User.findByEmail(Common.currentUser())));
		} else {
			//ToDo redirect to register page
			return redirect(routes.Application.index());
		}
	}

	public static class Login {
		
		public String email;
		public String password;
		
		public String validate() {
			if(email.isEmpty() || password.isEmpty()) {
				return "Falscher Nutzername oder Passwort";
			} 
			return null;
		}
	}

}
