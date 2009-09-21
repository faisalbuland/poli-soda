package dblp.social.utility;

import java.text.DecimalFormat;

/**
 * This class provides static methods to print more readable output
 * 
 * @author Staffiero
 *
 */
public class Glitterizer {
	/**
	 * Converts a given size (a long number which represents the bytes number) in a string.
	 * The size is given in GB, MB, kB or Bytes, according to the actual size.
	 * 
	 * @param length: the size to be converted
	 * @return the converted length
	 */
	public static String clearFormatLenght (float lenght){
        DecimalFormat df1 = new DecimalFormat("####.00");

		String lt;
		if (lenght/(1024*1024*1024)>1.0){
			lt =df1.format(lenght/(1024*1024*1024))+" GB";
			return lt;
		}
		if (lenght/(1024*1024)>1.0){
			lt =df1.format(lenght/(1024*1024))+" MB";
			return lt;
		}
		if (lenght/(1024)>1.0){
			lt =df1.format(lenght/(1024))+" kB";
			return lt;
		}
		lt = lenght + " Byte";
		return lt;
	}
}
