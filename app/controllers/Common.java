package controllers;

import static play.data.Form.form;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Book;
import models.User;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import play.Logger;
import play.Play;
import play.api.templates.Html;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;
import utils.SignedRequestsHelper;
import views.html.snippets.loginForm;
import controllers.UserController.Login;

public class Common extends Action.Simple {

  
    @Override
    public Promise<SimpleResult> call(Context ctx) throws Throwable {
        String userId = ctx.session().get("id");

        if (userId != null) {
            User user = User.findById(Long.parseLong(userId));
            if (user == null) {
                ctx.session().clear();
                return Promise.pure(redirect(routes.Application.index(1)));
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
        return (User) Common.getFromContext("user");
    }

    @SuppressWarnings("unchecked")
    public static Html getLoginForm() {
        Form<Login> form = form(UserController.Login.class);
        if (Common.getFromContext(ContextIdent.loginForm) != null) {
            form = (Form<Login>) Common.getFromContext("loginForm");
        }
        return loginForm.render(form);
    }
}
