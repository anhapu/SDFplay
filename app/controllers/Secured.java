package controllers;


import play.mvc.*;
import play.mvc.Http.*;
import models.*;
import models.enums.Roles;
import models.enums.States;


public class Secured extends Security.Authenticator
{
    
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("id");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.index(1));
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
    
    /**
     * Checks if we have a current user. Users are allowed to add books to the system.
     * @return Returns true if there is an user in the session otherwise false.
     */
    public static boolean isAllowedToEditBook(final Book book)
    {
        if(Common.currentUser() != null && book.owner.id == Common.currentUser().id)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Defines who's allowed to see a profile.
     * You can see your own profile and ADMINs can see a profile.
     * @param user The user of the userprofile that should be shown.
     * @return True if it's allowed to show the profile, otherwhise false.
     */
    public static boolean showUserProfile(User user)
    {
         boolean allowed = false;
         if (user.id == Common.currentUser().id || Roles.ADMIN == Common.currentUser().role) {
              allowed = true;
         }
         return allowed;
    }
   
    /**
     * Defines who's allowed to edit a profile.
     * You can edit your own profile and ADMINs can edit any profile.
     * @param user The user of the userprofile that should be edited.
     * @return True if it's allowed to edit the profile, otherwhise false.
     */
    public static boolean editUserProfile(User user) {
         boolean allowed = false;
         if (user.id == Common.currentUser().id || Roles.ADMIN == Common.currentUser().role) {
              allowed = true;
         }
         return allowed;
    }
     
    /**
     * Defines who's allowed to acces the UserAdminInterface.
     * Currently it's only allowed for Admins.
     * @param user The user who wants access.
     * @return True if the access is allowed, otherwhise false.
     */
    public static boolean isAllowedToAccessUserAdminInterface(User user) {
         boolean allowed = false;
         if (Roles.ADMIN == user.role) {
              allowed = true;
         }
         return allowed;
    }
    
    /**
     * Determines if the current user is allowed to access a specific TradeTransaction
     * @param trade
     * @return
     */
    public static boolean viewTradeTransaction(TradeTransaction trade) {
    	User current = Common.currentUser();
    	if(trade.owner.equals(current) || 
    			trade.recipient.equals(current) || 
    			current.role == Roles.ADMIN) {
    		return true;
    	} else {
    		return false;
    	} 	
    }
    
    /**
     * Determines if the current user is allowed to delete a specific TradeTransaction
     * @param trade
     * @return
     */
    public static boolean deleteTradeTransaction(TradeTransaction trade) {    	
    	User current = Common.currentUser();
    	
    	if(current.equals(trade.owner) || current.equals(trade.recipient)) {
    		return true;
    	}
    	
    	return false;	
    }
    
    /**
     * Determines if the current user is allowed to response to a specific TradeTransaction
     * @param trade
     * @return
     */
    public static boolean responseTradeTransaction(TradeTransaction trade) {
    	User current = Common.currentUser();
    	if(trade.recipient.equals(current) && trade.state == States.INIT) {
    		return true;
    	} else {
    		return false;
    	} 	
    }
    
    /**
     * Determines if the current user is allowed to approve to a specific TradeTransaction
     * @param trade
     * @return
     */
    public static boolean approveTradeTransaction(TradeTransaction trade) {
    	User current = Common.currentUser();
    	if(trade.owner.equals(current) && trade.state == States.RESPONSE) {
    		return true;
    	} else {
    		return false;
    	} 	
    }
        
}
