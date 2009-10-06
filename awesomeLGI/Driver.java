package awesomeLGI;
import java.util.*;
import moses.member.*;
import moses.util.*;

/**
 * Driver class that starts the simulation
 * @author Josh, Dunxu
 *
 */
public class Driver {

	/**
	 * Args[0] is the LGI server
	 * @param args
	 */
	public static void main(String[] args) {
		Monitor m = Monitor.getInstance(args[0],1000,2);
		
		String monitorAddress = m.getAddress();
		ParkingLot p = ParkingLot.getInstance(args[0],monitorAddress, 500);
		TrafficGenerator t = TrafficGenerator.getInstance(args[0],monitorAddress);
		
		try{
		Thread.sleep(3000);}
		catch(Exception e){}
		
		
		

		BGate g6 = new BGate(args[0],monitorAddress,6);
		Gate g1 = new Gate(args[0],monitorAddress);
		Gate g2 = new Gate(args[0],monitorAddress);
		Gate g3 = new Gate(args[0],monitorAddress);
		Gate g4 = new Gate(args[0],monitorAddress);
		Gate g5 = new Gate(args[0],monitorAddress);

		
		
		try{
			Thread.sleep(10000);
		}catch(Exception e){}
	//	g5 = null;
		try{
			Thread.sleep(100000);
		}catch(Exception e){}
		System.exit(0);
				
	}

}
