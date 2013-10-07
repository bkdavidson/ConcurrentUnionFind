package graph;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * The UnionFindGraph is thread safe and it implements the Union Find data structure.
 * The strategy it uses to handle contention is to return data it can't handle at that moment back to the caller, to then decide what to do with that data.
 * 
 * @author Brian
 *
 */
public class UnionFindGraph {
	
	//stores all cities
	private ConcurrentHashMap<String,Node> cities = new ConcurrentHashMap<>();
	//first city's root parent
	private Node connectedLeaderA;
	//second city's root parent
	private Node connectedLeaderB;
	//flag to determine if it is time to stop processing
	private boolean processing = true;
	//the handler for when connectedLeaderA and connectedLeaderB are equal to each other
	private ConnectedCitiesHandler connectedCitiesHandler;
	
	
	@SuppressWarnings("unused")
	private UnionFindGraph(){		
		
	}
	
	/**
	 * 
	 * @param cityA The first city to test for connectivity
	 * @param cityB The second city to test for connectivity
	 * @param connectedCitiesHandler The object that handles the event of cityA and cityB being connected.
	 */
	public UnionFindGraph(String cityA, String cityB, ConnectedCitiesHandler connectedCitiesHandler) {
		super();
		this.connectedCitiesHandler = connectedCitiesHandler;
		connectedLeaderA = new Node(cityA);
		connectedLeaderB = new Node(cityB);
		cities.putIfAbsent(cityA, connectedLeaderA);
		cities.putIfAbsent(cityB, connectedLeaderB);
	}
	
	/**
	 * Halts all future processing of Edges. Causes addEdge to immediately return.
	 */
	public void stopProcessing(){
		processing=false;
	}

	/**
	 * Add an edge to graph
	 * @param edge A list of 2 elements to be connected
	 */
	public List<String> addEdge(List<String> edge){
		if (edge.size() != 2 & !processing)
			return null;
		Node firstNode = new Node(edge.get(0));
		Node secondNode = new Node(edge.get(1));
		cities.putIfAbsent(firstNode.getName(), firstNode);
		cities.putIfAbsent(secondNode.getName(), secondNode);
		NodePair rejectedOutput =  addEdge(new NodePair(cities.get(firstNode.getName()), cities.get(secondNode.getName())));
		if (rejectedOutput == null)
			return null;
		return edge;
	}
	
	/**
	 * Convenience method for AddEdge
	 * @param connectedNodes Nodes that are connected.
	 */
	private NodePair addEdge(NodePair connectedNodes){
		return setUpUnion(connectedNodes);
	}

	/**
	 * This tests to see if the two nodes are good to make a union. It is checked again in the Union when locks are obtained.
	 * @param connectedNodes Nodes to Union.
	 * @return
	 */
	private NodePair setUpUnion(NodePair connectedNodes) {
		Node rootNodeFirst = connectedNodes.getFirstNode().getRootNode();
		Node rootNodeSecond = connectedNodes.getSecondNode().getRootNode();
		//do nothing when in same set		
		if (rootNodeFirst.equals(rootNodeSecond))
			return null;
		//try to make a join
		else {
			Node higher;
			Node lower;
			if (rootNodeFirst.getRank() >= rootNodeSecond.getRank()){
				higher = rootNodeFirst;
				lower = rootNodeSecond;
			} else{
				higher = rootNodeSecond;
				lower = rootNodeFirst;
			}
			return union(higher,lower);
		}
				
	}
	/**
	 * Attempts to make a union between parent and child. After locking it checks if invariants are still valid, and if not it reruns addEdge with same parameters.
	 * @param parent Parent node
	 * @param child	Child node
	 */
	private NodePair union(Node parent, Node child) {
		boolean rerun = false;
		Lock lockParent = parent.getLock();
		Lock lockChild = child.getLock();
		Boolean parentLocked = false;
		Boolean childLocked = false;
		
		try{
			parentLocked = lockParent.tryLock(1,TimeUnit.MILLISECONDS);
			childLocked = lockChild.tryLock(1,TimeUnit.MILLISECONDS);
			if (parentLocked && childLocked){
				//check invariants
				if (child.getRootNode().equals(child) && parent.getRootNode().equals(parent) && child.getRank() <= parent.getRank()){					
					if(connectedLeaderA.equals(child)){
						connectedLeaderA = parent;
					}
					if (connectedLeaderB.equals(child)){
						connectedLeaderB = parent;
					}
					if (connectedLeaderA.equals(connectedLeaderB)){
						this.connectedCitiesHandler.handleConnectedCityEvent(this);
					}
					parent.setRank(child.getRank()+1);
					child.setParent(parent);
				//rerun for invariant violations	
				} else{
					rerun = true;
				}
			}
		} catch (InterruptedException e) {		
		//unwind locks	
		} finally{
			if (parentLocked){
				lockParent.unlock();				
			}
			if (childLocked){
				lockChild.unlock();
			}
			if (rerun || (!parentLocked || !childLocked)) {
				//return the data, it needs to be processed again
				return new NodePair(parent,child);
			} 
		}
		return null;
		}
}


