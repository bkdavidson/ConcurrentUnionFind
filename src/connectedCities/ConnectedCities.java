package connectedCities;

import fileReader.FilePartitioner;
import graph.ConnectedCitiesHandler;
import graph.UnionFindGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedCities implements ConnectedCitiesHandler{
	private static Boolean areCitiesConnected = false;
	private static QueueProcessor[] queueProcessors;

	public static void main(String[] args) {
		String fileName = null;
		String firstCity = null;
		String secondCity = null;
		int queueProcessorCount = 3;
		boolean displayTiming = false;
        //handle args
		if (args.length == 0 || args.length > 5){
			//print usage
			System.out.println("Usage: " + "connectedCities <inputFile> <firstCity> <secondCity> [threads] [timing]");
			return;
		}
		if (args.length >= 3){
        fileName = args[0];
        firstCity = args[1];
        secondCity = args[2];
		}
        
        if (args.length >= 4){
            try{
            	queueProcessorCount = Integer.parseInt(args[3]);
            	if(args.length ==5)
            		displayTiming = Boolean.parseBoolean(args[4]);
                if (queueProcessorCount <= 0){
                	queueProcessorCount = 3;
                }
            } catch (NumberFormatException e){
            	queueProcessorCount = 3;
            }
        }
        long start = System.currentTimeMillis();
        
        //execute the QueueProcessors
        Boolean areConnected = runQueueProcessors(fileName,queueProcessorCount,firstCity,secondCity,displayTiming);
        if (areConnected == null){
        	return;
        }
        		
        if (areConnected){
        	System.out.println("yes");        
        } else{
        	System.out.println("no");       
        }
        long end = System.currentTimeMillis()-start;
        if (displayTiming)
        	System.out.println("Total time: " + end);        
	}

    @SuppressWarnings("unchecked")
	private static Boolean runQueueProcessors(String fileName, int queueProcessorCount, String firstCity, String secondCity, Boolean displayTiming) {
        List<FutureTask<Void>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(queueProcessorCount);
        
        UnionFindGraph graph = new UnionFindGraph(firstCity,secondCity,new ConnectedCities());
        queueProcessors = new QueueProcessor[queueProcessorCount];
        BlockingQueue<List<String>>[] queues = new BlockingQueue[queueProcessorCount];
        for(int i = 0; i < queueProcessorCount; i++){
        	QueueProcessor currentProcessor =new QueueProcessor(graph);
        	queueProcessors[i] = currentProcessor;
        	queues[i] = currentProcessor.getQueue();
        }
        
        for (QueueProcessor queueProcessor: queueProcessors){
            FutureTask<Void> future = new FutureTask<Void>(queueProcessor);
            futures.add(future);
            executor.submit(future);
        }
        
        try {
			FilePartitioner.partitionFileIntoQueues(fileName, queues,displayTiming);
		} catch (IOException e) {
			System.out.println("Could not open " + fileName);
			e.printStackTrace();
		}
        
        for (QueueProcessor queueProcessor: queueProcessors){
            queueProcessor.allDataHasBeenPutInQueue();
        }
        executor.shutdown();
        try {
			if (!executor.awaitTermination(2, TimeUnit.SECONDS)) { //optional *
				Logger logger = Logger.getLogger(ConnectedCities.class.getName());
				logger.log(Level.SEVERE, "Executor did not terminate in the specified time."); //optional *
			    List<Runnable> droppedTasks = executor.shutdownNow(); //optional **
			    logger.log(Level.SEVERE, "Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed."); //optional **
			    System.exit(-1);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
        for (FutureTask<Void> task : futures){
        	try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}                
        }
        return ConnectedCities.areCitiesConnected;
    }

	@Override
	public void handleConnectedCityEvent(UnionFindGraph graph) {
		ConnectedCities.areCitiesConnected = true;
		FilePartitioner.stopReading();
		for (QueueProcessor queueProcessor: queueProcessors){
			queueProcessor.stopProcessing();
		}
	}	
	
}
