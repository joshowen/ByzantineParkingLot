package awesomeLGI;
import java.util.*;
/**
 * Class used to keep track of cars path through system
 *
 */
public class Car {
	//Sequential ID of Car
	int carID;
	//Gate car enters from
	String gateIn;
	//Gate car leaves from
	String gateOut;
	//Time car leaves TrafficGenerator
	Calendar fromTG;
	//Time car arrives at Gate
	Calendar toGate;
	//Time car leaves Gate
	Calendar fromGate;
	//Time car arrives at ParkingLot
	Calendar toPL;
	//Time car leaves ParkingLot
	Calendar fromPL;
	//Time car arrives at Gate to exit ParkingLot
	Calendar backToGate;
}
