
import java.util.LinkedList;
import java.util.Vector;
import moses.member.*;
import moses.util.*;
import java.util.*;

public class GateDemo {
	Member m;
	Vector Gates;
	String TG;
	String PL;
	
	
	
	public void main(String[] args){
		
		String LGIServer = args[0];
		Vector Gates = new Vector();
		Random r = new Random();
		
		 m =
	         new Member(
		          "http://www.moses.rutgers.edu/cs431_2008/simple.java1",
		          Const.URL_LAW,
		          LGIServer,
			  9000,
			  "Gate-" + r.nextInt());

		m.send_lg("register", "Monitor");  
		
		while(true){
			//Listen for new messages
			String strMsg = m.receive_lg();
			//Parse out payload
			int s= strMsg.indexOf(",")+1;
			int e = strMsg.lastIndexOf(",")-1;
			strMsg = strMsg.substring(s,e);
			//Check if its a gate address
			if(strMsg.substring(9).equals("address")){
				int start = strMsg.indexOf(")")+1;
				int end=strMsg.indexOf(")")-1;
				strMsg = strMsg.substring(start,end);
				StringTokenizer token = new StringTokenizer(strMsg, ",");
				TG = token.nextToken();
				PL = token.nextToken();
				Gates.removeAllElements();
				while(token.hasMoreTokens()){
					Gates.add(token.nextToken());
				}
			}
		}
	}

}
