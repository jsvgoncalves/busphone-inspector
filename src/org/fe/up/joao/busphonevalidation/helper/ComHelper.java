package org.fe.up.joao.busphonevalidation.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Contains facilities to connect to the REST server
 * @author joao
 *
 */
public class ComHelper{
	
	public static String serverURL = "http://busphone-service.herokuapp.com/";

	private static String readStream(InputStream is) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			int i = is.read();
			while(i != -1) {
				bo.write(i);
				i = is.read();
			}
			return bo.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		   NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		   return (networkInfo != null && networkInfo.isConnected());
		}
	
	/**
	 * Makes a POST call to the server.
	 * @param params 0-> Call Method = POST; 1-> URL; ... -> Params
	 * @return
	 */
	public static String httpPost(String... params) {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(params[1]);

	    try {
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        for (int i = 1; i+1 < params.length; i+=2) {
	        	nameValuePairs.add(new BasicNameValuePair(params[i], params[i+1]));
			}
	        if (!nameValuePairs.isEmpty()) {
	        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			}

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        System.out.println(response.toString());
	        return response.toString();
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
			return "Client Protocol Exception";
	    } catch (IOException e) {
	    	e.printStackTrace();
			return "POST: Bad Con";
	    }
	}

	/**
	 * Makes a GET call to the server.
	 * @return The serve response (expected is JSON)
	 */
	public static String httpGet(String urlStr) {
		try {
			URL url = new URL(urlStr);
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String r = readStream(in);
			urlConnection.disconnect();
			return r;
		} catch (MalformedURLException e){
			Log.v("MyLog", "GET: Bad URL");
			e.printStackTrace();
			return "GET: Bad URL";
		} catch (IOException e) {
			Log.v("MyLog", "GET: Bad Con");
			e.printStackTrace();
			return "GET: Bad Con";
		}
	}
	
	protected void onPostExecute(String result) {
        System.err.println(result);
    }
}
