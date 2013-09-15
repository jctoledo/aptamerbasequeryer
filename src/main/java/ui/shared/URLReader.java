/**
 * Copyright (c) 2012 by Jose Cruz-Toledo
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ui.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.parser.ParseException;

import com.freebase.json.JSON;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * 
 * This class reads a URL and can initially return a string representation of
 * the contents of the passed in URL
 * 
 * @author Jose Cruz-Toledo
 * 
 */
public class URLReader {
	// get log4j handler
	// private static fi nal Logger logger = Logger.getLogger(URLReader.class);

	private URL url;

	String contents;
	public CookieStore cookieStore;
	public HttpContext localContext;

	public URLReader(String scheme, String host, String path, String query) {
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		// url = new URL(scheme+"://"+host+path+"/"+query);
		contents = this.getStringFromURLGET(scheme, host, path, query);
	}

	public URLReader(String scheme, String host, String path, String query,
			String key) {
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		contents = this.getStringFromPOST(scheme, host, path, query, key);
		// contents = this.getStringFromURLPOST(scheme, host, path, query, key);
	}

	private String getStringFromURLPOST(String scheme, String host,
			String path, String query, String key) {
		String rm = null;
		String uri = scheme + "://" + host + "/" + path;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		try {
			List<NameValuePair> nvp = new ArrayList<NameValuePair>(2);
			nvp.add(new BasicNameValuePair("query", query));
			nvp.add(new BasicNameValuePair("key", key));
			post.setEntity(new UrlEncodedFormEntity(nvp));
			HttpResponse resp = client.execute(post);
			HttpEntity entity = resp.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				String s = convertinputStreamToString(is);
				rm = s;
				// Do not need the rest
				post.abort();
				return rm;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rm;
	}

	private String getStringFromPOST(String scheme, String host, String path,
			String query, String key) {
		String rm = null;
		try {
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory httpRequestFactory = httpTransport
					.createRequestFactory();
			String data = "query=" + query + "&key=" + key;
			HttpContent content = new ByteArrayContent(
					"application/x-www-form-urlencoded", data.getBytes());
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/mqlread");
			HttpRequest request = httpRequestFactory.buildPostRequest(url,
					content);
			HttpHeaders headers = new HttpHeaders();
			headers.put("X-HTTP-Method-Override", "GET");
			request.setHeaders(headers);
			com.google.api.client.http.HttpResponse response = request
					.execute();
			InputStream is = response.getContent();
			if (is != null) {
				String s = convertinputStreamToString(is);
				rm = s;
				return rm;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rm;
	}

	private String getStringFromURLGET(String scheme, String host, String path,
			String query) {
		String returnMe;
		try {
			URI uri = new URI(scheme, host, path, query, null);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(uri);
			try {
				HttpResponse response = client.execute(get, localContext);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream is = entity.getContent();
					String s = convertinputStreamToString(is);
					returnMe = s;
					// Do not need the rest
					get.abort();
					return returnMe;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private DefaultHttpClient getSecuredHttpClient(HttpClient httpClient)
			throws Exception {
		final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {};
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				
				public X509Certificate[] getAcceptedIssuers() {
					return _AcceptedIssuers;
				}

				
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}
			};
			ctx.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory ssf = new SSLSocketFactory(ctx,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));
			return new DefaultHttpClient(ccm, httpClient.getParams());
		} catch (Exception e) {
			throw e;
		}
	}

	public String convertinputStreamToString(InputStream ists)
			throws IOException {

		if (ists != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader r1 = new BufferedReader(new InputStreamReader(
						ists, "UTF-8"));
				while ((line = r1.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				ists.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	/**
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @return A string representation of the contents of the URL
	 */
	public String getContents() {
		return contents;
	}

	public JSON getJSONContents() {
		JSON returnMe = null;
		if (contents.length() > 0) {
			try {
				returnMe = JSON.parse(contents);
				return returnMe;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return returnMe;
	}

	public JSONObject getJSON() {

		JSONObject rm = null;
		if (contents.length() > 0) {
			try {
				rm = new JSONObject(contents);
				return rm;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * @param contents
	 *            the contents to set
	 */
	public void setContents(String contents) {
		this.contents = contents;
	}

	@Override
	public String toString() {
		return "URLReader [url=" + url + ", contents=" + contents + "]";
	}

}
