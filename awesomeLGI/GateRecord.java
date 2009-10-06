package awesomeLGI;
import java.util.*;
public class GateRecord {
	List<Car> waitingCars = new Vector<Car>();
	//Messages not yet matched
	List<ReportMessage> outstandingMessagesTo = new Vector<ReportMessage>();
	List<ReportMessage> outstandingMessagesFrom = new Vector<ReportMessage>();
	//Messages already matched
	List<MessageStore> messages = new Vector<MessageStore>();
	 
	String address;
	int tokens;
	int queueSize;
	int lastIndexed;
	int lastCarAdmitted;
	
	public boolean finds(String name){
		//System.out.println("address is "+address);
		//System.out.println("name is "+name);
		//System.out.println("value is "+address.equals(name));
		return address.equals(name);
	}
	public GateRecord(String address){
		this.address = address;
	}
	public void updateTokens(int tokens){
		this.tokens = tokens;
	}
	public void messageRecvd(ReportMessage rm){
		MessageStore ms = new MessageStore();
		ms.rm = rm;
		ms.from = false;
		synchronized(ms){
			messages.add(ms);
		}
	}
	public void messageSent(ReportMessage rm){
		MessageStore ms = new MessageStore();
		ms.rm = rm;
		ms.from = true;
		synchronized(ms){
			messages.add(ms);
		}
	}
	public void CheckMessageSent(ReportMessage rm){
		for(int i=0;i<outstandingMessagesFrom.size();i++){
			if(outstandingMessagesFrom.get(i).equals(rm)){
				synchronized(outstandingMessagesFrom){
					outstandingMessagesFrom.remove(i);
				}
				System.out.println(rm.msg.toString());
				System.out.println("Found");
				return;
			}
		}
		synchronized(outstandingMessagesTo){
			outstandingMessagesTo.add(rm);
		}
	}
	public void CheckMessageAck(ReportMessage rm){
		for(int i=0;i<outstandingMessagesTo.size();i++){
			if(outstandingMessagesTo.get(i).equals(rm)){
				synchronized(outstandingMessagesTo){
					outstandingMessagesTo.remove(i);
				}
				System.out.println(rm.msg.toString());
				System.out.println("Found");
				return;
			}
		}
		synchronized(outstandingMessagesFrom){
			outstandingMessagesFrom.add(rm);
		}
	}
}
