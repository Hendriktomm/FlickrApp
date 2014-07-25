package com.example.het.photogallery;

import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by HET on 25.7.2014.
 */
public class FlickrFetchr {
    public static final String TAG = "FlickrFetchr";

    private static final String ENDPOINT = "http://api.flickr.com/sercices/rest/";
    private static final String API_kEY = "yourApiKeyHere";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String PARAM_EXTRAS = "extras";

    private static final String EXTRA_SMALL_URL = "url_s";

    private static final String XML_PHOTO ="photo";




    byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer))>0){
                out.write(buffer,0 ,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally{
            connection.disconnect();;
        }

    }

    public String getUrl(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
//https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=68cabd8e85710436c7016fa72dfbddaf&extras=url_s&format=rest&api_sig=5310dc7d33ef1b4d6d224b448f13406e
    public ArrayList<GalleryItem> fetchItems(){
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try{
            String url = Uri.parse("https://api.flickr.com/services/rest/").buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", "68cabd8e85710436c7016fa72dfbddaf")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("format", "rest")
                    .appendQueryParameter("api_sig", "5310dc7d33ef1b4d6d224b448f13406e")
                    .build().toString();
                    //Uri.parse(ENDPOINT).buildUpon().appendQueryParameter("method", METHOD_GET_RECENT).appendQueryParameter("api_key",API_kEY).appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URL).build().toString();
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: "+ xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));

            parseItems(items, parser);


        }catch(IOException ioe){
            Log.e(TAG,"Failed to fetch items", ioe);

        }
        catch(XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items", xppe);
        }
        return items;

    }

    void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser)
        throws XmlPullParserException, IOException{
        int eventType= parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null,"title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setmId(id);
                item.setmCaption(caption);
                item.setmUrl(smallUrl);
                items.add(item);
            }
            eventType = parser.next();

        }
    }
}
