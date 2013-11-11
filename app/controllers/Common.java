package controllers;

import static play.data.Form.form;
import models.User;
import play.Logger;
import play.api.templates.Html;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;
import views.html.snippets.*;
import controllers.UserController.Login;

public class Common extends Action.Simple {
    
    @Override
    public Promise<SimpleResult> call(Context ctx) throws Throwable {
    	String userId = ctx.session().get("id");
    	
    	if(userId != null) {
    		User user = User.findById(Long.parseLong(userId));
    		if(user == null) {
    			ctx.session().clear();
    			return Promise.pure(redirect(routes.Application.index()));
    		} else {
    			Common.addToContext("user", user);
    		}	
    	} else {
    		Common.addToContext("user", null);
    	}
        return delegate.call(ctx);
    }
    
    public static class ContextIdent {
        public static String loginForm = "loginForm";
    }
    
    public static void addToContext(String ident, Object object) {
        Context.current().args.put(ident, object);
    }
    
    public static Object getFromContext(String ident) {
        return Context.current().args.get(ident);
    }
    
    
    public static User currentUser() {
    	return (User)Common.getFromContext("user");
    }
    
    @SuppressWarnings("unchecked")
    public static Html getLoginForm() {
        Form<Login> form = form(UserController.Login.class);
        if(Common.getFromContext(ContextIdent.loginForm) != null) {
            form = (Form<Login>)Common.getFromContext("loginForm");
        }
        return loginForm.render(form);
    }

    /**
     * Generates an md5 hash of a String.
     * @param input String value
     * @return Hashvalue of the String.
     */
    public static String md5(String input) {
        
        String md5 = null;
        if(null == input) return null;
        
            try {
            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            //Converts message digest value in base 16 (hex) 
            md5 = new BigInteger(1, digest.digest()).toString(16);
    
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            
        return md5;
    }
    
}
