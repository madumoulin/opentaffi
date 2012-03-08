package code.google.com.opentaffi;

import java.util.List;


/**
 * Basic observer pattern, this class will be notified of pinches.
 * 
 * @author Luc Trudeau
 * @date 07/03/2012
 * 
 */
public interface PinchListener {

	/**
	 * If there is no pinching, pinches will be empty. If there are multiple
	 * pinch the highest pinch (Y axis) will be first in the list. THIS MEANS
	 * THAT IF THE HAND MOVE THE SAME PINCH MIGHT CHANGE POSITION IN THE LIST!
	 * 
	 * @param pinches
	 *            List of pinches occurring right now.
	 */
	void pinch(List<Pinch> pinches);
}
