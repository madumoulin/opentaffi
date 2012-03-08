package code.google.com.opentaffi;

import java.awt.Rectangle;

/**
 * Facilitator class to encapsulate pinch data.
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 */
public class Pinch extends Rectangle {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Angle of rotation in radiant
	 */
	private final float angle;

	/**
	 * 
	 * @param x position along x axis
	 * @param y position along y axis
	 * @param w width
	 * @param h height
	 * @param angle angle of rotation in radiant
	 */
	public Pinch(int x, int y, int w, int h, float angle)
	{
		super(x, y, w, h);
		this.angle = angle;
	}

	/**
	 * @return The angle of rotation in radiants.
	 */
	public float getAngle() {
		return angle;
	}

}
