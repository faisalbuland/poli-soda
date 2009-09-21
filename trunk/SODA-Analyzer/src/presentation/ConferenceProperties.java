package presentation;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import applicationLogic.conference.Conference;
import applicationLogic.conference.trend.TimeTrend;
import applicationLogic.graph.GraphManager;
import applicationLogic.statistics.Author;
import applicationLogic.statistics.AuthorsStatistics;
import applicationLogic.statistics.GraphStatistics;
import applicationLogic.statistics.PublicationsStatistics;

import dblp.social.exceptions.SodaHibernateException;
import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.SodaHibernateSessionFactory;
import dblp.social.hibernate.SodaModelProvider;
/**
 * A simple Presentation class used to print the results of the conference analysis.
 * The result of the computation is printed different txt files.
 * @author Staffiero
 *
 */
public class ConferenceProperties {
	private static final String CONFERENCE = "icse";
	
	private static DecimalFormat df1 = new DecimalFormat("###0.####");
	private static DecimalFormat df2 = new DecimalFormat("###.00");
	

	/**
	 * The main method simply calls the other methods defined in this class.
	 * @param args
	 */
	public static void main(String[] args){
		try {
			
			Calendar begin = Calendar.getInstance();
			
			Conference conf = initializeConference(CONFERENCE);
			
			if (conf.isEmpty()){
				System.out.println("No proceedings found for "+conf.getConferenceName());
				return;
			}
			
			System.out.println("Initialized conference: "+conf.getConferenceName());
			
			
			File dir = new File(conf.getConferenceName());
			if (!dir.exists())
				dir.mkdir();
			
			File subD1 = new File(dir.getAbsolutePath()+"/TimeTrend");
			if (!subD1.exists())
				subD1.mkdir();
			
			File subD2 = new File(dir.getAbsolutePath()+"/Graph");
			if (!subD2.exists())
				subD2.mkdir();
			
			File subD3 = new File(dir.getAbsolutePath()+"/Conference");
			if (!subD3.exists())
				subD3.mkdir();
	
						
			//timeTrendAnalysis(conf);
			//graphAnalysis(conf);
			conferenceStatistics(conf);
				
			Calendar end = Calendar.getInstance();
			long time = end.getTimeInMillis() - begin.getTimeInMillis();
			
			System.out.println("Time taken: "+time+" (mills)");

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		} 
		
	}
	
	
	
	/**
	 * Creates a new {@link Conference} object with the given conference short name.
	 * This method opens an Hiberante session which will be stored in the Conference object for further queries.
	 * 
	 * @param conference the conference short name
	 * @return the initialized Conference object.
	 * @throws SodaHibernateException
	 * @throws IOException
	 */
	public static Conference initializeConference(String conference) throws SodaHibernateException, IOException{
		//Create a session
		Properties props = DBConfLoader.loadDbConfigurations();
		
		if (props==null)
			return null;
		
		ISodaHibernateSession session = 
			SodaHibernateSessionFactory.getSession(
				props.getProperty("dbUrl"),
				props.getProperty("dbDialect"),
				props.getProperty("dbDriverName"), 
				props.getProperty("dbUser"), 
				props.getProperty("dbPasswd"), 
				new SodaModelProvider());
			
		
		//create a conference object
		
//		ArrayList<String> aamas =  new ArrayList<String>();
//		aamas.add("aamas");   
//		aamas.add("atal");
//		aamas.add("ecaiw");
		Conference conf = new Conference(session, conference);
		
		File dir = new File(conf.getConferenceName());
		if (!dir.exists())
			dir.mkdir();
		
		File subD1 = new File(dir.getAbsolutePath()+"/TimeTrend");
		if (!subD1.exists())
			subD1.mkdir();
		
		File subD2 = new File(dir.getAbsolutePath()+"/Graph");
		if (!subD2.exists())
			subD2.mkdir();
		
		File subD3 = new File(dir.getAbsolutePath()+"/Conference");
		if (!subD3.exists())
			subD3.mkdir();
		
		
		return conf;
	}
	
	/**
	 * Creates a new conference object with the given conference short names.
	 * This method opens an Hiberante session which will be stored in the Conference object for further queries.
	 * 
	 * @param conference a list of conference short names
	 * @return the initialized Conference object.
	 * @throws SodaHibernateException
	 * @throws IOException
	 */
	public static Conference initializeConference(List<String> conference) throws SodaHibernateException, IOException{
		//Create a session
		Properties props = DBConfLoader.loadDbConfigurations();
		
		if (props==null)
			return null;
		
		ISodaHibernateSession session = 
			SodaHibernateSessionFactory.getSession(
				props.getProperty("dbUrl"),
				props.getProperty("dbDialect"),
				props.getProperty("dbDriverName"), 
				props.getProperty("dbUser"), 
				props.getProperty("dbPasswd"), 
				new SodaModelProvider());
			
		
		//create a conference object
		
//		ArrayList<String> aamas =  new ArrayList<String>();
//		aamas.add("aamas");   
//		aamas.add("atal");
//		aamas.add("ecaiw");
		Conference conf = new Conference(session, conference);
		
		return conf;
	}
	
	/**
	 * Performs a time trend analysis of the given conference using the methods defined in the {@link TimeTrend} object.
	 * All the results are stored in txt files saved in the $APPLICATION_PATH/$CONFERENCE_NAME/TimeTrend/ folder
	 * 
	 * @param conf the conference to analyze.
	 * @throws SodaHibernateException
	 * @throws IOException
	 */
	public static void timeTrendAnalysis(Conference conf) throws SodaHibernateException, IOException{
		//Create a time trend analysis tool
		TimeTrend tt = new TimeTrend(conf);
		
		
		//AUTHORS TREND
		System.out.println("Calculating authors statistics");		
		List<AuthorsStatistics> authorsTrend = 
			tt.getAuthorsTimeTrend();
		System.out.println("Done");
		
		
		//Create a file and a buffer to store authors' statistics
		File output = new File(conf.getConferenceName()+"/TimeTrend"+
				"/authors-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		FileWriter fw = new FileWriter(output);
		StringBuffer buffer = new StringBuffer();
		
		
		
		//print statistics
		buffer.append("Year\t\t" +
				"Authors\t\t" +
				"New authors\t\t" +
				"New authors(%)\t\t" +
				"Total authors\n");
		for (AuthorsStatistics stats : authorsTrend){
			DecimalFormat df1 = new DecimalFormat("####.##");
			float nap = ((float)stats.getNewAuthorsNumber()/(float)stats.getAuthorsNumber())*100;
			buffer.append(stats.getYear()+"\t\t"+
					stats.getAuthorsNumber()+"\t\t"+
					stats.getNewAuthorsNumber()+"\t\t\t"+
					df1.format(nap)+"%\t\t\t"+
					stats.getTotalAuthors()+"\n");
		}
		
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		//PUBLICATIONS TREND
		System.out.println("Calculating publications statistics");
		List<PublicationsStatistics> publicationsTrend = 
			tt.getPublicationsTimeTrend();
		System.out.println("Done");
		
		output = new File(conf.getConferenceName()+"/TimeTrend"+
				"/publications-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("Year\t\tNumber of publications\n");
		for (PublicationsStatistics stats : publicationsTrend){
			buffer.append(stats.getYear()+"\t\t\t"+
					stats.getPublicationsNumber()+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		
		//PRODUCTIVITY TREND
		System.out.println("Calculating productivity 1");
		List<PublicationsStatistics> prodTrend = 
			tt.getProductivityTrend();
		System.out.println("Done");
		
		output = new File(conf.getConferenceName()+"/TimeTrend"+
				"/productivity-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("Year\t\tProductivity\n");
		for (PublicationsStatistics stats : prodTrend){
			buffer.append(stats.getYear()+"\t\t"+
					df1.format(stats.getProductivity())+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		//PRODUCTIVITY TREND (ACTIVE AUTHORS ONLY)
		System.out.println("Calculating productivity 2");
		prodTrend = 
			tt.getProductivityTrendActiveAuthors();
		System.out.println("Done");
		
		output = new File(conf.getConferenceName()+"/TimeTrend"+
				"/productivity-trend-active-authors.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("Year\t\tProductivity\n");
		for (PublicationsStatistics stats : prodTrend){
			buffer.append(stats.getYear()+"\t\t"+
					df1.format(stats.getProductivity())+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		//GRAPH TREND
		System.out.println("Calculating graph statistics");
		List<GraphStatistics> graphTrend = 
			tt.getGraphStatisticsTimeTrend();
		System.out.println("Done");
		
		
		
		output = new File(conf.getConferenceName()+"/TimeTrend"+
		"/authors-graph-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("LCC = Largest Connected Component\n"+
				"CPL = Characteristic Path Length\n"+
				"AVGCC = Average Clustering Coefficient\n" +
				"year\t\tgraph Size\tconn. components\tsecond LCC\t\tLCC size\t\tLCC CPL\t\tLCC AVGCC\tLCC Diameter\n");
		for (GraphStatistics stats : graphTrend){
			buffer.append(stats.getYear()+"\t\t"+
					stats.getGraphSize()+"\t\t"+
					stats.getConnectedComponentsNumber()+"\t\t\t"+
					stats.getSecondLargestConnectedComponentSize()+
					" ("+
					df2.format((double)stats.getSecondLargestConnectedComponentSize()/(double)stats.getGraphSize()*100)+
					"%)"+
					"\t\t"+
					stats.getLargestConnectedComponentSize()+
					" ("+
					df2.format((double)stats.getLargestConnectedComponentSize()/(double)stats.getGraphSize()*100)+
					"%)"+
					"\t\t"+
					df1.format(stats.getLargestComponentCPL())+"\t\t"+
					df1.format(stats.getLargestComponentAVGCC())+"\t\t"+
					stats.getLargestComponentDiameter()+
					"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		//GRAPH TREND (INCREMENTAL)
		System.out.println("Calculating graph statistics (incremental)");
		graphTrend = 
			tt.getIncrementalGraphStatistics();
		System.out.println("Done");
		
		
		output = new File(conf.getConferenceName()+"/TimeTrend"+
		"/authors-incremental-graph-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("LCC = Largest Connected Component\n"+
				"CPL = Characteristic Path Length\n"+
				"AVGCC = Average Clustering Coefficient\n" +
				"year\t\tgraph Size\tconn. components\tsecond LCC\t\tLCC size\t\tLCC CPL\t\tLCC AVGCC\tLCC Diameter\n");
		for (GraphStatistics stats : graphTrend){
			buffer.append(stats.getYear()+"\t\t"+
					stats.getGraphSize()+"\t\t"+
					stats.getConnectedComponentsNumber()+"\t\t\t"+
					stats.getSecondLargestConnectedComponentSize()+
					" ("+
					df2.format((double)stats.getSecondLargestConnectedComponentSize()/(double)stats.getGraphSize()*100)+
					"%)"+
					"\t\t"+
					stats.getLargestConnectedComponentSize()+
					" ("+
					df2.format((double)stats.getLargestConnectedComponentSize()/(double)stats.getGraphSize()*100)+
					"%)"+
					"\t\t"+
					df1.format(stats.getLargestComponentCPL())+"\t\t"+
					df1.format(stats.getLargestComponentAVGCC())+"\t\t"+
					stats.getLargestComponentDiameter()+
					"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		//GRAPH AVERAGE DEGREE (INCREMENTAL)
		output = new File(conf.getConferenceName()+"/TimeTrend"+
		"/average-coauthors-trend.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		//prints statistics
		buffer.append("year\t\tAverage Coauthors\n");
		for (GraphStatistics stats : graphTrend){
			buffer.append(stats.getYear()+"\t\t"+
					df2.format((double)stats.getAverageDegree())+
					"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
//		System.out.println("Trying fetch with time window");
//		publicationsTrend = 
//			tt.getPublicationsTimeWindowTrend(1998, 2008);
//		
//		//prints statistics
//		for (PublicationsStatistics stats : publicationsTrend){
//			System.out.println("\nYear: "+stats.getYear()+
//					"\nNumber of publications: "+stats.getPublicationsNumber());
//		}
	}
	
	
	/**
	 * Performs an analysis on the co-authorship graph of the given conference using the methods defined in the {@link GraphManager} object.
	 * All the results are stored in txt files saved in the $APPLICATION_PATH/$CONFERENCE_NAME/Graph/ folder
	 * 
	 * @param conf the conference to analyze.
	 * @throws SodaHibernateException
	 * @throws IOException
	 */
	public static void graphAnalysis(Conference conf) throws SodaHibernateException, IOException{
		//GRAPH CONSTRUCTION
		conf.initializeAll();
		
		GraphManager gm = new GraphManager(conf);
		
		System.out.println("Graph contains:\n" + 
				gm.getGraphSize()+
				" nodes\n"+
				//graph.getEdgeCount()+" edges"+
				"\nThe largest connected component contains:\n"+
				gm.getLargestConnectedComponentSize()+" nodes\n" +
				//lcs.getEdgeCount()+" edges");
				"There are "+gm.getConnectedComponentsNumber()+" connected subgraphs");
		
		//Some vars for later use
		List<Author> authors =null;
		
		int cont=1;
		
		StringBuffer buffer = new StringBuffer();
		
		File output;
		
		FileWriter fw;
		//NODES PROPERTIES
		
		
		//CLOSENESS CENTRALITY
		System.out.println("Calculating Closeness centrality");
		authors = gm.getAuthorsClosenessCentrality();
		System.out.println("Done");
		
		
		output = new File(conf.getConferenceName()+"/Graph"+
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
		
		
		output = new File(conf.getConferenceName()+"/Graph"+
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
		
		
		output = new File(conf.getConferenceName()+"/Graph"+
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
		output = new File(conf.getConferenceName()+"/Graph"+
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
		System.out.println("done\nTime taken: "+timeTaken+"\nGraph diameter: "+df1.format(diameter));
		
		buffer.append("Diameter: "+df1.format(diameter)+"\n");
		
		//CHARACTERISTIC PATH LENGTH
		begin = Calendar.getInstance();
		System.out.println("Calculating the characteristic path length of the largest connected subgraph");
		double cpl = gm.getGraphCharacteristicPathLength();
		end = Calendar.getInstance();
		timeTaken = end.getTimeInMillis() - begin.getTimeInMillis();
		System.out.println("done\nTime taken: "+timeTaken);
		
		buffer.append("Characteristic path length: "+df1.format(cpl)+"\n");
		
		
		//AVERAGE CLUSTERING COEFFICIENT
		System.out.println("Calculating the average clusering coefficient:");
		double gcc = gm.getGraphClusteringCoefficient();
		System.out.println("Done");
		buffer.append("Average clusering coefficient: "+df1.format(gcc)+"\n");
		
		
		//AVERAGE DEGREE
		System.out.println("Calculating the average degree");
		double avgdg =gm.getAverageDegree();
		System.out.println("Done");
		buffer.append("Average Degree: "+df1.format(avgdg)+"\n");
		
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		//DEGREE DISTRIBUTION (NORMALIZED)
		System.out.println("Calculating the degree distribution (normalized)");
		List<Double> ndd = gm.getNormalizedDegreeDistribution();
		System.out.println("Done");
		
		
		output = new File(conf.getConferenceName()+"/Graph"+
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
		System.out.println("Calculating the degree distribution (normalized)");
		List<Integer> dd = gm.getDegreeDistribution();
		System.out.println("Done");
		
		
		output = new File(conf.getConferenceName()+"/Graph"+
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
		
		
		//BETWEENNESS CENTRALITY
//		System.out.println("Calculating Betweenness centrality");
//		authors = gm.getAuthorsBetweennessCentrality();
//		System.out.println("Done");
//		Collections.sort(authors,new ClosenessComparator());
//		
//		output = new File(conf.getConferenceName()+
//				"/authors-betweenness-centrality.txt");
//		if (output.exists())
//			output.delete();
//		output.createNewFile();
//		fw = new FileWriter(output);
//		
//		cont=1;
//		buffer.append("Betweenness Centrality Score\n");
//		for (int i = 0; i<authors.size(); i++){
//			buffer.append("["+cont+"]\t\t"+df1.format(authors.get(i).getBetweennessCentrality())+
//			"\t\t"+authors.get(i).getName()+"\n");
//			cont++;
//		}
//		
//		//write the file and flush the buffer
//		fw.write(buffer.toString());
//		fw.close();
//		buffer = new StringBuffer();
		
		
		//Son tutti uguali, probabilmente perchè il grafo è non orientato 
//		System.out.println("Calculating Eigenvector centrality");
//		authors = gm.getAuthorsEigenvectorCentrality();
//		System.out.println("Done");
//		Collections.sort(authors,new EigenvectorComparator());
//		
//		DecimalFormat df2 = new DecimalFormat("####.######");
//		cont=1;
//		System.out.println("Eigenvector Centrality score (top 10):");
//		for (int i = authors.size()-1; i>authors.size()-10; i--){
//			System.out.println("["+cont+"]\t\t"+df2.format(authors.get(i).getEigenvectorCentrality())+
//			"\t\t"+authors.get(i).getName());
//			cont++;
//		}
//		
//		cont=1;
//		System.out.println("Eigenvector Centrality score (worst 10):");
//		for (int i = 0; i<10; i++){
//			System.out.println("["+cont+"]\t\t"+df2.format(authors.get(i).getEigenvectorCentrality())+
//			"\t\t"+authors.get(i).getName());
//			cont++;
//		}
	}
		
	/**
	 * Performs an analysis on the given conference, extracting some general statistics, using the methods defined in the {@link Conference} object itself.
	 * All the results are stored in txt files saved in the $APPLICATION_PATH/$CONFERENCE_NAME/Conference/ folder
	 * 
	 * @param conf the conference to analyze.
	 * @throws SodaHibernateException
	 * @throws IOException
	 */
	public static void conferenceStatistics(Conference conf) throws SodaHibernateException, IOException{
		
		StringBuffer buffer = new StringBuffer();
		//AUTHORS-PUBLICATIONS			
		System.out.println("Calculating authors-publications statistics");
		List<Integer> authorsPublications= 
			conf.getAuthorsPublications();
		System.out.println("Done");
		
		
		
		File output = new File(conf.getConferenceName()+"/Conference"+
				"/authors-publications.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		FileWriter fw = new FileWriter(output);
		//prints statistics
		buffer.append("Number of pulications with the given authors number\n");
		buffer.append("#Authors\t\t#Publications\n");
		for (int i=0; i<authorsPublications.size();i++){
			buffer.append(i+"\t\t\t"+
					authorsPublications.get(i)+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		//PUBLICATIONS AUTHORS
		System.out.println("Calculating publications-authors statistics");
		List<Integer> publicationsAuthors= 
			conf.getPublicationsAuthors();
		System.out.println("Done");
		
		output = new File(conf.getConferenceName()+"/Conference"+
				"/publications-authors.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		
		
		
		//prints statistics
		buffer.append("Number of authors with the given publications number\n");
		buffer.append("#Publications\t\t#Authors\n");
		for (int i=0; i<publicationsAuthors.size();i++){
			buffer.append(i+"\t\t\t"+
					publicationsAuthors.get(i)+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
		
		
		//AUTHORS PUBLICATIONS NUMBER
		System.out.println("Calculating publications number for each author");
		List<Author> pubsNumber= 
			conf.getAuthorsPublicationsNumber();
		System.out.println("Done");
		
		output = new File(conf.getConferenceName()+"/Conference"+
				"/publications-number-per-author.txt");
		if (output.exists())
			output.delete();
		output.createNewFile();
		fw = new FileWriter(output);
		
		//prints statistics
		buffer.append("Author\t\t\t#Publications\n");
		for (int i=pubsNumber.size()-1; i>=0;i--){
			buffer.append(pubsNumber.get(i).getPublicationsNumber()+"\t\t\t"+
					pubsNumber.get(i).getName()+"\n");
		}
		
		//write the file and flush the buffer
		fw.write(buffer.toString());
		fw.close();
		buffer = new StringBuffer();
		
	}

	
}
