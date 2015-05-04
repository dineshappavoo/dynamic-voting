
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
			    
			    if(messageObj.messageType.equals(MessageType.REQUEST_READ_LOCK))
			    {
			    	//TODO For Request Read lock
			    	//Check if write lock is granted
			    	//IF GRANTED
			    	
			    	//ELSE
			    	grantLock(messageObj.getNodeInfo().getHostId(), Boolean.TRUE);
			    		
			    }
			    else if(messageObj.messageType.equals(MessageType.REQUEST_WRITE_LOCK))
			    {
			    	//TODO for Request Write lock
			    	//Check if write lock is not already granted AND read lock is also not granted
			    	//IF GRANTED
			    	
			    	//ELSE
			    	grantLock(messageObj.getNodeInfo().getHostId(), Boolean.FALSE);
			    }
			    else if(messageObj.messageType.equals(MessageType.RESPONSE_READ))
			    {
			    	if(messageObj.status.equals(Status.GRANT))
			    	{
			    		//TODO for READ GRANT
			    	}
			    	else if(messageObj.status.equals(Status.DENY))
			    	{
			    		//TODO for READ DENY
			    	}
			    }
			    else if(messageObj.messageType.equals(MessageType.RESPONSE_WRITE))
			    {
			    	if(messageObj.status.equals(Status.GRANT))
			    	{
			    		//TODO for WRITE GRANT
			    	}
			    	else if(messageObj.status.equals(Status.DENY))
			    	{
			    		//TODO for WRITE DENY
			    	}
			    }
			    else if(messageObj.messageType.equals(MessageType.RELEASE_READ_LOCK))
			    {
			    	//Update the read lock granted map
			    		
			    }
			    else if(messageObj.messageType.equals(MessageType.RELEASE_WRITE_LOCK))
			    {
			    	//Update the write lock granted map
			    }
			    else if(messageObj.messageType.equals(MessageType.REQUEST_LATEST_FILE_READ))
			    {
			    	//Update the read lock granted map
			    		
			    }
			    else if(messageObj.messageType.equals(MessageType.REQUEST_LATEST_FILE_WRITE))
			    {
			    	//Update the write lock granted map
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

	//TO BE MOVED TO SERVICE CLASS
	public void grantLock(int node_id, Boolean isReadLock)
	{
		if(isReadLock)	
		{
			//Set Grant read lock hashmap
		}
		else 
		{
			//Set Grant Write lock hashmap
		}
			
			//Send Grant READ MESSAGE
			//Set Host data
			Host source = new Host();
			source.setHostId(0); //TODO: SET WITH THE CURRENT NODEID
			source.setHostName("hostName");//TODO: SET WITH HOSTNAME
			source.setHostPort(0);//TODO: SET WITH HOST PORT
			
			Host destination = new Host();
			destination.setHostId(nodeId);
			Host node = nodeMap.get(nodeId);
			destination.setHostPort(node.getHostPort());
			destination.setHostName(node.getHostName());
			
			
			// Increment logical timestamp
			
			// Increment vector timestamp
			
			Message msgObj = new Message();
			msgObj.setNodeInfo(source);
			if(isReadLock)
			{
				msgObj.setMessageType(MessageType.RESPONSE_READ);
			}
			else 
			{
				msgObj.setMessageType(MessageType.RESPONSE_WRITE);
			}
			msgObj.setStatus(Status.GRANT);
			//TODO: SET TIMESTAMPS - LOGICAL AND VECTOR
			
			//Client for sending message
			RCClient rcClient = new RCClient(destination, msgObj);
			rcClient.go();
		//}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	public synchronized void requestAllKeys()
	{
		Host host;
		for(int nId : nodeMap.keySet())
		{
			host = nodeMap.get(nId);
			if(!host.keyKnown && nodeId != host.hostId && nodeMap.get(host.hostId).isRequested != true )
			{
				startRCClient(host, MessageType.REQUEST_KEY);
				nodeMap.get(host.hostId).isRequested=true;
			}
		}
	}

	public void sendTermination()
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

	}

	public boolean isAllNodeKeysKnown()
	{
		Host host;
		for(int nId : nodeMap.keySet())
		{
			host = nodeMap.get(nId);
			if(!host.keyKnown && host.hostId != nodeId)
			{
				return false;
			}
		}
		return true;
	}


	public String sTime()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}

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

	public synchronized void startRCClient(Host host, MessageType sMessageType)
	{		
		if(host.hostId != nodeId)
		{
			if(sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY && nodeMap.get(host.hostId).isRequested == true)
			{
				sMessageType = MessageType.RESPONSE_KEY; 
			}

			if(sMessageType == MessageType.REQUEST_KEY && (nodeMap.get(host.hostId).isRequested == true || nodeMap.get(host.hostId).keyKnown == true) )
			{
				return;
			}

			if(sMessageType == MessageType.REQUEST_KEY)
			{
				nodeMap.get(host.hostId).isRequested = true;
			}
			RCClient  rCClient;
			Message message;
			//out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  Starting the client to request for a key to "+host.hostName+" at port "+host.hostPort);

			message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
			rCClient = new RCClient(host, message);
			new Thread(rCClient).start();
			//System.out.println("[INFO]	["+sTime()+"]	Node Id "+nodeId+"  client requested for a key to "+host.hostName);
		}

	}

	public synchronized void startRCClients(PriorityQueue<Message> minHeap, MessageType sMessageType)
	{		
		int size = minHeap.size();
		//out.println("Min Heap Size : "+size);
		int nNumOfThreads=size;
		Thread[] tThreads = new Thread[nNumOfThreads];
		RCClient  rCClient;
		Message message;

		//Already known hosts - MAY BE USEFUL FOR LIST OF GOT RESPONSE NODES
		/*ArrayList<Host> currentAdjList = new ArrayList<Host>();
		for(int nodeID : nodeMap.keySet())
		{
			currentAdjList.add(nodeMap.get(nodeID));
		}*/
		int i=0;
		currentNodeCSEnterTimestamp.incrementAndGet();

		while(minHeap.size()>0)
		{
			//System.out.println("Size inside while loop : "+size);
			Message m = minHeap.poll();
			if(m!=null)
			{
				Host host = m.sourceNode;
				//out.println("[INFO]	["+sTime()+"]	WAITING-LIST	Node Name : "+host.hostName);
				if (nodeId!=host.hostId)
				{

					if(sMessageType == MessageType.REQUEST_KEY && nodeMap.get(host.hostId).keyKnown == true)
					{
						return;
					}
					//Increment the count only on requests
					if(sMessageType == MessageType.RESPONSE_KEY || sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY)
					{
						nodeMap.get(host.hostId).keyKnown = false;
						if(sMessageType == MessageType.RESPONSE_AND_REQUEST_KEY)
						{
							nodeMap.get(host.hostId).isRequested=true;
						}
					}
					if(sMessageType == MessageType.REQUEST_KEY)
					{
						nodeMap.get(host.hostId).isRequested = true;
					}
					message = new Message(currentNodeCSEnterTimestamp, sMessageType, nodeMap.get(nodeId));
					//System.out.println("[INFO]	["+sTime()+"]	Message Type : "+message.messageType);

					rCClient = new RCClient(host, message);
					tThreads[i] = new Thread(rCClient);
					tThreads[i].start();
					i++;
				}
			}
		}
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