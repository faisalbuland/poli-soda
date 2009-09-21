package presentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfLoader {
	private static final String DB_CONFIG_FILE = "./config/db.properties";
	
	/**
	 * Loads the db connection properties.
	 * @throws IOException
	 */
	public static Properties loadDbConfigurations() throws IOException{
		//Load db properties
		Properties props = null;
		InputStream propertiesInputStream;
		props = new Properties();
		propertiesInputStream = new FileInputStream(new File(DB_CONFIG_FILE));
		if (propertiesInputStream != null) {
			props.load(propertiesInputStream);
		}
		propertiesInputStream.close();
		return props;
	}
	
}
