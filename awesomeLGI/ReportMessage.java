package awesomeLGI;

public class ReportMessage {
	String source;
	String dest;
	Message msg;
	long localTime;
	long remoteTime;
	public boolean equals(ReportMessage rm){
		if(!source.equals(rm.source)) return false;
		if(!dest.equals(rm.dest))return false;
		if(!msg.contentEquals(rm.msg))return false;
		return true;
	}
}
