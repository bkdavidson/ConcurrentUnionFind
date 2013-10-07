package graph;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
	private volatile int rank;
	private volatile Node parent;
	private String name;
	private final Lock lock = new ReentrantLock();
	
	@SuppressWarnings("unused")
	private Node(){		
	}
	
	public Node(String name){
		rank = 0;
		this.name = name;
	}
	
	public void setRank(int rank){
		this.rank = rank;
	}
	
	public int getRank(){
		return rank;
	}
	
	public void setParent(Node parent){
		this.parent = parent;
	}
		
	public Node getParent(){
		return parent;
	}
	
	public String getName(){
		return name;
	}
	
	public Node getRootNode(){
		if (this.parent == null)
			return this;
		else {
			parent = parent.getRootNode();
			return parent;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Lock getLock(){
		return this.lock;
	}
	
	
}
