import java.io.Serializable;

/**
 * 
 */

/**
 * @author Dany
 *
 */
public class FileInfo implements Serializable{

	String fileId;
	int versionNumber;
	int replicaUpdated;
	boolean lock;
	
	public FileInfo(String fileId, int versionNumber, int replicaUpdated, boolean lock)
	{
		this.fileId = fileId;
		this.versionNumber = versionNumber;
		this.replicaUpdated = replicaUpdated;
		this.lock = lock;
	}
	
	public FileInfo()
	{
		
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public int getReplicaUpdated() {
		return replicaUpdated;
	}

	public void setReplicaUpdated(int replicaUpdated) {
		this.replicaUpdated = replicaUpdated;
	}

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}
	
	
	
}
