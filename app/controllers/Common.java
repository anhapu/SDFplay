package controllers;

import static play.data.Form.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

import models.Book;
import models.User;
import play.Logger;
import play.api.templates.Html;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;
import views.html.snippets.loginForm;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.UserController.Login;

public class Common extends Action.Simple {

    @Override
    public Promise<SimpleResult> call(Context ctx) throws Throwable {
        String userId = ctx.session().get("id");

        if (userId != null) {
            User user = User.findById(Long.parseLong(userId));
            if (user == null) {
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

    /**
     * Generates an md5 hash of a String.
     *
     * @param input
     *            String value
     * @return Hashvalue of the String.
     */
    public static String md5(String input) {

        String md5 = null;
        if (null == input)
            return null;

        try {
            // Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            // Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());
            // Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return md5;
    }
    
    /**
     * Reads the content of an url connection.
     * @param url
     * @return
     * @throws IOException
     */
    public static String getGoogleBooksContent( final String isbn ) throws IOException
    {
        Logger.info( "Try to get book informations from google books api with isbn: " + isbn );
        final String urlString =
                "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn
                        + "&key=AIzaSyBg5QemrpNGcpr3irrWDAuffakuI3DjD3I";
        Logger.info( "Search for book on " + urlString);
        try
        {
            final URL url = new URL( urlString );
            InputStream ioStream = url.openConnection().getInputStream();
            final StringBuilder sb = new StringBuilder();
            final BufferedReader reader = new BufferedReader( new InputStreamReader( ioStream ) );
            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                sb.append( line );
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> m = mapper.readValue(sb.toString(), Map.class);
            
            Map<String, Object> theBook = (Map<String, Object>)((List)m.get("items")).get(0);
            Map<String, Object> volume = (Map<String, Object>)theBook.get("volumeInfo");
            
         
            Logger.info(volume.get("title").toString());
            Logger.info(volume.get("authors").toString());
            Logger.info(volume.get("publisher").toString());
            
            for (Entry<String, Object> entry : m.entrySet()) {
				String key = entry.getKey();
				Logger.info(key);
			}
            
            //Ignore unknown fields
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            Book book = mapper.readValue( url.openConnection().getInputStream(), Book.class );
//            Logger.debug( "Title: " +book.title );
            return sb.toString();
        }
        catch ( MalformedURLException e )
        {
            Logger.error( e.getMessage() );
            throw new MalformedURLException();
        }
        catch ( IOException e )
        {
            Logger.error( e.getMessage() );
            throw new IOException();
        }
    }

}
