package controllers;

import play.mvc.*;
import play.mvc.Http.*;

import models.*;


public class Secured extends Security.Authenticator
{
    
    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("email");
    }
    
    @Override
    public Result onUnauthorized(Context ctx) {
        
        //TODO Add a login view and rout so this methode is working!!!
        return null;
        //return redirect(routes.Application.login());
    }
    
    
    //Access Right methods here!!!
}
