package dblp.social.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class defines the methods used to parse and load in an in memory structure the XML _partsInfo file.
 * The _partsInfo file is the result of a previous pre-parse.
 * 
 * @author Staffiero
 *
 */
public class DescriptorLoader {
	private File xmlDescriptor=null;
	private ArrayList<PartFile> partFiles=null;
	private XMLReader xmlReader= null;
	
	
	public DescriptorLoader(File xmlDescriptor) {
		this.xmlDescriptor = xmlDescriptor;
		partFiles = new ArrayList<PartFile>();
	}
	
	/**
	 * Parses the XML _partsInfo file and stores the informations in
	 * the partFiles array.
	 * 
	 * @return result of the parse activity: true if the _partsInfo file has been correctly loaded, false otherwise
	 * @throws SAXException
	 * @throws IOException
	 */
	public boolean parseXmlDescriptor() throws SAXException, IOException{
		boolean result=true;
		if (this.xmlDescriptor==null){
			return false;
		}
		this.xmlReader = XMLReaderFactory.createXMLReader();
		DefaultHandler handl = new DescriptorHandler(partFiles);
		this.xmlReader.setContentHandler(handl);
		FileInputStream xmlData = new FileInputStream(xmlDescriptor);
		this.xmlReader.parse(new InputSource(xmlData));
		xmlData.close();
		for (PartFile p : partFiles){
			p.setPartFileDescriptor(this.xmlDescriptor.getAbsolutePath());
			p.setUpdated(true);
		}
		return result;
	}
	
	/**
	 * Getter, returns the partFiles array
	 * 
	 * @return
	 */
	public ArrayList<PartFile> getPartFiles(){
		return this.partFiles;
	}

}
