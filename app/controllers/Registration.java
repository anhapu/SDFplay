package controllers;

import models.enums.Roles;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
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
                if (!regForm.hasErrors()) {
                        User newUser = userForm.get();
                        String pwRepeat = form().bindFromRequest().get("repeatPassword");
                        String agbOK = form().bindFromRequest().get("accept");

                        if (User.findByUsername(newUser.username) == null)
                                if (User.findByEmail(newUser.email) == null)
                                        if (newUser.password.equals(pwRepeat))
                                                if (agbOK != null) {
                                                        newUser.password = Common.md5(newUser.password);
                                                        newUser.role = Roles.USER;
                                                        newUser.save();
                                                        sendRegistrationConfirmMail(newUser);
                                                        return ok(registrationSuccess.render());
                                                }
                                                else
                                                        regForm.reject("Sie müssen die AGB's akzeptieren!");
                                        else
                                                regForm.reject("Sie haben zwei unterschiedliche Passwoerter eingegeben!");
                                else
                                        regForm.reject("Diese E-Mail ist schon vorhanden!");
                        else
                                regForm.reject("Dieser Nutzername wird bereits verwendet!");
                }

                return badRequest(registrationForm.render(regForm));
        }

        private static void sendRegistrationConfirmMail(User newUser) {
                EmailSender.send("Ihre Registrierung bei Bücherbörse", "Sie haben sich bei der besten Bücherbörse der Welt registriert!", newUser.email);
        }
}