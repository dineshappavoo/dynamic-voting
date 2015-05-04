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
	REQUEST_READ_LOCK,REQUEST_WRITE_LOCK,RESPONSE_READ, RESPONSE_WRITE, RELEASE_READ_LOCK, RELEASE_WRITE_LOCK, REQUEST_LATEST_FILE_READ, REQUEST_LATEST_FILE_WRITE;
}

enum Status {
	GRANT, DENY;
}
public class Message implements Serializable{
	
	int logicalTimeStamp;
	int[] vectorTimestamp;
	MessageType messageType;
	Host sourceNode;
	Host clientNode;
	Status status;
	
	public int getLogicalTimeStamp() {
		return logicalTimeStamp;
	}
	public void setLogicalTimeStamp(int logicalTimeStamp) {
		this.logicalTimeStamp = logicalTimeStamp;
	}
	public int[] getVectorTimestamp() {
		return vectorTimestamp;
	}
	public void setVectorTimestamp(int[] vectorTimestamp) {
		this.vectorTimestamp = vectorTimestamp;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public Host getSourceNode() {
		return sourceNode;
	}
	public void setSourceNode(Host sourceNode) {
		this.sourceNode = sourceNode;
	}
	public Host getClientNode() {
		return clientNode;
	}
	public void setClientNode(Host clientNode) {
		this.clientNode = clientNode;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	
}
