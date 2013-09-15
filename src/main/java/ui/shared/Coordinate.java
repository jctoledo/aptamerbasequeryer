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

/**
 * A class to describe a coodinate tuple in a 2d graph
 * 
 * @author Jose Cruz-Toledo
 * 
 */
public class Coordinate <X, Y, Z>{
	
	private X x = null;
	private Y y = null;
	private Z z = null;
	private String mid = ""; //the topic mid

	public Coordinate(X anX, Y anY, Z anZ) {
		this.x = anX;
		this.y = anY;
		this.z = anZ;
	}

	/**
	 * @param anMid
	 * @param x_val
	 * @param y_val
	 * @param z_val
	 */
	public Coordinate(String anMid, X x_val, Y y_val, Z z_val) {
		this(x_val,y_val, z_val);
		mid = anMid;
	}

	/**
	 * @return the x
	 */
	public X getX() {
		return x;
	}

	public String getMid(){
		return mid;
	}
	/**
	 * @return the y
	 */
	public Y getY() {
		return y;
	}
	public Z getZ(){
		return z;
	}

	@Override
	public String toString() {
		return "Coordinate [x=" + x + ", y=" + y + ", z=" + z + ", mid=" + mid
				+ "]";
	}


}
