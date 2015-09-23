package csvToArffConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ARFFData {

	private String[] attributes;
	private List<String[]> data;
	
	public ARFFData(List<String[]> csv){
		this.attributes = csv.get(0);
		data = new ArrayList<String[]>(csv.size());
		for (int i = 1; i<csv.size(); i++){
			data.add(csv.get(i));
		}
	}
	
	public void deleteAttribute (String attribute){
		int attributeIndex = -1;
		for (int i = 0; i<attributes.length; i++){
			if (attribute.equals(attributes[i])){
				attributeIndex = i;
				break;
			}
		}
		if (attributeIndex == -1){
			//attribute not found
			return;
		}
		this.attributes = removeColumn(attributes, attributeIndex);
		for (int i = 0; i<data.size(); i++){
			data.set(i, removeColumn(data.get(i), attributeIndex));
		}
	}
	
	public void getARFF(File file){
		BufferedWriter bw = null;
		
		String attributeTag = "@attribute";
		
		try {
			bw=new BufferedWriter(new FileWriter(file));
			bw.append("@relation sensorAndMotion");
			bw.newLine();
			bw.newLine();
			
			for (int i = 0; i < attributes.length-1; i++) {
				bw.append(String.format("%s %s numeric", attributeTag, attributes[i]));
				bw.newLine();
			}
			
			String counterNominal = formatRatchetNominal();
			bw.append(String.format("%s %s %s", attributeTag, attributes[attributes.length-1], counterNominal));
			
			bw.newLine();
			bw.newLine();
			
			bw.append("@data");
			bw.newLine();
			
			for (String[] dataRow : data) {
				for (int i = 0; i < dataRow.length-1; i++) {
					bw.append(dataRow[i]);
					bw.append(",");
				}
				bw.append(dataRow[dataRow.length-1]);
				bw.newLine();
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			try {
				bw.close();
			}
			catch(IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private String formatRatchetNominal() {
		String[] lastRow  = data.get(data.size()-1);
		int counter = Integer.parseInt(lastRow[lastRow.length-1]);
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		for (int i = 0; i <= counter; i++){
			sb.append(i);
			if (i+1<=counter){
				sb.append(",");
			}
		}
		sb.append("}");
		 
		return sb.toString();
	}
	
	/**
	 * @return the attributes
	 */
	public String[] getAttributes() {
		return attributes;
	}

	/**
	 * @return the data
	 */
	public List<String[]> getData() {
		return data;
	}

	private String[] removeColumn (String[] row, int column){
		String[] newRow = new String[row.length-1];
		int j = 0;
		for (int i = 0; i<row.length; i++){
			if (i!=column){
				newRow[j] = row[i];
				j++;
			}
		}
		return newRow;
	}
}
