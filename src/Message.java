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
	REQUEST_READ_LOCK,REQUEST_WRITE_LOCK,RESPONSE_READ, RESPONSE_WRITE, RELEASE_READ_LOCK, 
	RELEASE_WRITE_LOCK, REQUEST_LATEST_FILE_READ, REQUEST_LATEST_FILE_WRITE, RESPONSE_LATEST_FILE_READ, RESPONSE_LATEST_FILE_WRITE,
	RECOVERY_REQUEST_BROADCAST, RECOVERY_RESPONSE_WRITE, RECOVERY_RESPONSE_READ;
}

enum Status {
	GRANT, DENY;
}
public class Message implements Serializable{

	AtomicInteger logicalTimeStamp;
	AtomicInteger[] vectorTimestamp;
	MessageType messageType;
	Status status;
	Host nodeInfo;
	FileInfo fileInfo;
	byte[] fileContent;
	public Message(AtomicInteger timeStamp,	AtomicInteger[] vectorTimestamp, MessageType messageType, Status status, Host nodeInfo, FileInfo fileInfo, byte[] fileContent)
	{
		this.logicalTimeStamp = timeStamp;
		this.vectorTimestamp = vectorTimestamp;
		this.messageType = messageType;
		this.status =status;
		this.nodeInfo = nodeInfo;
		this.fileInfo = fileInfo;
		this.fileContent = fileContent;
	}
	public Message() {
		// TODO Auto-generated constructor stub
	}
	
	public FileInfo getFileInfo() {
		return fileInfo;
	}
	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}
	public AtomicInteger getLogicalTimeStamp() {
		return logicalTimeStamp;
	}
	public void setLogicalTimeStamp(AtomicInteger logicalTimeStamp) {
		this.logicalTimeStamp = logicalTimeStamp;
	}
	public AtomicInteger[] getVectorTimestamp() {
		return vectorTimestamp;
	}
	public void setVectorTimestamp(AtomicInteger[] vectorTimestamp) {
		this.vectorTimestamp = vectorTimestamp;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Host getNodeInfo() {
		return nodeInfo;
	}
	public void setNodeInfo(Host nodeInfo) {
		this.nodeInfo = nodeInfo;
	}
	public byte[] getFileContent() {
		return fileContent;
	}
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
}
