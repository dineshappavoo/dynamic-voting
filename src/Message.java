import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 */

/**
 * @author Dany
 *
 */
enum MessageType {
	READ_REQUEST,READ_RESPONSE,WRITE_REQUEST, WRITE_RESPONSE,TERMINATION_MESSAGE;
}
enum MessageStatus {
	ACCEPT, DENY;
}
public class Message implements Serializable{
	AtomicInteger timeStamp;
	MessageType messageType;
	MessageStatus messageStatus;
	Host nodeInfo;
	public Message(AtomicInteger timeStamp, MessageType messageType, MessageStatus messageStatus, Host nodeInfo)
	{
		this.timeStamp = timeStamp;
		this.messageType = messageType;
		this.messageStatus = messageStatus;
		this.nodeInfo = nodeInfo;
	}
}
