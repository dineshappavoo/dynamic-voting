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
import java.util.HashMap;
import java.util.Map.Entry;
/**
 * @author Dany
 *
 */
public class ServiceSimulation {

	/**
	 * @param args
	 */
	public static String projectDir = "/home/004/d/dx/dxa132330/advanced-operating-system/projects/dynamic-voting/";
	public int nodeCount = 45;
	public PrintWriter out;

	public static void main(String[] args) throws Exception {

		ServiceSimulation oService = new ServiceSimulation();
		oService.createFiles(20);
		oService.loadFileContentToMemory(projectDir+"files/node1/file1.txt");

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
			out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
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

		for(int i=1;i<=nodeCount;i++)
		{
			for(int j=0;j<noOfFiles;j++)
			{
				try
				{
					String fileName = fileDir+"/node"+i+"/file"+j+".txt";
					out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
					out.println(System.currentTimeMillis());

				}catch(IOException ex)
				{
					out.close();
					ex.printStackTrace();	
				}
			}
		}
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

		// Increment vector timestamp

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
		//TODO: SET TIMESTAMPS - LOGICAL AND VECTOR

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

						//TODO: Update timestamps
						Message msgObj = new Message(null,null,MessageType.RELEASE_READ_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
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

						//TODO: Update timestamps and set in Message
						Message msgObj = new Message(null,null,MessageType.RELEASE_WRITE_LOCK, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
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
		if(isRead)
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.RESPONSE_READ, Status.DENY,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		else 
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.RESPONSE_WRITE, Status.DENY,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
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
		if(isRead)
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.REQUEST_LATEST_FILE_READ, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					fileInfo, null);
		}
		else 
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.REQUEST_LATEST_FILE_WRITE, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
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
		if(isRead)
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.RESPONSE_LATEST_FILE_READ, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					DynamicVoting.fileInfoMap.get(fileId), fileContent);
		}
		else 
		{
			//TODO: Update timestamps and set in Message
			msgObj = new Message(null,null,MessageType.RESPONSE_LATEST_FILE_WRITE, null,DynamicVoting.nodeMap.get(DynamicVoting.nodeId),
					DynamicVoting.fileInfoMap.get(fileId), fileContent);
		}
		Host destination = DynamicVoting.nodeMap.get(node_id);
		RCClient rcClient = new RCClient(destination, msgObj);
		rcClient.go();
	}


	public static byte[] convertToByteArray(String filename,String filepath) 
	{
		
		FileInputStream fis=null;
        File file = new File(filepath+filename);
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
	
	/////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////
	
	
	
	//Working on read and write operations

}
