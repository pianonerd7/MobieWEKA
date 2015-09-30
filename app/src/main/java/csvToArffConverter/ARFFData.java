package csvToArffConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ARFFData {

	private String[] attributes;
	private List<String[]> data;
	
	/**
	 * make a new arff data from the parsed csv values
	 * @param csv
	 */
	public ARFFData(List<String[]> csv){
		this.attributes = csv.get(0);
		data = new ArrayList<String[]>(csv.size());
		for (int i = 1; i<csv.size(); i++){
			data.add(csv.get(i));
		}
	}
	
	/**
	 * make a new arff data from existing attributes and rows
	 */
	public ARFFData (String[] attributes, List<String[]> data){
		this.attributes = attributes;
		this.data = data;
	}
	
	//stuff about splitting two labels into train and test data
	/**
	 * 1. update attribute string[] 
	 * 		1a. resizing the array
	 * 		1b. adding nominal thingy for label
	 * 2. update the string[] inside data list (2a. resizing the array)
	 * 3. add values inside the new column (label A or label T)
	 * @param attribute
	 */
	public void insertAttributeToEnd (String attribute, String label){
		this.attributes = addColumnToEnd(attributes, attribute);
		for (int i = 0; i<data.size(); i++){
			data.set(i, addColumnToEnd(data.get(i), label));
		}
	}
	
	/**
	 * 35/15
	 * 1. attribute stays the same
	 * 2. this arffData gets 35 rows of data (which is the train data)
	 * 3. return an arffData that has rest of the data (which is the test data)
	 * @param numCounter
	 */
	public ARFFData splitData (int numCounter){
		List<String[]> newData = new ArrayList<String[]>();
		List<String[]> splitData = new ArrayList<String[]>();
		if (data.size()<numCounter){
			System.out.println("not enough data");
			return null;
		}
		int index = 0;
		int count = 0;
		while (count<numCounter){
			newData.add(data.get(index));
			index++;
			count = Integer.parseInt(data.get(index)[data.get(index).length-2]); //this is where counter is at
		}
		
		for (int i =index; i<data.size(); i++){
			splitData.add(data.get(i));
		}
		this.data = newData;
		return new ARFFData(attributes, splitData);
	}
	
	public void combineData (ARFFData arffToCombine){
		for (String[] row : arffToCombine.getData()){
			data.add(row);
		}
	}
	
	public void deleteAttribute (String attribute){
		int attributeIndex = searchColumn(attribute);
		if (attributeIndex == -1){
			//attribute not found
			return;
		}
		this.attributes = removeColumn(attributes, attributeIndex);
		for (int i = 0; i<data.size(); i++){
			data.set(i, removeColumn(data.get(i), attributeIndex));
		}
	}
	
	//deprecated
	String formatRatchetNominal() {
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
	
	String formatNominal(String attributeName) {
		int columnIndex = searchColumn(attributeName);
		Set<String> set = new TreeSet<String>();
		for (int i = 0; i<data.size(); i++){
			String[] dataArr = data.get(i);
			set.add(dataArr[columnIndex]);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		String[] nominalSet = set.toArray(new String[1]);
		for (int i = 0; i<nominalSet.length; i++){
			sb.append(nominalSet[i]);
			if (i+1<nominalSet.length){
				sb.append(",");
			}
		}
		sb.append("}");
		
		return sb.toString();
	}
	
	//formatLabel
	
	private int searchColumn (String attribute){
		for (int i = 0; i<attributes.length; i++){
			if (attribute.equals(attributes[i])){
				return i;
			}
		}
		return -1;
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
	
	private String[] addColumnToEnd (String[] row, String data){
		String[] newRow = new String[row.length+1];
		for (int i = 0; i<row.length; i++){
			newRow[i] = row[i];
		}
		newRow[newRow.length-1] = data;
		return newRow;
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
