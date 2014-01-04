package utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import play.Logger;
import play.Play;
import controllers.Common;

public class Utils {

    private static final String AWS_ENDPOINT   = "ecs.amazonaws.com";

    // TODO Make sure that you updated your application.conf
    private static final String AWS_ACCESS_KEY = Play.application().configuration().getString(
                                                       "aws.key" );
    // TODO Make sure that you updated your application.conf
    private static final String AWS_SECRET_KEY = Play.application().configuration().getString(
                                                       "aws.secret" );

    /**
     * Generates an md5 hash of a String.
     *
     * @param input
     *            String value
     * @return Hashvalue of the String is null when the input is null.
     */
    public static String md5( String input ) {

        String md5 = null;
        if ( null == input ) {
            return null;
        }
        try {
            // Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance( "MD5" );
            // Update input string in message digest
            digest.update( input.getBytes(), 0, input.length() );
            // Converts message digest value in base 16 (hex)
            md5 = new BigInteger( 1, digest.digest() ).toString( 16 );

        }
        catch ( NoSuchAlgorithmException e ) {
            Logger.error(e.getMessage());
        }

        return md5;
    }

    /**
     * Tries to get informations for a book via aws.
     *
     * @param isbn
     *            The isbn of a book.
     * @return Returns an persisted book with the current user as owner or null.
     * @throws IOException
     */
    public static Book getBookInformationFromAWS( final String isbn ) {
        Logger.info( "Try to get book informations from aws with isbn: " + isbn );

        SignedRequestsHelper helper;
        Book book = null;
        try {
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
            params.put( "AssociateTag",  isbn  ); // This stuff is only if you
                                                // wanna
                                                // earn money with it.
            params.put( "ItemId", isbn );
            params.put( "ResponseGroup", "Large" );

            requestUrl = helper.sign( params );
            Logger.debug( "Signed Request is \"" + requestUrl + "\"" );

            book = fetchBook( requestUrl );

        }
        catch ( IllegalArgumentException e ) {
            Logger.error( e.getMessage() );
        }
        catch ( InvalidKeyException e ) {
            Logger.error( e.getMessage() );
        }
        catch ( NoSuchAlgorithmException e ) {
            Logger.error( e.getMessage() );
        }
        catch ( UnsupportedEncodingException e ) {
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
            Node isbnNode = doc.getElementsByTagName( "ISBN" ).item( 0 );
            Node dateNode = doc.getElementsByTagName( "PublicationDate" ).item( 0 );
            if(titleNode != null) {
                book.title = titleNode.getTextContent();
            }
            if(authorNode != null) {
                book.author = authorNode.getTextContent();
            }
            if(mediumImageUrl != null) {
                book.coverUrl = mediumImageUrl.getFirstChild().getTextContent();
            }
            book.owner = Common.currentUser();
            if(isbnNode != null) {
                book.isbn = isbnNode.getTextContent();
            }
            if(dateNode != null) {
                book.year = formatter.parse( dateNode.getTextContent() ).getTime();
            }
            book.tradeable = false;
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
