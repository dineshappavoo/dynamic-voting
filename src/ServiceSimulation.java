/**
 * 
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
	
	public void pushDataFromMemoryToFile(String fileName, byte[] data) throws Exception
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
	
	
	

}
