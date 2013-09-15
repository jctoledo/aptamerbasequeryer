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
package ui.server;

import static com.freebase.json.JSON.a;
import static com.freebase.json.JSON.o;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ui.shared.FreebaseCredentials;
import ui.shared.URLReader;

import com.freebase.json.JSON;

/**
 * @author  Jose Cruz-Toledo
 *
 */
public class GetEnumerations extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3026738853775701816L;
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp){
		resp.setContentType("text/json");
		PrintWriter out = null;
		try{
			out = resp.getWriter();
		}catch(IOException e){
			e.printStackTrace();
		}
		Map<String, String[]> requestMap = new HashMap<String, String[]>();
		requestMap = req.getParameterMap();
		String [] requests = null;
		JSONObject o = new JSONObject();
		if(requestMap.containsKey("q")){
			requests = requestMap.get("q");
			if(requests.length != 0){
				for(String aReq: requests){
					JSON q = null;
					String sig = null;
					if(aReq.equalsIgnoreCase("partitioningMethods")){
						q = a(o("mid", null, "name", null, "type",
								"/base/aptamer/separation_methods"));
						sig = "partitioningMethods";
					}else if(aReq.equalsIgnoreCase("affinityMethods")){
						q = a(o("mid", null, "name", null, "type",
								"/base/aptamer/affinity_method"));
						sig = "affinityMethods";
					}else if(aReq.equalsIgnoreCase("bufferingAgents")){
						q = a(o("mid", null, "name", null, "type",
									"/base/aptamer/buffering_agent"));
						sig = "bufferingAgents";
					}else if(aReq.equalsIgnoreCase("recoveryMethods")){
						q = a(o(
								"mid", null,
								"name", null,
								"type", "/base/aptamer/recovery_methods"
							));
						sig = "recoveryMethods";
					}else if(aReq.equalsIgnoreCase("selexMethods")){
						q = a(o(
								"mid", null,
								"name", null,
								"type", "/base/aptamer/selex_method"
								));
						sig = "selexMethods";
					}else if(aReq.equalsIgnoreCase("secondaryStructures")){
						q = a(o("mid", null, "name", null, "type",
								"/base/aptamer/nucleic_acid_secondary_structure"));
						sig = "secondaryStructures";
						
					}
					if(q ==null){
						break;
					}else{
						String query = q.stringify().replace("\\", "");
						URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
								query, FreebaseCredentials.getKey());
						JSON resComplete = ur.getJSONContents();
						JSON result = resComplete.get("result");
						JSONArray sms = new JSONArray();
						//iterate over results
						int currResult = 0;
						boolean lf = true;
						while(lf){
							try{
								JSONObject sm = new JSONObject();
								JSON aR = result.get(currResult);
								String name = aR.get("name").stringify().replace("\\", "").replace("\"", "");
								String mid = aR.get("mid").stringify().replace("\\", "").replace("\"", "");
								sm.put("mid", mid);
								sm.put("name", name);
								sms.put(sm);
								currResult++;
							}catch(NullPointerException e){
								lf = false;
							}catch (IndexOutOfBoundsException e){
								lf = false;
							} catch (JSONException e) {
							}
						}
						try{
							o.put(sig, sms);
						}catch(JSONException e){
							e.printStackTrace();
						}
					}
				}
				out.println(o);
			}
		}
		/*String[] incomming = req.getParameter("q").split(" ");
		if(incomming.length != 0){
			for(String aVal: incomming){
				JSON q = a(o("mid", null, "name", null, "type",
						"/base/aptamer/separation_methods"));
				String query = q.stringify().replace("\\", "");
				URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
						FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
						query, FreebaseCredentials.getKey());
				JSON resComplete = ur.getJSONContents();
				JSON result = resComplete.get("result");
				JSONArray pms = new JSONArray();
				//iterate over results
				int currResult = 0;
				boolean lf = true;
				while(lf){
					try{
						JSONObject pm = new JSONObject();
						JSON aR = result.get(currResult);
						String name = aR.get("name").stringify().replace("\\", "").replace("\"", "");
						String mid = aR.get("mid").stringify().replace("\\", "").replace("\"", "");
						pm.put("mid", mid);
						pm.put("name", name);
						pms.put(pm);
						currResult++;
					}catch(NullPointerException e){
						lf = false;
					}catch (IndexOutOfBoundsException e){
						lf = false;
					} catch (JSONException e) {
					}
				}//while
				//now put the json array in the output
				out.println(pms);
				
			}
		}//if*/
	}

}
