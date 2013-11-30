package controllers;

import controllers.Common;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import java.util.List;

@With(Common.class)
public class UserAdministrationController extends Controller {

     public static Result index() {
          List<User> users = null;
          users = User.findAll();
          return ok(views.html.userAdministration.render(users));
     }

}
