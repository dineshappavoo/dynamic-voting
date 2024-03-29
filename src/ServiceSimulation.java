/**
 * 
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
/**
 * @author Dany
 *
 */
public class ServiceSimulation implements Runnable{

	/**
	 * @param args
	 */
	public static String projectDir = "/home/eng/a/axt131730/AOS_Proj3";
	public int nodeCount = 45;
	public PrintWriter out;
	public static Random rand;//= new Random();
	static int noOfOperations;
	static int meanDelay;
	public Logger logger;
	static int noOfReadOperations;
	static int noOfWriteOperations;
	private static int nodeId;


	public ServiceSimulation(int nodeId, int noOfOperations, int meanDelay, Logger logger)
	{
		this.noOfOperations = noOfOperations;
		this.nodeId = nodeId;
		this.meanDelay = meanDelay;
		this.logger = logger;
	}

	public ServiceSimulation()
	{
		;
	}

	public byte[] loadFileContentToMemory(String filename) throws Exception
	{
		Path path = Paths.get(filename);
		byte[] data = Files.readAllBytes(path);
		System.out.println("Data : "+data);
		return data;
	}

	public void pushDataFromMemoryToFile(String fileName, byte[] data)
	{
		try
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
			out.println(data);

		}catch(IOException ex)
		{
			out.close();
			ex.printStackTrace();	
		}
	}

	public void createFiles(int noOfFiles)
	{
		String fileDir = projectDir+"files";

		for(int i=0;i<nodeCount;i++)
		{
			for(int j=0;j<noOfFiles;j++)
			{
				try
				{
					String nodeDir = fileDir+"/node"+i;
					File nodeDirFile = new File(nodeDir);
					if(!nodeDirFile.exists()){
						nodeDirFile.mkdirs();
					}
					String fileName = fileDir+"/node"+i+"/file"+j+".txt";
					out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
					out.println(System.currentTimeMillis());

				}catch(IOException ex)
				{
					if(out!=null){
						out.close();
					}
					ex.printStackTrace();	
				}
			}
		}
	}


	public void initiateVoting()
	{
		int operationsCompleted = 0;
		int readOperationsCompleted = 0;
		int writeOperationsCompleted = 0;

		float percentage = ((float)DynamicVoting.fractionOfOperations / (float)100);

		noOfReadOperations  = (int) (((float)noOfOperations) * (percentage));
		noOfWriteOperations = noOfOperations - noOfReadOperations;

		int coinToss = 0;
		int fileNumber;
		String fileName = "";
		String fileId = "";

		System.out.println("INITIATED THE PROTOCOL");
		System.out.println("Number of Operations : "+noOfOperations);		

		System.out.println("Number of Read Operations : "+noOfReadOperations);
		System.out.println("Number of Write Operations : "+noOfWriteOperations);

		try {

			while(operationsCompleted<noOfOperations)
			{
				fileNumber = randInt(0, DynamicVoting.noOfFiles-1);		
				fileId="file"+fileNumber;

				coinToss = randInt(0, 1);
				System.out.println("Coin Toss "+coinToss);
				if(coinToss == 0)
				{
					//Read Operation
					if(readOperationsCompleted<noOfReadOperations)
					{
						System.out.println("Read operation initiated");
						read(fileId);
						readOperationsCompleted++;
						operationsCompleted++;
					}else
					{
						if(writeOperationsCompleted<noOfWriteOperations)
						{

							System.out.println("Write operation initiated");
							write(fileId);
							writeOperationsCompleted++;
							operationsCompleted++;
						}
					}
				}else
				{
					//write operation
					if(writeOperationsCompleted<noOfWriteOperations)
					{

						System.out.println("Write operation initiated");
						write(fileId);
						writeOperationsCompleted++;
						operationsCompleted++;

					}else
					{
						if(readOperationsCompleted<noOfReadOperations)
						{
							System.out.println("Read operation initiated");
							read(fileId);
							readOperationsCompleted++;
							operationsCompleted++;
						}
					}
				}

				Thread.sleep(meanDelay);
			}
		}catch(InterruptedException ex)
		{
			ex.printStackTrace();
		}


		System.out.println("Node "+nodeId+" completed the process");

	}

	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	public void grantLock(int node_id, Boolean isReadLock, String file_id)
	{
		FileInfo fileInfo;
		if(isReadLock)	
		{
			synchronized(DynamicVoting.readLockGranted)
			{
				DynamicVoting.readLockGranted.get(node_id).get(file_id).setLock(true);
				fileInfo = DynamicVoting.readLockGranted.get(node_id).get(file_id);  
			}
		}
		else 
		{
			synchronized(DynamicVoting.writeLockGranted)
			{
				DynamicVoting.writeLockGranted.get(node_id).get(file_id).setLock(true);
				fileInfo = DynamicVoting.writeLockGranted.get(node_id).get(file_id);
			}
		}

		//Send Grant READ MESSAGE
		//Set Host data
		Host source = new Host(DynamicVoting.nodeId,DynamicVoting.nodeName,DynamicVoting.nodePort);

		Host destination = new Host(node_id,DynamicVoting.nodeMap.get(node_id).getHostName(),DynamicVoting.nodeMap.get(node_id).getHostPort());

		// Increment logical timestamp
		DynamicVoting.currentNodeTimestamp.incrementAndGet();
		// Increment vector timestamp
		DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();

		Message msgObj = new Message();
		msgObj.setNodeInfo(source);
		msgObj.setFileInfo(fileInfo);
		if(isReadLock)
		{
			msgObj.setMessageType(MessageType.RESPONSE_READ);
		}
		else 
		{
			msgObj.setMessageType(MessageType.RESPONSE_WRITE);
		}
		msgObj.setStatus(Status.GRANT);
		msgObj.setLogicalTimeStamp(DynamicVoting.currentNodeTimestamp);
		msgObj.setVectorTimestamp(DynamicVoting.vectorTimeStamp);

		//Client for sending message
		RCClient rcClient = new RCClient(destination, msgObj);
		rcClient.go();
		//}

	}


	public void releaseLock(Boolean isRead, String fileId)
	{
		if(isRead)
		{
			synchronized(DynamicVoting.readLockReceived)
			{
				for(Entry<Integer,HashMap<String, FileInfo>> nodeEntry : DynamicVoting.readLockReceived.entrySet())
				{
					if(nodeEntry.getValue().get(fileId) != null && nodeEntry.getValue().get(fileId).isLock())
					{
						Host destination = DynamicVoting.nodeMap.get(nodeEntry.getKey());

						//// Increment logical timestamp
						DynamicVoting.currentNodeTimestamp.incrementAndGet();
						// Increment vector timestamp
						DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();
						Message msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.RELEASE_READ_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
								nodeEntry.getValue().get(fileId), null);
						RCClient rcClient = new RCClient(destination, msgObj);
						rcClient.go();

						nodeEntry.getValue().get(fileId).setLock(false);
					}
				}
			}
		}
		else
		{
			synchronized(DynamicVoting.writeLockReceived)
			{
				for(Entry<Integer,HashMap<String, FileInfo>> nodeEntry : DynamicVoting.writeLockReceived.entrySet())
				{
					if(nodeEntry.getValue().get(fileId) != null && nodeEntry.getValue().get(fileId).isLock())
					{
						Host destination = DynamicVoting.nodeMap.get(nodeEntry.getKey());

						// Increment logical timestamp
						DynamicVoting.currentNodeTimestamp.incrementAndGet();
						// Increment vector timestamp
						DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();
						Message msgObj = new Message(DynamicVoting.currentNodeTimestamp, DynamicVoting.vectorTimeStamp,MessageType.RELEASE_WRITE_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
								nodeEntry.getValue().get(fileId), null);
						RCClient rcClient = new RCClient(destination, msgObj);
						rcClient.go();
						nodeEntry.getValue().get(fileId).setLock(false);
					}
				}
			}

		}
	}

	public void sendDenyLockMessage(int node_id, Boolean isRead, String fileId)
	{
		Message msgObj;
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileId(fileId);
		// Increment logical timestamp
		DynamicVoting.currentNodeTimestamp.incrementAndGet();
		// Increment vector timestamp
		DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();
		if(isRead)
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.RESPONSE_READ, Status.DENY,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		else 
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(DynamicVoting.currentNodeTimestamp, DynamicVoting.vectorTimeStamp,MessageType.RESPONSE_WRITE, Status.DENY,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		Host destination = DynamicVoting.nodeMap.get(node_id);


		RCClient rcClient = new RCClient(destination, msgObj);
		rcClient.go();
	}


	public void getUpdatedFileVersion(int node_id, Boolean isRead, String fileId)
	{
		Message msgObj;
		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileId(fileId);
		// Increment logical timestamp
		DynamicVoting.currentNodeTimestamp.incrementAndGet();
		// Increment vector timestamp
		DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();
		if(isRead)
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.REQUEST_LATEST_FILE_READ, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		else 
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(DynamicVoting.currentNodeTimestamp, DynamicVoting.vectorTimeStamp,MessageType.REQUEST_LATEST_FILE_WRITE, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		Host destination = DynamicVoting.nodeMap.get(node_id);


		RCClient rcClient = new RCClient(destination, msgObj);
		rcClient.go();
	}




	public void sendUpdatedFileVersion(int node_id, Boolean isRead, String fileId)
	{

		Message msgObj;
		byte[] fileContent =convertToByteArray(fileId,DynamicVoting.filePath);
		DynamicVoting.currentNodeTimestamp.incrementAndGet();
		DynamicVoting.vectorTimeStamp[DynamicVoting.nodeId].incrementAndGet();
		if(isRead)
		{
			msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.RESPONSE_LATEST_FILE_READ, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					DynamicVoting.fileInfoMap.get(fileId), fileContent);
		}
		else 
		{
			msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.RESPONSE_LATEST_FILE_WRITE, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					DynamicVoting.fileInfoMap.get(fileId), fileContent);
		}
		Host destination = DynamicVoting.nodeMap.get(node_id);
		RCClient rcClient = new RCClient(destination, msgObj);
		rcClient.go();
	}


	public static byte[] convertToByteArray(String filename,String filepath) 
	{
		filepath = filepath;
		FileInputStream fis=null;
		File file = new File(filepath+filename+ ".txt");
		byte[] byteArray = new byte[(int) file.length()];
		try {
			fis = new FileInputStream(file);
			fis.read(byteArray);
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return byteArray;
	}


	/**
	 * Check if read/write lock is granted
	 * @param node_id
	 * @param isReadLock
	 */
	public Boolean checkLock(Boolean isRead, String fileId)
	{
		if(isRead)
		{
			//System.out.println("File Id : "+fileId);
			for(HashMap<String, FileInfo> file : DynamicVoting.readLockGranted.values())
			{
				if(file.get(fileId).isLock())
				{
					return true;
				}
			}
		}
		else 
		{
			for(HashMap<String, FileInfo> file : DynamicVoting.writeLockGranted.values())
			{
				if(file.get(fileId).isLock())
				{
					return true;
				}
			}
		}
		return false;
	}




	public void read(String fileId) throws InterruptedException
	{

		int currentTimer = DynamicVoting.minBackOff;
		while(true)
		{
			if(!DynamicVoting.isWriting)
			{

				DynamicVoting.timerOff = false;
				FileInfo fileInfo;
				synchronized(DynamicVoting.readLockReceived)
				{
					fileInfo = DynamicVoting.readLockReceived.get(DynamicVoting.nodeId).get(fileId);
					DynamicVoting.readLockReceived.get(DynamicVoting.nodeId).get(fileId).setLock(true);
				}

				int maxVersionNumber = fileInfo.getVersionNumber();
				int nValue = fileInfo.getReplicaUpdated();

				for (int id = 0; id < DynamicVoting.noOfNodes; id++) 
				{
					if(id != DynamicVoting.nodeId)
					{
						//TODO: SET TIMESTAMPS
						Message msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.REQUEST_READ_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
								DynamicVoting.fileInfoMap.get(fileId), null);

						Host destination = DynamicVoting.nodeMap.get(id);
						if(destination ==null){
							System.out.println("DEst*************************"+id);
						}
						System.out.println("Read Dest Host name :"+destination.hostName+" Host port : "+destination.hostPort);
						RCClient rcClient = new RCClient(destination, msgObj);
						rcClient.go();
					}
				}
				try {
					Thread.sleep(DynamicVoting.timer);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("Requested nodes to get the quorum for read");
				DynamicVoting.timerOff = Boolean.TRUE;
				ArrayList<Integer> pList = new ArrayList<Integer>();
				ArrayList<Integer> quorumList = new ArrayList<Integer>();
				synchronized(DynamicVoting.readLockReceived)
				{

					for(Map.Entry<Integer,HashMap<String,FileInfo>> fileInfoMap: DynamicVoting.readLockReceived.entrySet()) 
					{

						FileInfo file= fileInfoMap.getValue().get(fileId);						
						if(file.isLock())
						{
							pList.add(fileInfoMap.getKey());
							if(maxVersionNumber<file.getVersionNumber())
							{
								maxVersionNumber = file.getVersionNumber();
								nValue = file.getReplicaUpdated();
							}
						}
					}
					for(Map.Entry<Integer,HashMap<String,FileInfo>> fileInfoMap: DynamicVoting.readLockReceived.entrySet())
					{
						FileInfo file= fileInfoMap.getValue().get(fileId);
						if(pList.contains(fileInfoMap.getKey()) && file.getVersionNumber()==maxVersionNumber)
						{
							quorumList.add(fileInfoMap.getKey());
						}

					}
				}

				if(quorumList.size()> Math.ceil(nValue/2))
				{
					readFromQuorum(quorumList, maxVersionNumber, fileInfo.getVersionNumber(), fileInfo.getFileId());
				}
				else if(quorumList.size()== Math.ceil(nValue/2))
				{
					if(quorumList.contains(DynamicVoting.dsNodeId))
					{
						readFromQuorum(quorumList, maxVersionNumber, fileInfo.getVersionNumber(), fileInfo.getFileId());
					}
				}
				else
				{
					releaseLock(Boolean.TRUE , fileId);
					if(currentTimer>DynamicVoting.maxBackOff)
						currentTimer = DynamicVoting.minBackOff;
					Thread.sleep(currentTimer);
					currentTimer = currentTimer + currentTimer;
				}

				if(DynamicVoting.requestCompleted)
				{
					DynamicVoting.requestCompleted = false;
					break;
				}
			}
		}


	}

	private void readFromQuorum(ArrayList<Integer> quorumList , Integer maxVersionNumber, Integer currentFileVersion, String fileId) {

		if(currentFileVersion!=maxVersionNumber)
		{
			DynamicVoting.isWaitingForUpdate=true;
			getUpdatedFileVersion(quorumList.get(0),Boolean.TRUE, fileId);
		}

		while(DynamicVoting.isWaitingForUpdate);
		readOp(DynamicVoting.filePath+fileId);

		DynamicVoting.requestCompleted=true;
		releaseLock(true, fileId);
		System.out.println("Completed the read Operation");
	}


	public void write(String fileId) throws InterruptedException
	{

		int currentTimer = DynamicVoting.minBackOff;
		while(true)
		{
			if(!DynamicVoting.isWriting && !checkLock(true, fileId) && !checkLock(false, fileId))
			{

				DynamicVoting.timerOff = false;
				FileInfo fileInfo;
				DynamicVoting.isWriting = true;
				synchronized(DynamicVoting.writeLockReceived)
				{
					fileInfo = DynamicVoting.writeLockReceived.get(DynamicVoting.nodeId).get(fileId);
					DynamicVoting.writeLockReceived.get(DynamicVoting.nodeId).get(fileId).setLock(true);
				}

				int maxVersionNumber = fileInfo.getVersionNumber();
				int nValue = fileInfo.getReplicaUpdated();

				for (int id = 0; id < DynamicVoting.noOfNodes; id++) 
				{
					if(id != DynamicVoting.nodeId)
					{
						//TODO: SET TIMESTAMPS
						Message msgObj = new Message(DynamicVoting.currentNodeTimestamp,DynamicVoting.vectorTimeStamp,MessageType.REQUEST_WRITE_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
								DynamicVoting.fileInfoMap.get(fileId), null);
						Host destination = DynamicVoting.nodeMap.get(id);
						if(destination ==null){
							System.out.println("DEst*************************"+id);
						}
						RCClient rcClient = new RCClient(destination, msgObj);
						rcClient.go();
					}
				}
				try {
					Thread.sleep(DynamicVoting.timer);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				DynamicVoting.timerOff = Boolean.TRUE;
				ArrayList<Integer> pList = new ArrayList<Integer>();
				ArrayList<Integer> quorumList = new ArrayList<Integer>();
				synchronized(DynamicVoting.writeLockReceived)
				{

					for(Map.Entry<Integer,HashMap<String,FileInfo>> fileInfoMap: DynamicVoting.writeLockReceived.entrySet()) 
					{

						FileInfo file= fileInfoMap.getValue().get(fileId);						
						if(file.isLock())
						{
							pList.add(fileInfoMap.getKey());
							if(maxVersionNumber<file.getVersionNumber())
							{
								maxVersionNumber = file.getVersionNumber();
								nValue = file.getReplicaUpdated();
							}
						}
					}
					for(Map.Entry<Integer,HashMap<String,FileInfo>> fileInfoMap: DynamicVoting.writeLockReceived.entrySet())
					{
						FileInfo file= fileInfoMap.getValue().get(fileId);
						if(pList.contains(fileInfoMap.getKey()) && file.getVersionNumber()==maxVersionNumber)
						{
							quorumList.add(fileInfoMap.getKey());
						}

					}
				}

				if(quorumList.size()> Math.ceil(nValue/2))
				{
					writeIntoQuorum(quorumList, maxVersionNumber, fileInfo.getVersionNumber(), fileInfo.getFileId());
				}
				else if(quorumList.size()== Math.ceil(nValue/2))
				{
					if(quorumList.contains(DynamicVoting.dsNodeId))
					{
						writeIntoQuorum(quorumList, maxVersionNumber, fileInfo.getVersionNumber(), fileInfo.getFileId());
					}
				}
				else
				{
					releaseLock(Boolean.FALSE , fileId);
					if(currentTimer>DynamicVoting.maxBackOff)
						currentTimer = DynamicVoting.minBackOff;
					Thread.sleep(currentTimer);
					currentTimer = currentTimer + currentTimer;
				}

				if(DynamicVoting.requestCompleted)
				{

					//Update RU Value
					DynamicVoting.fileInfoMap.get(fileId).setReplicaUpdated(quorumList.size());
					//Increment Version Number
					DynamicVoting.fileInfoMap.get(fileId).setVersionNumber(maxVersionNumber+1);

					for (int i = 0; i < pList.size(); i++) 
					{
						sendUpdatedFileVersion(pList.get(i), false, fileId);
					}
					DynamicVoting.requestCompleted = false;
					break;
				}

			}
		}
	}


	private void writeIntoQuorum(ArrayList<Integer> quorumList , Integer maxVersionNumber, Integer currentFileVersion, String fileId) {

		if(currentFileVersion!=maxVersionNumber)
		{
			DynamicVoting.isWaitingForUpdate=true;
			getUpdatedFileVersion(quorumList.get(0),Boolean.FALSE, fileId);
		}

		while(DynamicVoting.isWaitingForUpdate);
		writeOp(DynamicVoting.filePath+fileId);
		DynamicVoting.requestCompleted=true;
		releaseLock(false, fileId);
		DynamicVoting.isWriting = false;
	}



	public void readOp(String fileId)
	{
		System.out.println("got the quorum for read and gonna perform read");
		System.out.println("File id : "+fileId);
		fileId = fileId+".txt";
		AtomicInteger[] timeStamp = DynamicVoting.vectorTimeStamp;
		String s = "[";
		for(AtomicInteger i : timeStamp){
			s+=i+",";
		}
		s = s.substring(0,s.length()-1);
		s+="]";
		logger.info(fileId+"~"+nodeId+"#R#E#"+s);
		Scanner scanner = null;
		try
		{
			File file = new File(fileId);
			scanner=new Scanner(file);
			//Read file
			while(scanner.hasNext())
			{
				//System.out.println("Test Read");
				scanner.nextLine();
				Thread.sleep(10);
			}

		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			if(scanner != null){
				//Close resource file
				scanner.close();
			}
		}
		timeStamp = DynamicVoting.vectorTimeStamp;
		String s1 = "[";
		for(AtomicInteger i : timeStamp){
			s1+=i+",";
		}
		s1 = s1.substring(0,s1.length()-1);
		s1+="]";
		logger.info(fileId+"~"+nodeId+"#R#L#"+s1);
		System.out.println("Read completed");

	}

	public void writeOp(String fileId)
	{
		fileId = fileId+".txt";
		AtomicInteger[] timeStamp = DynamicVoting.vectorTimeStamp;
		String s = "[";
		for(AtomicInteger i : timeStamp){
			s+=i+",";
		}
		s = s.substring(0,s.length()-1);
		s+="]";
		logger.info(fileId+"~"+nodeId+"#W#E#"+s);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileId);
			pw.println("[Node-"+DynamicVoting.nodeId+"] writing on File");

		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			timeStamp = DynamicVoting.vectorTimeStamp;
			String s1 = "[";
			for(AtomicInteger i : timeStamp){
				s1+=i+",";
			}
			s1 = s1.substring(0,s1.length()-1);
			s1+="]";
			logger.info(fileId+"~"+nodeId+"#W#L#"+s1);
			if(pw != null){
				//Close resource file
				pw.close();
			}
		}
	}


	public void run()
	{
		createFiles(DynamicVoting.noOfFiles);
		initiateVoting();
	}

	public static void main(String[] args) throws Exception {

		//ServiceSimulation oService = new ServiceSimulation();
		//oService.createFiles(20);
		//oService.loadFileContentToMemory(projectDir+"files/node1/file1.txt");

	}
}
