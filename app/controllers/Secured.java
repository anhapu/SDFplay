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
    
  
    
    /**
     * Checks if the current user is the owner of the given book.
     * @param book Book object that we want to check.
     * @return Returns true if the current user is the owner of the book otherwise false.
     */
    public static boolean isOwnerOfBook(final Book book)
    {
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
     * Checks if we have a current user. Users are allowed to add books to the system.
     * @return Returns true if there is an user in the session otherwise false.
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
