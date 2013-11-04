package controllers;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import static play.data.Form.*;

public class UserController extends Controller {
	
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
