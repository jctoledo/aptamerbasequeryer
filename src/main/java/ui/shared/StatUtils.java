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

import java.util.Iterator;
import java.util.List;

/**
 * @author Jose Cruz-Toledo
 * 
 */
public class StatUtils {
	public static List<Double> normalize(List<Double> aList) {
		Iterator<Double> itr = aList.iterator();

		while (itr.hasNext()) {
			Double d = itr.next();
		}
		return null;
	}
	
	/**
	 * Finds the lowest positive double in a list. Only the non negative lowest
	 * double is returned. If no positive doubles are found -1.0 will be
	 * returned
	 * 
	 * @param aList
	 *            a list of doubles
	 * @return the lowest positive dobule in the list. If no positive doubles
	 *         are found -1.0 will be returned
	 */
	public static Double getMin(List<Double> aList) {
		Double lowest = Double.POSITIVE_INFINITY;
		boolean check = false;
		for (Iterator<Double> iterator = aList.iterator(); iterator.hasNext();) {
			Double d = iterator.next();
			if (lowest >= d && d > 0) {
				lowest = d;
				check = true;
			}
		}
		if (check) {
			return lowest;
		} else {
			return -1.0;
		}
	}
	/**
	 * @param coords
	 * @param string
	 * @return
	 */
	public static Double computeVariance(
			List<Coordinate<Double, Double, Double>> coords, String column) {
		double[] values = new double[coords.size()];
		int i = 0;
		if (column.equalsIgnoreCase("sequence")) {
			for (Coordinate<Double, Double, Double> d : coords) {
				values[i] = d.getX();
				i++;
			}
		} else if (column.equalsIgnoreCase("kd")) {
			for (Coordinate<Double, Double, Double> d : coords) {
				values[i] = d.getY();
				i++;
			}
		}
		if (values.length > 0) {
			org.apache.commons.math3.stat.StatUtils.variance(values);
		}
		return null;
	}
	public static Double computeAverage(List<Double> aList) {
		Double sum = 0.0;
		if (aList.size() > 0) {
			for (Iterator<Double> iterator = aList.iterator(); iterator
					.hasNext();) {
				Double double1 = iterator.next();
				sum += double1;
			}
			return sum / aList.size();
		} else {
			return 0.0;
		}
	}
	
	
	public static Double getMax(List<Double> aList) {
		Iterator<Double> itr = aList.iterator();
		if (aList.size() > 0) {
			Double max = aList.get(0);
			while (itr.hasNext()) {
				Double d = itr.next();
				if (d > max) {
					max = d;
				}
			}
			return max;
		}
		return null;
	}
}
