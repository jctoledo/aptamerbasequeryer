/**
 * Copyright (c) 2013  Jose Cruz-Toledo
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

import static com.freebase.json.JSON.a;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

import com.freebase.json.JSON;

/**
 * @author  Jose Cruz-Toledo
 *
 */
public class FreebaseHelper {
	
	/**
	 * Return a JSON query that has the "return":"count" restriction on it
	 * 
	 * @param q
	 *            a JSON query that does not return number of results
	 * @return the modified JSON query
	 */
	public static JSON addResultCountToQuery(JSON q) {
		JSON returnMe = q;
		try {
			q.get(0).get("return").string();
		} catch (NullPointerException e) {
			JSON tmp = null;
			tmp = q.get(0);
			tmp.put("return", "count");
			JSON x = a();
			x.put(tmp);
			returnMe = x;
		} catch (RuntimeException e) {
		}
		return returnMe;
	}
	
	@SuppressWarnings("deprecation")
	public static HttpClient wrapClient(HttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
 
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }
 
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            X509HostnameVerifier verifier = new X509HostnameVerifier() {
 
                public void verify(String string, SSLSocket ssls) throws IOException {
                }
 
                public void verify(String string, X509Certificate xc) throws SSLException {
                }
 
                
                public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                }
 
                public boolean verify(String string, SSLSession ssls) {
                    return true;
                }

				
				
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(verifier);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
	
	/**
	 * Return a JSON query with the specified limit
	 * 
	 * @param aQuery
	 *            a JSON query to which a limit will be added if there is a
	 *            limit nothing will be done
	 * @param aLimit
	 *            the limit that you wish to add
	 * @return the modified JSON query
	 */
	public static JSON addLimitToQuery(JSON aQuery, int aLimit) {
		JSON returnMe = aQuery;
		try {
			aQuery.get(0).get("limit").string();
		} catch (NullPointerException e) {
			JSON penis = null;
			penis = aQuery.get(0);
			penis.put("limit", aLimit);
			JSON rm = a();
			returnMe = rm.put(penis);
		} catch (RuntimeException e) {
			returnMe = aQuery;
		}
		return returnMe;
	}
	
	public static int retrieveTotalNumberOfResults(JSON aQry) {
		int returnMe = 0;
		URLReader ur = null;
		String q =  aQry.stringify().replace("\\", "") ;
		String k = FreebaseCredentials.getKey();
		ur = new URLReader(FreebaseCredentials.getScheme(), FreebaseCredentials.getHost(), FreebaseCredentials.getPath(), q, k);
		JSON result = null;
		result = ur.getJSONContents();
		returnMe = Integer.parseInt(result.get("result").get(0).stringify());
		/*URLReader ur = null;
		String q = "query=" + aQry.stringify().replace("\\", "") + "&key="
				+ FreebaseCredentials.getKey();
		ur = new URLReader(FreebaseCredentials.getScheme(), FreebaseCredentials.getHost(), FreebaseCredentials.getPath(), q);
		JSON result = null;
		result = ur.getJSONContents();
		returnMe = Integer.parseInt(result.get("result").get(0).stringify());*/
		return returnMe;
		
	}
	
	public static JSON removeResultCountFromQuery(JSON q) {
		JSON rm = q;
		try {
			rm = q.get(0).del("return");
		} catch (RuntimeException e) {
			rm = q;
		}
		return q;
	}
}
