package controllers;


import java.util.List;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.With;
import views.html.index;
import views.html.error;

@With(Common.class)
public class Application extends Controller {

	/**
	 * Start Page, where a lot of showcases of other users are presented
	 * 
	 * @return
	 */

    public static Result index() {
    	List<User> users = null;
    	if(Common.currentUser() != null){
    		users = User.findAllBut(Common.currentUser());
    	} else {
    		users = User.findAll();
    	}
        return ok(index.render(users));
    }
    
    public static Result error() {
    	return badRequest(error.render());
    }

}
