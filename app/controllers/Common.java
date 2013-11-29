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
import play.api.templates.Html;
import play.data.Form;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;
import views.html.snippets.loginForm;
import controllers.UserController.Login;

public class Common extends Action.Simple
{

    private static final String AWS_ENDPOINT   = "ecs.amazonaws.com";
    
    //TODO Replace this with your AWS_KEY
    private static final String AWS_ACCESS_KEY = "";
    //TODO Replace this with your AWS_SECRET
    private static final String AWS_SECRET_KEY = "";


    @Override
    public Promise< SimpleResult > call( Context ctx ) throws Throwable
    {
        String userId = ctx.session().get( "id" );

        if ( userId != null )
        {
            User user = User.findById( Long.parseLong( userId ) );
            if ( user == null )
            {
                ctx.session().clear();
                return Promise.pure( redirect( routes.Application.index() ) );
            }
            else
            {
                Common.addToContext( "user", user );
            }
        }
        else
        {
            Common.addToContext( "user", null );
        }
        return delegate.call( ctx );
    }

    public static class ContextIdent
    {
        public static String loginForm = "loginForm";
    }

    public static void addToContext( String ident, Object object )
    {
        Context.current().args.put( ident, object );
    }

    public static Object getFromContext( String ident )
    {
        return Context.current().args.get( ident );
    }

    public static User currentUser()
    {
        return ( User ) Common.getFromContext( "user" );
    }

    @SuppressWarnings( "unchecked" )
    public static Html getLoginForm()
    {
        Form< Login > form = form( UserController.Login.class );
        if ( Common.getFromContext( ContextIdent.loginForm ) != null )
        {
            form = ( Form< Login > ) Common.getFromContext( "loginForm" );
        }
        return loginForm.render( form );
    }

    /**
     * Generates an md5 hash of a String.
     *
     * @param input
     *            String value
     * @return Hashvalue of the String.
     */
    public static String md5( String input )
    {

        String md5 = null;
        if ( null == input )
            return null;

        try
        {
            // Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance( "MD5" );
            // Update input string in message digest
            digest.update( input.getBytes(), 0, input.length() );
            // Converts message digest value in base 16 (hex)
            md5 = new BigInteger( 1, digest.digest() ).toString( 16 );

        }
        catch ( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }

        return md5;
    }

    /**
     * Tries to get informations for a book via aws.
     *
     * @param isbn The isbn of a book.
     * @return Returns an persisted book with the current user as owner or null.
     * @throws IOException
     */
    public static Book getGoogleBooksContent( final String isbn ) throws IOException
    {
        Logger.info( "Try to get book informations from aws with isbn: " + isbn );

        SignedRequestsHelper helper;
        Book book = null;
        try
        {
            helper =
                    SignedRequestsHelper.getInstance( AWS_ENDPOINT, AWS_ACCESS_KEY, AWS_SECRET_KEY );
            String requestUrl = null;
            Logger.debug( "Map form example:" );
            Map< String, String > params = new HashMap< String, String >();
            params.put( "Service", "AWSECommerceService" );
            params.put( "Version", "2009-03-31" );
            params.put( "Operation", "ItemLookup" );
            params.put( "SearchIndex", "Books" );
            params.put( "IdType", "ISBN" );
            params.put( "AssociateTag", isbn); // This stuff is only if you wanna earn money with it.
            params.put( "ItemId", isbn );
            params.put( "ResponseGroup", "Large" );

            requestUrl = helper.sign( params );
            Logger.debug( "Signed Request is \"" + requestUrl + "\"" );

            book = fetchBook( requestUrl );

        }
        catch ( IllegalArgumentException e )
        {
            Logger.error( e.getMessage() );
        }
        catch ( InvalidKeyException e )
        {
            Logger.error( e.getMessage() );
        }
        catch ( NoSuchAlgorithmException e )
        {
            Logger.error( e.getMessage() );
        }

        return book;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * title, author, isbn, imageurl and pubdate from the XML.
     */
    private static Book fetchBook( String requestUrl )
    {
        Book book = null;
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
        try
        {
            book = new Book();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse( requestUrl );
            Node titleNode = doc.getElementsByTagName( "Title" ).item( 0 );
            Node authorNode = doc.getElementsByTagName( "Author" ).item( 0 );
            Node mediumImageUrl = doc.getElementsByTagName( "MediumImage" ).item( 0 );
            book.title = titleNode.getTextContent();
            book.author = authorNode.getTextContent();
            book.coverUrl = mediumImageUrl.getFirstChild().getTextContent();
            book.owner = currentUser();
            book.isbn = doc.getElementsByTagName( "ISBN" ).item( 0 ).getTextContent();
            book.year =
                    formatter.parse(
                            doc.getElementsByTagName( "PublicationDate" ).item( 0 )
                                    .getTextContent() ).getTime();
            book.tradeable = false;
            book.save();
        }
        catch ( DOMException e )
        {
            Logger.error( e.getMessage() );
            throw new RuntimeException( e );
        }
        catch ( ParserConfigurationException e )
        {
            Logger.error( e.getMessage() );
        }
        catch ( SAXException e )
        {
            Logger.error( e.getMessage() );
        }
        catch ( IOException e )
        {
            Logger.error( e.getMessage() );
        }
        catch ( ParseException e )
        {
            Logger.error( e.getMessage() );
        }
        return book;
    }
}
