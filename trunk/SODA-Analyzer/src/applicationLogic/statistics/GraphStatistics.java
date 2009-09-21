package applicationLogic.statistics;
/**
 * This class is a container of some co-authorship graph statistics.
 * The year field is used to store the year if the graph is built with a conference, or the volume number if the graph is built with a transaction.
 * 
 * @author Staffiero
 *
 */
public class GraphStatistics {
	private int year;
	
	private int graphSize;
	
	private int connectedComponentsNumber;
	
	private int secondLargestConnectedComponentSize;
	
	private int largestConnectedComponentSize;
	
	/**
	 * Largest connected subgraph characteristic path length
	 */
	private double largestComponentCPL;
	
	/**
	 * Largest connected subgraph average clustering coefficient
	 */
	private double largestComponentAVGCC;
	/**
	 * Largest connected subgraph Diameter
	 */
	private int largestComponentDiameter;
	
	/**
	 * The whole graph average degree
	 */
	private double averageDegree;
	
	
	public GraphStatistics(int year) {
		super();
		this.year = year;
	}


	public GraphStatistics(int year, int graphSize,
			int connectedComponentsNumber, int largestConnectedComponent,
			double largestComponentCPL, double largestComponentAVGCC,
			int largestComponentDiameter) {
		super();
		this.year = year;
		this.graphSize = graphSize;
		this.connectedComponentsNumber = connectedComponentsNumber;
		this.largestConnectedComponentSize = largestConnectedComponent;
		this.largestComponentCPL = largestComponentCPL;
		this.largestComponentAVGCC = largestComponentAVGCC;
		this.largestComponentDiameter = largestComponentDiameter;
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}


	public int getGraphSize() {
		return graphSize;
	}


	public void setGraphSize(int graphSize) {
		this.graphSize = graphSize;
	}


	public int getConnectedComponentsNumber() {
		return connectedComponentsNumber;
	}


	public void setConnectedComponentsNumber(int connectedComponentsNumber) {
		this.connectedComponentsNumber = connectedComponentsNumber;
	}


	public int getLargestConnectedComponentSize() {
		return largestConnectedComponentSize;
	}


	public void setLargestConnectedComponentSize(int largestConnectedComponent) {
		this.largestConnectedComponentSize = largestConnectedComponent;
	}


	public double getLargestComponentCPL() {
		return largestComponentCPL;
	}


	public void setLargestComponentCPL(double largestComponentCPL) {
		this.largestComponentCPL = largestComponentCPL;
	}


	public double getLargestComponentAVGCC() {
		return largestComponentAVGCC;
	}


	public void setLargestComponentAVGCC(double largestComponentAVGCC) {
		this.largestComponentAVGCC = largestComponentAVGCC;
	}


	public int getLargestComponentDiameter() {
		return largestComponentDiameter;
	}


	public void setLargestComponentDiameter(int largestComponentDiameter) {
		this.largestComponentDiameter = largestComponentDiameter;
	}


	public int getSecondLargestConnectedComponentSize() {
		return secondLargestConnectedComponentSize;
	}


	public void setSecondLargestConnectedComponentSize(
			int secondLargestConnectedComponentSize) {
		this.secondLargestConnectedComponentSize = secondLargestConnectedComponentSize;
	}


	public double getAverageDegree() {
		return averageDegree;
	}


	public void setAverageDegree(double averageDegree) {
		this.averageDegree = averageDegree;
	}
	
	
	
	
}
