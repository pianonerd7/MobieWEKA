package csvToArffConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ARFFWriter {


	public static void convertToARFF(File fileToWrite, ARFFData arff){
		BufferedWriter bw = null;
		
		String attributeTag = "@attribute";
		
		try {
			bw=new BufferedWriter(new FileWriter(fileToWrite));
			bw.append("@relation sensorAndMotion");
			bw.newLine();
			bw.newLine();
			
			for (int i = 0; i < arff.getAttributes().length-1; i++) {
				bw.append(String.format("%s %s numeric", attributeTag, arff.getAttributes()[i]));
				bw.newLine();
			}
			
			String nominal = arff.formatNominal("Label");
			bw.append(String.format("%s %s %s", attributeTag, arff.getAttributes()[arff.getAttributes().length-1], nominal));
			
			bw.newLine();
			bw.newLine();
			
			bw.append("@data");
			bw.newLine();
			
			for (String[] dataRow : arff.getData()) {
				for (int i = 0; i < dataRow.length-1; i++) {
					bw.append(dataRow[i]);
					bw.append(",");
				}
				bw.append(dataRow[dataRow.length-1]);
				bw.newLine();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				bw.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
