package graph;

/**
 * This is an event handler for when two cities are connected in the UnionFindGraph.
 * @author Brian
 *
 */
public interface ConnectedCitiesHandler {
	public void handleConnectedCityEvent(UnionFindGraph graph);
}
