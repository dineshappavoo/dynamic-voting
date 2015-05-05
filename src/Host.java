import java.io.Serializable;

/**
 * 
 */


/**
 * @author Dany
 *
 */
public class Host implements Serializable{
	
	public int hostId;
	public String hostName;
	public int hostPort;
	public boolean isTerminated;
	
	public Host(int hostId, String hostName, int hostPort, boolean keyKnown, boolean isRequested)
	{
		this.hostId = hostId;
		this.hostName = hostName;
		this.hostPort = hostPort;
		this.isTerminated = false;
	}
	
	public Host()
	{
		this.hostId = 0;
		this.hostName = "";
		this.hostPort = 0;	
		this.isTerminated = false;
	}
}

