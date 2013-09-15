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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;


/**
 * @author  Jose Cruz-Toledo
 *
 */
public  class FreebaseCredentials {
	//private static String key = "AIzaSyAyzMNH9hGWG9OxAIV09LvG5tcz2E1o9lY";
	//private static String key = "AIzaSyB8ouPZ2w1rkMS3bGL6PVNJm6AHLTKFhC4";//old
	private static String key = "AIzaSyDY0YaK8CcdN24iJ2y1Zsg3Q2O6vBTkZ5s";
	private final static String scheme = "https";
	private final static String host = "www.googleapis.com";
	private final static String path = "/freebase/v1/mqlread";
	
	private String retrieveDevKey(){
		String rm = "";
		InputStream is =FreebaseCredentials.class.getClassLoader().getResourceAsStream("google_dev_key.txt");
		try {
			key = IOUtils.toString(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		IOUtils.closeQuietly(is);
		return rm;
	}
	
	
	
	/**
	 * @return the key
	 */
	public static String getKey() {
		return key;
	}
	/**
	 * @return the scheme
	 */
	public static String getScheme() {
		return scheme;
	}
	/**
	 * @return the host
	 */
	public static String getHost() {
		return host;
	}
	/**
	 * @return the path
	 */
	public static String getPath() {
		return path;
	}
}
