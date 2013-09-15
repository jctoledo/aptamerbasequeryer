/**
 * Copyright (c) 2012  Jose Cruz-Toledo
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

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class makes use of NCBI's efetch to get info about a pmid
 * 
 * @author Jose Cruz-Toledo
 * 
 */
public class PubmedInfo {

	public String pmid = "";
	public int yearOfPublication = -1;
	private String efetchUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";
	private String scheme = "http";
	private String host = "eutils.ncbi.nlm.nih.gov";
	private String path = "/entrez/eutils/efetch.fcgi";

	public PubmedInfo(String aPMID){
		pmid = aPMID;
		yearOfPublication = fetchPublicationYear(aPMID);
	}

	private int fetchPublicationYear(String aPMID) {
		String q = "db=pubmed&id=" + aPMID + "&rettype=xml";
		URLReader ur = new URLReader(scheme, host, path, q);
		String urlStr = ur.getContents();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(urlStr));
			Document document = builder.parse(is);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath
					.compile("//PubmedArticle/MedlineCitation/Article/Journal/JournalIssue/PubDate/Year/text()");
			Object result = expr.evaluate(document, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			return Integer.parseInt(nodes.item(0).getNodeValue());
		
		}catch(SocketException e){
			e.printStackTrace();
			return -1;
		} catch(UnknownHostException e){
			e.printStackTrace();
			return -1;
		}catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			return -1;
		}
		return -1;
	}

	public int getPublicationYear() {
		return yearOfPublication;
	}
	
	public URL getPmidURL(){
		if(this.pmid != ""){
			try {
				return new URL("http://www.ncbi.nlm.nih.gov/pubmed/"+this.pmid);
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((efetchUrl == null) ? 0 : efetchUrl.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((pmid == null) ? 0 : pmid.hashCode());
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
		result = prime * result + yearOfPublication;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PubmedInfo))
			return false;
		PubmedInfo other = (PubmedInfo) obj;
		if (efetchUrl == null) {
			if (other.efetchUrl != null)
				return false;
		} else if (!efetchUrl.equals(other.efetchUrl))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (pmid == null) {
			if (other.pmid != null)
				return false;
		} else if (!pmid.equals(other.pmid))
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		if (yearOfPublication != other.yearOfPublication)
			return false;
		return true;
	}
}
