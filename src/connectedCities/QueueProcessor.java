package connectedCities;

import graph.UnionFindGraph;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The responsibility of the QueueProcessor is to empty a queue into the UnionFindGraph, and to avoid contention by giving data to a more central queue when there is an issue.
 * @author Brian
 *
 */
public class QueueProcessor implements Callable<Void> {
	private BlockingQueue<List<String>> myQueue = new LinkedBlockingQueue<>();
	private UnionFindGraph graph;
	private boolean processing = true;
	private boolean dataPending = true;
	

	@SuppressWarnings("unused")
	private QueueProcessor(){				
	}
	
	public QueueProcessor(UnionFindGraph graph){		
		this.graph = graph;
	}
	
	
	public BlockingQueue<List<String>> getQueue(){
		return myQueue;
	}
	
	public void allDataHasBeenPutInQueue(){
		dataPending=false;
	}
	
	public void stopProcessing(){
		processing=false;
	}	
	
	/**
	 * This call will empty the queue until the "empty" item arrives or processing is over.
	 */
	@Override
	public Void call() throws Exception {
		List<String> input = null;
		while(dataPending ||(processing && (input = myQueue.poll()) != null)){
			if(input == null){
				Thread.sleep(0,100);
			}
			if (input != null){
				List<String> rejectedInput = graph.addEdge(input);
				//when there is  rejected input, put it 
				if (rejectedInput != null){
					this.getQueue().add(rejectedInput);
				}					
			}
		}
		return null;
	}

}
