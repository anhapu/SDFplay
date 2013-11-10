package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.Form;
import views.html.registrationForm;


public class Registration extends Controller {

    public static Result index() {
        return ok(registrationForm.render(Form.form(Registration.class)));
    }

    public static Result validate() {
        Form<User> registrationForm = Form.form(User.class).bindFromRequest();
        if (registrationForm.hasErrors()) {
            return redirect(routes.Application.index());
        } else {
            addUserToDB(generateUserObject());
            sendRegConfirmationMail();
            return redirect(routes.Application.index());
        }
            
    }

    public static Result submit() {
        return ok();
    }

    private static User generateUserObject() {
        return new User();
    }

    private static void addUserToDB(User newUser) {
        //TODO
    }

    private static void sendRegConfirmationMail() {
        //TODO
    }
}