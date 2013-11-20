package controllers;

import models.enums.Roles;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import play.data.validation.ValidationError;
import static play.data.Form.*;
import views.html.user.registrationForm;
import views.html.user.registrationSuccess;
import play.api.templates.Html;
import controllers.UserController.SimpleProfile;


public class Registration extends Controller {

	public static Result index() {
		return ok(registrationForm.render(form(UserController.SimpleProfile.class)));
	}

	public static Result submit() {
		Form<User> userForm = form(User.class).bindFromRequest();
		Form<SimpleProfile> regForm = form(UserController.SimpleProfile.class).bindFromRequest();

		if (userForm.field("password").value() == "" || userForm.field("repeatPassword").value() == "") {
			regForm.reject("Bitte Passwortfelder ausfüllen.");
		}

		if (!regForm.hasGlobalErrors() && !userForm.hasGlobalErrors()) {
			User newUser = userForm.get();
			String pwRepeat = form().bindFromRequest().field("repeatPassword").value();
			String agbOK = form().bindFromRequest().field("accept").value();

			if (User.findByUsername(newUser.username) != null) {
				regForm.reject("Dieser Nutzername wird bereits verwendet!");
			}
			if (User.findByEmail(newUser.email) != null) {
				regForm.reject("Diese E-Mail ist schon vorhanden!");
			}
			if (!newUser.password.equals(pwRepeat)) {
				regForm.reject("Sie haben zwei unterschiedliche Passwoerter eingegeben!");
			}
			if (agbOK == null) {
				regForm.reject("Sie müssen die AGB's akzeptieren!");
			}

			if (!regForm.hasErrors()) {
				newUser.password = Common.md5(newUser.password);
				newUser.role = Roles.USER;
				newUser.save();
				sendRegistrationConfirmMail(newUser);
				return ok(registrationSuccess.render());
			}
		}

		for (ValidationError error : userForm.globalErrors()) {
			regForm.reject(error.message());
		}

		return badRequest(registrationForm.render(regForm));
	}

	private static void sendRegistrationConfirmMail(User newUser) {
		EmailSender.send("Ihre Registrierung bei Bücherbörse", "Sie haben sich bei der besten Bücherbörse der Welt registriert!", newUser.email);
	}
}