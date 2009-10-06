
import java.util.LinkedList;
import java.util.Vector;
import moses.member.*;
import moses.util.*;

public class MonitorDemo {
	Member m;
	Vector Gates;
	public void main(String[] args){
		String LGIServer = args[0];
		Vector Gates = new Vector();
		 m =
	         new Member(
		          "http://www.moses.rutgers.edu/cs431_2008/simple.java1",
		          Const.URL_LAW,
		          LGIServer,
			  9000,
			  "Monitor");
		 
		while(true){
			//Listen for messages
			String strMsg = m.receive_lg();
			//Parse out from
			int fs = strMsg.indexOf("(")+1;
			int fe = strMsg.indexOf(",")-1;
			String from = strMsg.substring(fs,fe);
			//Parse out payload
			int s= strMsg.indexOf(",")+1;
			int e = strMsg.lastIndexOf(",")-1;
			strMsg = strMsg.substring(s,e);
			//Is it a registration?
			if(strMsg.substring(7).equals("register")){
				Gates.add(from);
			}
			
			//Notify Gates
			//Build msg
			strMsg = "addresses(TG,PL";
			for(int i=0;i<Gates.size();i++){
				strMsg = strMsg + "," + (String)Gates.elementAt(i);
			}
			strMsg = strMsg + ")";
			//Send msg
			for(int i=0;i<Gates.size();i++){
				m.send_lg(strMsg,(String)Gates.elementAt(i));  
			}
		}
	}

}
