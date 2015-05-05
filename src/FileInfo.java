/**
 * 
 */

/**
 * @author Dany
 *
 */
public class FileInfo {

	int versionNumber;
	int replicaUpdated;
	boolean lock;
	
	public FileInfo(int versionNumber, int replicaUpdated, boolean lock)
	{
		this.versionNumber = versionNumber;
		this.replicaUpdated = replicaUpdated;
		this.lock = lock;
	}
}
