package controllers;


import play.mvc.*;
import play.mvc.Http.*;

import models.*;


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
    /**
     * Checks if the current user is the owner of the given book.
     * @param book Book which should be edit
     * @return returns true or false.
     */
    public static boolean isAllowedToEditBook(final Book book){
        final User user = Common.currentUser();
        if(user != null && user.id.equals( book.owner.id ))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Checks if we have a current user.
     * @return
     */
    public static boolean isAllowedToAddBook()
    {
        if(Common.currentUser() != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    
   
}
