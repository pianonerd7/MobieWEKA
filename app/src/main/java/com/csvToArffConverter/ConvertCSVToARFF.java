package csvToArffConverter;

import java.io.File;

public class ConvertCSVToARFF {

	public static void main(String[] args) {
		
		ARFFData arff = new ARFFData(CSVParser.parseCSV(new File(args[0])));
		
		arff.deleteAttribute("TimeStamp");
		
		arff.getARFF(new File(args[1]));
		System.out.println("done");
	}
}
