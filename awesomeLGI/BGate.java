package awesomeLGI;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import moses.member.Answer;
import moses.member.Member;
import moses.util.Const;


/**
 * 
 * bad gate
 * */

public class BGate {

	/**
	 * 1 for greedy token request 
	 * 2 for not reporting message //Not Valid anymore
	 * 3 for not let car in //TODO
	 * 4 for lifo process car
	 * 5 for create token
	 * 6 for lost token//TODO
	 */
	private int type;
	/**
	 * vector which contains a list of all gates
	 */
	private Vector gates = new Vector();
	
	/**
	 * vector which contains a list of cars waitting to get into this gate
	 */
	private LinkedList cars = new LinkedList();
	
	
	/**
	 * number of tokens this gate currently has
	 */
	private Integer tokens;
	
	/**
	 * queue of out going messages 
	 */
	private LinkedList outMessageQueue = new LinkedList();
	
	/**
	 * address of the monitor
	 */
	private String monitorAddress;

	
	/**
	 * the LGI member that associates with this gate
	 */
	private Member m;
	
	/**
	 * address of the LGI server
	 */
	private String LGIServer;
	
	/**
	 * name of this gate
	 */
	private String name;
	
	
	/**
	 * this gate's listener thread that listens to incomming messages
	 */
	private Listener listener ;
	
	/**
	 * sender thread that sends out messages
	 */
	private Sender sender ;
	
	/**
	 * Master thread that processes waiting cars
	 */
	private Master master ;
	
	private KeepAlive keep;
	
	/**
	 * semaphore for if a response was received
	 */
	private Boolean responseRecvd = new Boolean(true);
	
	/**
	 * Boolean variable for if a response was received
	 */
	private boolean recvdFlag;
	
	private int minuteLength=1000;
	/**
	 * Constructor to create a Gate with random name and start all the threads
	 *
	 */
	public BGate(String LGIServer,String monitorAddress,int type){
		this.type = type;
		Random r = new Random();
		List inMessageQueue = new LinkedList();
		List outMessageQueue = new LinkedList();
		this.LGIServer = LGIServer;
		this.monitorAddress = monitorAddress;
		name = "badGate-" + Math.abs(r.nextInt())%71;
		m =
	         new Member(
	        	  "http://remus.rutgers.edu/~jowen/law.java",
		          Const.URL_LAW,
		          LGIServer,
			  9000,name);
		m.adopt("", "role(Gate)");
		
		listener = new Listener();
		sender = new Sender();
		master = new Master();
		keep = new KeepAlive();
		master.start();
		sender.start();
		listener.start();
		keep.start();
		
		registerGate();
	}
	
	
	/**
	 * Destroy the car, Create a token
	 * @param c Car that is leaving gate
	 */
	void exitCar(String car){
		
		
		
		
		
		synchronized(cars)
		{
			

		     cars.remove(car); // remove car from cars queue
		}
		synchronized(tokens)
		{
	    tokens++;
		}
	    System.out.println("Car exited from " + name + " Tokens:" + tokens);
	    //TODO send a message to monitor saying that this car is removed
	}
	
	
	
	/**
	 * Announce gate to monitor
	 *
	 */
	void registerGate(){
		
		Message msg = new Message(2,null,"Monitor"+"@"+LGIServer,name);
		sender.addMsg(msg);

	}
	
	
	
	/**
	 * equals method for Gate, gate is compared based on their name
	 */
	 public boolean equals(Object o)
	 {
		 Gate g = (Gate)o;
		 return this.name.equals(g.name); 
	 }
	
	
	
	 /**
		 * Sends ping message to LGI
		 * @author jowen
		 *
		 */
		 class KeepAlive extends Thread{
			 public void run(){
				 while(true){
					 String[] content = new String[1];
					 content[0] = "ping";
					 Message m = new Message(12,content,"Monitor@"+LGIServer,name);
					 sender.addMsg(m);
					 System.out.println("SENT MSG");
					 try{
						 Thread.sleep(minuteLength);
					 }catch(Exception e){}
				 }
			 }
		 }
	
	
	/**
	 * listener thread that listens to incomming messages
	 * @author kajihu
	 *
	 */
	class Listener extends Thread{

		/**
		 * add new gate from broadcasting message
		 * @param gateList broadcast message's content
		 */
		void addNewGates(String gateList[])
		{
			
		
			for(int i = 2;i<gateList.length;i++)
			{
			 String g = gateList[i];
			 if(!gates.contains(g)&&!g.equals(name+"@"+LGIServer)) // if g is not in list and g is not myself
			  gates.add(g);
			 
			}
			
		}
		
		
		
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
		 * 
		 *  
		 */
		 void reportArriveMessageToMonitor(Message msg)
		{
			 if(type != 2)
			 {
			String[] str = new String[3];
			str[0]=msg.toString();
			str[2] =""+ Calendar.getInstance().getTimeInMillis();
			str[1]=msg.source; 
			msg= new Message(11,str,"Monitor@"+LGIServer,name);
	   // 	sender.addMsg(msg);
	    	//System.out.println("sent report");
			 }
		}
		
		/**
		 * assign initial mount of token 
		 * @param msg
		 */
		 void assignInitialToken(Message msg)
		 {
			 int numberOfToken = Integer.parseInt(msg.content[0]);
			 tokens = numberOfToken;
			 System.out.println("initial assignments of tokens" + tokens);
		 }
		 
		 /**
		  * receive a token request from other gates
		  * if type == 5 then it always fulfil the request and does not decrease its tokens
		  * @param msg
		  */
		 void receiveTokenRequest(Message msg)
		 {
			 
			 
				System.out.println("Recvd request for " + msg.content[0] + " from " + msg.source);

				int size;
				Message m;
				String[] content = new String[1];
				synchronized(cars)
				{
				 size = cars.size();
				}
				
				
				int requestNO = Integer.parseInt(msg.content[0]);
				
				synchronized(tokens)
				{
					
					if(type != 5)
					{
					if(requestNO<=(tokens-size)) 		//Send all tokens they need
					{
						tokens = tokens - requestNO;
						content[0] = ""+requestNO;
					}
					else if((tokens-size)>0) //Send the extra tokens you have
					{
						int send = tokens;
						tokens = 0;
						content[0] = "" + send;
					}
					else                       // send 0
						content[0] = "0";
					}
					else
						content[0] = ""+requestNO;
						
				}
				m = new Message(9,content,msg.source,name);
				sender.addMsg(m);
	
				System.out.println( "send token"+content[0]+ "to"+msg.source +" from"+name);
			 
			 
			 
		 }
		 
		 
		 /**
		  * handler for token receiving
		  * 
		  * if type==6 then it doesn't received token
		  * @param msg
		  */
		 void receiveTokens(Message msg)
		 {
			 
			 
			int receivedNO =  Integer.parseInt(msg.content[0]);
				//add the number of tokens given to me
				synchronized(tokens)
				{
				
				if(receivedNO>0)
				{
			    if(type !=6)
				tokens = tokens + receivedNO ;
				//System.out.println("tokens received "+msg.content[0]+" from " + msg.source+" at"+name );
				}
				}
				recvdFlag = true;
				synchronized(responseRecvd)
				{
				responseRecvd.notify();
				}
			 
		 }
		 
		 
		
		public void run(){
			while(true){
				
	
				Message msg = readOneIncomingMessage();
				reportArriveMessageToMonitor(msg);
				


/*2 G-->M: register
3 M-->G: addresses(T,P,G1,G2,....Gn)
4 M-->G: tokensAssigned(t)
5 T-->G: newCar(carID)
6 G-->P: enterCar(carID)
7 P-->G: exitCar(carID)
8 G-->G: RequestTokens(q) %it is the queue length of the requesting token that is to be specified
9 G-->G: transferTokens(t)  %no. of tokens being given
10 G-->M: report(sent(M,X),t)  % message M sent by G to X (X could be G, P or T) at local time t
11 G-->M: report(arrived(M,X),t)  % message M  sent by X (X could be G, P or T) arrived at G, at time t. 
12 all to all: ping|pong*/

			//	System.out.println(msg);
				
				
				switch(msg.type){

					case 3: // Add new gates to gate vector
						addNewGates(msg.content);
						break;
						
					case 4:  // Set new amount of tokens from number assigned
						assignInitialToken(msg);
						break;
						
					case 5: //new car into gate	
						master.addCar(msg.content[0]);
						break;
						
					case 7: //parkinglot sends car out to gate, the gate sends the car immediately
						exitCar(msg.content[0]);
						break;
						
					case 8: //other gate request tokens
						receiveTokenRequest(msg);
						break;
						
					case 9:   //receive tokens
						receiveTokens(msg);
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
				System.out.println("call send report msg type"+msg.type);
				//Send a copy to the monitor
				String[] s = new String[3];
				s[0] = msg.toString();
				s[1] = msg.toAddress;
				s[2] = ""+Calendar.getInstance().getTimeInMillis();
		//		Message reportM = new Message(10,s,"Monitor@" + LGIServer,name);
		//		m.send_lg(reportM.toString(),"Monitor@" + LGIServer);
		//		System.out.println("send report "+msg);
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

	
	
	class Master extends Thread{
		
		boolean flag = false;
		
		private void addCar(String car)
		{
			synchronized(cars)
			{
				cars.add(car);
				System.out.println("Car arrived at " + name + " Queue Length:" + cars.size());
				cars.notify();
			}
			
		}
		
		
		
		/**
		 * if type == 3 do not allow carin 
		 * if type == 4 let car in in reverse order
		 * otherwise let one car in , tokens down by 1
		 */
		void letCarIn()
		{
			String carid;
			if(type!=3 && type!=4)
			{
			
			carid = (String)cars.removeFirst(); //fifo
			String[] msg = new String[1];
			msg[0]= carid;
			Message m = new Message(6,msg,"ParkingLot@"+LGIServer,name);//Create message to send to lot
			tokens--;
			sender.addMsg(m);
			System.out.println("Car entered from " + name + " Tokens:" + tokens);
			flag = true;
			}
			
			if(type == 4)
			{
				carid = (String)cars.removeLast();//lifo
				String[] msg = new String[1];
				msg[0]= carid;
				Message m = new Message(6,msg,"ParkingLot@"+LGIServer,name);//Create message to send to lot
				tokens--;
				sender.addMsg(m);
				System.out.println("Car entered from " + name + " Tokens:" + tokens);
				flag = true;
				
				
			}
			
		}
		
		
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
		
		
		/**
		 * when a request time out or we didnt get any token, wait for a while till next request
		 * @param waitTime
		 */
		void waitToRequestAgain(int waitTime)
		{
			
			try   // wait for processing  one car in the queue
			{
				this.wait(waitTime * minuteLength);
			}
			catch(Exception e){}
	
		}
		
		/**
		 * request tokens form other gates
		 * every time it will request 50 more tokens
		 */
		void requestTokens()
		{
			int targetGate=0;
			while(tokens==0&&cars.size()!=0){
				String[] size = new String[1];
				if(type == 1)   //request 50 token more
				size[0]="" + cars.size()+"50";
				else
					size[0]="" + cars.size();
				System.out.println("queue size"+size[0]+"token requested:"+size[0]);
				//System.out.println(name + "@" + LGIServer + " Requesting Tokens from " + gates.get(targetGate) + " gateindex="+ targetGate );
				int gateToGet = targetGate%gates.size();
				String local = name + "@" + LGIServer;
				String target = (String)gates.get(gateToGet);
				if(!local.trim().equalsIgnoreCase(target.trim())) // do not request from itself
				{
					Message m = new Message(8,size,target,name);
					sender.addMsg(m); //send request
					
					synchronized(responseRecvd)
					{
					try //wait for time out
					{ 
					responseRecvd.wait(minuteLength *1 );
					}catch(Exception e){}
					}
				}
					
				if(tokens>0&&recvdFlag==true)//received the token
				{
					recvdFlag =false;
					break;
				}
				
				else       //time out happened ,try again
				targetGate++;

				
				waitToRequestAgain(targetGate/(gateToGet+1));
			
			}
			flag = false;
		
			
		}
		
		
		public void run()
		{
			while(true)
			{
			  
			  	synchronized(cars)
				{	
					while(cars.isEmpty()) //wait for cars to come in
					{
					     try
					     {
					    	cars.wait();
				         }
				         catch (InterruptedException ie) {}
					 }
				
					synchronized(tokens)
					{
					if(tokens>0)
						letCarIn();
					}
				}
			  	
			  	
			if(tokens<=0)
			requestTokens();
						

			if(flag = true) // if a car just enters need to wait for an amount of time
			waitToProcessCar(1/5);
				
			}
		}
	}
	
}
