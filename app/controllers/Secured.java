package controllers;

import play.mvc.*;
import play.mvc.Http.*;

import models.*;
import models.enums.Roles;


public class Secured extends Security.Authenticator
{
    
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("id");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.index());
    }
    
    //Access Right methods
    public static boolean editBook(User user){
    	return true;
    }
   
		/**
		 * Defines who's allowed to see a profile.
		 * You can see your own profile and ADMINs can see a profile.
		 * @param user The user of the userprofile that should be shown.
		 * @return True if it's allowed to show the profile, otherwhise false. 
		 */
		public static boolean showUserProfile(User user) {
			boolean allowed = false;
			if (user.id == Common.currentUser().id || Roles.ADMIN == Common.currentUser().role) {
				allowed = true;
			}
			return allowed;
		}
}
