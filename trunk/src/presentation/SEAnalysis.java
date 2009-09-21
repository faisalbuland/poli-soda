package presentation;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import applicationLogic.conference.Conference;
import applicationLogic.graph.GraphManager;
import applicationLogic.journal.Transaction;
import applicationLogic.statistics.Author;



import dblp.social.exceptions.SodaHibernateException;

public class SEAnalysis {
	private static final String CONFERENCE = "icse";
	
	private static final String[] TRANSACTIONS = {"tse","tosem"};
	
	
	/**
	 * Perform an analysis on both ICSE conference and TSE and TOSEM transactions.
	 * This methods just group the methods defined in {@link ConferenceProperties}
	 * and {@link TransactionProperties}
	 * @param args
	 * @see {@link ConferenceProperties}, {@link TransactionProperties}
	 */
	public static void main(String[] args){
		try{
			Calendar begin = Calendar.getInstance();
			
			//conference initialization - ICSE
			Conference conf = ConferenceProperties.initializeConference(CONFERENCE);
			if (conf.isEmpty()){
				System.out.println("No proceedings found for "+conf.getConferenceName());
				return;
			}
			System.out.println("Initialized conference: "+conf.getConferenceName());
			
			//CONFERENCE ANALYSIS - ICSE
			//ConferenceProperties.timeTrendAnalysis(conf);
			//ConferenceProperties.graphAnalysis(conf);
			//ConferenceProperties.conferenceStatistics(conf);
			//to free memory
			conf = null;
			
			
			//transaction initialization - TSE
			Transaction t1 = TransactionProperties.initializeTransaction(TRANSACTIONS[0]);
			if (t1.isEmpty()){
				System.out.println("No Journals found for "+t1.getTransactionName());
				return;
			}
			System.out.println("Initialized transaction: "+t1.getTransactionName());
			
			//TRANSACTION ANALYSIS - TSE						
			TransactionProperties.volumeTrendAnalysis(t1);
			TransactionProperties.graphAnalysis(t1);
			TransactionProperties.transactionStatistics(t1);
			t1=null;
			
			//transaction initialization - TOSEM
			Transaction t2 = TransactionProperties.initializeTransaction(TRANSACTIONS[1]);
			if (t2.isEmpty()){
				System.out.println("No Journals found for "+t2.getTransactionName());
				return;
			}			
			System.out.println("Initialized transaction: "+t2.getTransactionName());			
			//TRANSACTION ANALYSIS - TOSEM						
			TransactionProperties.volumeTrendAnalysis(t2);
			TransactionProperties.graphAnalysis(t2);
			TransactionProperties.transactionStatistics(t2);
			t2=null;
			
			
			
			comuntyGraphAnalysis();

			Calendar end = Calendar.getInstance();
			long time = end.getTimeInMillis() - begin.getTimeInMillis();
			System.out.println("Time taken: "+time+" (mills)");
			
			
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	/**
	 * Creates a graph joining the co-authorship graphs of ICSE, TSE and TOSEM and extract its properties.
	 * The results are printed on txt files which can be found in the $APPLICATION_PATH/SEComunity/Graph/ folder
	 * @throws IOException
	 * @throws SodaHibernateException
	 */
	public static void comuntyGraphAnalysis() throws IOException, SodaHibernateException{
		//GRAPH ANALYSIS OVER THE WHOLE COMUNITY GRAPH (ICSE+TSE+TOSEM)
		GraphManager gm = new GraphManager();
		System.out.println("Initializing: ICSE");
		Conference conf = ConferenceProperties.initializeConference(CONFERENCE);
		System.out.println("Expanding graph");
		gm.expandGraph(conf);
		conf=null;
		
		System.out.println("Initializing: TSE");
		Transaction t1 = TransactionProperties.initializeTransaction(TRANSACTIONS[0]);
		System.out.println("Expanding graph");
		gm.expandGraph(t1);
		t1=null;
		
		System.out.println("Initializing: TOSEM");
		Transaction t2 = TransactionProperties.initializeTransaction(TRANSACTIONS[1]);
		System.out.println("Expanding graph");
		gm.expandGraph(t2);
		t2=null;
		
		
		System.out.println("Graph contains:\n" + 
				gm.getGraphSize()+
				" nodes"+
				//graph.getEdgeCount()+" edges"+
				"\nThe largest connected component contains:\n"+
				gm.getLargestConnectedComponentSize()+" nodes\n" +
				//lcs.getEdgeCount()+" edges");
				"There are "+gm.getConnectedComponentsNumber()+" connected subgraphs");
		
		List<Author> authors =null;
		int cont=1;
		StringBuffer buffer = new StringBuffer();
		File output;
		FileWriter fw;
		
		DecimalFormat df1 = new DecimalFormat("####.####");
	
		File dir = new File("SEComunity");
		if (!dir.exists())
			dir.mkdir();
		
		File subD2 = new File(dir.getAbsolutePath()+"/Graph");
		if (!subD2.exists())
			subD2.mkdir();
		
		//NODES PROPERTIES
		
		
		//CLOSENESS CENTRALITY
		System.out.println("Calculating Closeness centrality");
		authors = gm.getAuthorsClosenessCentrality();
		System.out.println("Done");
		
		
		
		
		output = new File("SEComunity/Graph"+
				"/authors-closeness-centrality.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		cont=1;
		buffer.append("Closeness Centrality Score\n");
		for (int i = 0; i<authors.size(); i++){
			buffer.append("["+cont+"]\t\t"+df1.format(authors.get(i).getClosenessCentrality())+
			"\t\t"+authors.get(i).getName()+"\n");
			cont++;
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		
		//DEGREE
		System.out.println("Calculating authors' degree");
		authors = gm.getAuthorsDegree();
		System.out.println("Done");
		
		
		
		output = new File("SEComunity/Graph"+
				"/authors-degree.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		cont=1;
		buffer.append("Degree Score\n");
		for (int i = authors.size()-1; i>=0; i--){
			buffer.append("["+cont+"]\t\t"+df1.format(authors.get(i).getDegree())+
			"\t\t"+authors.get(i).getName()+"\n");
			cont++;
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		
		
		//CLUSTERING COEFFICIENT
		System.out.println("Calculating Clustering Coefficient");
		authors = gm.getAuthorsClusteringCoefficient();
		System.out.println("Done");
		
		
		output = new File("SEComunity/Graph"+
				"/authors-clustering-coefficient.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		cont=1;
		buffer.append("Clustering Coefficient Score\n");
		for (int i = authors.size()-1; i>=0; i--){
			buffer.append("["+cont+"]\t\t"+df1.format(authors.get(i).getClusteringCoefficient())+
			"\t\t"+authors.get(i).getName()+"\n");
			cont++;
		}
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		
		//GRAPH PROPRIETIES
		
		
		output = new File("SEComunity/Graph"+
				"/graph-properties.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		
		buffer.append("Graph Properties\n");
		buffer.append("Graph size: "+gm.getGraphSize()+"\n");
		buffer.append("Largest Connected Component size: "+gm.getLargestConnectedComponentSize()+"\n");
		double perc = (double)gm.getLargestConnectedComponentSize()/(double)gm.getGraphSize()*100;
		buffer.append("Percentage: "+df1.format(perc)+"%\n");
		
		//DIAMETER
		Calendar begin = Calendar.getInstance();
		System.out.println("Calculating largest connected subgraph diameter");
		double diameter = gm.getGraphDiameter();
		Calendar end = Calendar.getInstance();
		long timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
		System.out.println("done\nTime taken: "+timeTaken);
		
		buffer.append("Diameter: "+df1.format(diameter)+"\n");
		
		//CHARACTERISTIC PATH LENGTH
		begin = Calendar.getInstance();
		System.out.println("Calculating characteristic path length of the largest connected subgraph");
		double cpl = gm.getGraphCharacteristicPathLength();
		end = Calendar.getInstance();
		timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
		System.out.println("done\nTime taken: "+timeTaken);
		
		buffer.append("Characteristic path length: "+df1.format(cpl)+"\n");
		
		
		//AVERAGE CLUSTERING COEFFICIENT
		System.out.println("Calculating average clusering coefficient");
		double gcc = gm.getGraphClusteringCoefficient();
		System.out.println("Done");
		
		buffer.append("Average clusering coefficient: "+df1.format(gcc)+"\n");
		
		
		//AVERAGE DEGREE
		System.out.println("Calculating average degree");
		double avgdg =gm.getAverageDegree();
		System.out.println("Done");
		buffer.append("Average Degree: "+df1.format(avgdg)+"\n");
		
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		//DEGREE DISTRIBUTION (NORMALIZED)
		System.out.println("Calculating degree distribution (normalized)");
		List<Double> ndd = gm.getNormalizedDegreeDistribution();
		System.out.println("Done");
		
		output = new File("SEComunity/Graph"+
				"/graph-normalized-degree-distribution.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		buffer.append("Normalized Degree Distribution\n");
		buffer.append("DEGREE\t\tVALUE\n");
		
		for (int i=0;i<ndd.size();i++){
			buffer.append(i+"\t\t"+df1.format(ndd.get(i))+"\n");
		}
		
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		//DEGREE DISTRIBUTION
		System.out.println("Calculating degree distribution");
		List<Integer> dd = gm.getDegreeDistribution();
		System.out.println("Done");
		
		
		output = new File("SEComunity/Graph"+
				"/graph-degree-distribution.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		buffer.append("Degree Distribution\n");
		buffer.append("DEGREE\t\tVALUE\n");
		for (int i=0;i<dd.size();i++){
			buffer.append(i+"\t\t"+df1.format(dd.get(i))+"\n");
		}
		
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
	}
}
