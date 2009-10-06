package awesome;
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
		Monitor m = Monitor.getInstance(args[0],500,5);
		
		String monitorAddress = m.getAddress();
		ParkingLot p = ParkingLot.getInstance(args[0],monitorAddress, 500);
		TrafficGenerator t = TrafficGenerator.getInstance(args[0],monitorAddress);
		
		try{
		Thread.sleep(3000);}
		catch(Exception e){}
		
		Gate g1 = new Gate(args[0],monitorAddress);
		Gate g2 = new Gate(args[0],monitorAddress);
		Gate g3 = new Gate(args[0],monitorAddress);
		Gate g4 = new Gate(args[0],monitorAddress);
		Gate g5 = new Gate(args[0],monitorAddress);
		
		
		
		try{
			Thread.sleep(300000);}
			catch(Exception e){}
		System.exit(0);
		
/*		
		Member mem =
	         new Member(
		          "http://www.moses.rutgers.edu/cs431_2008/simple.java1",
		          Const.URL_LAW,
		          args[0],
			  9000,
			  "TEST");
		mem.adopt("", "");

		Random r = new Random();
		int i=0;
		while(i<10){
			String to = (String)m.gates.elementAt(Math.abs(r.nextInt()%m.gates.size()));
			String[] cnt = new String[1];
			cnt[0]=""+i;
			Message msg = new Message(5,cnt,to,"TEST");
			mem.send_lg(msg.toString(), msg.toAddress);
			i++;
		}
		*/
				
	}

}
