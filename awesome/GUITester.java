/**
 * 
 */
package awesome;

import java.util.Vector;


/**
 * @author bobby
 *
 */
public class GUITester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ParkingLotGUI(new Vector<GateRecord>(), new Vector<Car>(), 0, ParkingLotGUI.NAIVE, 25);

	}

}
