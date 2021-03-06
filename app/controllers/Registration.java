package controllers;

import static play.data.Form.form;

import java.sql.Timestamp;

import models.User;
import models.enums.Roles;
import play.data.Form;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import views.html.user.registrationForm;
import views.html.user.registrationSuccess;
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
                regForm.reject("Du hast zwei unterschiedliche Passwoerter eingegeben!");
            }
            if (agbOK == null) {
                regForm.reject("Du musst die AGB's akzeptieren!");
            }

            if (!regForm.hasErrors()) {
                newUser.password = Utils.md5(newUser.password);
                newUser.role = Roles.USER;
                newUser.lastActivity = Timestamp.valueOf("1970-01-01 00:00:00");
                newUser.save();
                sendRegistrationConfirmMail(newUser);
                flash("success", "Du hast dich erfolgreich bei der besten Bücherbörse der Welt registriert! Du kannst dich jetzt einloggen");
                return redirect(routes.Application.index(1));
            }
        }

        for (ValidationError error : userForm.globalErrors()) {
            regForm.reject(error.message());
        }

        return badRequest(registrationForm.render(regForm));
    }


    private static void sendRegistrationConfirmMail(User newUser) {
        EmailSender.sendRegistration(newUser);
    }
}