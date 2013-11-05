package controllers;

import controllers.Common;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.profileForm;
import static play.data.Form.*;
import play.mvc.Http.Context;

public class UserController extends Controller {
	
	private static Common common = new Common();

	public static Result login() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if(loginForm.hasErrors()) {
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
		if (!"".equals(common.currentUser())) {
			return ok(profileForm.render(form.fill(User.findByEmail(common.currentUser()))));
		} else {
			//ToDo redirect to register page
			return redirect(routes.Application.index());
		}
	}

	public static Result saveProfile() {
		return redirect(routes.Application.index());
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
