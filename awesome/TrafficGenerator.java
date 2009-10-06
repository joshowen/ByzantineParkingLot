package awesome;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import moses.member.Answer;
import moses.member.Member;
import moses.util.Const;

/**
 * TrafficGenerator- creates traffic for gates
 * @author Bobby, Aravin, Josh, Dunxu
 *
 */
public class TrafficGenerator {
	private Vector gates;
	private String monitor;
	private LinkedList inMessageQueue = new LinkedList();
	private LinkedList outMessageQueue = new LinkedList();
	private static String monitorAddress;
	private String LGIServer;
	private Member m;
	private static TrafficGenerator instance;
	private int nextIDNumberForCar;
	private String name;
	private Listener listerner ;
	private Sender sender ;
	private Master master ;
	private int minuteLength = 100;
	private boolean poisson = false;

	
	
	
	/**
	 * Private Constructor for Traffic Generator
	 * @param LGIServer - LGI address of LGI Server
	 * @param monitor - LGI address of parking lot monitor
	 * @return - The first instance of Traffic Generator
	 * For an instance of the traffic generator use
	 * <code>TrafficGenerator tg = TrafficGenerator.getInstance()</code>
	 */
	private TrafficGenerator(String LGIServer,String monitorAddress){
		//this.monitorAddress = monitorAddress;
		gates = new Vector();
		nextIDNumberForCar = 1;
		 m =
	         new Member(
		          "http://www.moses.rutgers.edu/cs431_2008/simple.java1",
		          Const.URL_LAW,
		          LGIServer,
			  9000,
			  "TrafficGenerator");
		m.adopt("", "");
		this.LGIServer = LGIServer;
		name = m.longName;
		
		listerner = new Listener();
		sender = new Sender();
		master = new Master();
		master.start();
		sender.start();
		listerner.start();
				
	}
	
	
	/**
	 * Remove gate from list of known gates
	 * @param g - Address of gate to be removed
	 */
	void deleteGate(String g){
		synchronized(gates){
			for(int i = 0; i < gates.size(); i++){
				if (((String)gates.get(i)).equals(g))
					gates.remove(i);
			}
		}
	}
	
	/**
	 * Private method to send car to random gate 
	 */
	private void sendCar()
	{
	
		synchronized(gates)
		{
			if(!gates.isEmpty())
			{
				String[] content = new String[1];
				content[0] = "Car"+ nextIDNumberForCar;
				Random r = new Random();
				//int gate = Math.abs(r.nextInt()%gates.size());
				//nextIDNumberForCar++;
				int gate = 1;
				Message msg = new Message(5, content, (String)gates.get(gate), "TrafficGenerator");
				sender.addMsg(msg);

			}
		}
	}
	
	private int poisson(double c) { // c is the intensity
		   int x=0;
		   double t=0.0;
		   for (;;) {
		      t -= Math.log(Math.random())/c;
		      if (t > 1.0)
		          return x;
		      x++;
		   }
		}

	
	
	
	/**
	 * Private method to simulate low level of traffic
	 */
	private void low(){
		if(poisson){
			int p = poisson(1);
			for(int i=0;i<p;i++){
				sendCar();
			}
		}else{
			sendCar();
		}
		try 
		{
		 Thread.sleep(minuteLength/2);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}
	
	
	
	/**
	 * Private method to simulate medium traffic
	 */
	private void medium(){
		if(poisson){
			int p = poisson(3);
			for(int i=0;i<p;i++){
				sendCar();
			}
		}else{
			sendCar();
			sendCar();
			sendCar();
		}
		try {
			Thread.sleep(minuteLength/2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}
	
	
	
	/**
	 * Private method to simulate high traffic
	 */
	private void high(){
		if(poisson){
			int p = poisson(5);
			for(int i=0;i<p;i++){
				sendCar();
			}
		}else{
			sendCar();
			sendCar();
			sendCar();
			sendCar();
			sendCar();
		}
		try {
			Thread.sleep(minuteLength/2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}	
	
	
	
	/**
	 * Creator method for traffic generator
	 * @param LGIServer - Address of LGI Server
	 * @param monitor - Address of Parking Lot Monitor
	 * @return - instance of Traffic Generator
	 */
	public static TrafficGenerator getInstance(String LGIServer,String monitor){
		if(instance==null){
			instance = new TrafficGenerator(LGIServer,monitor);
			return instance;
		}else{
			return instance;
		}
	}
	
	
	
	
	
	/**
	 * Listener Control Thread
	 * 
	 *
	 */
	class Listener extends Thread{
	
		/**
		 * read one message from the member
		 * @return the new message
		 */
		Message readOneIncomingMessage()
		{
			Answer a=  m.generic_receive_lg();
			String strMsg = a.s_payload;
			Message msg = Message.parseStringToMessage(strMsg,a.source,name +"@"+LGIServer);
			return msg;
			
		}
		
		
		/**
		 * 
		 * report a arrived message to monitor
		 */
		 void reportArriveMessageToMonitor(Message msg)
		{
			
			String[] str = new String[3];
			str[0]=msg.toString();
			str[2] =""+ Calendar.getInstance().getTimeInMillis();
			str[1]=msg.source; 
			msg= new Message(11,str,"Monitor@"+LGIServer,name);
	    	sender.addMsg(msg);
		}
		
		 /**
			 * add new gate from broadcasting message
			 * @param gateList broadcast message's content
			 */
			void addNewGates(String gateList[])
			{
				for(int i = 2;i<gateList.length;i++)
				{
				 String g = gateList[i];
				 if(!gates.contains(g)) // if g is not in list and g is not myself
				  gates.add(g);
				 
				}
				
			}
		
		public void run()
		{
			while(true)
			{
				Message msg = readOneIncomingMessage();
				reportArriveMessageToMonitor(msg);
				switch(msg.type)
				{
				
					case 3: // Add new gates to gate vector
						addNewGates(msg.content);
					break;
					
					case 13://minuteLength
						minuteLength = Integer.parseInt(msg.content[0]);
						break;
					default:
						break;
				}
			}
		}
	}
	
	/**
	 * Sender Control Thread
	 * 
	 *
	 */
	class Sender extends Thread
	{
		
		public  void addMsg(Message m)
		{
			
			synchronized(outMessageQueue)
			{
				outMessageQueue.add(m);
				outMessageQueue.notify();
			}
		}
		
		
		public void reportSentToMonitor(Message msg)
		{
			if(msg.type<10){
				//Send a copy to the monitor
				String[] s = new String[3];
				s[0] = msg.toString();
				s[1] = msg.toAddress;
				s[2] = ""+Calendar.getInstance().getTimeInMillis();
				Message reportM = new Message(10,s,"Monitor@" + LGIServer,name);
				m.send_lg(reportM.toString(),reportM.toAddress);
			}
			
		}
		
		
		public void run()
		  {
			Message msg = null;

			while(true)
			 {
				synchronized(outMessageQueue)
				{	
				  while(outMessageQueue.isEmpty())
				 {
				     try
			         {
				    	outMessageQueue.wait();
			         }
			         catch (InterruptedException ie) {}
				 }
				  msg = (Message)outMessageQueue.removeFirst();  
			    }
				
				m.send_lg(msg.toString(), msg.toAddress);
				reportSentToMonitor(msg);
				}
			}

	}
	
	/**
	 * Master Control Thread
	 */
	class Master extends Thread
	{
		public void run()
		{
			while(true){
				
				// Main Loop for sending Cars and Time Simulation
				for(int i = 0; i < 360; i++)
				{
					if (i < 30){ 		//7-8am
						high();
					}else if(i < 90){ 	// 8-10am
						high();
					}else if(i < 270){ 	// 10-4pm
						high();
					}else if(i < 330){ 	// 4-6pm
						high();
					}else{				// 6-7pm
						high();
					}
				}
			}
		}
	}
	
	
	
	
}
