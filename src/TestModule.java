import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class TestModule {

	public List<String> listLogFilesForFolder(File folder) {
		List<String> logFiles = new LinkedList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            continue;
	        } else if(fileEntry.getName().startsWith("log.txt")){
	           logFiles.add(fileEntry.getAbsolutePath());
	        }
	    }
		return logFiles;
	}
	
	public void test() throws IOException{
		
		File folder = new File("/home/eng/a/axt131730/AOS_Proj3");
		List<String> logFiles = listLogFilesForFolder(folder);
		Map<String,List<String>> vectorTimeStampMaps = new HashMap<String, List<String>>();
		for(String filePath : logFiles){
			BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			String line = "";
			while((line=br.readLine())!=null){
				if(line.startsWith("INFO")){
					String[] parts = line.split(":");
					String[] subparts = parts[1].split("~");
					if(vectorTimeStampMaps.containsKey(subparts[0])){
						vectorTimeStampMaps.get(subparts[0]).add(subparts[1]);
					}else{
						List<String> values = new LinkedList<String>();
						values.add(subparts[1]);
						vectorTimeStampMaps.put(subparts[0],values);
					}
				}
			}
			br.close();
		}
		
		boolean hasConflict = false;
		for(Entry<String,List<String>> entry : vectorTimeStampMaps.entrySet()){
			List<String> values = entry.getValue();
			for(int i =0; i<values.size();i++){
				String value = values.get(i);
				for(int j=i ;j<values.size(); j++){
					String comparable = values.get(j);
					if(value.contains("R#L")){
						if(comparable.contains("W#E")){
							String[] subvalues = value.split("#");
							String[] comValues = comparable.split("#");
							if(compare(subvalues[3], comValues[3])){
								hasConflict = true;
							}
						}
					}
						
				}
			}
			for(int i =0; i<values.size();i++){
				String value = values.get(i);
				for(int j=i ;j<values.size(); j++){
					String comparable = values.get(j);
					if(value.contains("W#L")){
						if(comparable.contains("R#E") || comparable.contains("W#E")  ){
							String[] subvalues = value.split("#");
							String[] comValues = comparable.split("#");
							if(compare(subvalues[3], comValues[3])){
								hasConflict = true;
							}
						}
					}
						
				}
			}
			
		}
		if(hasConflict){
			System.out.println("There was a conflict");
		}else{
			System.out.println("No conflict found");
		}
	}

	private boolean compare(String string1, String string2) {
		string1 = string1.substring(1,string1.length()-1);
		string2 = string2.substring(1,string2.length()-1);
		String[]strings_1 = string1.split(",");
		String[]strings_2 = string2.split(",");
		for(int i=0 ; i< strings_1.length ;i++){
			int val1 = Integer.parseInt(strings_1[i].trim());
			int val2 = Integer.parseInt(strings_2[i].trim());
			if(val1 > val2){
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException{
		TestModule testModule = new TestModule();
		testModule.test();
	}
	
}
