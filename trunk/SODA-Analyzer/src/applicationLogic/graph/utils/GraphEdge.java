package applicationLogic.graph.utils;
/**
 * This class defines an edge for a co-authorship graph.
 * 
 * @author Staffiero
 *
 */
public class GraphEdge {
	/**
	 * A unique id for the edge.
	 */
	private int edgeId;
	/**
	 * The edge weight
	 */
	private float weight;



	public GraphEdge(int edgeId) {
		super();
		this.edgeId = edgeId;
	}

	public GraphEdge(int edgeId, float weight) {
		super();
		this.edgeId = edgeId;
		this.weight = weight;
	}

	
	public int getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(int edgeId) {
		this.edgeId = edgeId;
	}

	public float getweight() {
		return weight;
	}

	public void setweight(float weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "["+this.edgeId+"]";
	}

	
	
	
}
