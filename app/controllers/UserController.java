package controllers;

import controllers.Common;
import models.User;
import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;
import views.html.user.profileForm;
import views.html.user.userProfile;
import views.html.book.bookshelf;
import views.html.user.passwordForm;
import views.html.snippets.passwordRecoveryMailForm;
import views.html.snippets.passwordRecoveryMailSuccess;
import static play.data.Form.*;
import play.mvc.Http.Context;
import play.mvc.Security;
import play.api.mvc.Call;
import play.db.ebean.*;
import java.util.Date;
import java.security.SecureRandom;
import java.math.BigInteger;

@With(Common.class)
public class UserController extends Controller {

        public static Result login() {
             Result result;
             String[] postAction = request().body().asFormUrlEncoded().get("action");
             String action = postAction[0];
             if (action.equals("login")) {
                  Form<Login> loginForm = form(Login.class).bindFromRequest();
                  if(loginForm.hasErrors()) {
                       Common.addToContext(Common.ContextIdent.loginForm, loginForm);
                       result = badRequest(index.render());
                  }
                  else {
                       session().clear();
                       User user = User.findByEmail(loginForm.get().email);
                       session("id", user.id.toString());
                       result = redirect(routes.Application.index());
                  }
             }
             else if (action.equals("passwordRecovery")) {
                  Form<Email> emailForm = form(Email.class);
                  result = ok(passwordRecoveryMailForm.render(emailForm.fill(new Email())));
             }
             else {
                  result = badRequest("Invalide Aktion!");
             }
             return result;
        }
        
        public static Result logout() {
                session().clear();
                return redirect(routes.Application.index());
        }

        
        @Transactional
        public static Result sendRecoveryMail() {
          Form<Email> emailForm = form(Email.class).bindFromRequest();
          if(emailForm.hasErrors()) {
               return badRequest(index.render());
          }
          SecureRandom random = new SecureRandom();
          String token = new BigInteger(130, random).toString(32);
          User user = User.findByEmail(emailForm.get().email);
          if (user != null) {
               user.token = token;
               user.tokenCreatedAt = new Date();
               user.update();
               // TODO change the URL
               EmailSender.send("pw reset request", "Klick hier http://localhost:9000/passwordRecovery/" + token + " " + "Here be lions", emailForm.get().email);
               return ok(passwordRecoveryMailSuccess.render());
          }
          else {
               return badRequest("Interner Fehler");
          }
        }


        public static Result checkPasswordRecoveryToken(String token) {
             Result result = redirect(routes.Application.index());
             User user = User.findByToken(token);
             if (user != null) {
                  Date currentDate = new Date();
                  long elapsedTime = ((currentDate.getTime()/60000) - (user.tokenCreatedAt.getTime()/60000));
                  if (elapsedTime > 5) {
                       result = badRequest("Zu langsam :D");
                  }
                  else {
                    // forward to change PW page ...
                  }
             }
             else {
               result = badRequest("Dieser Token ist invalid.");
             }
             return result;
        }
     
        @Security.Authenticated(Secured.class)
        public static Result editProfile(Long id) {
             Form<SimpleProfile> form = form(SimpleProfile.class);
            User searchedUser = User.findById(id);
            if (Secured.editUserProfile(searchedUser)) {
                 if (searchedUser != null) {
                      return ok(profileForm.render(form.fill(new SimpleProfile(searchedUser.email,
                                               searchedUser.username, searchedUser.lastname, searchedUser.firstname)), searchedUser.id));
                 }
                 else {
                      return redirect(routes.Registration.index());
                 }
            }
            else {
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


        public static class Login {
                
                public String email;
                public String password;
                
                public String validate() {
                        User user = User.findByEmail(email);
                        if(!user.isActive()) {
                             return "Benutzer nicht aktiv!";
                        }
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
                                return "Bitte alle Felder ausfüllen!";
                        }
                        if (username.length() <= 3) {
                                return "Nutzername muss min. 4 Zeichen lang sein!";
                        }
                        
                        EmailValidator validator = new EmailValidator();
                        if (!validator.isValid(email)) {
                            return "Bitte eine gültige E-Mail Adresse eingeben!";
                        }
                        return null;
                }
        }

        public static class Email {
          public String email;

          public String validate() {
               EmailValidator validator = new EmailValidator();
               if (!validator.isValid(email)) {
                    return "Bitte eine gültige E-Mail Adresse eingeben!";
               }
               return null;
          }
        }
}
