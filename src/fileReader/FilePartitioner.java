package fileReader;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class FilePartitioner {
	//a flag used to prematurely stop reading the file input
	private static boolean keepReading = true;
	
	/**
	 * This takes a files contents and round robin submits them to each queue.
	 * @param fileName The file to read
	 * @param queues The queues to put data into
	 * @param displayTiming Whether time should be printed
	 * @throws IOException There is a problem reading the file
	 */
    public static void partitionFileIntoQueues(String fileName, final BlockingQueue<List<String>>[] queues, boolean displayTiming) throws IOException {
    	FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
            String csvString = null;
            int currentQueue = 0;
            long startTime = System.currentTimeMillis();
            while (keepReading && (csvString = bufferedReader.readLine()) != null) {
                ArrayList<String> line = new ArrayList<>();
                String[] splitLines = csvString.split(",");
                for (String parsedString : splitLines){
                    line.add(parsedString);
                }
                //partition into queues round robin style
                queues[currentQueue].add(line);
                if (currentQueue >= queues.length-1){
                	currentQueue = 0;
                } else{
                	currentQueue++;
                }
            }  
        bufferedReader.close();
        fileReader.close();
        if(displayTiming)
        	System.out.println("File input time: " + (System.currentTimeMillis() - startTime));
    }
    
    public static void stopReading(){
    	keepReading = false;
    }
    
}
