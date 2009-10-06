law(law,language(java))
import java.io.*;
import java.util.*;

public class law extends Law{
	
	public void exception(moses.message.Message m, String failurecause) {
	    System.out.println("EXCEPTION " + m.ex_po + " " + failurecause);

	}
	public void sent(String source, String message, String dest, String destlaw){
		Message m1 = new Message(0,null,null,null);
		Message m = m1.parseStringToMessage(message, source, dest);
		if(CS.has("role(Gate)")){
			reportSent(message,source,dest);
			switch(m.type){
			case 6:
				if(!checkCarSequence(Integer.parseInt(m.content[0]))){
					reportFaultyGate("Cars in incorrect sequence",source);
				}
				if(removeTokens(1)){
					doDecr("qSize",1);
					int oldAllowTime = CS.fetchInt("lastCarAdmitted");
					int newAllowTime = (int)(Calendar.getInstance().getTimeInMillis()%1000000);
					doIncr("lastCarAdmitted",newAllowTime-oldAllowTime);
					doForward();
					return;
				}
				//Not enough tokens
				reportFaultyGate("Allowed car without tokens",source);
				return;
			case 8:
				if(CS.fetchInt("tokens")>=3){
					reportFaultyGate("Requesting too many tokens",source);
					return;
				}
				if(Math.abs(Integer.parseInt(m.content[0])-CS.fetchInt("qSize"))<5){
					
					doForward();
					return;
				}
				reportFaultyGate("Requesting too many tokens",source);
				return;
			case 9:
				//System.out.println("Sent tokens " + m.content[0] + "\n\n\n\n\n");
				if(removeTokens(Integer.parseInt(m.content[0]))){
					doForward();
					return;
				}
				//Not enough tokens
				reportFaultyGate("Sent more tokens than had",source);
				return;
			case 12:
				int time = (int)(Calendar.getInstance().getTimeInMillis()%1000000);
				int oldTime = CS.fetchInt("lastUpdate");
				doIncr("lastUpdate",time-oldTime);
				return;
			case 13:
				doForward();
				return;
			}
			doForward();
			return;
		}
		doForward();
		return;
	}
	public void arrived(String source, String sourcelaw, String message, String dest) {
		Message m1 = new Message(0,null,null,null);
		Message m = m1.parseStringToMessage(message, source, dest);
		if(CS.has("role(Gate)")){
			int newTime = (int)(Calendar.getInstance().getTimeInMillis()%1000000);
			int oldTime = CS.fetchInt("lastUpdate");
			if(newTime-oldTime>10000){
				//Dead gate
				reportFaultyGate("Dead gate",source);
			}
			reportArrived(message,source,dest);
			switch(m.type){
			case 4:
				//System.out.println("INITIAL GOT TOKENS " + Integer.parseInt(m.content[0]) + "\n\n\n\n");
				int t = Integer.parseInt(m.content[0]);
				doIncr("tokens",t);
				doDeliver();
				return;
			case 5:
				doIncr("qSize",1);
				if((newTime-CS.fetchInt("lastCarAdmitted"))>15000 && CS.fetchInt("qSize")>10 && CS.fetchInt("tokens")>10){
					//System.out.println("" + (newTime-CS.fetchInt("lastCarAdmitted")) + "    " + CS.fetchInt("qSize") + "    " + CS.fetchInt("tokens"));
					reportFaultyGate("Gate not admitting cars",source);
				}
				doDeliver();
				return;
			case 7:
				doIncr("tokens",1);
				doDeliver();
				return;
			case 9:
				//System.out.println("GOT TOKEN TRANSFER + " + m.content[0] + "\n\n\n");
				int t1 = Integer.parseInt(m.content[0]);
				
				
				
				
				doIncr("tokens",t1);
				doDeliver();
				return;
			
			}
			doDeliver();
			return;
		}
		doDeliver();
		return;
	}
	
	
	public void adopted(String args) {
		doAdd(args);
		doAdd("tokens(0)");
		doAdd("qSize(0)");
		doAdd("lastCar(0)");
		int time = (int)(Calendar.getInstance().getTimeInMillis()%1000000);
		doAdd("lastUpdate(" + time + ")");
		doAdd("lastCarAdmitted("+time+")");
        System.out.println("AGENT " + Self + " WAS ADOPTED as " + args);
    }

	
    public void disconnected() {
        System.out.println("AGENT " + Self + " WAS DISCONNECTED");
        doQuit();
    }
    
    public boolean checkCarSequence(int carID){
    	if(CS.fetchInt("lastCar")<carID){
    		doIncr("lastCar",carID-CS.fetchInt("lastCar"));
    		return true;
    	}
    	return false;
    }
	public void reportFaultyGate(String reason,String source){
		String[] s = new String[2];
		s[0]=reason;
		s[1]="" + CS.fetchInt("tokens");
		Message msg = new Message(14,s,"Monitor@orzo.rutgers.edu",Self);
		doForward(Self,msg.toString(),"Monitor@orzo.rutgers.edu");
		System.out.println("faulty gate " + reason);
		doQuit();
	}
	public void reportArrived(String m,String source,String dest){
		String[] str = new String[3];
		str[0]=m;
		str[2] =""+ Calendar.getInstance().getTimeInMillis();
		str[1]=source; 
		Message msg= new Message(11,str,"Monitor",Self);
		doForward(Self,msg.toString(),"Monitor@orzo.rutgers.edu");
	}
	public void reportSent(String m,String source,String dest){
		String[] str = new String[3];
		str[0]=m;
		str[2] =""+ Calendar.getInstance().getTimeInMillis();
		str[1]=dest; 
		Message msg= new Message(11,str,"Monitor",Self);
		doForward(Self,msg.toString(),"Monitor@orzo.rutgers.edu");
	}
	/**
	 * Check if there are tokens, if so, remove them, return true
	 * Else return false
	 * @param t
	 * @return
	 */
	public boolean removeTokens(int t){
		if(CS.fetchInt("tokens")>=t){
			doDecr("tokens",t);
			return true;
		}
		return false;
	}


	/* 
	the number in front of the plain text is the message type, do not add it to the message u want to send!
	This is just an internal agreement by our group how to distinguish different type of messages!

	2 G-->M: register
	3 M-->G: addresses(T,P,G1,G2,....Gn)
	4 M-->G: tokensAssigned(t)
	5 T-->G: newCar(carID)
	6 G-->P: enterCar(carID)
	7 P-->G: exitCar(carID)
	8 G-->G: RequestTokens(q) %it is the queue length of the requesting token that is to be specified
	9 G-->G: transferTokens(t)  %no. of tokens being given
	10 G-->M: report(sent(M,X),t)  % message M sent by G to X (X could be G, P or T) at local time t
	11 G-->M: report(arrived(M,X),t)  % message M  sent by X (X could be G, P or T) arrived at G, at time t. 
	12 all to all: ping|pong
	13 monitor to all updateMinute(milliseconds)
	14 error : Error(reason)



	message content would be the arugments of the message:
	for example, for type 3 Message the content is a string array contains T P G1 ....Gn in sequence 
	for type 4, it will be a string array and array[0] is t
	for type 10, it will be a string array contains M, X and t in sequence
	 */
	public class Message implements Serializable {
	  	String toAddress;
		int type;//message type
		String[] content;//message's content
		String source;
		//static final long serialVersionUID = 0xabcdedd;
		
	
		
		
	/*create a message
	message content would be the arguments of the message:
	for example, for type 3 Message the content is a string array content contains T P G1 ....Gn in sequence 
	for type 4, it will be a string array content and content[0] is t
	for type 10, it will be a string array content contains M, X and t in sequence*/
		
		/**
		 * constructor
		 */
		Message(int type,String[] content,String to,String source)
		{
		this.type = type;
		this.content = content;
		this.toAddress = to;
		this.source = source;
		}
		
		/**
		 * parse a message in string format to a Message object
		 * 
		 */
		public Message parseStringToMessage(String m,String source, String dest) throws IllegalArgumentException
		{
			int type = 0;
			String[] content  = null;

			switch(m.charAt(0))
			{
			
			case 'b':
				  type = 15;
				  {
				  content = new String[1];
				  int firstp = m.indexOf('(');
				  int lastp = m.lastIndexOf(')');
				  content[0] = m.substring(firstp+1, lastp).trim();
				  break;
				  }
				  
			case 'p':
				type=12;
				break;
			case 'E':
				if(m.regionMatches(false, 0, "Error", 0,5 ))
				{
				content = new String[2];
				type = 14;
				int firstp = m.indexOf('(');
				int lastc = m.lastIndexOf(',');
				int lastp = m.lastIndexOf(')');
				content[0] = m.substring(firstp+1, lastc).trim();
				content[1] = m.substring(lastc+1,lastp);
				}
				break;
				
			case 'r':
				if(m.regionMatches(false, 0, "register", 0,8 ))
					type = 2;
				
				else if(m.regionMatches(false,0,"report",0,6))
				{
					content  = new String[3];
					int firstp = m.indexOf('(');
					int lastc = m.lastIndexOf(',');
					int lastp = m.lastIndexOf(')');
					String substring = m.substring(firstp+1, lastc);
					content[2] = m.substring(lastc+1,lastp);
					int lastc1 = substring.lastIndexOf(',');
					int lastp1 = substring.lastIndexOf(')');
					int firstp1 = substring.indexOf('(');
					content[0] = substring.substring(firstp1+1, lastc1).trim(); //M
					content[1] =  substring.substring(lastc1+1,lastp1).trim();
					
					if(m.regionMatches(false,7,"sent",0,4))  //type 10   G-->M: report(sent(M,X),t)
						type = 10;
					else if(m.regionMatches(false,7,"arrived",0,7)) //type 11
						type = 11;
				}	
				else throw new IllegalArgumentException("illegal message "+m);	
				break;
				
			case 'a':
				if(m.regionMatches(false, 0, "addresses", 0,9)) //type 3
				{
					type = 3;
					int firstp = m.indexOf('(');
					int lastp = m.lastIndexOf(')');
					String t  = m.substring(firstp+1,lastp).trim();
					content = t.split(",");
				}
				else throw new IllegalArgumentException();
				break;
				
			case 't':
				if(m.regionMatches(false, 0, "tokensAssigned", 0,14)) //type 4
					type = 4;
				else if(m.regionMatches(false, 0, "transferTokens", 0,14 )) //type 9
					type = 9;
				else throw new IllegalArgumentException("illegal message "+m);
				{
				int firstp = m.indexOf('(');
				int lastp = m.lastIndexOf(')');
				content  = new String[1];
				content[0] = m.substring(firstp+1,lastp).trim();
				}
				break;
			
			case 'n':
				if(m.regionMatches(false, 0, "newCar", 0,6)) //type 5
				{
					type = 5;
					int firstp = m.indexOf('(');
					int lastp = m.lastIndexOf(')');
					content = new String[1];
					content[0] = m.substring(firstp+1,lastp).trim();
				}
				else throw new IllegalArgumentException("illegal message "+m);
				break;
			case 'e':
				if(m.regionMatches(false, 0, "enterCar", 0,8 ))      //type 6
					type = 6;
				else if(m.regionMatches(false, 0, "exitCar", 0,7)) //type 7
					type = 7;
				else throw new IllegalArgumentException("illegal message "+m);
			
				{
				int firstp = m.indexOf('(');
				int lastp = m.lastIndexOf(')');
				content  = new String[1];
				content[0] = m.substring(firstp+1,lastp).trim();
				}
				break;
			case 'R':
				if(m.regionMatches(false, 0, "RequestTokens", 0,13 )) //type 8
				{
					type = 8;
					int firstp = m.indexOf('(');
					int lastp = m.lastIndexOf(')');
					content  = new String[1];
					content[0] = m.substring(firstp+1,lastp).trim();
				}
				else throw new IllegalArgumentException("illegal message "+m);
				break;
			case 'u':
				if(m.regionMatches(false, 0, "updateMinute", 0,12 ))      //type 13
				{
					type = 13;
					int firstp = m.indexOf('(');
					int lastp = m.lastIndexOf(')');
					content  = new String[1];
					content[0] = m.substring(firstp+1,lastp).trim();		
				}
				else throw new IllegalArgumentException("illegal message "+m);
				break;
				
			default:
				throw new IllegalArgumentException("illegal message "+m);	
			}
			
			Message mess = new Message(type,content,source,dest);
			return mess;
		}


		
		public String toString()
		{
			
			switch(this.type)
			{
			case 0:
				 return null;
			case 2:
				//System.out.println("type 2");
				return "register";
			case 3:
				String s = "";
				int i;
				for(i = 0;i<content.length-1;i++)
				s=s+content[i]+',';
				s=s+content[i];
				return "addresses("+s+")";
			case 4:
				return "tokensAssigned("+content[0]+")";
			case 5:
				return "newCar("+content[0]+")";
			case 6:
				return "enterCar("+content[0]+")";
			case 7:
				return "exitCar("+content[0]+")";
			case 8:
				return "RequestTokens("+content[0]+")";
			case 9:
				return "transferTokens(" +content[0]+")";
			case 10:
				return "report(sent("+content[0]+","+content[1]+"),"+content[2] + ")";
			case 11:
				return "report(arrived("+content[0]+","+content[1]+"),"+content[2]+")";
			case 12:
				return "ping("+content[0]+")";
			case 13:
				return "updateMinute("+content[0]+")";
			case 14:
				return "Error("+content[0]+"," + content[1] + ")";
			case 15:
				return "badgate("+content[0]+")";
			}
		
			
			return null;
		}
	}

}





