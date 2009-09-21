package dblp.social;

import java.io.File;

import dblp.social.runner.Runner;
/**
 * This class runs the parser or the updater. The parser and the updater can be launched in single or multi thread mode.
 * When calling the main method of this class you have to provide the file to be parsed, the parsing mode and choose whether to launch the parser or the updater.
 * Call the main method without arguments or with the -h|--help options.
 * The file to be parsed is saved in the local var fileXML: String.
 * To choose the parser mode
 * 
 * 
 * @author Staffiero
 *
 */
public class MainClass {
	private static final String JAR_NAME ="importer";
	
	public static void main(String[] args){
		if(args.length==0){
			showHelp();
			return;
		}
		
		//the XML file to be parsed
		//String fileXML = "./dblp_proceedings_snippet3.xml";
		//String fileXML = "./dblp_book_complete.xml";
		//String fileXML = "./dblp_journal_snippet.xml";
		//String fileXML = "./dblp_mixed_snippet.xml";
		if (args[0].compareTo("-f")!=0){
			if (args[0].compareTo("-c")==0){
				if (args.length>1){
					showHelp();
					return;
				}
				//runs a method which clears all info stored in the db
				Runner.runCleaner();
				return;
			}
			else{
				showHelp();
				return;
			}
		}
		if (args.length==1){
			showHelp();
			return;
		}
		String fileXML=args[1];
		File inputFile = new File(fileXML);
		if (!inputFile.exists()){
			System.out.println("Error, the selected file: "+fileXML+
					" does not exist");
			return;
		}
		boolean multithread=true;
		boolean clearAll=false;
		boolean update=false;
		if(args.length>=2){
			for (int i=2; i<args.length;i++){
				if (args[i].equalsIgnoreCase("-m")){
					multithread=true;
				}
				else if (args[i].equalsIgnoreCase("-s")){
					multithread=false;
				}
				else if (args[i].equalsIgnoreCase("-c")){
					clearAll=true;
				}
				else if(args[i].equalsIgnoreCase("--update")){
					update=true;
				}
				else{
					showHelp();
					return;
				}
					
			}
			if (clearAll && update){
				System.out.println("Cannot run the updater after cleaning all stored " +
						"informations.\n" +
						"To run the updater use:\t"+JAR_NAME+" -f filename --update [-m/-s]\n");
				return;
			}
			if (clearAll){
				//runs a method which clears all info stored in the db
				Runner.runCleaner();
			}
			if (!update){
				if (multithread){
					//runs the importer in multi-thread mode
					Runner.runImporterMultiThread(fileXML);
				}
				else{
					//runs the importer in single-thread mode (default)
					Runner.runImporterSingleThread(fileXML);
				}
			}
			else{
				if (multithread){
					//runs the updater in multi-thread mode
					Runner.runUpdaterMultiThread(fileXML);
				}
				else{
					//runs the updater in single-thread mode (default)
					Runner.runUpdaterSingleThread(fileXML);
				}
			}
		}else{
			//runs the importer in single-thread mode (default)
			Runner.runImporterMultiThread(fileXML);
		}
		
		
		
		
		//runs the importer in multi-thread mode
		//Runner.runImporterMultiThread(fileXML);
		
		//runs the importer in single-thread mode
		//Runner.runImporterSingleThread(fileXML);
	}


	private static void showHelp(){
		System.out.println("\nUsage: "+JAR_NAME+" -f filename [-options]\n\n" +
			"Where options include:\n" +
			"\t-m/-M\t\trun the application in multi-thread mode (default).\n" +
			"\t-s/-S\t\trun the application in single-thread mode.\n" +
			"\t-c/-C\t\tclear all the stored info before running the importer, " +
			"\n\t\t\tnote that everything stored in the database will be lost.\n" +
			"\t--update\tUpdates the database with the given file (to use if the " +
			"XML file contains entries already inserted in the database)" +
			"\n"+
			"\nIt is also possible to call "+JAR_NAME+" [-c|-h|--help]\n" +
					"\t-c: will delete all stored informations\n" +
					"\t-h, --help: display the help page\n");
	}
}
