package controllers;

import static play.data.Form.form;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import models.User;

import org.apache.commons.codec.binary.Base64;

import play.data.Form;
import play.data.validation.Constraints.EmailValidator;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import utils.Utils;
import views.html.snippets.passwordRecoveryMailForm;
import views.html.snippets.passwordRecoveryMailSuccess;
import views.html.user.passwordForm;
import views.html.user.profileForm;
import views.html.user.userProfile;

import com.typesafe.config.ConfigFactory;

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
                       flash("error", loginForm.globalError().message());
                       result = Application.index();
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
        
        @Security.Authenticated(Secured.class)
        public static Result logout() {
                session().clear();
                return redirect(routes.Application.index());
        }

        
        @Transactional
        public static Result sendRecoveryMail() {
          Form<Email> emailForm = form(Email.class).bindFromRequest();
          if(emailForm.hasErrors()) {
              return redirect(routes.Application.index());
          }
          SecureRandom random = new SecureRandom();
          String token = new BigInteger(130, random).toString(32);
          User user = User.findByEmail(emailForm.get().email);
          if (user != null) {
               user.token = token;
               user.tokenCreatedAt = new Date();
               user.update();
               String url = ConfigFactory.load().getString("application.URL");
                   //Play.application().configuration().get("langs");
               EmailSender.send("Bücherbörse: Password Recovery", "Klick auf diesen Link " + url + "/passwordRecovery/" + token + " " + "um ein neues Passwort zu vergeben.", emailForm.get().email);
               return ok(passwordRecoveryMailSuccess.render());
          }
          else {
               return badRequest("Interner Fehler");
          }
        }


        public static Result checkPasswordRecoveryToken(String token) {
             User user = User.findByToken(token);
             if (user != null) {
                  Date currentDate = new Date();
                  long elapsedTime = ((currentDate.getTime()/60000) - (user.tokenCreatedAt.getTime()/60000));
                  if (elapsedTime > 5) {
                       return badRequest("Die Zeit von 5 Minuten ist abgelaufen!");
                  }
                  else {
                    // forward to change PW page ...
                    String mystery = createMystery(user.id);
                    return redirect(routes.UserController.editPassword(mystery));
                  }
             }
             else {
               return badRequest("Dieser Token ist invalid.");
             }
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

        @Security.Authenticated(Secured.class)
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
        
        private static String createMystery(Long id) {
               User user = User.findById(id);
               String email = user.email;
               String trash = "";
               for(int i = 0; i < 12; i++) {
                    if (i == 7) {
                         trash += "#";
                    }
                    else if (i == 8) {
                         trash += email;
                    }
                    else if (i == 9) {
                         trash += "#";
                    }
                    else {
                         SecureRandom random = new SecureRandom();
                         String data = new BigInteger(130, random).toString(1);
                         trash += data;
                    }
               }
               byte[] mystery = Base64.encodeBase64(trash.getBytes());
               return new String(mystery);
        }
        
        private static User solveMystery(String mystery) {
             byte[] solvedMystery = Base64.decodeBase64(mystery.getBytes());
             String foob = new String(solvedMystery);
             String[] data = foob.split("#");
             User user = User.findByEmail(data[1]);
             return user;
        }

        @Security.Authenticated(Secured.class)
        public static Result editPassword(String mystery) {
                final Form form = form().bindFromRequest();
                User searchedUser = solveMystery(mystery);
                if (searchedUser != null) {
                     return ok(passwordForm.render(mystery, form));
                } else {
                     return redirect(routes.Registration.index());
                }
        }

        @Security.Authenticated(Secured.class)
        @Transactional
        public static Result savePassword(String mystery) {
                Form<ChangePassword> pForm = form(ChangePassword.class).bindFromRequest();
                if (pForm.hasErrors()) {
                        return badRequest(views.html.user.passwordForm.render(mystery, pForm));
                }
                else {
                        User user = solveMystery(mystery);
                        user.password = Utils.md5(form().bindFromRequest().get("password"));

                        //activate user
                        user.token = null;
                        user.tokenCreatedAt = null;

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
                        if(user == null)  {
                            return "Der Nutzer existiert nicht";
                        }
                        if(!user.isActive()) {
                             return "Benutzer nicht aktiv!";
                        }
                        if(User.authenticate(email, password) == null) {
                                return "Falscher Nutzername oder Passwort";
                        }
                        return null;
                }
        }

        public static class ChangePassword {
        
                public String password;
                public String repeatPassword;
                
                public String validate() {
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
