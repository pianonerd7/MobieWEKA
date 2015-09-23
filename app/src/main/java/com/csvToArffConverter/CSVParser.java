package csvToArffConverter;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVParser {

	public static List<String[]> parseCSV (File file){
		final String csvSplitBy = ",";
		String row= "";
		BufferedReader br = null;
		List<String[]> rows = new ArrayList<String[]>();
		try {
			br = new BufferedReader(new FileReader(file));
			while ((row=br.readLine()) != null){
				rows.add(row.split(csvSplitBy));
			}
		}
		catch(IOException io){
			//log here
		}
		finally{
			if (br!= null){
				try {
					br.close();
				}
				catch(IOException e){
					//log here
				}
			}
		}
		return rows;
	}
}
