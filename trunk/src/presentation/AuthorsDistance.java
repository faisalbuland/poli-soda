package presentation;

import java.util.Calendar;
import java.util.Properties;

import applicationLogic.comunity.Distance;


import dblp.social.hibernate.ISodaHibernateSession;
import dblp.social.hibernate.SodaHibernateSessionFactory;
import dblp.social.hibernate.SodaModelProvider;
/**
 * A simple Presentation class to request the computation of the distance between 2 authors: AUTHOR1 and AUTHOR2 within LIMIT steps.
 * The result of the computation is printed on the standard output.
 * @author Staffiero
 *
 */
public class AuthorsDistance {
	
//	private static final String AUTHOR1 ="Nicola Gatti";
//	private static final String AUTHOR2 = "Giordano Tamburrelli";
//	private static final int LIMIT = 3;
//	"Nicola Gatti";
//	"Carlo Siciliano";
//	"Axel Polleres";
	
	public static void main(String[] args){
		if (args.length==0){
			showHelp();
			return;
		}
		if(args.length!=3){
			System.out.println("First arg: "+args[0]+" Second arg: "+args[1]);
			showHelp();
			return;
		}
		String author1 = args[0];
		String author2 = args[1];
		int limit = 0;
		try{
			 limit = Integer.parseInt(args[2]);
		}
		catch(Exception e){
			e.printStackTrace();
			showHelp();
			return;
		}
		try{
			Properties props = DBConfLoader.loadDbConfigurations();
			
			if (props==null)
				return;
			
			ISodaHibernateSession session = 
				SodaHibernateSessionFactory.getSession(
					props.getProperty("dbUrl"),
					props.getProperty("dbDialect"),
					props.getProperty("dbDriverName"), 
					props.getProperty("dbUser"), 
					props.getProperty("dbPasswd"), 
					new SodaModelProvider());
			
			Calendar begin = Calendar.getInstance();
			
			System.out.println("Calculating distance from "+author1+" to "
					+author2+" limit: "+limit);
			Distance dist = new Distance(session); 
			int d = dist.distanceAI(author1, author2, limit);
			if(d==-1)
				System.out.println(author2+" can not be reached from " +
						author1+" within "+limit+" steps");
			else if(d==-2)
				System.out.println("Autor"+author1+" not found");
			else if(d==-3)
				System.out.println("Autor"+author2+" not found");
			else
				System.out.println("Distance from "+author1+" to " +
						author2+": "+d);
			Calendar end = Calendar.getInstance();
			
			long time = end.getTimeInMillis()-begin.getTimeInMillis();
			System.out.println("Time taken: "+time);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void showHelp(){
		System.out.println("Arguments: \n" +
				"author1: \tthe name of the first author\n" +
				"author2: \tthe name of the second author\n" +
				"limit: \t\tthe search depth limit\n");
	}
}

