package controllers;

import models.User;
import play.mvc.Http.Context;

public class Common {
	
	public static String currentUser() {
		return Context.current().session().get("email");
	}
	
}
