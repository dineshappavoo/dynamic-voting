
/**
 * @author Dany
 *
 */
import java.io.*;
import java.net.*;

import com.sun.nio.sctp.*;
import com.sun.xml.internal.ws.api.message.Packet;

import java.nio.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
public class RCServer extends DynamicVoting implements Runnable{
	public static final int MESSAGE_SIZE = 1000;
	public static boolean hasAllTerminated = false;
	//private HashMap<Integer, Host> nodeMap;
	//private int nodeId;
	//private int nWaitingForTerminationResponseCount;

	public RCServer()
	{
		//this.nodeMap = nodeMap; 
		//this.nodeId = nodeId;
		//this.nWaitingForTerminationResponseCount = nWaitingForTerminationResponseCount;
	}

	public void go()
	{
		//Buffer to hold messages in byte format
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		String message;

		try
		{
			//Open a server channel
			SctpServerChannel sctpServerChannel = SctpServerChannel.open();
			int port = nodeMap.get(nodeId).hostPort;
			System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"	Port : "+port);
			//Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(port);
			//Bind the channel's socket to the server in the current machine at port 5000
			sctpServerChannel.bind(serverAddr);


			System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  SERVER STARTED");
			System.out.println("[INFO]	["+sTime()+"]	==================================");
			ServiceSimulation service = new ServiceSimulation();
			Thread.sleep(7000);
			minHeap = getPriorityQueue();
			//preEmptQueue = new ArrayList<Host>();

			//Server goes into a permanent loop accepting connections from clients			
			while(true)
			{
				SctpChannel sctpChannel = sctpServerChannel.accept();
				MessageInfo messageInfo = sctpChannel.receive(byteBuffer,null,null);
				byteBuffer.clear();
				ByteArrayInputStream in = new ByteArrayInputStream(byteBuffer.array());
				ObjectInputStream os = new ObjectInputStream(in);
				Message messageObj = (Message) os.readObject();
				int hostId = messageObj.getNodeInfo().getHostId();
				String fileId = messageObj.getFileInfo().getFileId();
				if(messageObj.messageType.equals(MessageType.REQUEST_READ_LOCK))
				{
					//TODO For Request Read lock
					//Check if write lock is granted
					if(!service.checkLock(false,fileId) && !isWriting)
					{
						service.grantLock(hostId, Boolean.TRUE, fileId); //CALL FROM SERVICE CLASS
					}

				}
				else if(messageObj.messageType.equals(MessageType.REQUEST_WRITE_LOCK))
				{
					//TODO for Request Write lock
					//Check if write lock is not already granted AND read lock is also not granted
					if(!service.checkLock(false, fileId) &&
							!service.checkLock(true, fileId) && !isWriting)
					{
						service.grantLock(hostId, Boolean.FALSE, fileId);
					}
				}
				else if(messageObj.messageType.equals(MessageType.RESPONSE_READ))
				{
					if(messageObj.status.equals(Status.GRANT))
					{
						//TODO for READ GRANT
						if(timerOff)
						{
							service.sendDenyLockMessage(hostId, Boolean.TRUE, fileId);
						}
						else
						{
							synchronized(readLockReceived)
							{
								FileInfo fileInfo = new FileInfo();
								fileInfo.setFileId(fileId);
								fileInfo.setVersionNumber(messageObj.getFileInfo().getVersionNumber());
								fileInfo.setReplicaUpdated(messageObj.getFileInfo().getReplicaUpdated());
								fileInfo.setLock(true);
								readLockReceived.get(hostId).put(fileInfo.getFileId(), fileInfo);
							}
						}
					}
					else if(messageObj.status.equals(Status.DENY))
					{
						synchronized(readLockReceived)
						{
							readLockReceived.get(hostId).get(fileId).setLock(false);
						}
					}
				}
				else if(messageObj.messageType.equals(MessageType.RESPONSE_WRITE))
				{
					if(messageObj.status.equals(Status.GRANT))
					{
						if(timerOff)
						{
							service.sendDenyLockMessage(hostId, Boolean.FALSE, fileId);
						}
						else
						{
							synchronized(writeLockReceived)
							{
								FileInfo fileInfo = new FileInfo();
								fileInfo.setFileId(fileId);
								fileInfo.setVersionNumber(messageObj.getFileInfo().getVersionNumber());
								fileInfo.setReplicaUpdated(messageObj.getFileInfo().getReplicaUpdated());
								fileInfo.setLock(true);
								writeLockReceived.get(hostId).put(fileInfo.getFileId(), fileInfo);
							}
						}
					}
					else if(messageObj.status.equals(Status.DENY))
					{
						synchronized(writeLockReceived)
						{
							writeLockReceived.get(hostId).get(fileId).setLock(false);
						}
					}
				}
				else if(messageObj.messageType.equals(MessageType.RELEASE_READ_LOCK))
				{
					synchronized(readLockGranted)
					{
						readLockGranted.get(hostId).get(fileId).setLock(false);
					}
				}
				else if(messageObj.messageType.equals(MessageType.RELEASE_WRITE_LOCK))
				{
					synchronized(writeLockGranted)
					{
						writeLockGranted.get(hostId).get(fileId).setLock(false);
					}
				}
				else if(messageObj.messageType.equals(MessageType.REQUEST_LATEST_FILE_READ))
				{
					service.sendUpdatedFileVersion(hostId, Boolean.TRUE, fileId);

				}
				else if(messageObj.messageType.equals(MessageType.REQUEST_LATEST_FILE_WRITE))
				{
					service.sendUpdatedFileVersion(hostId, Boolean.FALSE, fileId);
				}
				else if(messageObj.messageType.equals(MessageType.RESPONSE_LATEST_FILE_READ))
				{
					//TO BE REPLACED WITH METHOD FOR FILEINFO UPDATE METHODS IN SERVICE
					service.pushDataFromMemoryToFile(messageObj.getFileInfo().getFileId(), messageObj.getFileContent());
					DynamicVoting.fileInfoMap.put(messageObj.getFileInfo().getFileId(), messageObj.getFileInfo());
					DynamicVoting.isWaitingForUpdate = false;

				}
				else if(messageObj.messageType.equals(MessageType.RESPONSE_LATEST_FILE_WRITE))
				{
					//TO BE REPLACED WITH METHOD FOR FILEINFO UPDATE METHODS IN SERVICE
					service.pushDataFromMemoryToFile(messageObj.getFileInfo().getFileId(), messageObj.getFileContent());
					DynamicVoting.fileInfoMap.put(messageObj.getFileInfo().getFileId(), messageObj.getFileInfo());
					DynamicVoting.isWaitingForUpdate = false;
				}
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();	
		}catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}catch(ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}


	


	/*public void sendTermination()
	{
		Host host = null;
		nodeMap.get(nodeId).isTerminated=true;
		Thread a[] = new Thread[nodeMap.keySet().size()];
		int index=0;
		//currentNodeCSEnterTimestamp.incrementAndGet();

		for(int nId : nodeMap.keySet())
		{
			if(nId != nodeId)
			{
				host = nodeMap.get(nId);
				RCClient  rCClient;
				Message message;
				//out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Starting the client to send termination message to "+host.hostName+" at port "+host.hostPort);

				message = new Message(currentNodeCSEnterTimestamp, MessageType.TERMINATION_MESSAGE, nodeMap.get(nodeId));
				rCClient = new RCClient(host, message);
				a[index]= new Thread(rCClient);
				a[index].start();
				index++;
			}
			//startRCClients(minHeap, MessageType.TERMINATION_MESSAGE);
		}

		Boolean isAllDone = true;
		for(int i : nodeMap.keySet()){
			if(i != nodeId)
			{
				//out.println("[INFO]	["+sTime()+"]	Node: "+nodeMap.get(i).hostName);
				if(!nodeMap.get(i).isTerminated){
					isAllDone = false;
				}
			}
		}
		if(isAllDone)
		{
			for(int j=0;j<a.length;j++)
			{

				while(a[j] != null && a[j].isAlive());
			}
			System.out.println("[INFO]	["+sTime()+"]	Terminating Server at node "+nodeId);
			System.exit(0);
		}
		isTerminationSent = true;

	}*/

//	public boolean isAllNodeKeysKnown()
//	{
//		Host host;
//		for(int nId : nodeMap.keySet())
//		{
//			host = nodeMap.get(nId);
//			if(!host.keyKnown && host.hostId != nodeId)
//			{
//				return false;
//			}
//		}
//		return true;
//	}
//
//
//	public String sTime()
//	{
//		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
//		return timeStamp;
//	}

	public String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	public void writeOutputToFile() throws FileNotFoundException, UnsupportedEncodingException
	{
		String fileName = "node"+nodeId+".txt";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.println("================================================");
		writer.println("Discovered all nodes in this network");
		writer.println("Total No. of Nodes : "+nodeMap.size());
		writer.println("================================================");
		writer.println("               LIST OF NODES                    ");
		writer.println("================================================");
		for(int n : nodeMap.keySet())
		{
			Host hNode = nodeMap.get(n);
			writer.println(hNode.hostId+"	"+hNode.hostName+"	 "+hNode.hostPort);
		}
		writer.close();
	}


	public void run()
	{
		go();
	}

	public static void main(String args[])
	{
		//SctpServerdc01 SctpServerObj = new SctpServerdc01();
		//SctpServerObj.go();
	}

}