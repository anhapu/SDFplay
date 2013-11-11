package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import static play.data.Form.*;
import views.html.registrationForm;
import play.api.templates.Html;


public class Registration extends Controller {

	public static Result index() {
		return ok(registrationForm.render());
	}

	private static boolean validateNewUser(User newUser, String pwRepeat, String agbOK) {
		boolean valid = true;
		if (null == agbOK || 
			!newUser.password.equals(pwRepeat) || 
			User.findByUsername(newUser.username) != null || 
			User.findByEmail(newUser.email) != null)
			valid = false;
		return valid;			
	}

	public static Result submit() {
		Result result = ok("Erfolgreich registriert!");
		Form<User> userForm = form(User.class).bindFromRequest();
		if (!userForm.hasErrors()) {
			User newUser = form(User.class).bindFromRequest().get();
			String pwRepeat = form().bindFromRequest().get("repeatPassword");
			String agbOK = form().bindFromRequest().get("accept");
			if (validateNewUser(newUser, pwRepeat, agbOK)) {
				System.out.println("valide");
				newUser.password = Common.md5(newUser.password);
				newUser.save();
				sendRegConfirmationMail(newUser);
			}
			else {
				result = badRequest(registrationForm.render());
			}
		} else {
			result = badRequest(registrationForm.render());
		}
		return result;
	}

	private static void sendRegConfirmationMail(User newUser) {
		//TODO
	}
}