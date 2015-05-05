import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dany
 *
 */
public class DynamicVoting {

	public static int noOfNodes;
	public static int nodeId;
	public static int noOfFiles;
	public static int noOfOperations;
	public static int meanDelay;
	public static int fractionOfOperations;
	public static int minBackOff;
	public static int maxBackOff;
	
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> readLockReceived = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> readLockGranted = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> writeLockReceived = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> writeLockGranted = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> ReadLockReceived = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();


	public static Boolean isTerminationSent = false;
	public static HashMap<Integer, Host> nodeMap;
	public static int nWaitingForTerminationResponseCount=0;
	public static boolean isWaitingForLock = false;
	public static PriorityQueue<Message> minHeap = getPriorityQueue();
	public static AtomicInteger currentNodeTimestamp = new AtomicInteger(0);
	public static int count = 0;
	public static  ApplicationClient oAppClient;	
	//public static RCServer rCServer = new RCServer();
	public PrintWriter out;

	public void startServer()
	{
		//new Thread(rCServer).start();
		//oAppClient = new ApplicationClient(noOfCriticalSectionRequests, meanDelayInCriticalSection, durationOfCriticalSection);
		//new Thread(oAppClient).start();
	}

	public void displayCSMessage(int nodeId)
	{
		System.out.print("[INFO]	["+sTime()+"]\t");

		for(int i=0;i<noOfNodes;i++)
		{
			if(i!=nodeId)
			{
				System.out.print("|\t");
			}else
			{
				System.out.print("|  "+nodeId+" CS ");
			}
		}
		System.out.print("|");
		System.out.println();
	}


	public static PriorityQueue<Message> getPriorityQueue()
	{
		PriorityQueue<Message> queue = new PriorityQueue<Message>(11, new Comparator<Message>()
				{
			public int compare(Message o1, Message o2)
			{
				AtomicInteger t1 = o1.logicalTimeStamp;
				AtomicInteger t2 = o2.logicalTimeStamp;
				if(t1.get()>=t2.get())
					return 1;
				else
					return -1;
			}
				}
				);
		return queue;	
	}


	public HashMap<Integer, Host> constructGraph(String fileName, int nodeId) throws FileNotFoundException
	{
		nodeMap = new HashMap<Integer, Host>();
		File file = new File(fileName);
		Scanner scanner=new Scanner(file);
		int hostId, hostPort;
		String hostName="";
		String checker="";

		//Read input from config file
		while(scanner.hasNext())
		{
			if((checker=scanner.next()).equals("p") && (!(checker.equals("#"))))
			{
				noOfNodes=scanner.nextInt();
				//To add the node information from config file [FORMAT : n  0	dc01		3332]
				for(int j=0;j<noOfNodes;j++)
				{
					if((checker=scanner.next()).equals("n")){
						hostId = scanner.nextInt();
						hostName = scanner.next()+".utdallas.edu";
						hostPort = scanner.nextInt();

						if(nodeMap.get(hostId)==null)// || hostId != nodeId)
						{
							nodeMap.put(hostId, new Host(hostId, hostName, hostPort, false, false));
						}
					}
				}
			}

			checker=scanner.next();
			if(checker.equals("totalnooffiles") && (!(checker.equals("#"))))
			{
				noOfFiles = scanner.nextInt();
			}else if(checker.equals("totalnoofoperations") && (!(checker.equals("#"))))
			{
				noOfOperations = scanner.nextInt();
			}else if(checker.equals("meandelay") && (!(checker.equals("#"))))
			{
				meanDelay = scanner.nextInt();
			}else if(checker.equals("fractionofoperations") && (!(checker.equals("#"))))
			{
				fractionOfOperations = scanner.nextInt();
			}else if(checker.equals("min_backoff") && (!(checker.equals("#"))))
			{
				minBackOff = scanner.nextInt();
			}else if(checker.equals("max_backoff") && (!(checker.equals("#"))))
			{
				maxBackOff = scanner.nextInt();
			}

		}
		printNodeMap();
		return nodeMap;	
	}


	public String sTime()
	{
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		return timeStamp;
	}

	public void printNodeMap()
	{
		Host host;
		for(int nodeId : nodeMap.keySet())
		{
			host = nodeMap.get(nodeId);
			System.out.println("[INFO]	["+sTime()+"]	Host Id "+nodeId+"  Name : "+host.hostName+"  port : "+host.hostPort);
		}
	}

	public void simulateDynamicVoting() throws FileNotFoundException
	{
		HashMap<Integer, Host> nMap = constructGraph("/Users/Dany/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/roucairol-carvalho/src/config.txt", nodeId);
		//HashMap<Integer, Host> nMap = constructGraph("config.txt", nodeId);

		//System.out.println("[INFO]	["+sTime()+"]	No Of CS : "+noOfCriticalSectionRequests+"  Mean Delay : "+meanDelayInCriticalSection+"  Duration Of CS : "+durationOfCriticalSection);
		//startServer();
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub

		/*if(args.length > 0) {
			nodeId = Integer.parseInt(args[0]);
		}
		 */
		DynamicVoting dvObject = new DynamicVoting();
		dvObject.simulateDynamicVoting();
	}

}
