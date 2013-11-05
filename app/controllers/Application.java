package controllers;

import play.*;
import static play.data.Form.*;
import play.mvc.*;
import views.html.*;

@With(Common.class)
public class Application extends Controller {

    public static Result index() {
        return ok(index.render(form(UserController.Login.class)));
    }

}
