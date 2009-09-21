package dblp.social.importer;

/**
 * This class provides static methods used to generate a valid identifier for the database entries.
 * 
 * @author Staffiero
 *
 */
public class DblpIdUtility {

	/**
	 * Checks if a given url can be used as identifier for an InProceedings or Article entry, and returns a valid id.
	 * The generated id can be the given url itself or a new id created using the dblp key attribute and the year (or volume) tag. 
	 * 
	 * @param url the url to check.
	 * @param currentKey the dblp key attribute.
	 * @param suffix the content of the year tag (only the last two digits), for InProceedings entries, or the volume tag, for article entries.
	 * @param clazz the class of the current object (InProceedings or Article)
	 * @return a valid identifier.
	 */
	public static String createInProceedingsId(String url, String currentKey, String suffix, String clazz){
		if (url == null && (currentKey==null || clazz==null))
			return null;
		
		String id=url;
		String dblpEnd = (currentKey.substring(currentKey.lastIndexOf("/")+1));
		if (url==null){
			//if we do not have the url but we have the right suffix 
			//we can create our own id
			if ( (clazz.compareToIgnoreCase("InProceedings")==0 && currentKey.startsWith("conf/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("journals/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("tr/"))){
				//the id is created as follows: 
				//"db/"+type+"/"+shortName+"/"+shortName+suffix+"#"+endOfdblpKey
				//where:
				//type = conf or journals. Found in the dblpKey
				//shortName = the element short name. Found in the dblpKey
				//suffix = year (2 digits) for inproceedings, volume for articles
				String shortName=currentKey.substring(currentKey.indexOf("/")+1);
				shortName = shortName.substring(0,shortName.indexOf("/"));
				String type = currentKey;
				type = type.substring(0,type.indexOf("/"));
				id ="db/"+type+"/"+shortName+"/"+shortName+
				suffix+".html#"+dblpEnd;
			}else
				id =currentKey;
		}else if (url.compareTo("")==0){
			//if we do not have the url but we have the right suffix and the right key
			//we can create our own id
			if ( (clazz.compareToIgnoreCase("InProceedings")==0 && currentKey.startsWith("conf/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("journals/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("tr/"))){
				//the id is created as follows: 
				//"db/"+type+"/"+shortName+"/"+shortName+suffix+"#"+endOfdblpKey
				//where:
				//type = conf or journals. Found in the dblpKey
				//shortName = the element short name. Found in the dblpKey
				//suffix = year (2 digits) for inproceedings, volume for articles
				String shortName=currentKey.substring(currentKey.indexOf("/")+1);
				shortName = shortName.substring(0,shortName.indexOf("/"));
				String type = currentKey;
				type = type.substring(0,type.indexOf("/"));
				id ="db/"+type+"/"+shortName+"/"+shortName+
				suffix+".html#"+dblpEnd;
			} //else: we cannot create a valid id, we have to use the dblpKey 
			else
				id = currentKey;
		}
		//evaluate url:
		//if the url contains ".html#" it is a good candidate to be the id for
		//an InProceedings|Article
		else if (url.contains(".html#")){
			//WORKAROUND caused by entries conf/dl/TakedaN98, conf/dl/TsaiM98
			if (currentKey!=null){
				if (currentKey.compareTo("")!=0){
					String urlEnd = url.substring(url.lastIndexOf("#")+1);
					if (urlEnd.compareTo(dblpEnd)!=0){
						id = url.substring(0, url.lastIndexOf("#")+1)+
						dblpEnd;
					}
				}
			}
		}
		//if the url contains ("db/conf/" or "db/journals/") and ".html" and (not contains "#")
		//we need to append an identifier for the InProceedings|Article
		else if((url.startsWith("db/conf/") || url.startsWith("db/journals/")) &&
				url.contains(".html") && !url.contains("#")){
			id = url+"#"+dblpEnd;
		}
		//else the url is not a good candidate to be the identifier for the InProceedings|Article
		//we need to build our own id
		else{
			//if we do not have a valid url but we have the right suffix and the right key
			//we can create our own id
			if ( (clazz.compareToIgnoreCase("InProceedings")==0 && currentKey.startsWith("conf/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("journals/")) ||
					(clazz.compareToIgnoreCase("Article")==0 && currentKey.startsWith("tr/"))){
			
				//the id is created as follows: 
				//"db/"+type+"/"+shortName+"/"+shortName+suffix+"#"+endOfdblpKey
				//where:
				//type = conf or journals. Found in the dblpKey
				//shortName = the element short name. Found in the dblpKey
				//suffix = year (2 digits) for inproceedings, volume for articles
				String shortName=currentKey.substring(currentKey.indexOf("/")+1);
				shortName = shortName.substring(0,shortName.indexOf("/"));
				String type = currentKey;
				type = type.substring(0,type.indexOf("/"));
				id ="db/"+type+"/"+shortName+"/"+shortName+
				suffix+".html#"+dblpEnd;
			}//else: we cannot create a valid id, we have to use the dblpKey 
			else 
				id = currentKey;
		}
		return id;
	}
	
	/**
	 * Checks if a given url can be used as identifier for a Proceedings and returns a valid id.
	 * The generated id can be the given url itself or a new id created using the dblp key attribute.
	 * 
	 * 
	 * @param url the url to check.
	 * @param currentKey the dblp key attribute.
	 * @return a valid identifier
	 */
	public static String createProceedingsId(String url, String currentKey){
		String id=url;
		if (currentKey==null)
			return null;
		if (url==null)
			return "db/"+currentKey+".html";
		//if the url contains ("db/conf/" or "db/journals/") and ".html"
		//it is used as the id for the proceedings, if not we need to create 
		//our own id
		if( !((url.startsWith("db/conf/") || url.startsWith("db/journals/")) &&
				url.contains(".html")) ){
			//the id is created as follows: 
			//"db/"+dblpKey+".html"
			id ="db/"+currentKey+".html";
		}
		
		return id;
	}
	
//	/**
//	 * Creates a vaild id for an InProceedings or Article
//	 * 
//	 * @param suffix the year
//	 * @return the id 
//	 */
//	public String generateInProceedingsId(String suffix, String currentKey){
//		String id="";
//		String dblpEnd = (currentKey.substring(currentKey.lastIndexOf("/")+1));
//		//the id is created as follows: 
//		//"db/"+type+"/"+shortName+"/"+shortName+suffix+"#"+endOfdblpKey
//		//where:
//		//type = conf or journals. Found in the dblpKey
//		//shortName = the element short name. Found in the dblpKey
//		//suffix = year (2 digits) for inproceedings, volume for articles
//		String shortName=currentKey.substring(currentKey.indexOf("/")+1);
//		shortName = shortName.substring(0,shortName.indexOf("/"));		
//		String type = currentKey;
//		type = type.substring(0,type.indexOf("/"));
//		id ="db/"+type+"/"+shortName+"/"+shortName+
//		suffix+".html#"+dblpEnd;
//		return id;
//	}
//	/**
//	 * Creates a vaild id for a Proceedings
//	 * 
//	 * @return the id 
//	 */
//	public String generateProceedingsId(String currentKey){
//		String id="";
//		//the id is created as follows: 
//		//"db/"+dblpKey+".html"
//		id ="db/"+currentKey+".html";
//		return id;
//	}
	
	
}
