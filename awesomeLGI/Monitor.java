package awesomeLGI;
import java.util.*;

import moses.member.*;
import moses.util.*;

public class Monitor {
	private Vector<GateRecord> gates = new Vector<GateRecord>();
	private LinkedList<Message> inMessages = new LinkedList<Message>();
	private LinkedList<Message> outMessageQueue = new LinkedList<Message>();
	private Vector<Car> cars = new Vector<Car>();
	private String LGIServer;
	private String name;
	private int size;
	private int numGates;
	private Member m;
	private static Monitor instance;
	private Listener listerner;
	private Sender sender;
	private Master master;
	private int minuteLength = 100;
	private Detecter detecter;
	private ParkingLotGUI plg;
	
	private Monitor(String LGIServer,int lotSize,int numGates){
		m =
	         new Member(
	        	  "http://remus.rutgers.edu/~jowen/law.java",
		          Const.URL_LAW,
		          LGIServer,
			  9000,
			  "Monitor");
		m.adopt("", "role(Monitor)");
		
		name = m.longName;
		this.numGates=numGates;
		this.LGIServer = LGIServer;
		this.size = lotSize;
		
		plg = new ParkingLotGUI(gates, cars, this.size,ParkingLotGUI.NAIVE, minuteLength);
		
		listerner = new Listener();
		sender = new Sender();
		master = new Master();
		detecter = new Detecter();
		master.start();
		sender.start();
		listerner.start();
		//detecter.start();
	}
	
	
	/**
	 * Creates an instance of monitor
	 * @param LGIServer
	 * @param lotSize
	 * @param numGates
	 * @return
	 */
	public static Monitor getInstance(String LGIServer,int lotSize, int numGates){
		if(instance==null){
			instance = new Monitor(LGIServer,lotSize,numGates);
			return instance;
		}else{
			return instance;
		}
	}
	
	
	
	
	class Listener extends Thread{
		
		/**
		 * remove bad gate from gate list 
		 * @param bname
		 */
		void removeBadGate(String bname)
		{
			synchronized(gates){
				for(int i = 0;i<gates.size();i++)
				{
				if(bname.equals((String)gates.get(i).address))
					gates.remove(i);
					plg.updateGateRecord(gates);
				}
				//System.out.println(gates.size());
				//System.exit(1);
				for(int i = 0;i<gates.size();i++)
				{
					String[] c = new String[1];
					c[0] = bname;
					Message msg = new Message(15,c,(String)gates.get(i).address, name+"@"+LGIServer);
				 	sender.addMsg(msg);
				}
				{
					String[] c = new String[1];
					c[0] = bname;
					Message msg = new Message(15,c,"TrafficGenerator@" + LGIServer, name+"@"+LGIServer);
				 	sender.addMsg(msg);
				 	msg = new Message(15,c,"ParkingLot@" + LGIServer, name+"@"+LGIServer);
				 	sender.addMsg(msg);
				}
			}
				
		}
		
		/**
		 * Main function of monitor, handles all request and messages
		 */
		public void run(){
			System.out.println("start monitor");
			while(true){
				Answer a=  m.generic_receive_lg();;
				String source = a.source;
				String strMsg = a.s_payload;
				Message msg = Message.parseStringToMessage(strMsg,source,name+"@"+LGIServer);
				//System.out.println("RECVD " + msg + " AT " + name + " FROM " + source);
				
				//Keep the message in the log
				inMessages.add(msg);
				
				//Add message to gates Sent and Recvd lists
				switch(msg.type){
				
				case 2: //register the source gate 
					addAndBroadcastGate(source);
					tokenDist(source);
					break;
				case 10://Take note of message sent
					//System.out.println("GOT REPORT SENT MSG\n\n\n\n");
					reportMsgSent2(msg,source);
					break;
				case 11://Take note of message recvd

					//System.out.println("GOT REPORT RECVD MSG\n\n\n\n");
					reportMsgAck2(msg,source);
					break;
				case 13://minuteLength
					minuteLength = Integer.parseInt(msg.content[0]);
					plg.updateML(minuteLength);
					break;
				case 14:
					System.err.println("Gate " + a.source + " is faulty for " + msg.content[0] + " Need to add  " + msg.content[1] + " tokens");
					removeBadGate(a.source);
					String[] content = new String[1];
					content[0] = msg.content[1];
					//Add the gate's tokens to the system
					Message m = new Message(4,content,gates.get(0).address,name);
					sender.addMsg(m);
				}			
			}
		}
		/*
		 * Mon -> Gate
		 * Gate							
		 * msg sent to		...			msg recvd
		 * 
		 * 
		 * sent
		 * if to gate add gate to
		 * rec
		 * if from gate, gate add recvd
		 */
		
		/*
		 * Logs messags using ReportMessage Type
		 */
		private void reportMsgSent2(Message msg,String source){
			//Build ReportMessage
			ReportMessage rm = new ReportMessage();
			rm.source = source;
			rm.dest = msg.content[1];
			rm.remoteTime = Long.parseLong(msg.content[2]);
			rm.msg = Message.parseStringToMessage(msg.content[0],source,msg.content[1]);
			rm.localTime = Calendar.getInstance().getTimeInMillis();
			//Sent to gate
			if(msg.content[1].contains("Gate")){
				int index = indexOf(msg.content[1]);
				if(index>=0){
					gates.elementAt(index).CheckMessageAck(rm);
				}
			}
			if(source.contains("Gate")){
				int index = indexOf(source);
				if(index>=0){
					gates.elementAt(index).messageSent(rm);
				}
			}
		}
		
		/**
		 * Builds report message for ACK'd messages
		 * @param msg
		 * @param source
		 */
		private void reportMsgAck2(Message msg,String source){
			//Build ReportMessage
			ReportMessage rm = new ReportMessage();
			rm.source = msg.content[1];
			rm.dest = source;
			rm.remoteTime = Long.parseLong(msg.content[2]);
			rm.msg = Message.parseStringToMessage(msg.content[0],msg.content[1],source);
			rm.localTime = Calendar.getInstance().getTimeInMillis();
			//Arrived at gate
			//System.out.println(source);
			if(source.contains("Gate")){
				int index = indexOf(source);
				if(index>=0){
					gates.elementAt(index).CheckMessageAck(rm);
					gates.elementAt(index).messageRecvd(rm);
				}
			}
		}
		/**
		 * Builds report message for ReportMessage rec'd
		 * @param msg
		 * @param source
		 */
		private void reportMsgSent(Message msg,String source){
			//Build ReportMessage
			ReportMessage rm = new ReportMessage();
			rm.source = source;
			rm.dest = msg.content[1];
			rm.remoteTime = Long.parseLong(msg.content[2]);
			rm.msg = Message.parseStringToMessage(msg.content[0],source,msg.content[1]);
			rm.localTime = Calendar.getInstance().getTimeInMillis();
			//Find the gate
			int index = indexOf(source);
			//Check to see if it is matched
			gates.elementAt(index).CheckMessageSent(rm);
			//Add to outgoing message list
			gates.elementAt(index).messageSent(rm);
		}
		/**
		 * Builds report message for ReportMessage ack'd
		 * @param msg
		 * @param source
		 */
		private void reportMsgAck(Message msg,String source){
			//Build ReportMessage
			ReportMessage rm = new ReportMessage();
			rm.source = msg.content[1];
			rm.dest = source;
			rm.remoteTime = Long.parseLong(msg.content[2]);
			rm.msg = Message.parseStringToMessage(msg.content[0],msg.content[1],source);
			rm.localTime = Calendar.getInstance().getTimeInMillis();
			//Find the gate
			int index = indexOf(rm.msg.source);
			//Check to see if it is matched
			if(index>=0){
			gates.elementAt(index).CheckMessageAck(rm);
			}
			//Add to incoming incoming list
			index = indexOf(source);
			if(index>=0){
				gates.elementAt(index).messageRecvd(rm);
			}
		}
		/**
		 * Finsd the index of a gate, else returns -1
		 * @param source
		 * @return
		 */
		private int indexOf(String source){
			for(int i=0;i<gates.size();i++){
				if(gates.get(i).finds(source))
					return i;
			}
			return -1;
		}
	}
	
	
	/**
	 * Sends messages
	 * @author jowen
	 *
	 */
	class Sender extends Thread{
		/**
		 * Adds message to Queue, and notifies
		 * @param m
		 */
		public  void addMsg(Message m)
		{
			
			synchronized(outMessageQueue)
			{
				outMessageQueue.add(m);
				outMessageQueue.notify();
			}
		}
		/**
		 * Sends a report meessage to monitor, no longer used with LGI
		 * @param msg
		 */
		public void reportSentToMonitor(Message msg)
		{
		
			if(msg.type<10){
				//System.out.println("call send report msg type"+msg.type);
				//Send a copy to the monitor
				String[] s = new String[3];
				s[0] = msg.toString();
				s[1] = msg.toAddress;
				s[2] = ""+Calendar.getInstance().getTimeInMillis();
				Message reportM = new Message(10,s,"Monitor@" + LGIServer,name);
				m.send_lg(reportM.toString(),"Monitor@" + LGIServer);
				//System.out.println("send report "+msg);
			}
			
		}
		
		
		/**
		 * Sends messages when there is something in the queue
		 */
		public void run(){
			Message msg = null;
			while(true){
				synchronized(outMessageQueue){
					 while(outMessageQueue.isEmpty()){
						 try{
					    	outMessageQueue.wait();
				         }catch (InterruptedException ie) {}
					 }
					 msg = (Message)outMessageQueue.removeFirst();
				}
				m.send_lg(msg.toString(), msg.toAddress);
				reportSentToMonitor(msg);
				//System.out.println("Sent " + msg + " FROM " + name + " TO " + msg.toAddress);
			}
		}
	}
	
	
	/**
	 * Detects errors, 
	 * NOT USED WTIH LGI
	 * @author jowen
	 *
	 */
	class Detecter extends Thread{
		public void run(){
			while(true){
				for(int gate=0;gate<gates.size();gate++){
					/** Check for a dead gate by stale messages */
					if(staleMessagesCheck(gate)){
						//TODO Gate may be dead, or is more than 5 minutes behind on reporting messages
						System.err.println("Gate " + gate + " could be dead!");
					}
					
					/** Keep indexing the messages from where I left off (indexedTo) */
					int tokens = gates.get(gate).tokens;
					int queueSize = gates.get(gate).queueSize;
					//Parse messages from
					int size = gates.get(gate).messages.size();
					int  index = gates.get(gate).lastIndexed + 1;
					//System.out.println("size " + size + " index " + index);
					
					while(index<size){
						MessageStore ms = gates.get(gate).messages.get(index);
						//Check if it gives queue size
						if(wrongQueueSize(ms,queueSize)){
							System.out.println("Wrong Queue size");
							//TODO Queue Size is wrong
						}
						
						if(allowsWithNoTokens(ms,tokens)){
							System.out.println("Allowing in with no tokens");
							//TODO Allowing cars in without any tokens, could be off by one or 2 due to network errors
						}
						
						
						if(admitsCarsInWrongOrder(ms,gates.get(gate))){
							System.out.println("Allowing cars in the wrong order");
							//TODO Allows cars in incorrect order
						}
					
						//Parse message
						if(ms.from){
							tokens = tokens + tokensAfterMsgFrom(ms.rm.msg);
						}else{
							tokens = tokens + tokensAfterMsgTo(ms.rm.msg);
						}
						//System.out.println("TOKENS " + tokens);
						index++;
					}
					gates.get(gate).tokens = tokens;
					gates.get(gate).lastIndexed = index-1;
				}
				try{
					this.wait(5*minuteLength);
				}catch(Exception e){}
			}
		}
		/*
		 * Checks if the gate admits cars
		 */
		private boolean admitsCarsInWrongOrder(MessageStore ms, GateRecord gr){
			if(ms.from && ms.rm.msg.type==6){
				if(gr.lastCarAdmitted>Integer.parseInt(ms.rm.msg.content[0].replace("Car", ""))){
					return true;
				}
			}
			return false;
		}
		/*
		 * Checks if the gate allows cars without tokens
		 */
		private boolean allowsWithNoTokens(MessageStore ms,int tokens){
			if(ms.from && ms.rm.msg.type==6){
				if(tokens<0){
					return true;
				}
			}
			return false;
		}
		/*
		 * Checks if the carQueue size is wrong
		 */
		private boolean wrongQueueSize(MessageStore ms,int queueSize){
			if(ms.from && ms.rm.msg.type==8){
				if(ms.queueSize!=queueSize){
					return true;
				}
			}
			return false;
		}
		/*
		 * Checks if there are stale messages, indicating a dead gate
		 */
		private boolean staleMessagesCheck(int gateIndex){
			GateRecord gr = gates.get(gateIndex);
			//Check incoming messages
			long now = Calendar.getInstance().getTimeInMillis();
			synchronized(gr.outstandingMessagesFrom){
				if(gr.outstandingMessagesFrom.size()>0){
					if(now - gr.outstandingMessagesFrom.get(0).localTime > minuteLength*10){
						//System.out.println("1" + (now - gr.outstandingMessagesFrom.get(0).localTime));
						//System.out.println(gr.outstandingMessagesFrom.get(0).msg.toString());
						return true;
					}
				}
			}
			//Check outgoing messages
			synchronized(gr.outstandingMessagesTo){
				if(gr.outstandingMessagesTo.size()>0){
					if(now - gr.outstandingMessagesTo.get(0).localTime > minuteLength*10){
						//System.out.println("2" + (now - gr.outstandingMessagesTo.get(0).localTime));
						//System.out.println(gr.outstandingMessagesTo.get(0).msg.toString());
						return true;
					}
				}
			}
			return false;
		}
		/*
		 * Calculates the number of tokens the gate should have
		 */
		private int tokensAfterMsgTo(Message msg){
			switch(msg.type){
			case 4:  // Set new amount of tokens from number assigned
				return Integer.parseInt(msg.content[0]);
			case 7: //parkinglot sends car out to gate
				return 1;
			case 9:   //receive tokens
				return Integer.parseInt(msg.content[0]);
			}
			return 0;
		}
		/*
		 * Calculates the number of tokens the gate should have
		 */
		private int tokensAfterMsgFrom(Message msg){
			switch(msg.type){
			case 6://Car enters
				return -1;
			case 9://Transfer tokens
				return (-1) * Integer.parseInt(msg.content[0]);
			}
			return 0;
		}
	}
	
	class Master extends Thread{
		public void run(){
		}
	}
	
	
	/**
	 * Distribute tokens to all Gates in Gate Vector.
	 * Used to initialize gates.
	 * should be called after all gates are up
	 *
	 */
	
	void tokenDist(String source){
		//Distribute tokens to gates in gate vector
			String[] content = new String[1];
			content[0] = Integer.toString(size/numGates);
			Message m = new Message(4,content,source,"Monitor");
			sender.addMsg(m);
		
	}
	
	
	/**
	 * add the new gate to list and broadcast it to other gates
	 */
	void addAndBroadcastGate(String g){
	//	System.out.println(g);
		if(!gates.contains(g))
			{
			GateRecord gr = new GateRecord(g);
			gates.add(gr);
			plg.updateGateRecord(gates);
			notifyOfOneNewGate(g);
			notifyOfAllGateToNewGate(g);
			}
	}
	
	void deleteGate(String g){
		gates.remove(g);
	}
	
	String getAddress(){
		return m.longName;
	}
	
	
	
	/**
	 * broadcast a new gate to all gates , send type 2 message
	 *
	 */
	void notifyOfOneNewGate(String newGate)
	{
		String[] content = new String[3];
		content[0] = "TrafficGenerator";  //first is TrafficGenerator's address
		content[1] = "ParkingLot"; //second is TrafficGenerator's address
		content[2] = newGate;
			 
		//Tell the TrafficGenerator
		String dest = "TrafficGenerator@"+LGIServer;
	    Message m = new Message(3,content,dest,"Monitor");
	    sender.addMsg(m);
	    
	    //Tell the ParkingLot
	    dest = "ParkingLot@"+LGIServer;
	    m = new Message(3,content,dest,"Monitor");
	    sender.addMsg(m);
		
	    //Tell the gates
	    for(int i = 0;i<gates.size();i++)
		{
		   dest = gates.get(i).address;
		   m = new Message(3,content,dest,"Monitor");
		   sender.addMsg(m);
		}

	}
	
	
	/**
	 * broadcast a new gate to all gates , send type 2 message
	 *
	 */
	void notifyOfAllGateToNewGate(String newGate)
	{
		
		synchronized(gates)
		{
			
		String[] content = new String[gates.size()+2];
		content[0] = "TrafficGenerator";  //first is TrafficGenerator's address
		content[1] = "ParkingLot"; //second is TrafficGenerator's address
		
		for(int i = 0;i<gates.size();i++) // add all gates to message content
		content[i+2] = gates.get(i).address;

		   Message m = new Message(3,content,newGate,"Monitor");
		   sender.addMsg(m);
		}
	}
	
	
	/**
	 * broadcasts new minute length to all
	 */
	void updateMinuteLength(int minuteLength){
		this.minuteLength = minuteLength;
		String[] str = new String[1];
		str[0]=""+minuteLength;
		//Send msg to TG
		Message msg = new Message(13,str,"TrafficGenerator@" + LGIServer,name);
		synchronized(outMessageQueue){
			outMessageQueue.add(msg);
		}
		//Send msg to PL
		msg = new Message(13,str,"ParkingLot@" + LGIServer,name);
		synchronized(outMessageQueue){
			outMessageQueue.add(msg);
		}
		//Send msg to gates
		for(int i=0;i<gates.size();i++){
			msg = new Message(13,str,gates.get(i).address,name);
			synchronized(outMessageQueue){
				outMessageQueue.add(msg);
			}
		}
	}
}
