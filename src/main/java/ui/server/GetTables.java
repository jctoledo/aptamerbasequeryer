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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ui.shared.Coordinate;
import ui.shared.FreebaseCredentials;
import ui.shared.PubmedInfo;
import ui.shared.StatUtils;
import ui.shared.URLReader;


/**
 * @author Jose Cruz-Toledo
 * 
 */
public class GetTables extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4831790898593738979L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();
		Map<String, String[]> requestMap = new HashMap<String, String[]>();
		requestMap = req.getParameterMap();
		String gn = "gn"; // the name of the desired graph output
		String at = "at"; // the type of aptamer desired
		String l = "log"; // set to true to use the logbase10 for kd values;
		String d = "desc"; // use this to get a description of the graph
		String b = "best"; // set to true to only retrieve coordinates that
							// display the lowest kd
		String f = "logErrors"; // set to true to show the errors
		String u = "url"; // set to true to add as the last colum the url of the
							// topic
		boolean url = getBooleanFlag(u, requestMap);
		boolean desc = getBooleanFlag(d, requestMap);
		boolean log = getBooleanFlag(l, requestMap);
		boolean best = getBooleanFlag(b, requestMap);
		String aptamerType = getAptamerType(at, requestMap);
		String graphname = getGraphName(gn, requestMap);
		if (graphname != null) {
			if (graphname.equalsIgnoreCase("lengthvskd")) {
				// check if description is requested
				if (desc == true) {
					out.println("The graph KD (Y axis) vs average sequence length (X axis). Aptamer types can be selected (DNA or RNA) using the at parameter.");
				} else {
					List<Coordinate<Double, Double, Double>> coords = this
							.makeKdVsLength(aptamerType, log, best);
					Double seq_var = StatUtils.computeVariance(coords,
							"sequence");
					Double kd_var = StatUtils.computeVariance(coords, "kd");
					// now check the outputformat
					String rm = "";
					if (url) {
						rm += "Average sequence length, KD, Interaction topic URL\n";
					} else {
						rm += "Average sequence length, KD\n";
					}
					for (Iterator<Coordinate<Double, Double, Double>> iterator = coords
							.iterator(); iterator.hasNext();) {
						Coordinate<Double, Double, Double> coordinate = iterator
								.next();
						Double x = coordinate.getX();
						Double y = coordinate.getY();
						if (url == false) {
							rm += x + "," + y + "\n";
						} else {
							rm += x + "," + y + ", http://freebase.com"
									+ coordinate.getMid() + "\n";
						}
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvstt")) {
				if (desc == true) {
					out.println("The graph target type frequency vs year of publication");
				} else {
					// TODO: finishme
					this.makeTTFreqVsYear(aptamerType);
				}
			} else if (graphname.equalsIgnoreCase("yearvskd")) {
				if (desc == true) {
					out.println("A graph of year vs average kd. Best KD can also be selected.");
				} else {
					List<Coordinate<Integer, Double, ?>> coords = this
							.makeyearvsKd(aptamerType, log, best);
					String rm = "";
					if (url) {
						rm += "Year, KD, Interaction Topic URL\n";
					} else {
						rm += "Year, KD\n";
					}
					for (Coordinate<Integer, Double, ?> coordinate : coords) {
						Integer x = coordinate.getX();
						Double y = coordinate.getY();
						if (url == false) {
							rm += x + "," + y + "\n";
						} else {
							rm += x + "," + y + ",http://freebase.com"
									+ coordinate.getMid() + "\n";
						}
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsselexmethods")) {
				if (desc == true) {
					out.println("A graph of year vs selex methods used");
				} else {
					String rm = "Year, Selex Method, Count";
					List<Coordinate<Integer, String, Integer>> coords = this
							.makeyearVsSelexMethods(aptamerType);
					for (Coordinate<Integer, String, Integer> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY() + ","
								+ coordinate.getZ() + "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvspartitioningmethods")) {
				if (desc == true) {
					out.println("A graph of year vs partitioning method used");
				} else {
					String rm = "Year, Partitioning Method, Count\n";
					List<Coordinate<Integer, String, Integer>> coords = this
							.makeYearVsPartitioningMethods(aptamerType);
					for (Coordinate<Integer, String, Integer> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY() + ","
								+ coordinate.getZ() + "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsrecoverymethods")) {
				if (desc) {
					out.println("A graph of year vs recovery method used");
				} else {
					String rm = "Year, Separation Method, Count \n";
					List<Coordinate<Integer, String, Integer>> coords = this
							.makeYearVsRecoveryMethods(aptamerType);
					for (Coordinate<Integer, String, Integer> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY() + ","
								+ coordinate.getZ() + "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsaptamerlength")) {
				if (desc) {
					out.println("A graph of year vs average aptamer length");
				} else {
					String rm = "Year, Average Aptamer Length\n";
					List<Coordinate<Integer, Double, ?>> coords = this
							.makeYearVsAptamerLength(aptamerType);
					for (Coordinate<Integer, Double, ?> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsgccontent")) {
				if (desc) {
					out.println("A graph of year vs average aptamer gc content");
				} else {
					String rm = "Year, Average GC content\n";
					List<Coordinate<Integer, Double, ?>> coords = this
							.makeYearVsAptamerGcContent(aptamerType);
					for (Coordinate<Integer, Double, ?> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsnumofrounds")) {
				if (desc) {
					out.println("A graph of year vs the average number of rounds used in every selex experiment");
				} else {
					String rm = "Year, average number of rounds\n";
					List<Coordinate<Integer, Double, ?>> coords = this
							.makeYearVsNumOfRounds(aptamerType);
					for (Coordinate<Integer, Double, ?> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvstemplatelength")) {
				if (desc) {
					out.println("A graph of year vs the average length of the template");
				} else {
					String rm = "Year, average template length\n";
					List<Coordinate<Integer, Double, ?>> coords = this
							.makeYearVsTemplateLength(aptamerType);
					for (Coordinate<Integer, Double, ?> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsaptamertype")) {
				if (desc) {
					out.println("A graph of year vs the proportion of each type of aptamer");
				} else {
					String rm = "Year, DNA proportion, RNA proportion\n";
					List<Coordinate<Integer, Double, Double>> coords = this
							.makeYearvsAptamerType();
					for (Coordinate<Integer, Double, Double> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY() + ","
								+ coordinate.getZ() + "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsph")) {
				if (desc) {
					out.println("A graph of year vs the average ph used in a selection solution");
				} else {
					String rm = "Year, Average pH\n";
					List<Coordinate<Integer, Double, Double>> coords = this
							.makeYearVsPh(aptamerType);
					for (Coordinate<Integer, Double, Double> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			} else if (graphname.equalsIgnoreCase("yearvsbuffer")) {
				if (desc) {
					out.println("A graph of year vs a count of the buffering agents used for the selection solution");
				} else {
					String rm = "Year, Buffering agent, Count\n";
					List<Coordinate<Integer, String, Integer>> coords = this
							.makeYearVsBufferingAgent(aptamerType);
					for (Coordinate<Integer, String, Integer> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY() + ","
								+ coordinate.getZ() + "\n";
					}
					out.println(rm);
				}
			} else if (graphname
					.equalsIgnoreCase("aptamerLengthvsminimalLength")) {
				if (desc) {
					out.println("A graph of aptamer length vs its minimal aptamer length");
				} else {
					String rm = "Aptamer Length, Corresponding average Minimal Aptamer Length\n";
					List<Coordinate<Integer, Double, Double>> coords = this
							.makeAptamerLengthVsMinimalAptamerLength(aptamerType);
					for (Coordinate<Integer, Double, Double> coordinate : coords) {
						rm += coordinate.getX() + "," + coordinate.getY()
								+ "\n";
					}
					out.println(rm);
				}
			}

		} else {
			throw new IOException("invalid graphname selected!");
		}
	}

	// TODO: deal with the selex experiments that do not have PMIDs in them
	/**
	 * 
	 * @param anAptamerType
	 * @return a coordinate with the following parts : x->(Integer) year, y->
	 *         (Double)frequency, z->(String)term
	 */
	private List<Coordinate<Integer, Double, String>> makeTTFreqVsYear(
			String anAptamerType) {
		List<Coordinate<Integer, Double, String>> rm = new ArrayList<Coordinate<Integer, Double, String>>();

		String q = null;
		q = makeTTfreqQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						JSONObject jo = r.getJSONObject(i);
						// get the value of the pmid
						JSONArray pmids = jo
								.getJSONArray("/base/aptamer/experimental_outcome/is_outcome_of");
						JSONObject aPmidObj = pmids.getJSONObject(0);
						String aPmid = null;
						int year = -1;
						String tt = null;
						try {
							aPmid = aPmidObj.getJSONArray(
									"/base/aptamer/experiment/pubmed_id")
									.getString(0);
						} catch (JSONException e) {
							// TODO: deal when no pmid is found
							continue r;
						}
						if (aPmid != null && aPmid.length() > 0) {
							// get the year
							PubmedInfo pi = new PubmedInfo(aPmid);
							int y = pi.getPublicationYear();
							// check if positive
							if (y > 0) {
								year = y;
							}
							// now get the target type
							List<String> target_types = new ArrayList<String>();

							JSONArray ints = jo
									.getJSONArray("/base/aptamer/interaction/has_participant");
							for (int j = 0; j < ints.length(); j++) {
								JSONObject anInt = ints.getJSONObject(j);
								String aTt = null;
								try {
									aTt = anInt
											.getJSONArray(
													"/base/aptamer/aptamer_target/has_type")
											.getString(0);
									target_types.add(aTt);

								} catch (JSONException e) {
									System.out.println("here");
									continue r;
								}
							}
							System.out.println(target_types);
						} else {
							continue r;
						}

					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeTTfreqQuery(anAptamerType) + "&cursor="
								+ cursorVal;
					}
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rm;
	}

	private List<Coordinate<Integer, Double, ?>> makeYearVsAptamerGcContent(
			String anAptamerType) {
		List<Coordinate<Integer, Double, ?>> rm = new ArrayList<Coordinate<Integer, Double, ?>>();
		String q = null;
		q = makeYearVsAptamerLengthQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		Map<Integer, Double> year_to_gc_content_map = new HashMap<Integer, Double>();
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						try {
							JSONObject ja = r.getJSONObject(i);
							// get the pmid
							String pmid = null;
							int year = -1;
							try {
								JSONArray pmid_arr = ja
										.getJSONArray("/base/aptamer/experimental_outcome/is_outcome_of");
								JSONObject aPmid = pmid_arr.getJSONObject(0);
								pmid = aPmid.getJSONArray(
										"/base/aptamer/experiment/pubmed_id")
										.getString(0);
								if (pmid != null && pmid.length() > 0) {
									if (!pubmeds.containsKey(pmid)) {
										// get the year
										PubmedInfo pi = new PubmedInfo(pmid);
										year = pi.getPublicationYear();
										pubmeds.put(pmid, pi);
									} else {
										year = pubmeds.get(pmid)
												.getPublicationYear();
									}
								}
							} catch (JSONException e) {
								continue r;
							}
							// get the average sequence length
							JSONArray parts = ja
									.getJSONArray("/base/aptamer/interaction/has_participant");
							List<Double> seq_gc_contents = new ArrayList<Double>();
							for (int w = 0; w < parts.length(); w++) {
								JSONObject aPart = parts.getJSONObject(w);
								String aSeq = aPart
										.getString("/base/aptamer/linear_polymer/sequence");
								if (aSeq.length() > 0) {
									seq_gc_contents
											.add(this.getGCContent(aSeq));
								}
							}
							Double avg_gc_content = StatUtils
									.computeAverage(seq_gc_contents);
							if (year > 0 && avg_gc_content > 0.0) {
								if (year_to_gc_content_map.containsKey(year)) {
									Double anAvg = year_to_gc_content_map
											.get(year);
									year_to_gc_content_map.put(year,
											(anAvg + avg_gc_content) / 2);
								} else {
									// year has never been seen
									year_to_gc_content_map.put(year,
											avg_gc_content);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeYearVsAptamerLengthQuery(anAptamerType)
								+ "&cursor=" + cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// now iterate over the count map
		for (Map.Entry<Integer, Double> entry : year_to_gc_content_map
				.entrySet()) {
			int year = entry.getKey();
			Double value = entry.getValue();
			// create a coordinate object
			Coordinate<Integer, Double, ?> c = new Coordinate<Integer, Double, Object>(
					year, value, "");
			rm.add(c);
		}
		return rm;
	}

	private List<Coordinate<Integer, Double, ?>> makeYearVsAptamerLength(
			String anAptamerType) {
		List<Coordinate<Integer, Double, ?>> rm = new ArrayList<Coordinate<Integer, Double, ?>>();
		String q = null;
		q = makeYearVsAptamerLengthQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		Map<Integer, Double> year_to_length_map = new HashMap<Integer, Double>();
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						try {
							JSONObject ja = r.getJSONObject(i);
							// get the pmid
							String pmid = null;
							int year = -1;
							try {
								JSONArray pmid_arr = ja
										.getJSONArray("/base/aptamer/experimental_outcome/is_outcome_of");
								JSONObject aPmid = pmid_arr.getJSONObject(0);
								pmid = aPmid.getJSONArray(
										"/base/aptamer/experiment/pubmed_id")
										.getString(0);
								if (pmid != null && pmid.length() > 0) {
									if (!pubmeds.containsKey(pmid)) {
										// get the year
										PubmedInfo pi = new PubmedInfo(pmid);
										year = pi.getPublicationYear();
										pubmeds.put(pmid, pi);
									} else {
										year = pubmeds.get(pmid)
												.getPublicationYear();
									}
								}
							} catch (JSONException e) {
								continue r;
							}
							// get the average sequence length
							JSONArray parts = ja
									.getJSONArray("/base/aptamer/interaction/has_participant");
							List<Double> seq_lenghts = new ArrayList<Double>();
							for (int w = 0; w < parts.length(); w++) {
								JSONObject aPart = parts.getJSONObject(w);
								String aSeq = aPart
										.getString("/base/aptamer/linear_polymer/sequence");
								if (aSeq.length() > 0) {
									seq_lenghts.add((double) aSeq.length());
								}
							}
							Double avg_length = StatUtils
									.computeAverage(seq_lenghts);
							if (year > 0 && avg_length > 0.0) {
								if (year_to_length_map.containsKey(year)) {
									Double anAvg = year_to_length_map.get(year);
									year_to_length_map.put(year,
											(anAvg + avg_length) / 2);
								} else {
									// year has never been seen
									year_to_length_map.put(year, avg_length);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeYearVsAptamerLengthQuery(anAptamerType)
								+ "&cursor=" + cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// now iterate over the count map
		for (Map.Entry<Integer, Double> entry : year_to_length_map.entrySet()) {
			int year = entry.getKey();
			Double value = entry.getValue();

			// create a coordinate object
			Coordinate<Integer, Double, ?> c = new Coordinate<Integer, Double, Object>(
					year, value, "");
			rm.add(c);
		}
		return rm;
	}

	private List<Coordinate<Integer, Double, Double>> makeAptamerLengthVsMinimalAptamerLength(
			String anAptamerType) {
		List<Coordinate<Integer, Double, Double>> rm = new ArrayList<Coordinate<Integer, Double, Double>>();
		String q = makeAptamerLengthVsMinimalAptamerLengthQuery(anAptamerType)
				+ "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the aptamer length
					try {
						String seq = ja
								.getString("/base/aptamer/linear_polymer/sequence");
						if (seq.length() > 0) {
							JSONArray minArr = ja
									.getJSONArray("/base/aptamer/aptamer/has_minimal_aptamer");
							List<Double> minimalLengths = new ArrayList<Double>();
							p: for (int j = 0; j < minArr.length(); j++) {
								JSONObject aminApt = minArr.getJSONObject(j);
								try {
									String min_seq = aminApt.getString("/base/aptamer/linear_polymer/sequence");
									if (min_seq.length() > 0) {
										//remove the double quotes
										min_seq.replace("\"", "");
										Pattern p = Pattern.compile("[ACGTRUYKMSWBDHVNX]+");
										Matcher m = p.matcher(min_seq);
										if (m.matches()) {
											minimalLengths.add((double)min_seq.length());
										}else{
											continue p;
										}
									} else {
										continue p;
									}
								} catch (JSONException e2) {
									continue p;
								}
							}
							//create a coordinate
							if(minimalLengths.size() >0){
								Double avg = StatUtils.computeAverage(minimalLengths);
								Coordinate<Integer, Double, Double> c = new Coordinate<Integer, Double, Double>(seq.length(), avg, null);
								rm.add(c);
							}
						} else {
							continue r;
						}
					} catch (JSONException e) {
						continue r;
					}
				}
				// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeAptamerLengthVsMinimalAptamerLengthQuery(anAptamerType)
							+ "&cursor=" + cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return rm;
	}

	private List<Coordinate<Integer, String, Integer>> makeYearVsBufferingAgent(
			String anAptamerType) {
		List<Coordinate<Integer, String, Integer>> rm = new ArrayList<Coordinate<Integer, String, Integer>>();
		String q = makeYearVsBufferQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		Map<Integer, Map<String, Integer>> year_count = new HashMap<Integer, Map<String, Integer>>();
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the pmid
					String pmid = null;
					int year = -1;
					try {
						pmid = ja.getJSONArray(
								"/base/aptamer/experiment/pubmed_id")
								.getString(0);
						if (pmid != null && pmid.length() > 0) {
							if (!pubmeds.containsKey(pmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(pmid);
								year = pi.getPublicationYear();
								pubmeds.put(pmid, pi);
							} else {
								year = pubmeds.get(pmid).getPublicationYear();
							}
						}
					} catch (JSONException e) {
						continue r;
					}
					// get the buffering agents
					try {
						List<String> bufList = new ArrayList<String>();
						JSONArray ec = ja
								.getJSONArray("/base/aptamer/experiment/has_experimetal_conditions");
						for (int j = 0; j < ec.length(); j++) {
							JSONObject anExpCond = ec.getJSONObject(j);
							JSONArray ss = anExpCond
									.getJSONArray("/base/aptamer/selex_conditions/has_selection_solution");
							for (int k = 0; k < ss.length(); k++) {
								JSONObject aSS = ss.getJSONObject(k);
								JSONArray bufs = aSS
										.getJSONArray("/base/aptamer/selection_solution/has_buffering_agent");
								for (int l = 0; l < bufs.length(); l++) {
									String aba = bufs.getString(l);
									bufList.add(aba);
								}
							}
						}
						if (year != -1 && bufList.size() > 0) {
							if (year_count.containsKey(year)) {
								Map<String, Integer> conts = year_count
										.get(year);
								for (String aba : bufList) {
									if (conts.containsKey(aba)) {
										int count = conts.get(aba) + 1;
										conts.put(aba, count);
										year_count.put(year, conts);
									} else {
										conts.put(aba, 1);
										year_count.put(year, conts);
									}
								}
							} else {
								Map<String, Integer> conts = new HashMap<String, Integer>();
								for (String aba : bufList) {
									conts.put(aba, 1);
								}
								year_count.put(year, conts);
							}
						}
					} catch (JSONException e) {
						continue r;
					}

				}// for
					// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeYearVsBufferQuery(anAptamerType) + "&cursor="
							+ cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}
			}// while
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Map.Entry<Integer, Map<String, Integer>> entry : year_count
				.entrySet()) {
			Integer y = entry.getKey();
			Map<String, Integer> conts = entry.getValue();
			for (Map.Entry<String, Integer> en : conts.entrySet()) {
				Coordinate<Integer, String, Integer> c = new Coordinate<Integer, String, Integer>(
						y, en.getKey(), en.getValue());
				rm.add(c);
			}
		}
		return rm;
	}

	private List<Coordinate<Integer, Double, Double>> makeYearVsPh(
			String anAptamerType) {
		List<Coordinate<Integer, Double, Double>> rm = new ArrayList<Coordinate<Integer, Double, Double>>();
		String q = makeYearVsPhQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		Map<Integer, List<Double>> year_avg = new HashMap<Integer, List<Double>>();
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the pmid
					String pmid = null;
					int year = -1;
					try {
						pmid = ja.getJSONArray(
								"/base/aptamer/experiment/pubmed_id")
								.getString(0);
						if (pmid != null && pmid.length() > 0) {
							if (!pubmeds.containsKey(pmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(pmid);
								year = pi.getPublicationYear();
								pubmeds.put(pmid, pi);
							} else {
								year = pubmeds.get(pmid).getPublicationYear();
							}
						}
					} catch (JSONException e) {
						continue r;
					}
					// get the ph values
					try {
						List<Double> phList = new ArrayList<Double>();
						JSONArray ec = ja
								.getJSONArray("/base/aptamer/experiment/has_experimetal_conditions");
						for (int j = 0; j < ec.length(); j++) {
							JSONObject anExpCond = ec.getJSONObject(j);
							JSONArray ss = anExpCond
									.getJSONArray("/base/aptamer/selex_conditions/has_selection_solution");
							for (int k = 0; k < ss.length(); k++) {
								JSONObject aSS = ss.getJSONObject(k);
								JSONArray phs = aSS
										.getJSONArray("/base/aptamer/selection_solution/ph");
								for (int l = 0; l < phs.length(); l++) {
									Double aPh = phs.getDouble(l);
									phList.add(aPh);
								}
							}
						}
						if (year != -1 && phList.size() > 0) {
							if (year_avg.containsKey(year)) {
								List<Double> l = year_avg.get(year);
								for (Double d : phList) {
									l.add(d);
								}
							} else {
								year_avg.put(year, phList);
							}
						}
					} catch (JSONException e) {
						continue r;
					}
				}// for
					// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeYearVsPhQuery(anAptamerType) + "&cursor="
							+ cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Map.Entry<Integer, List<Double>> entry : year_avg.entrySet()) {
			Integer y = entry.getKey();
			List<Double> vals = entry.getValue();
			Double avg = StatUtils.computeAverage(vals);
			Coordinate<Integer, Double, Double> c = new Coordinate<Integer, Double, Double>(
					y, avg, null);
			rm.add(c);
		}
		return rm;
	}

	private List<Coordinate<Integer, Double, Double>> makeYearvsAptamerType() {
		List<Coordinate<Integer, Double, Double>> rm = new ArrayList<Coordinate<Integer, Double, Double>>();
		String q = makeYearVsAptamerTypeQuery() + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		// a map where the key is the year and the value is a map where the key
		// rna|dna and the value is their count
		Map<Integer, Map<String, Integer>> aptamerTypeCountMap = new HashMap<Integer, Map<String, Integer>>();
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the pmid
					String pmid = null;
					int year = -1;
					try {
						pmid = ja.getJSONArray(
								"/base/aptamer/experiment/pubmed_id")
								.getString(0);
						if (pmid != null && pmid.length() > 0) {
							if (!pubmeds.containsKey(pmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(pmid);
								year = pi.getPublicationYear();
								pubmeds.put(pmid, pi);
							} else {
								year = pubmeds.get(pmid).getPublicationYear();
							}
						}
					} catch (JSONException e) {
						continue r;
					}
					// now get the types
					try {
						JSONArray outcomes = new JSONArray();
						outcomes = ja
								.getJSONArray("/base/aptamer/experiment/has_outcome");
						for (int j = 0; j < outcomes.length(); j++) {
							JSONObject anOutcome = outcomes.getJSONObject(j);
							JSONArray participants = new JSONArray();
							participants = anOutcome
									.getJSONArray("/base/aptamer/interaction/has_participant");
							for (int k = 0; k < participants.length(); k++) {
								JSONObject anInteractor = participants
										.getJSONObject(k);
								JSONArray types = anInteractor
										.getJSONArray("w:type");
								for (int l = 0; l < types.length(); l++) {
									String aType = types.getString(l);
									if (year != -1) {
										if (aptamerTypeCountMap
												.containsKey(year)) {
											Map<String, Integer> contents = aptamerTypeCountMap
													.get(year);
											if (aType
													.equals("/base/aptamer/dna")) {
												if (contents.containsKey("dna")) {
													int val = contents
															.get("dna") + 1;
													contents.put("dna", val);
													aptamerTypeCountMap.put(
															year, contents);
												} else {
													contents.put("dna", 1);
													aptamerTypeCountMap.put(
															year, contents);
												}
											} else if (aType
													.equals("/base/aptamer/rna")) {
												if (contents.containsKey("rna")) {
													int val = contents
															.get("rna") + 1;
													contents.put("rna", val);
													aptamerTypeCountMap.put(
															year, contents);
												} else {
													contents.put("rna", 1);
													aptamerTypeCountMap.put(
															year, contents);
												}
											}
										} else {
											Map<String, Integer> contents = new HashMap<String, Integer>();
											if (aType
													.equals("/base/aptamer/dna")) {
												contents.put("dna", 1);
											} else if (aType
													.equals("/base/aptamer/rna")) {
												contents.put("rna", 1);
											}
											aptamerTypeCountMap.put(year,
													contents);
										}
									} else {
										continue r;
									}
								}
							}
						}
					} catch (JSONException e) {
						continue r;
					}

				}// for
					// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeYearVsAptamerTypeQuery() + "&cursor=" + cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}
			}// while
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Map.Entry<Integer, Map<String, Integer>> entry : aptamerTypeCountMap
				.entrySet()) {
			int year = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			double yearTotal = 0.0;
			double dnaCount = 0.0;
			double rnaCount = 0.0;
			double dnaProp = 0.0;
			double rnaProp = 0.0;
			if (value.containsKey("dna")) {
				dnaCount = value.get("dna");
				yearTotal += value.get("dna");
			}
			if (value.containsKey("rna")) {
				rnaCount = value.get("rna");
				yearTotal += value.get("rna");
			}
			if (yearTotal > 0) {
				dnaProp = dnaCount / yearTotal;
				rnaProp = rnaCount / yearTotal;
			}
			Coordinate<Integer, Double, Double> c = new Coordinate<Integer, Double, Double>(
					year, dnaProp, rnaProp);
			rm.add(c);
		}

		return rm;
	}

	private List<Coordinate<Integer, Double, ?>> makeYearVsTemplateLength(
			String anAptamerType) {
		List<Coordinate<Integer, Double, ?>> rm = new ArrayList<Coordinate<Integer, Double, ?>>();
		String q = makeYearVsTemplateLengthQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		Map<Integer, List<Double>> year_map = new HashMap<Integer, List<Double>>();
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the pmid
					String pmid = null;
					int year = -1;
					try {
						pmid = ja.getJSONArray(
								"/base/aptamer/experiment/pubmed_id")
								.getString(0);
						if (pmid != null && pmid.length() > 0) {
							if (!pubmeds.containsKey(pmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(pmid);
								year = pi.getPublicationYear();
								pubmeds.put(pmid, pi);
							} else {
								year = pubmeds.get(pmid).getPublicationYear();
							}
						}
					} catch (JSONException e) {
						continue r;
					}
					// get the template length
					List<Double> templateLengths = new ArrayList<Double>();
					JSONArray ecArr = ja
							.getJSONArray("/base/aptamer/experiment/has_experimetal_conditions");
					for (int j = 0; j < ecArr.length(); j++) {
						JSONObject anEc = ecArr.getJSONObject(j);
						String aTemplate = null;
						try {
							aTemplate = anEc
									.getJSONArray(
											"/base/aptamer/selex_conditions/has_template_sequence")
									.getString(0);
						} catch (JSONException e) {
							continue r;
						}
						Pattern p = Pattern.compile("\\w+\\-N(\\d+)\\-\\w+");
						Matcher m = p.matcher(aTemplate);
						if (m.matches()) {
							String length = m.group(1);
							try {
								templateLengths.add(Double.parseDouble(length));
							} catch (NumberFormatException e) {
								continue r;
							}
						}
					}
					if (year != -1 && templateLengths.size() > 0) {
						// add to entry to the year_map
						if (year_map.containsKey(year)) {
							List<Double> l = year_map.get(year);
							for (Double al : templateLengths) {
								l.add(al);
							}
							year_map.put(year, l);
						} else {
							year_map.put(year, templateLengths);
						}
					}
				}
				// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeYearVsTemplateLengthQuery(anAptamerType)
							+ "&cursor=" + cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Map.Entry<Integer, List<Double>> entry : year_map.entrySet()) {
			int year = entry.getKey();
			List<Double> value = entry.getValue();
			Double avg = StatUtils.computeAverage(value);
			Coordinate<Integer, Double, ?> c = new Coordinate<Integer, Double, Double>(
					year, avg, null);
			rm.add(c);
		}

		return rm;
	}

	private List<Coordinate<Integer, Double, ?>> makeYearVsNumOfRounds(
			String anAptamerType) {
		List<Coordinate<Integer, Double, ?>> rm = new ArrayList<Coordinate<Integer, Double, ?>>();
		String q = null;
		q = makeYearVsNumOfRoundsQuery(anAptamerType) + "&cursor";
		;
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		Map<Integer, List<Double>> year_map = new HashMap<Integer, List<Double>>();
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				String cursorVal = rc.getString("cursor").replace("\"", "");
				r: for (int i = 0; i < r.length(); i++) {
					JSONObject ja = r.getJSONObject(i);
					// get the pmid
					String pmid = null;
					int year = -1;
					try {
						pmid = ja.getJSONArray(
								"/base/aptamer/experiment/pubmed_id")
								.getString(0);
						if (pmid != null && pmid.length() > 0) {
							if (!pubmeds.containsKey(pmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(pmid);
								year = pi.getPublicationYear();
								pubmeds.put(pmid, pi);
							} else {
								year = pubmeds.get(pmid).getPublicationYear();
							}
						}
					} catch (JSONException e) {
						continue r;
					}
					// get the number of rounds
					List<Double> nor = new ArrayList<Double>();
					JSONArray ecArr = ja
							.getJSONArray("/base/aptamer/experiment/has_experimetal_conditions");
					for (int j = 0; j < ecArr.length(); j++) {
						JSONObject anEc = ecArr.getJSONObject(j);
						try {
							Double aVal = anEc
									.getDouble("/base/aptamer/selex_conditions/number_of_selection_rounds");
							nor.add(aVal);
						} catch (JSONException e) {
							continue r;
						}
					}
					if (year != -1 && nor.size() > 0) {
						// add to entry to the year_map
						if (year_map.containsKey(year)) {
							List<Double> l = year_map.get(year);
							for (Double al : nor) {
								l.add(al);
							}
							year_map.put(year, l);
						} else {
							year_map.put(year, nor);
						}
					} else {
						continue r;
					}
				}
				// add the cursor to the query
				if (cursorVal.equals("false")) {
					lf = false;
				} else {
					q = makeYearVsNumOfRoundsQuery(anAptamerType) + "&cursor="
							+ cursorVal;
					ur = new URLReader(FreebaseCredentials.getScheme(),
							FreebaseCredentials.getHost(),
							FreebaseCredentials.getPath(), q,
							FreebaseCredentials.getKey());
					rc = ur.getJSON();
					r = rc.getJSONArray("result");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		for (Map.Entry<Integer, List<Double>> entry : year_map.entrySet()) {
			int year = entry.getKey();
			List<Double> value = entry.getValue();
			Double avg = StatUtils.computeAverage(value);
			Coordinate<Integer, Double, ?> c = new Coordinate<Integer, Double, Double>(
					year, avg, null);
			rm.add(c);
		}
		return rm;
	}

	private List<Coordinate<Integer, String, Integer>> makeYearVsRecoveryMethods(
			String anAptamerType) {
		List<Coordinate<Integer, String, Integer>> rm = new ArrayList<Coordinate<Integer, String, Integer>>();
		// Map<Year, Map<Partitioining Method,Count>>
		Map<Integer, Map<String, Integer>> count_map = new HashMap<Integer, Map<String, Integer>>();
		String q = null;
		q = makeRecoveryMethodPerYearQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						try {
							JSONObject ja = r.getJSONObject(i);
							// get the pmid
							String pmid = null;
							int year = -1;
							try {
								pmid = ja.getJSONArray(
										"/base/aptamer/experiment/pubmed_id")
										.getString(0);
								if (pmid != null && pmid.length() > 0) {
									if (!pubmeds.containsKey(pmid)) {
										// get the year
										PubmedInfo pi = new PubmedInfo(pmid);
										year = pi.getPublicationYear();
										pubmeds.put(pmid, pi);
									} else {
										year = pubmeds.get(pmid)
												.getPublicationYear();
									}
								}
							} catch (JSONException e) {
								// TODO: deal with no pmid
								continue r;
							}
							// get the recovery methods
							List<String> recovery_methods = new ArrayList<String>();
							JSONArray pm_arr = ja
									.getJSONArray("/base/aptamer/selex_experiment/has_recovery_method_ne");
							for (int k = 0; k < pm_arr.length(); k++) {
								JSONObject aPm = pm_arr.getJSONObject(k);
								JSONArray pm_arr_vals = aPm
										.getJSONArray("/base/aptamer/recovery_method_se/has_recovery_method");
								for (int e = 0; e < pm_arr_vals.length(); e++) {
									recovery_methods.add(pm_arr_vals
											.getString(e));
								}
							}
							if (year > 0 && recovery_methods.size() > 0) {
								if (count_map.containsKey(year)) {
									for (String aMethod : recovery_methods) {
										if (count_map.get(year).containsKey(
												aMethod)) {
											// get the count and add one
											int c = count_map.get(year).get(
													aMethod) + 1;
											count_map.get(year).put(aMethod, c);
										} else {
											count_map.get(year).put(aMethod, 1);
										}
									}
								} else {
									Map<String, Integer> meths = new HashMap<String, Integer>();
									for (String aMethod : recovery_methods) {
										// then add every selex_method with
										// count 1
										meths.put(aMethod, 1);
									}
									count_map.put(year, meths);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeRecoveryMethodPerYearQuery(anAptamerType)
								+ "&cursor=" + cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// now iterate over the count map
		for (Map.Entry<Integer, Map<String, Integer>> entry : count_map
				.entrySet()) {
			int year = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			for (Map.Entry<String, Integer> aVal : value.entrySet()) {
				String partitioning_method = aVal.getKey();
				int count = aVal.getValue();
				// create a coordinate object
				Coordinate<Integer, String, Integer> c = new Coordinate<Integer, String, Integer>(
						year, partitioning_method, count);
				rm.add(c);
			}
		}
		return rm;
	}

	/**
	 * Create a list of coordinates with the following components: X->(Integer)
	 * year, Y->(String) partitioning method, Z->(Integer) count
	 * 
	 * @param anAptamerType
	 *            rna or dna
	 * @return a list of coordinates with the following components: X->(Integer)
	 *         year, Y->(String) partitioning method, Z->(Integer) count
	 */
	private List<Coordinate<Integer, String, Integer>> makeYearVsPartitioningMethods(
			String anAptamerType) {
		List<Coordinate<Integer, String, Integer>> rm = new ArrayList<Coordinate<Integer, String, Integer>>();
		// Map<Year, Map<Partitioining Method,Count>>
		Map<Integer, Map<String, Integer>> count_map = new HashMap<Integer, Map<String, Integer>>();
		String q = null;
		q = makePartitioningMethodPerYearQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						try {
							JSONObject ja = r.getJSONObject(i);
							// get the pmid
							String pmid = null;
							int year = -1;
							try {
								pmid = ja.getJSONArray(
										"/base/aptamer/experiment/pubmed_id")
										.getString(0);
								if (pmid != null && pmid.length() > 0) {
									if (!pubmeds.containsKey(pmid)) {
										// get the year
										PubmedInfo pi = new PubmedInfo(pmid);
										year = pi.getPublicationYear();
										pubmeds.put(pmid, pi);
									} else {
										year = pubmeds.get(pmid)
												.getPublicationYear();
									}
								}
							} catch (JSONException e) {
								// TODO: deal with no pmid
								continue r;
							}
							// get the partitioning methods
							List<String> part_methods = new ArrayList<String>();
							JSONArray pm_arr = ja
									.getJSONArray("/base/aptamer/selex_experiment/has_partitioning_method");
							for (int k = 0; k < pm_arr.length(); k++) {
								JSONObject aPm = pm_arr.getJSONObject(k);
								JSONArray pm_arr_vals = aPm
										.getJSONArray("/base/aptamer/partitioning_method/has_separation_method");
								for (int e = 0; e < pm_arr_vals.length(); e++) {
									part_methods.add(pm_arr_vals.getString(e));
								}
							}
							if (year > 0 && part_methods.size() > 0) {
								if (count_map.containsKey(year)) {
									for (String aMethod : part_methods) {
										if (count_map.get(year).containsKey(
												aMethod)) {
											// get the count and add one
											int c = count_map.get(year).get(
													aMethod) + 1;
											count_map.get(year).put(aMethod, c);
										} else {
											count_map.get(year).put(aMethod, 1);
										}
									}
								} else {
									Map<String, Integer> meths = new HashMap<String, Integer>();
									for (String aMethod : part_methods) {
										// then add every selex_method with
										// count 1
										meths.put(aMethod, 1);
									}
									count_map.put(year, meths);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makePartitioningMethodPerYearQuery(anAptamerType)
								+ "&cursor=" + cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// now iterate over the count map
		for (Map.Entry<Integer, Map<String, Integer>> entry : count_map
				.entrySet()) {
			int year = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			for (Map.Entry<String, Integer> aVal : value.entrySet()) {
				String partitioning_method = aVal.getKey();
				int count = aVal.getValue();
				// create a coordinate object
				Coordinate<Integer, String, Integer> c = new Coordinate<Integer, String, Integer>(
						year, partitioning_method, count);
				rm.add(c);
			}
		}
		return rm;
	}

	private List<Coordinate<Integer, String, Integer>> makeyearVsSelexMethods(
			String anAptamerType) {
		List<Coordinate<Integer, String, Integer>> rm = new ArrayList<Coordinate<Integer, String, Integer>>();
		// Map<Year, Map<SelexMethod,Count>>
		Map<Integer, Map<String, Integer>> count_map = new HashMap<Integer, Map<String, Integer>>();
		String q = null;
		q = makeSelexMethodPerYearQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		// a map to keep track of pumedinfo objects
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					r: for (int i = 0; i < r.length(); i++) {
						try {
							JSONObject ja = r.getJSONObject(i);
							// get the pmid
							String pmid = null;
							int year = -1;
							try {
								pmid = ja.getJSONArray(
										"/base/aptamer/experiment/pubmed_id")
										.getString(0);
								if (pmid != null && pmid.length() > 0) {
									if (!pubmeds.containsKey(pmid)) {
										// get the year
										PubmedInfo pi = new PubmedInfo(pmid);
										year = pi.getPublicationYear();
										pubmeds.put(pmid, pi);
									} else {
										year = pubmeds.get(pmid)
												.getPublicationYear();
									}
								}

							} catch (JSONException e) {
								// TODO: what if no pmid is not found??
								continue r;
							}
							// get the selex methods
							List<String> selex_methods = new ArrayList<String>();
							JSONArray sm_arr = ja
									.getJSONArray("/base/aptamer/selex_experiment/has_selex_method");
							for (int w = 0; w < sm_arr.length(); w++) {
								String aM = sm_arr.getString(w);
								selex_methods.add(aM);
							}
							if (year > 0 && selex_methods.size() > 0) {
								if (count_map.containsKey(year)) {
									for (String aMethod : selex_methods) {
										if (count_map.get(year).containsKey(
												aMethod)) {
											// get the count and add one
											int c = count_map.get(year).get(
													aMethod) + 1;
											count_map.get(year).put(aMethod, c);
										} else {
											count_map.get(year).put(aMethod, 1);
										}
									}
								} else {
									Map<String, Integer> meths = new HashMap<String, Integer>();
									for (String aMethod : selex_methods) {
										// then add every selex_method with
										// count 1
										meths.put(aMethod, 1);
									}
									count_map.put(year, meths);
								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeSelexMethodPerYearQuery(anAptamerType)
								+ "&cursor=" + cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// now iterate over the count map
		for (Map.Entry<Integer, Map<String, Integer>> entry : count_map
				.entrySet()) {
			int year = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			for (Map.Entry<String, Integer> aVal : value.entrySet()) {
				String selex_method = aVal.getKey();
				int count = aVal.getValue();
				// create a coordinate object
				Coordinate<Integer, String, Integer> c = new Coordinate<Integer, String, Integer>(
						year, selex_method, count);
				rm.add(c);
			}
		}
		return rm;
	}

	/**
	 * Compute the average kd per interaction and record the results in a
	 * List<coordinates> per year
	 * 
	 * @param aptamerType
	 * @param negativeLog10
	 * @param bestKdOnly
	 * @return a list of coordinates x->(Integer) year, y->(double)kd,
	 *         z->(?)null
	 * @throws IOException
	 */
	private List<Coordinate<Integer, Double, ?>> makeyearvsKd(
			String aptamerType, boolean negativeLog10, boolean bestKdOnly)
			throws IOException {
		List<Coordinate<Integer, Double, ?>> rm = new ArrayList<Coordinate<Integer, Double, ?>>();
		String q = null;
		q = makeKdYearQuery(aptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		// A map to keep track of the PMIDs that have already been retrieved
		Map<String, PubmedInfo> pubmeds = new HashMap<String, PubmedInfo>();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					// store the kds in a temp list
					r: for (int i = 0; i < r.length(); i++) {
						JSONObject ja = r.getJSONObject(i);
						String anMid = ja.getString("mid");
						JSONArray kds = ja
								.getJSONArray("/base/aptamer/interaction/has_dissociation_constant");
						List<Double> tmpKds = new ArrayList<Double>();
						for (int j = 0; j < kds.length(); j++) {
							JSONObject currKd = kds.getJSONObject(j);
							String kd = currKd
									.getString("/base/aptamer/dissociation_constant/has_value");
							String kd_tmp = currKd
									.getString(
											"/base/aptamer/dissociation_constant/has_temporary_string_value")
									.replace("\"", "");
							Double aKd = -1.0;
							try {
								aKd = Double.parseDouble(kd);
							} catch (NumberFormatException e) {
								try {
									aKd = Double.parseDouble(kd_tmp);
								} catch (NumberFormatException e2) {
									aKd = -1.0;
								}
							}
							if (negativeLog10 == true) {
								if (aKd > 0.0) {
									Double x = (-1) * Math.log10(aKd);
									tmpKds.add(x);
								}
							} else {
								tmpKds.add(aKd);
							}
						}
						// now store only the average or the best kd
						Double kd_val = -1.0;
						if (bestKdOnly) {
							if (tmpKds.size() > 1) {
								kd_val = StatUtils.getMin(tmpKds);
							} else {
								kd_val = StatUtils.getMin(tmpKds);
							}
						} else {
							kd_val = StatUtils.computeAverage(tmpKds);
						}
						// now get year
						// get the value of the pmid
						JSONArray pmids = ja
								.getJSONArray("/base/aptamer/experimental_outcome/is_outcome_of");
						JSONObject aPmidObj = pmids.getJSONObject(0);
						String aPmid = null;
						int year = -1;
						try {
							aPmid = aPmidObj.getJSONArray(
									"/base/aptamer/experiment/pubmed_id")
									.getString(0);
						} catch (JSONException e) {
							continue r;
						}
						if (aPmid != null && aPmid.length() > 0) {
							// check that the pmid is not in the pubmeds map
							if (!pubmeds.containsKey(aPmid)) {
								// get the year
								PubmedInfo pi = new PubmedInfo(aPmid);
								int y = pi.getPublicationYear();
								// check if positive
								if (y > 0) {
									year = y;
								}
								pubmeds.put(aPmid, pi);
							} else {
								// get the year from the map
								PubmedInfo pi = pubmeds.get(aPmid);
								int y = pi.getPublicationYear();
								if (y > 0) {
									year = y;
								}
							}
						}
						if (kd_val > 0 && year > 0) {
							// then add a coordinate
							Coordinate<Integer, Double, Double> c = new Coordinate<Integer, Double, Double>(
									anMid, year, kd_val, null);
							rm.add(c);
						} else {
							// TODO:deal with the case that there is no valid kd
							// or year
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeKdYearQuery(aptamerType) + "&cursor="
								+ cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rm;
	}

	/**
	 * Compute the average kd per interaction and the average sequence length
	 * per interaction and return a List<coordinates>
	 * 
	 * @param anAptamerType
	 *            the type of aptamer
	 * @param negativeLog10
	 *            if true the logarithm of base 10 of kd values will be
	 *            multiplied by -1 and returned i.e.: -log(kd)
	 * @param bestKdOnly
	 *            if true every coordinate will be compsed by the average
	 *            sequence length (x) and the best kd (y)
	 * 
	 * @return the list of coodinates
	 */
	private List<Coordinate<Double, Double, Double>> makeKdVsLength(
			String anAptamerType, boolean negativeLog10, boolean bestKdOnly)
			throws IOException {
		List<Coordinate<Double, Double, Double>> rm = new ArrayList<Coordinate<Double, Double, Double>>();
		String q = null;
		q = makeKdLengthQuery(anAptamerType) + "&cursor";
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				q, FreebaseCredentials.getKey());
		JSONObject rc = ur.getJSON();
		try {
			JSONArray r = rc.getJSONArray("result");
			boolean lf = true;
			while (lf) {
				try {
					String cursorVal = rc.getString("cursor").replace("\"", "");
					for (int i = 0; i < r.length(); i++) {
						JSONObject jo = r.getJSONObject(i);
						JSONArray kds = jo
								.getJSONArray("/base/aptamer/interaction/has_dissociation_constant");
						String mid = jo.getString("mid");
						// store the kds in a temp list
						List<Double> tmpKds = new ArrayList<Double>();
						for (int j = 0; j < kds.length(); j++) {
							JSONObject ja = kds.getJSONObject(j);
							String kd = ja
									.getString("/base/aptamer/dissociation_constant/has_value");
							String kd_tmp = ja
									.getString(
											"/base/aptamer/dissociation_constant/has_temporary_string_value")
									.replace("\"", "");
							Double aKd = -1.0;
							try {
								aKd = Double.parseDouble(kd);
							} catch (NumberFormatException e) {
								try {
									aKd = Double.parseDouble(kd_tmp);
								} catch (NumberFormatException e2) {
									aKd = -1.0;
								}
							}
							if (negativeLog10 == true) {
								if (aKd > 0.0) {
									Double x = (-1) * Math.log10(aKd);
									tmpKds.add(x);
								}
							} else {
								tmpKds.add(aKd);
							}
						}

						Double kd_val = -1.0;
						if (bestKdOnly) {
							if (tmpKds.size() > 1) {
								kd_val = StatUtils.getMin(tmpKds);
							} else {
								kd_val = StatUtils.getMin(tmpKds);
							}
						} else {
							kd_val = StatUtils.computeAverage(tmpKds);
						}

						JSONArray seqlengths = jo
								.getJSONArray("/base/aptamer/interaction/has_participant");
						List<Double> tmpSeqLens = new ArrayList<Double>();
						for (int k = 0; k < seqlengths.length(); k++) {
							JSONObject ja = seqlengths.getJSONObject(k);
							String seq = ja.getString(
									"/base/aptamer/linear_polymer/sequence")
									.replace("\"", "");
							Double length = (double) seq.length();
							tmpSeqLens.add(length);
						}
						Double seq_avg = StatUtils.computeAverage(tmpSeqLens);

						if (kd_val > 0.0 && seq_avg > 0.0) {
							Coordinate<Double, Double, Double> c = new Coordinate<Double, Double, Double>(
									mid, seq_avg, kd_val, null);
							rm.add(c);
						}
					}
					// add the cursor to the query
					if (cursorVal.equals("false")) {
						lf = false;
					} else {
						q = makeKdLengthQuery(anAptamerType) + "&cursor="
								+ cursorVal;
						ur = new URLReader(FreebaseCredentials.getScheme(),
								FreebaseCredentials.getHost(),
								FreebaseCredentials.getPath(), q,
								FreebaseCredentials.getKey());
						rc = ur.getJSON();
						r = rc.getJSONArray("result");
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rm;
	}

	private String makeTTfreqQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			JSONArray outcome_of = new JSONArray();
			JSONObject anExp = new JSONObject();
			anExp.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			outcome_of.put(anExp);
			root.put("\"/base/aptamer/experimental_outcome/is_outcome_of\"",
					outcome_of);
			root.put("\"c:type\"", "/base/aptamer/experimental_outcome");
			root.put("\"type\"", "/base/aptamer/interaction");
			root.put("\"mid\"", JSONObject.NULL);

			JSONArray parts = new JSONArray();
			JSONObject aPart = new JSONObject();
			aPart.put("\"a:type\"", "/base/aptamer/aptamer_target");
			aPart.put("\"/base/aptamer/aptamer_target/has_type\"",
					new JSONArray());
			aPart.put("\"optional\"", true);
			parts.put(aPart);
			root.put("\"/base/aptamer/interaction/has_participant\"", parts);
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeYearVsAptamerTypeQuery() {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("\"mid\"", JSONObject.NULL);
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			JSONArray ho = new JSONArray();
			JSONObject anOut = new JSONObject();
			anOut.put("\"b:type\"", "/base/aptamer/interaction");
			JSONArray parts = new JSONArray();
			JSONObject type = new JSONObject();
			JSONArray type_opts = new JSONArray();
			type_opts.put("/base/aptamer/rna");
			type_opts.put("/base/aptamer/dna");
			type.put("\"type|=\"", type_opts);
			type.put("w:type", new JSONArray());
			parts.put(type);
			anOut.put("\"/base/aptamer/interaction/has_participant\"", parts);
			ho.put(anOut);
			root.put("\"/base/aptamer/experiment/has_outcome\"", ho);
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeAptamerLengthVsMinimalAptamerLengthQuery(
			String anAptamerType) {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("\"type\"", "/base/aptamer/aptamer");
			root.put("\"/base/aptamer/linear_polymer/sequence\"",
					JSONObject.NULL);
			JSONArray minArr = new JSONArray();
			JSONObject aMinApt = new JSONObject();
			aMinApt.put("\"b:type\"", "/base/aptamer/minimal_aptamer");
			aMinApt.put("\"/base/aptamer/linear_polymer/sequence\"",
					JSONObject.NULL);
			aMinApt.put("\"optional\"", false);
			minArr.put(aMinApt);
			root.put("\"/base/aptamer/aptamer/has_minimal_aptamer\"", minArr);
			if (anAptamerType.equalsIgnoreCase("dna")) {
				root.put("\"d:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				root.put("\"d:type\"", "/base/aptamer/rna");
			}
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeYearVsBufferQuery(String anAptamerType) {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("\"mid\"", JSONObject.NULL);
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			JSONArray ecArr = new JSONArray();
			JSONObject ec = new JSONObject();
			ec.put("\"c:type\"", "/base/aptamer/selex_conditions");
			ec.put("\"optional\"", false);
			JSONArray ss = new JSONArray();
			JSONObject aSS = new JSONObject();
			aSS.put("\"/base/aptamer/selection_solution/has_buffering_agent\"",
					new JSONArray());
			aSS.put("\"optional\"", false);
			ss.put(aSS);
			ec.put("\"/base/aptamer/selex_conditions/has_selection_solution\"",
					ss);
			ecArr.put(ec);
			root.put("\"/base/aptamer/experiment/has_experimetal_conditions\"",
					ecArr);
			if (anAptamerType.equalsIgnoreCase("dna")
					|| anAptamerType.equalsIgnoreCase("rna")) {
				JSONArray ho = new JSONArray();
				JSONObject anOut = new JSONObject();
				anOut.put("\"b:type\"", "/base/aptamer/interaction");
				JSONArray parts = new JSONArray();
				JSONObject type = new JSONObject();
				if (anAptamerType.equalsIgnoreCase("dna")) {
					type.put("\"c:type\"", "/base/aptamer/dna");
				} else {
					type.put("\"c:type\"", "/base/aptamer/rna");
				}
				parts.put(type);
				anOut.put("\"/base/aptamer/interaction/has_participant\"",
						parts);
				ho.put(anOut);
				root.put("\"/base/aptamer/experiment/has_outcome\"", ho);
			}
			rm.put(root);

			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeYearVsPhQuery(String anAptamerType) {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("\"mid\"", JSONObject.NULL);
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			JSONArray ecArr = new JSONArray();
			JSONObject ec = new JSONObject();
			ec.put("\"c:type\"", "/base/aptamer/selex_conditions");
			ec.put("\"optional\"", false);
			JSONArray ss = new JSONArray();
			JSONObject aSS = new JSONObject();
			aSS.put("\"/base/aptamer/selection_solution/ph\"", new JSONArray());
			aSS.put("\"optional\"", false);
			ss.put(aSS);
			ec.put("\"/base/aptamer/selex_conditions/has_selection_solution\"",
					ss);
			ecArr.put(ec);
			root.put("\"/base/aptamer/experiment/has_experimetal_conditions\"",
					ecArr);
			if (anAptamerType.equalsIgnoreCase("dna")
					|| anAptamerType.equalsIgnoreCase("rna")) {
				JSONArray ho = new JSONArray();
				JSONObject anOut = new JSONObject();
				anOut.put("\"b:type\"", "/base/aptamer/interaction");
				JSONArray parts = new JSONArray();
				JSONObject type = new JSONObject();
				if (anAptamerType.equalsIgnoreCase("dna")) {
					type.put("\"c:type\"", "/base/aptamer/dna");
				} else {
					type.put("\"c:type\"", "/base/aptamer/rna");
				}
				parts.put(type);
				anOut.put("\"/base/aptamer/interaction/has_participant\"",
						parts);
				ho.put(anOut);
				root.put("\"/base/aptamer/experiment/has_outcome\"", ho);
			}
			rm.put(root);

			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String makeYearVsTemplateLengthQuery(String anAptamerType) {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("mid", JSONObject.NULL);
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			JSONArray ecArr = new JSONArray();
			JSONObject ec = new JSONObject();
			ec.put("\"b:type\"", "/base/aptamer/selex_conditions");
			ec.put("\"/base/aptamer/selex_conditions/has_template_sequence\"",
					new JSONArray());
			ec.put("\"optional\"", false);
			ecArr.put(ec);
			root.put("\"/base/aptamer/experiment/has_experimetal_conditions\"",
					ecArr);
			if (anAptamerType.equalsIgnoreCase("dna")
					|| anAptamerType.equalsIgnoreCase("rna")) {
				JSONArray ho = new JSONArray();
				JSONObject anOut = new JSONObject();
				anOut.put("\"b:type\"", "/base/aptamer/interaction");
				JSONArray parts = new JSONArray();
				JSONObject type = new JSONObject();
				if (anAptamerType.equalsIgnoreCase("dna")) {
					type.put("\"c:type\"", "/base/aptamer/dna");
				} else {
					type.put("\"c:type\"", "/base/aptamer/rna");
				}
				parts.put(type);
				anOut.put("\"/base/aptamer/interaction/has_participant\"",
						parts);
				ho.put(anOut);
				root.put("\"/base/aptamer/experiment/has_outcome\"", ho);
			}
			rm.put(root);

			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeYearVsNumOfRoundsQuery(String anAptamerType) {
		try {
			JSONArray rm = new JSONArray();
			JSONObject root = new JSONObject();
			root.put("\"mid\"", JSONObject.NULL);
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			JSONArray ecArr = new JSONArray();
			JSONObject ec = new JSONObject();
			ec.put("\"b:type\"", "/base/aptamer/selex_conditions");
			ec.put("\"/base/aptamer/selex_conditions/number_of_selection_rounds\"",
					JSONObject.NULL);
			ec.put("\"optional\"", false);
			ecArr.put(ec);
			root.put("\"/base/aptamer/experiment/has_experimetal_conditions\"",
					ecArr);

			if (anAptamerType.equalsIgnoreCase("dna")
					|| anAptamerType.equalsIgnoreCase("rna")) {
				JSONArray ho = new JSONArray();
				JSONObject anOut = new JSONObject();
				anOut.put("\"b:type\"", "/base/aptamer/interaction");
				JSONArray parts = new JSONArray();
				JSONObject type = new JSONObject();
				if (anAptamerType.equalsIgnoreCase("dna")) {
					type.put("\"c:type\"", "/base/aptamer/dna");
				} else {
					type.put("\"c:type\"", "/base/aptamer/rna");
				}
				parts.put(type);
				anOut.put("\"/base/aptamer/interaction/has_participant\"",
						parts);
				ho.put(anOut);
				root.put("\"/base/aptamer/experiment/has_outcome\"", ho);
			}
			rm.put(root);

			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String makeYearVsAptamerLengthQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			root.put("\"type\"", "/base/aptamer/interaction");
			JSONArray parts = new JSONArray();
			JSONObject part = new JSONObject();
			part.put("\"c:type\"", "/base/aptamer/aptamer");
			part.put("\"b:type\"", "/base/aptamer/linear_polymer");
			part.put("\"/base/aptamer/linear_polymer/sequence\"",
					JSONObject.NULL);
			if (anAptamerType.equalsIgnoreCase("dna")) {
				part.put("\"a:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				part.put("\"a:type\"", "/base/aptamer/rna");
			}
			parts.put(part);
			root.put("\"/base/aptamer/interaction/has_participant\"", parts);
			JSONArray iof = new JSONArray();
			JSONObject anOutcome = new JSONObject();
			anOutcome.put("\"/base/aptamer/experiment/pubmed_id\"",
					new JSONArray());
			anOutcome.put("\"optional\"", false);
			iof.put(anOutcome);
			root.put("\"/base/aptamer/experimental_outcome/is_outcome_of\"",
					iof);
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return
	 */
	private String makeKdLengthQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();

		try {
			JSONArray parts = new JSONArray();
			JSONArray kds = new JSONArray();
			root.put("\"type\"", "/base/aptamer/interaction");
			JSONObject part = new JSONObject();
			part.put("\"type\"", "/base/aptamer/aptamer");
			part.put("\"b:type\"", "/base/aptamer/linear_polymer");
			part.put("\"/base/aptamer/linear_polymer/sequence\"",
					JSONObject.NULL);
			if (anAptamerType.equalsIgnoreCase("dna")) {
				part.put("\"a:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				part.put("\"a:type\"", "/base/aptamer/rna");
			}
			parts.put(part);
			root.put("\"/base/aptamer/interaction/has_participant\"", parts);
			root.put("\"mid\"", JSONObject.NULL);
			JSONObject aKd = new JSONObject();
			aKd.put("\"/base/aptamer/dissociation_constant/has_value\"",
					JSONObject.NULL);
			aKd.put("\"/base/aptamer/dissociation_constant/has_temporary_string_value\"",
					JSONObject.NULL);
			kds.put(aKd);
			root.put("\"/base/aptamer/interaction/has_dissociation_constant\"",
					kds);
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeRecoveryMethodPerYearQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"b:type\"", "/base/aptamer/experiment");
			JSONArray sep_meths_arr = new JSONArray();
			JSONObject a_sep_method = new JSONObject();
			a_sep_method.put(
					"\"/base/aptamer/recovery_method_se/has_recovery_method\"",
					new JSONArray());
			sep_meths_arr.put(a_sep_method);
			root.put(
					"\"/base/aptamer/selex_experiment/has_recovery_method_ne\"",
					sep_meths_arr);
			JSONArray anInt_arr = new JSONArray();
			JSONObject anInteraction = new JSONObject();
			JSONArray interactants_arr = new JSONArray();
			JSONObject anInteractant = new JSONObject();
			if (anAptamerType.equalsIgnoreCase("dna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/rna");
			}
			interactants_arr.put(anInteractant);
			anInteraction.put("\"/base/aptamer/interaction/has_participant\"",
					interactants_arr);
			anInt_arr.put(anInteraction);
			root.put("\"/base/aptamer/experiment/has_outcome\"", anInt_arr);
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makePartitioningMethodPerYearQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"b:type\"", "/base/aptamer/experiment");
			JSONArray part_meths_arr = new JSONArray();
			JSONObject aPart_meth = new JSONObject();
			aPart_meth
					.put("\"/base/aptamer/partitioning_method/has_separation_method\"",
							new JSONArray());
			part_meths_arr.put(aPart_meth);
			root.put(
					"\"/base/aptamer/selex_experiment/has_partitioning_method\"",
					part_meths_arr);
			JSONArray anInt_arr = new JSONArray();
			JSONObject anInteraction = new JSONObject();
			JSONArray interactants_arr = new JSONArray();
			JSONObject anInteractant = new JSONObject();
			if (anAptamerType.equalsIgnoreCase("dna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/rna");
			}
			interactants_arr.put(anInteractant);
			anInteraction.put("\"/base/aptamer/interaction/has_participant\"",
					interactants_arr);
			anInt_arr.put(anInteraction);
			root.put("\"/base/aptamer/experiment/has_outcome\"", anInt_arr);
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeSelexMethodPerYearQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			JSONArray anInt_arr = new JSONArray();
			JSONObject anInteraction = new JSONObject();
			JSONArray interactants_arr = new JSONArray();
			JSONObject anInteractant = new JSONObject();
			if (anAptamerType.equalsIgnoreCase("dna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				anInteractant.put("\"d:type\"", "/base/aptamer/rna");
			}
			interactants_arr.put(anInteractant);
			anInteraction.put("\"/base/aptamer/interaction/has_participant\"",
					interactants_arr);
			anInt_arr.put(anInteraction);
			root.put("\"/base/aptamer/experiment/has_outcome\"", anInt_arr);
			root.put("\"type\"", "/base/aptamer/selex_experiment");
			root.put("\"b:type\"", "/base/aptamer/experiment");
			root.put("\"/base/aptamer/selex_experiment/has_selex_method\"",
					new JSONArray());
			root.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String makeKdYearQuery(String anAptamerType) {
		JSONArray rm = new JSONArray();
		JSONObject root = new JSONObject();
		try {
			root.put("\"mid\"", JSONObject.NULL);
			root.put("\"type\"", "/base/aptamer/interaction");
			JSONArray parts = new JSONArray();
			JSONObject part = new JSONObject();
			part.put("\"type\"", "/base/aptamer/aptamer");
			part.put("\"b:type\"", "/base/aptamer/linear_polymer");
			part.put("\"/base/aptamer/linear_polymer/sequence\"",
					JSONObject.NULL);
			if (anAptamerType.equalsIgnoreCase("dna")) {
				part.put("\"a:type\"", "/base/aptamer/dna");
			} else if (anAptamerType.equalsIgnoreCase("rna")) {
				part.put("\"a:type\"", "/base/aptamer/rna");
			}
			parts.put(part);
			root.put("\"/base/aptamer/interaction/has_participant\"", parts);

			root.put("\"mid\"", JSONObject.NULL);
			JSONArray kdsArr = new JSONArray();
			JSONObject aKd = new JSONObject();
			aKd.put("\"/base/aptamer/dissociation_constant/has_value\"",
					JSONObject.NULL);
			aKd.put("\"/base/aptamer/dissociation_constant/has_temporary_string_value\"",
					JSONObject.NULL);
			kdsArr.put(aKd);
			root.put("\"/base/aptamer/interaction/has_dissociation_constant\"",
					kdsArr);
			JSONArray outcome_of = new JSONArray();
			JSONObject anExp = new JSONObject();
			anExp.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			outcome_of.put(anExp);
			root.put("\"/base/aptamer/experimental_outcome/is_outcome_of\"",
					outcome_of);
			JSONArray int_arr = new JSONArray();
			JSONObject anInt = new JSONObject();
			anInt.put("\"/base/aptamer/experiment/pubmed_id\"", new JSONArray());
			anInt.put("\"optional\"", false);
			int_arr.put(anInt);
			root.put("\"/base/aptamer/experimental_outcome/is_outcome_of\"",
					int_arr);
			rm.put(root);
			String query = rm.toString();
			query = query.replace("\\\"", "");
			query = query.replace("\\\\", "");
			query = query.replace("\\/", "/");
			return query;
		} catch (JSONException e) {
			e.printStackTrace();

		}
		return null;
	}

	private boolean getBooleanFlag(String l, Map<String, String[]> requestMap) {
		boolean rm = false;
		if (requestMap.containsKey(l)) {
			String[] x = requestMap.get(l);
			if (x.length > 0) {
				String s = x[0].trim();
				if (s.equalsIgnoreCase("true")) {
					return true;
				} else if (s.equalsIgnoreCase("false")) {
					return false;
				} else {
					return false;
				}
			}
		}
		return rm;
	}

	private String getAptamerType(String at, Map<String, String[]> requestMap) {
		String rm = "";
		if (requestMap.containsKey(at)) {
			String[] x = requestMap.get(at);
			if (x.length > 0) {
				String s = x[0].trim();
				if (s.equalsIgnoreCase("dna") || s.equalsIgnoreCase("rna")) {
					return s;
				}
			}
		}
		return rm;
	}

	/**
	 * Gets a valid graphname. Valid values are: kdvslength
	 * 
	 * @param gn
	 * @param requestMap
	 * @return
	 */
	private String getGraphName(String gn, Map<String, String[]> requestMap) {
		String rm = "";
		if (requestMap.containsKey(gn)) {
			String[] x = requestMap.get(gn);
			if (x.length > 0) {
				String s = x[0].trim();
				return s;
			}
		}
		return null;
	}

	/**
	 * @param of
	 * @param requestMap
	 * @return
	 */
	private String getOutputFormat(String of, Map<String, String[]> requestMap) {
		String rm = "";
		if (requestMap.containsKey(of)) {
			String[] x = requestMap.get(of);
			if (x.length > 0) {
				String s = x[0].trim();
				if (s.equalsIgnoreCase("json") || s.equalsIgnoreCase("csv")) {
					return s;
				}
			}
		}
		return null;
	}

	/**
	 * The GC Content for the nucleotide sequence
	 * 
	 * @return the gc constant value
	 */
	public double getGCContent(String aSequence) {
		double g = StringUtils.countMatches(aSequence, "G") * 1.0;
		double c = StringUtils.countMatches(aSequence, "C") * 1.0;
		double t = StringUtils.countMatches(aSequence, "T") * 1.0;
		double u = StringUtils.countMatches(aSequence, "U") * 1.0;
		double a = StringUtils.countMatches(aSequence, "A") * 1.0;

		if (u == 0) {// if dna
			double q = a + t + g + c;
			double s = t / q;
			return s * 100;
		} else if (t == 0) {// if rna
			return ((g + c) / (a + g + u + c)) * 100;
		} else {
			return ((g + c) / (a + g + u + t + c)) * 100;
		}
	}
}
