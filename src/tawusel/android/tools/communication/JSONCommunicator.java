package tawusel.android.tools.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

/**
 * @author mareikeziese
 *
 */
public class JSONCommunicator {

	private static final String TAG = "JSONCommunicator";

	/**
	 * Get the json-result of the url
	 * 
	 * @param String
	 *            url
	 * @return JSONObject
	 * @throws Exception
	 */
	public static JSONArray getJSONArray(String method, String params,
			String serverUrl) throws Exception {
		String url = serverUrl + method + params;
		Log.i(TAG, url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response;
		JSONArray json = null;

		try {
			response = httpClient.execute(httpGet);
			Log.i(TAG, response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String httpResultContent = getResultContent(entity);
				Log.i(TAG, httpResultContent);
				json = new JSONArray(httpResultContent);
			}

			httpGet.abort();
		} catch (Exception e) {
			httpGet.abort();
			e.printStackTrace();
			throw e;
		}
		return json;
	}

	public static JSONArray parseJSArray(String arrayString)
			throws JSONException {
		JSONArray json = new JSONArray(arrayString);
		return json;
	}

	
	/**
	 * gets a JsonObject via HttpGet form the json-server
	 * 
	 * @param method
	 * @param params
	 * @param serverUrl
	 * @return JSONObject
	 * @throws Exception
	 */
	public static JSONObject getJSONObject(String method, String params,
			String serverUrl) throws Exception {
		String url = serverUrl + method + params;
		Log.i(TAG, url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response;
		JSONObject json = null;

		try {
			response = httpClient.execute(httpGet);
			Log.i(TAG, response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String httpResultContent = getResultContent(entity);
				Log.i(TAG, httpResultContent);
				json = new JSONObject(httpResultContent);
			}

			httpGet.abort();
		} catch (Exception e) {
			httpGet.abort();
			e.printStackTrace();
			throw e;
		}
		return json;
	}

	private static String getResultContent(HttpEntity entity)
			throws IllegalStateException, IOException {
		InputStream instream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null)
			sb.append(line + "n");
		instream.close();
		return sb.toString();

	}

	
	/**
	 * hashes a String with SHA1
	 * @param hstr
	 * @return String
	 */
	public static String getHashString(String hstr) {
		try {
			return SHA1(hstr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	
	/**
	 * convert a byte[] field to a String
	 * @param data
	 * @return String 
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	/**
	 * return the answer of the server if post was successful, else "couldn't connect to server"
	 * 
	 * @param url
	 * @param data
	 * @param objectName
	 * @return boolean
	 */
	@SuppressWarnings("finally")
	public static String postJSONObject(String url, JSONObject data) {
		String message="";
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost postMethod = new HttpPost(url);
		boolean success = false;

		try {
			HttpParams params = new BasicHttpParams();
			Iterator<String> keys = data.keys();
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			Log.i(TAG, "Post request, data: " + nameValuePairs.toString());
			while (keys.hasNext()) {
				String key = (String) keys.next();
				nameValuePairs.add(new BasicNameValuePair(key, data.getString(key)));
			}
		    postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		    HttpResponse response = httpClient.execute(postMethod);
		    HttpEntity entity = response.getEntity();
			success = true;
			message =EntityUtils.toString(entity);

		} catch (Exception e) {

			e.printStackTrace();
			message =  "couldn't connect to server";

		} finally {

			postMethod.abort();
			return message;

		}

	}
}