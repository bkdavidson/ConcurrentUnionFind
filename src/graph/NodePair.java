package graph;

/**
 * This class is meant to be a key object that stores two nodes in natural string order.
 * User: Brian
 */
public class NodePair {
    private Node firstNode;
    private Node secondNode;

    public NodePair(Node firstNode, Node secondNode) {
        //maintain order
        if (firstNode.getName().compareTo(secondNode.getName()) <= 0) {
            this.firstNode = firstNode;
            this.secondNode = secondNode;
        } else {
            this.secondNode = firstNode;
            this.firstNode = secondNode;
        }
    }
    
    public Node getFirstNode(){
    	return firstNode;
    }
    public Node getSecondNode(){
    	return secondNode;
    }
    
}
