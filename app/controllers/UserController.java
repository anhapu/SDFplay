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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		    return ok(profileForm.render(
						form.fill(new SimpleProfile(searchedUser.email, searchedUser.username, searchedUser.lastname, searchedUser.firstname)),
						searchedUser.id));
		} else {
			return redirect(routes.Registration.index());
		}
	}

	@Transactional
	public static Result saveProfile(Long id) {
		Form<SimpleProfile> pForm = form(SimpleProfile.class).bindFromRequest();
		if (pForm.hasErrors()) {
			return badRequest(views.html.user.profileForm.render(pForm, Long.valueOf(form().bindFromRequest().get("id"))));
		}
		else {
			User created = User.findById(Long.valueOf(form().bindFromRequest().get("id")));
			created.email = pForm.get().email;
			created.username = pForm.get().username;
			created.lastname = pForm.get().lastname;
			created.firstname = pForm.get().firstname;
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
		public String username;
		public String firstname;
		public String lastname;
		public String email;

		//Needed for Email validation
		private Pattern pattern;
		private Matcher matcher;
		private static final String EMAIL_PATTERN =	"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		public SimpleProfile() {
		}

		public SimpleProfile(String newEmail, String newUsername, String newLastname, String newFirstname) {
			email = newEmail;
			username = newUsername;
			lastname = newLastname;
			firstname = newFirstname;
		}

		public String validate() {
			if (email.length() == 0 || username.length() == 0 || lastname.length() == 0 || firstname.length() == 0) {
				return "Felder dürfen nicht leer sein!";
			}
			if (username.length() < 6) {
				return "Benutzername muss min. 6 Zeichen lang sein!";
			}
			
			//E-Mail validation
			pattern = Pattern.compile(EMAIL_PATTERN);
			matcher = pattern.matcher(email);
			if (!matcher.matches()) {
				return "Invalide Emailaddresse!";
			}
			return null;
		}
	}
}
