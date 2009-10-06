package awesome;
import java.util.*;

import awesome.Monitor.Listener;
import awesome.Monitor.Master;
import awesome.Monitor.Sender;
import moses.member.*;
import moses.util.*;

public class ParkingLot {
	private Vector gates = new Vector();
	private PriorityQueue lot = new PriorityQueue(10);
	private LinkedList inMessageQueue = new LinkedList();
	private LinkedList outMessageQueue = new LinkedList();
	private static String monitorAddress;
	private Member m;
	private static ParkingLot instance;
	private String name;
	private int size;
	private Listener listerner ;
	private Sender sender ;
	private Master master ;
	private int minuteLength = 100;
	private String LGIServer;
	
	
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
		 
		 void addCarInLot(Message msg)
		 {
			 Random r = new Random();
			 String carId = msg.content[0];
			 Calendar cal = Calendar.getInstance();
				//Add 1-10 seconds to calendar.
				cal.add(cal.MILLISECOND, (r.nextInt()%360+1)*minuteLength);
				//assign the stay time for car, ranging between 1 to 10000 milliseconds
				ParkedCar c = new ParkedCar(msg.source,cal,carId);
				synchronized(lot)
				{
				lot.add(c);
				System.out.println("LOT SIZE " + lot.size());
				}
			 
		 }
		 
		 
		public void run(){
			while(true){
			
				Message msg = readOneIncomingMessage();
				reportArriveMessageToMonitor(msg);
				
				
				
				switch(msg.type){
				
				case 3:
					addNewGates(msg.content);
					break;
					
				case 6:  //Car into lot
					
					
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
	

	class Sender extends Thread
	{
		
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
		
		public  void addMsg(Message m)
		{
			
			synchronized(outMessageQueue)
			{
				outMessageQueue.add(m);
				outMessageQueue.notify();
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
	
	
	
	class Master extends Thread
	{
		
		/**
		 * wait for a certain amount of time to process a car out 
		 * @param waitTime
		 */
		void waitToProcessCar(int waitTime)
		{
			try{
				Thread.sleep(waitTime*minuteLength);//TODO process time Needs to be set, 50 millseconds just a guess
			}catch(Exception e){}
			
		}
		
		public void run()
		{
			while(true)
			{
				//Check if lot should send car
				synchronized(lot)
				{
					if(!lot.isEmpty())
					{
						Calendar cal = Calendar.getInstance();
						while((!lot.isEmpty()) && ((ParkedCar)lot.peek()).leaveTime.getTimeInMillis()<=cal.getTimeInMillis())
							sendCar((ParkedCar)lot.remove());
					}
	
				}
				
				waitToProcessCar(2);
			}
		}
	}
	
	
	/**
	 * Sends car to a gate
	 * @param c Car to send
	 */
	void sendCar(ParkedCar c){
		String[] content = new String[1];
		content[0]=c.carID;
		//Send car back to the gate it came from 
		//String gate = c.gateFrom;
		//Message msg = new Message(7,content,gate,"ParkingLot");
		
		//Send car back to a random gate
		
		Random r = new Random();
		String gate = (String)gates.elementAt(Math.abs(r.nextInt())%gates.size());
		Message msg = new Message(7,content,gate,"ParkingLot");
		
		
		sender.addMsg(msg);
	}

	
	private ParkingLot(String LGIServer,String monitorAddress, int lotSize){
		m =
	         new Member(
		          "http://www.moses.rutgers.edu/cs431_2008/simple.java1",
		          Const.URL_LAW,
		          LGIServer,
			  9000,
			  "ParkingLot");
		m.adopt("", "");
		name = m.longName;
		this.size = lotSize;
		this.LGIServer = LGIServer;
		listerner = new Listener();
		sender = new Sender();
		master = new Master();
		master.start();
		sender.start();
		listerner.start();
		
	}
	
	public static ParkingLot getInstance(String LGIServer,String monitorAddress2, int lotSize){
		if(instance==null){
			monitorAddress = monitorAddress2;
			instance = new ParkingLot(LGIServer,monitorAddress,lotSize);
			return instance;
		}else{
			return instance;
		}
	}
}
