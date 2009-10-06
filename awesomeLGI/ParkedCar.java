package awesomeLGI;
import java.util.*;


/**
 * Car class that represents a car ,only used in parkinglot 
 * @author Josh,Dunxu
 *
 */
public class ParkedCar implements Comparable{
	String gateFrom;
	public Calendar leaveTime;
	String carID;
	
	/**
	 * only for test
	 * */
	 private ParkedCar(String n)
	{
		leaveTime = Calendar.getInstance();
		carID = n;
		
	}
	
	 /**
	  * 
	  * @param gateFrom String that lists the gate address the car came from.  Used in determining where to send back to
	  * @param leaveTime Calendar that specifies when the car should leave the Parking Lot
	  * @param carID
	  */
	public ParkedCar(String gateFrom,Calendar leaveTime,String carID){
		this.gateFrom =gateFrom ;
		this.leaveTime = leaveTime ;
		this.carID = carID ;
	}
	
	/**
	 * return 1 if this leaves later
	 * -1 if this leaves earlier
	 */
	public int compareTo(Object o){
		long diff = leaveTime.getTimeInMillis() - ((ParkedCar)o).leaveTime.getTimeInMillis();
		if(diff>0){
			return 1;
		}else if(diff==0){
			return 0;
		}else{
			return -1;
		}
	}
	
	/**
	 * Checks if 2 car objects are the same
	 */
	public boolean equals(Object o)
	{
		ParkedCar c = (ParkedCar)o;
		return  c.carID.equals(this.carID);
	}
	/**
	 * Standard to string method, gives info about carid and when it leaves
	 */
	public String toString()
	{
		return "carID "+ carID+" leaves at "+leaveTime.getTimeInMillis();
	}



}



