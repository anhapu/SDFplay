package controllers;

import static play.data.Form.form;

import models.User;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.Http.Context;
import views.html.snippets.*;
import controllers.UserController.Login;

public class Common {
	
	public static class ContextIdent {
		public static String loginForm = "loginForm";
	}
	
	public static void addToContext(String ident, Object object) {
		Context.current().args.put(ident, object);
	}
	
	public static Object getFromContext(String ident) {
		return Context.current().args.get(ident);
	}
	
	public static void removeFromContext(String ident) {
		Context.current().args.remove(ident);
	}
	
	/**
	 * Just temporally !!!
	 * Should return a real User object 
	 * 
	 * @return
	 */
	public static String currentUser() {
		return (Context.current().session().get("email") == null) ? "" : Context.current().session().get("email");
	}
	
	@SuppressWarnings("unchecked")
	public static Html getLoginForm() {
		Form<Login> form = form(UserController.Login.class);
		if(Common.getFromContext(ContextIdent.loginForm) != null) {
			form = (Form<Login>)Common.getFromContext("loginForm");
		}
		return loginForm.render(form);
	}
	
}
