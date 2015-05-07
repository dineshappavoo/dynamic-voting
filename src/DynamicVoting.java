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
	public static String nodeName;
	public static int nodePort;
	public static boolean isReading = false;
	public static Boolean isWriting = false;
	public static int noOfFiles;
	public static int noOfOperations;
	public static int meanDelay;
	public static int fractionOfOperations;
	public static int minBackOff;
	public static int maxBackOff;
	public static Boolean requestCompleted = false;
	public static long timer = 200; //200ms
	public static int dsNodeId =1 ; //hardcoded for now to node-1
	public static String filePath; //Hardode with the filepath for the node
	
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> readLockReceived = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> readLockGranted = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> writeLockReceived = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<Integer, HashMap<String, FileInfo>> writeLockGranted = new ConcurrentHashMap<Integer, HashMap<String, FileInfo>>();
	public static ConcurrentHashMap<String, FileInfo> fileInfoMap = new ConcurrentHashMap<String, FileInfo>();

	
	public static Boolean isTerminationSent = false;
	public static HashMap<Integer, Host> nodeMap;
	public static int nWaitingForTerminationResponseCount=0;
	public static boolean isWaitingForLock = false;
	public static PriorityQueue<Message> minHeap = getPriorityQueue();
	public static AtomicInteger currentNodeTimestamp = new AtomicInteger(0);
	public static int count = 0;
	public static Boolean timerOff;
	public static boolean isWaitingForUpdate = false;
	public static RCServer rCServer = new RCServer();
	public ServiceSimulation oService;
	public PrintWriter out;

	public void startServer()
	{
		filePath = "files/node"+nodeId+"/";
		nodeName = nodeMap.get(nodeId).getHostName();
		nodePort = nodeMap.get(nodeId).getHostPort();
		
		System.out.println("No Of Operations DV : "+noOfOperations);
		new Thread(rCServer).start();
		oService = new ServiceSimulation(nodeId, noOfOperations, meanDelay);
		new Thread(oService).start();
	}
	
	/**
	 * Introduce failure
	 */
	public void introduceFailure()
	{
		int failureDuration = 10000;
		//initiateFailure();
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
							nodeMap.put(hostId, new Host(hostId, hostName, hostPort));
						}
					}
				}
				checker=scanner.next();
				System.out.println("Checker :"+checker);
				if(checker.equals("totalnooffiles") && (!(checker.equals("#"))))
				{
					noOfFiles = scanner.nextInt();
				}
				checker = scanner.next();

				if(checker.equals("totalnoofoperations") && (!(checker.equals("#"))))
				{
					noOfOperations = scanner.nextInt();
				}
				checker=scanner.next();

				if(checker.equals("meandelay") && (!(checker.equals("#"))))
				{
					meanDelay = scanner.nextInt();
				}
				checker=scanner.next();

				if(checker.equals("fractionofoperations") && (!(checker.equals("#"))))
				{
					fractionOfOperations = scanner.nextInt();
				}
				checker=scanner.next();

				if(checker.equals("min_backoff") && (!(checker.equals("#"))))
				{
					minBackOff = scanner.nextInt();
				}
				checker=scanner.next();

				if(checker.equals("max_backoff") && (!(checker.equals("#"))))
				{
					maxBackOff = scanner.nextInt();
				}
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
		System.out.println("Number of operations : "+noOfOperations);
		System.out.println("Number of files : "+noOfFiles);
		System.out.println("Fraction of operations : "+fractionOfOperations);
		System.out.println("Mean Delay : "+meanDelay);
		System.out.println("Min Backoff : "+minBackOff);
		System.out.println("Max Backoff : "+maxBackOff);

	}

	public void simulateDynamicVoting() throws FileNotFoundException
	{
		//HashMap<Integer, Host> nMap = constructGraph("/Users/Dany/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/dynamic-voting/src/config.txt", nodeId);
		HashMap<Integer, Host> nMap = constructGraph("config.txt", nodeId);
		startServer();
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		if(args.length > 0) {
			nodeId = Integer.parseInt(args[0]);
		}
		 
		DynamicVoting dvObject = new DynamicVoting();
		dvObject.simulateDynamicVoting();
	}

}
