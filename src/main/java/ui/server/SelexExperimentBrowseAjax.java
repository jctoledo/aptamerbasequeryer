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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import ui.shared.FreebaseCredentials;
import ui.shared.FreebaseHelper;
import ui.shared.URLReader;

import com.freebase.json.JSON;

/**
 * Retrieve Basic details about SELEX experiments from Aptamer Base
 * 
 * @author Jose Cruz-Toledo
 * 
 */
// TODO: add temporary string range for kd

public class SelexExperimentBrowseAjax extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5405307754543024304L;

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// set the response type
		resp.setContentType("text/json");
		PrintWriter out = resp.getWriter();
		Map<String, String[]> requestMap = new HashMap<String, String[]>();
		requestMap = req.getParameterMap();
		String tn = "tn";// target name selected
		String ck = "cursor";// for paging results
		String of = "offset"; //for paging offset counting
		String lim = "limit";
		String targetName = getTargetName(tn, requestMap);
		int limit = getLimit(lim, requestMap);// defaults to 15
		String cursor = getCursor(ck, requestMap);
		//Get the total number of results that the user has already seen
		Integer offset = getOffset(of, requestMap);
		// construct an MQL query using thegetTotalSeen payloads
		JSON template = null;
		template = getTemplateQuery();
		
		// add the payloads to the template query
		JSON q = null;
		if ((q = addTargetName(targetName, template)) != null) {
			template = q;
		}

		// get the total number of results
		template = FreebaseHelper.addResultCountToQuery(template);
		int totalNumberOfResults = FreebaseHelper.retrieveTotalNumberOfResults(template);
		template = FreebaseHelper.removeResultCountFromQuery(template);
		
		// add the limit to query
		template = FreebaseHelper.addLimitToQuery(template, limit);

		// now create a query string to be sent to freebase
		String query = "";
		// check if the cursor is there
		if (cursor.length() == 0 || cursor == null) {
			// add the cursor to the query string
			query = template.stringify().replace("\\", "") + "&cursor";
		} else {
			query =  template.stringify().replace("\\", "") +  "&cursor=" + cursor;
		}
		
		// now get the results from freebase
		URLReader ur = new URLReader(FreebaseCredentials.getScheme(),
				FreebaseCredentials.getHost(), FreebaseCredentials.getPath(),
				query, FreebaseCredentials.getKey());
		JSON resComplete = ur.getJSONContents();
		JSON result = resComplete.get("result");
		//FileUtils.writeStringToFile(new File("/tmp/output.txt"), result.stringify());
		// now iterate over the results
		String cursorVal = null;
		JSONArray output = new JSONArray();
		JSONArray experiments = new JSONArray();
		JSONObject cursorJson = new JSONObject();
		int currResult = 0;
		try {
			cursorVal = resComplete.get("cursor").stringify().replace("\"", "");
			cursorJson.put("cursor", cursorVal);
			output.put(cursorJson);
			boolean lf = true;
			while (lf) {
				try {
					JSONObject se = new JSONObject();
					String mid = null;
					String name = null;
					String pmid = null;
					JSON aR = result.get(currResult);
					// get the name
					name = aR.get("name").stringify().replace("\\", "")
							.replace("\"", "");
					// get the mid
					mid = aR.get("mid").stringify().replace("\\", "")
							.replace("\"", "");
					// get the pmid
					pmid = aR.get("pmid:/base/aptamer/experiment/pubmed_id")
							.get(0).stringify().replace("\\", "")
							.replace("\"", "");
					se.put("pmidUrl", "http://www.ncbi.nlm.nih.gov/pubmed/"
							+ pmid);
					se.put("name", name);
					se.put("topicUrl", "http://www.freebase.com" + mid);
					
					// get the Selex methods
					JSONArray smethods = new JSONArray();
					boolean smloop = true;
					int currsm = 0;
					while (smloop) {
						try {
							String smMid = aR
									.get("selex_method:/base/aptamer/selex_experiment/has_selex_method")
									.get(currsm).get("selex_method:mid")
									.stringify().replace("\\", "")
									.replace("\"", "");
							String smName = aR
									.get("selex_method:/base/aptamer/selex_experiment/has_selex_method")
									.get(currsm).get("selex_method:name")
									.stringify().replace("\\", "")
									.replace("\"", "");
							JSONObject sm = new JSONObject();
							sm.put("smUrl", "http://www.freebase.com" + smMid);
							sm.put("name", smName);
							smethods.put(sm);
							currsm++;
						} catch (NullPointerException e) {
							smloop = false;
						} catch (IndexOutOfBoundsException e) {
							smloop = false;
						}
					}// smloop
					se.put("selexMethods", smethods);
					// get the partitioningMethods methods
					JSONArray pmmethods = new JSONArray();
					boolean pmLoop = true;
					int currpm = 0;
					while (pmLoop) {
						try {
							JSON r = aR
									.get("partitioning_method:/base/aptamer/selex_experiment/has_partitioning_method")
									.get(currpm);
							String sepMeth = r
									.get("partitioning_method:has_separation_method")
									.stringify().replace("\\", "")
									.replace("\"", "");
							String pmMid = r.get("partitioning_method:mid")
									.stringify().replace("\\", "")
									.replace("\"", "");
							JSONObject rm = new JSONObject();
							rm.put("rmUrl", "http://www.freebase.com" + pmMid);
							rm.put("name", sepMeth);
							pmmethods.put(rm);
							currpm++;
						} catch (NullPointerException e) {
							pmLoop = false;
						} catch (IndexOutOfBoundsException e) {
							pmLoop = false;
						}
					}//pmloop
					se.put("partitioningMethods", pmmethods);
					
					// get the recovery methods
					JSONArray rmmethods = new JSONArray();
					boolean rmLoop = true;
					int currrm = 0;
					while (rmLoop) {
						try {
							JSONObject anRm = new JSONObject();
							JSON r = aR
									.get("recovery_method:/base/aptamer/selex_experiment/has_recovery_method_ne")
									.get(currrm);
							// get the mid
							String rmMid = r.get("recovery_method:mid")
									.stringify().replace("\\", "")
									.replace("\"", "");
							// now iterate over the methods
							JSONArray rMethods = new JSONArray();
							boolean methodsloop = true;
							int currMethod = 0;
							while (methodsloop) {
								try {
									JSON p = r
											.get("recovery_method:has_recovery_method")
											.get(currMethod);
									String aName = p
											.get("recovery_method:name")
											.stringify().replace("\\", "")
											.replace("\"", "");
									rMethods.put(aName);
									currMethod++;
								} catch (NullPointerException e) {
									methodsloop = false;
								} catch (IndexOutOfBoundsException e) {
									methodsloop = false;
								}
							}// methodsloop
							anRm.put("rmUrl", "http://www.freebase.com" + rmMid);
							anRm.put("names", rMethods);
							rmmethods.put(anRm);
							currrm++;
						} catch (NullPointerException e) {
							rmLoop = false;
						} catch (IndexOutOfBoundsException e) {
							rmLoop = false;
						}
					}// while rmloop
					se.put("recoveryMethods", rmmethods);

					// get the experimental conditions
					JSONArray expConditions = new JSONArray();
					boolean ecloop = true;
					int currEc = 0;
					while (ecloop) {
						try {
							JSONObject anEc = new JSONObject();
							JSON ec = aR
									.get("ec:/base/aptamer/experiment/has_experimetal_conditions")
									.get(currEc);
							// number of rounds
							int numOfRounds = 0;
							try {
								numOfRounds = Integer
										.parseInt(ec
												.get("ec:/base/aptamer/selex_conditions/number_of_selection_rounds")
												.stringify().replace("\\", "")
												.replace("\"", ""));
								if (numOfRounds != 0 && numOfRounds > 0) {
									anEc.put("numOfRounds", numOfRounds);
								}
							} catch (NumberFormatException e) {}
							// template sequence
							JSONArray templSeqs = new JSONArray();
							boolean tempSeqLoop = true;
							int currTempSeq = 0;
							while(tempSeqLoop){
								try{
									String tSeq = ec
											.get("ec:/base/aptamer/selex_conditions/has_template_sequence")
											.get(currTempSeq).stringify().replace("\\", "")
											.replace("\"", "");
									if (tSeq.length() > 0) {
										templSeqs.put(tSeq);
									}
									currTempSeq ++;
								}catch(NullPointerException e){
									tempSeqLoop = false;
								}catch(IndexOutOfBoundsException e){
									tempSeqLoop = false;
								}
							}//tempSeqLoop
							if(templSeqs.length()>0){
								anEc.put("templateSequences", templSeqs);
							}
							
							// selection solutions
							JSONArray selSolutions = new JSONArray();
							boolean ssloop = true;
							int sscounter = 0;
							while (ssloop) {
								try {
									JSONObject aSelSolution = new JSONObject();
									JSON anSs = ec
											.get("ec:/base/aptamer/selex_conditions/has_selection_solution")
											.get(sscounter);
									try{
										String temp = anSs
												.get("ss:/base/aptamer/selection_solution/temperature")
												.stringify().replace("\\", "")
												.replace("\"", "");
										if (isNumeric(temp)) {
											aSelSolution.put("temperature", temp);
										}
									}catch(NullPointerException e){}
									try{
										String ph = anSs.get("ss:/base/aptamer/selection_solution/ph").stringify().replace("\\", "")
												.replace("\"", "");
										if(isNumeric(ph)){
											aSelSolution.put("ph", ph);
										}
									}catch(NullPointerException e){}
									
									// get the buffering agents
									JSONArray bufAgents = new JSONArray();
									boolean bufloop = true;
									int bufcounter = 0;
									while (bufloop) {
										try {
											String abuf = anSs
													.get("ss:/base/aptamer/selection_solution/has_buffering_agent")
													.get(bufcounter)
													.stringify()
													.replace("\\", "")
													.replace("\"", "");
											if (abuf.length() > 0) {
												bufAgents.put(abuf);
											}
											bufcounter++;
										} catch (NullPointerException e) {
											bufloop = false;
										} catch (IndexOutOfBoundsException e) {
											bufloop = false;
										}
									}// bufloop
									if (bufAgents.length() > 0) {
										aSelSolution.put("bufferingAgents",
												bufAgents);
									}
									
									// get the ionic strengths
									JSONArray ionicStrenghts = new JSONArray();
									boolean ionloop = true;
									int ioncounter = 0;
									while (ionloop) {
										try {
											String ais = anSs
													.get("ss:/base/aptamer/selection_solution/ionic_strength")
													.get(ioncounter)
													.stringify()
													.replace("\\", "")
													.replace("\"", "");
											if (ais.length() > 1) {
												ionicStrenghts.put(ais);
											}
											ioncounter++;
										} catch (NullPointerException e) {
											ionloop = false;
										} catch (IndexOutOfBoundsException e) {
											ionloop = false;
										}
									}// ionloop
									if (ionicStrenghts.length() > 0) {
										aSelSolution.put("ionic_strengths",
												ionicStrenghts);
									}
									selSolutions.put(aSelSolution);
									sscounter++;
								} catch (NullPointerException e) {
									ssloop = false;
								} catch (IndexOutOfBoundsException e) {
									ssloop = false;
								}
							}// ssloop
							if(selSolutions.length()>0){
								anEc.put("selectionSolutions", selSolutions);
							}

							expConditions.put(anEc);
							currEc++;
						} catch (NullPointerException e) {
							ecloop = false;
						} catch (IndexOutOfBoundsException e) {
							ecloop = false;
						}
					}// ecloop
					se.put("experimentalConditions", expConditions);
					
					//iterate over the experimental outcomes
					JSONArray expOutcomes = new JSONArray();
					boolean eoloop = true;
					int currEo = 0;
					while(eoloop){
						try{
							JSONObject anOutcome = new JSONObject();
							JSON anO = aR.get("aptamer:/base/aptamer/experiment/has_outcome").get(currEo);
							//iterate over the aptamer targets
							JSONArray tns = new JSONArray();
							boolean targetLoop = true;
							int currTarget = 0;
							while(targetLoop){
								try{
									JSON aTarget = anO.get("aptamer_target:/base/aptamer/interaction/has_participant").get(currTarget);
									String aTargetName = aTarget.get("aptamer_target:name").stringify()
											.replace("\\", "")
											.replace("\"", "");
									tns.put(aTargetName);
									currTarget ++;
								}catch(NullPointerException e){
									targetLoop = false;
								}catch(IndexOutOfBoundsException e){
									targetLoop = false;
								}
							}
							anOutcome.put("targetNames", tns);
							//iterate over the sequences 
							JSONArray seqs = new JSONArray();
							boolean seqloop = true;
							int currSeq = 0;
							while(seqloop){
								try{
									JSON aSeq = anO.get("aptamer:/base/aptamer/interaction/has_participant").get(currSeq);
									String s = aSeq.get("aptamer:/base/aptamer/linear_polymer/sequence").stringify()
											.replace("\\", "")
											.replace("\"", "");
									seqs.put(s);
									
									currSeq++;
								}catch(NullPointerException e){
									seqloop=false;
								}catch(IndexOutOfBoundsException e){
									seqloop=false;
								}
							}//seqloop
							anOutcome.put("sequences", seqs);
							//iterate over kds
							JSONArray kdArr = new JSONArray();
							boolean kdloop =true;
							int currKd = 0;
							while(kdloop){
								try{
									JSONObject aKd = new JSONObject();
									JSON k = anO.get("kd:/base/aptamer/interaction/has_dissociation_constant").get(currKd);
									try{
										String value = k.get("kd:/base/aptamer/dissociation_constant/has_value").stringify()
											.replace("\\", "")
											.replace("\"", "");
										if(this.isNumeric(value)){
											aKd.put("value", value);
										}
									}catch(NullPointerException e){}
									try{
										String tmpValue = k.get("kd:/base/aptamer/dissociation_constant/has_temporary_string_value").stringify()
												.replace("\\", "")
												.replace("\"", "");
										if(this.isNumeric(tmpValue)){
											aKd.put("tmpStringVal", tmpValue);
										}
									}catch(NullPointerException e){}
									try{
										String error = k.get("kd:/base/aptamer/dissociation_constant/has_error").stringify()
												.replace("\\", "")
												.replace("\"", "");
										if(error != null && error.length() >0){
											aKd.put("error", error);
										}
									}catch(NullPointerException e){}
									if(aKd.length() >0){
										kdArr.put(aKd);
									}
									currKd++;
								}catch(NullPointerException e){
									kdloop=false;
								}catch(IndexOutOfBoundsException e){
									kdloop=false;
								}
							}//kdLoop
							anOutcome.put("kds", kdArr);
							//iterate over the affinity experiments
							JSONArray affExpArr = new JSONArray();
							boolean affloop = true;
							int curraff = 0;
							while(affloop){
								try{
									JSONObject affinityExp = new JSONObject();
									JSON ae = anO.get("affinity_exp:/base/aptamer/interaction/is_confirmed_by").get(curraff);
									JSONArray bindingSols = new JSONArray();
									boolean bindloop = true;
									int currBindingSol = 0;
									while(bindloop){
										try{
											JSONObject aBindSol = new JSONObject();
											JSON x = ae.get("affinity_exp:/base/aptamer/experiment/has_experimetal_conditions").get(0).get("affinity_exp:/base/aptamer/affinity_conditions/has_binding_solution").get(currBindingSol);
											//get the buffering agents
											JSONArray bufAgents = new JSONArray();
											boolean bufloop = true;
											int bufcounter = 0;
											while (bufloop) {
												try {
													String abuf = x
															.get("binding_sol:/base/aptamer/binding_solution/has_buffering_agent")
															.get(bufcounter)
															.stringify()
															.replace("\\", "")
															.replace("\"", "");
													if (abuf.length() > 0) {
														bufAgents.put(abuf);
													}
													bufcounter++;
												} catch (NullPointerException e) {
													bufloop = false;
												} catch (IndexOutOfBoundsException e) {
													bufloop = false;
												}
											}// bufloop
											if (bufAgents.length() > 0) {
												aBindSol.put("bufferingAgents",
														bufAgents);
											}
											//get the temperatures
											JSONArray temps = new JSONArray();
											boolean temploop = true;
											int tempCounter = 0;
											while (temploop) {
												try {
													String temp = x
															.get("binding_sol:/base/aptamer/binding_solution/temperature")
															.get(tempCounter)
															.stringify()
															.replace("\\", "")
															.replace("\"", "");
													if (temp.length() > 0) {
														temps.put(temp);
													}
													tempCounter++;
												} catch (NullPointerException e) {
													temploop = false;
												} catch (IndexOutOfBoundsException e) {
													temploop = false;
												}
											}// temploop
											if(temps.length()>0){
												aBindSol.put("temperatures", temps);
											}
											//get the ph
											JSONArray phs = new JSONArray();
											boolean phloop = true;
											int phcounter = 0;
											while (phloop) {
												try {
													JSON y = x;
													String ph = x
															.get("binding_sol:/base/aptamer/binding_solution/ph")
															.get(phcounter)
															.stringify()
															.replace("\\", "")
															.replace("\"", "");
													if (ph.length() > 0) {
														temps.put(ph);
													}
													phcounter++;
												} catch (NullPointerException e) {
													phloop = false;
												} catch (IndexOutOfBoundsException e) {
													phloop = false;
												}
											}// temploop
											if(phs.length()>0){
												aBindSol.put("phs", phs);
											}
											
											
											//get the ionic strengths
											JSONArray ionS = new JSONArray();
											boolean ionloop = true;
											int ioncounter = 0;
											while (ionloop) {
												try {
													JSON y = x;
													String ionstr = x
															.get("binding_sol:/base/aptamer/binding_solution/ionic_strength")
															.get(ioncounter)
															.stringify()
															.replace("\\", "")
															.replace("\"", "");
													if (ionstr.length() > 0) {
														ionS.put(ionstr);
													}
													ioncounter++;
												} catch (NullPointerException e) {
													ionloop = false;
												} catch (IndexOutOfBoundsException e) {
													ionloop = false;
												}
											}// temploop
											if(ionS.length()>0){
												aBindSol.put("inonicStrengths", ionS);
											}
											
											
											
											if(aBindSol.length() >0){
												bindingSols.put(aBindSol);
											}
											currBindingSol ++;
										}catch(NullPointerException e){
											bindloop=false;
										}catch(IndexOutOfBoundsException e){
											bindloop=false;
										}
									}//bindloop
									if(bindingSols.length()>0){
										affinityExp.put("bindingSolutions", bindingSols);
									}
								
									if(affinityExp.length() >0){
										affExpArr.put(affinityExp);
									}
									curraff++;
								}catch(NullPointerException e){
									affloop=false;
								}catch(IndexOutOfBoundsException e){
									affloop=false;
								}
							}//affloop
							anOutcome.put("affinityExperiments", affExpArr);
							
							
							
							
							
							expOutcomes.put(anOutcome);
							currEo++;
						}catch(NullPointerException e){
							eoloop=false;
						}catch(IndexOutOfBoundsException e){
							eoloop=false;
						}
					}//ecloop
					se.put("experimentalOutcomes",expOutcomes );
					
					experiments.put(se);
					currResult++;
				} catch (NullPointerException e) {
					lf = false;
				} catch (IndexOutOfBoundsException e) {
					lf = false;
				}
			}// while lf
			output.put(experiments);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//set the offset
		JSONObject os = new JSONObject();
		try{
			os.put("totalNumberOfResults", totalNumberOfResults);	
			os.put("page_length", currResult);
		}catch(JSONException e){
			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}finally{
			if(os.length()>0){
				output.put(os);
			}
		}
		out.println(output);

	}



	/**
	 * Add a list of aptamer types to the incoming JSON queyr
	 * 
	 * @param types
	 *            a list of types : rna|dna|peptide
	 * @param aQuery
	 *            a template json query
	 * @return a json query with the specified aptamer types
	 */
	private JSON addAptamerType(List<String> types, JSON aQuery) {
		if (types != null && types.size() > 0) {
			Iterator<String> itr = types.iterator();
			JSON aTs = a();
			while (itr.hasNext()) {
				String atype = itr.next();
				if (atype.equalsIgnoreCase("rna")) {
					aTs.put("/base/aptamer/rna");
				}
				if (atype.equalsIgnoreCase("dna")) {
					aTs.put("/base/aptamer/dna");
				}
				if (atype.equalsIgnoreCase("peptide")) {
					aTs.put("/base/aptamer/peptide");
				}
			}
			aQuery.get(0).get("/base/aptamer/experiment/has_outcome").get(0)
					.get("aptamer:/base/aptamer/interaction/has_participant")
					.get(0).put("e:type|=", aTs);
			return aQuery;
		}
		return null;
	}

	/**
	 * Add a target name to the incoming JSON query
	 * 
	 * @param aTargetName
	 *            a name of a target
	 * @param aQuery
	 *            a template json query
	 * @return a json query with a target name restriction
	 */
	private JSON addTargetName(String aTargetName, JSON aQuery) {
		if (aTargetName != null && aTargetName.length() > 0) {
			aQuery.get(0)
					.get("aptamer:/base/aptamer/experiment/has_outcome")
					.get(0)
					.get("aptamer_target:/base/aptamer/interaction/has_participant")
					.get(0).put("aptamer_target:name", aTargetName);
			return aQuery;
		}
		return aQuery;
	}

	/**
	 * Check if string is a positive number
	 * 
	 * @param s
	 * @return
	 */
	private boolean isNumeric(String s) {
		return s.matches("^[+]?[0-9]*\\.?[0-9]+([eE][-]?[0-9]+)?");
	}

	/**
	 * Create a template query to display basic information about the selex
	 * experiments
	 * 
	 * @return
	 */
	private JSON getTemplateQuery() {

		JSON r = a(o(
				"aptamer:/base/aptamer/experiment/has_outcome",a(o(
					"kd:/base/aptamer/interaction/has_dissociation_constant",a(o(
						"kd:/base/aptamer/dissociation_constant/has_value",null,
						"kd:/base/aptamer/dissociation_constant/has_value_range",a(o(
							"/measurement_unit/floating_point_range/low_value",null,
							"/measurement_unit/floating_point_range/high_value",null,
							"optional", true
						 )),
					"kd:/base/aptamer/dissociation_constant/has_error",null,
					"kd:/base/aptamer/dissociation_constant/has_temporary_string_value",null,
					"optional", true
					)),
					"affinity_exp:/base/aptamer/interaction/is_confirmed_by",a(o(
						"affinity_exp:mid",null,
						"affinity_exp:/base/aptamer/experiment/has_experimetal_conditions",a(o(
							"affinity_exp:/base/aptamer/affinity_conditions/has_binding_solution",a(o(
								"binding_sol:/base/aptamer/binding_solution/has_buffering_agent",a(),
								"binding_sol:/base/aptamer/binding_solution/ionic_strength",a(),
								"binding_sol:/base/aptamer/binding_solution/ph",a(),
								"binding_sol:/base/aptamer/binding_solution/temperature",a(),
								"optional", true
							 )),
							 "optional",true
						)),
						"optional", true
					)),
					"aptamer:/base/aptamer/interaction/has_participant",a(o(
						"aptamer:name",null,
						"aptamer:mid",null,
						"aptamer:type","/base/aptamer/aptamer",
						"aptamer:/base/aptamer/linear_polymer/sequence",null
					)),
					"aptamer_target:/base/aptamer/interaction/has_participant",a(o(
						"aptamer_target:/base/aptamer/aptamer_target/has_type",	a(o()),
							"aptamer_target:name", null,
							"aptamer_target:type","/base/aptamer/aptamer_target",
							"aptamer_target:mid", null
						))
				)),
				"mid",null,
				"name",null,
				"type","/base/aptamer/selex_experiment",
				"selex_method:/base/aptamer/selex_experiment/has_selex_method",a(o(
					"selex_method:name", null, 
					"selex_method:mid", null,
					"optional", true
				)),
				"pmid:/base/aptamer/experiment/pubmed_id",a(),
				"partitioning_method:/base/aptamer/selex_experiment/has_partitioning_method",a(o(
					"partitioning_method:mid", null,
					"partitioning_method:has_separation_method", null,
					"optional", true
				)),
				"recovery_method:/base/aptamer/selex_experiment/has_recovery_method_ne",a(o(
					"recovery_method:mid", null,
					"optional", true,
					"recovery_method:has_recovery_method",a(o(
						"recovery_method:name", null
					 ))
				)),
				"ec:/base/aptamer/experiment/has_experimetal_conditions",a(o(
						"ec:mid",null,
						"optional", true,
						"ec:/base/aptamer/selex_conditions/number_of_selection_rounds",null,
						"ec:/base/aptamer/selex_conditions/has_template_sequence",a(),
						"ec:/base/aptamer/selex_conditions/has_selection_solution",a(o(
							"ss:mid",null,
							"optional", true,
							"ss:/base/aptamer/selection_solution/has_buffering_agent",a(),
							"ss:/base/aptamer/selection_solution/ph",null,
							"ss:/base/aptamer/selection_solution/ionic_strength",a(),
							"ss:/base/aptamer/selection_solution/temperature",null
						))
						,"optional", true
						))
						));
		return r;
	}

	/**
	 * Iterate over all request variables and return an array of all values
	 * 
	 * @param aptaType
	 *            the name of the variable to find in the request
	 * @param aMap
	 *            a map of all the variables in the request
	 * @return a list of all requested aptamer types
	 */
	private List<String> getAptamerTypes(String aptaType,
			Map<String, String[]> aMap) {
		ArrayList<String> returnMe = new ArrayList<String>();

		if (aMap.containsKey(aptaType)) {
			String[] aptaTypes = aMap.get(aptaType);
			for (String aType : aptaTypes) {
				returnMe.add(aType.trim());
			}
		}
		return returnMe;
	}

	private String getTargetName(String tn, Map<String, String[]> requestMap) {
		String returnMe = "";
		if (requestMap.containsKey(tn)) {
			String[] tnames = requestMap.get(tn);
			returnMe = tnames[0].trim();
		}
		return returnMe;
	}
	/**
	 * Retrieve the offset- the total number of rows that have already been passed to that user
	 * @param of
	 * @param requestMap
	 * @return
	 */
	private Integer getOffset(String of, Map<String, String[]> requestMap) {
		Integer rm = null;
		if(requestMap.containsKey(of)){
			String[] x = requestMap.get(of);
			if(x.length > 0){
				return Integer.parseInt(x[0].trim());
			}
		}
		return rm;
	}

	private int getLimit(String limit, Map<String, String[]> requestMap) {
		int returnMe = 18;
		if (requestMap.containsKey(limit)) {
			String[] limits = requestMap.get(limit);
			try{
				returnMe = Integer.parseInt(limits[0].trim());
			}catch(NumberFormatException e){
				returnMe = 18;
			}
		}
		return returnMe;
	}

	private String getCursor(String cursor, Map<String, String[]> requestMap) {
		String returnMe = "";
		if (requestMap.containsKey(cursor)) {
			String[] c = requestMap.get(cursor);
			returnMe = c[0].trim();
		}
		return returnMe;
	}

}
