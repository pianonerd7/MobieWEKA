package csvToArffConverter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ConvertCSVToARFF {

	public static List<ARFFData> convertWithStat(File fileA, File fileB){
		ARFFData arffA = null;
		ARFFData arffB = null;
		try {
			arffA = new ARFFData(CSVParser.parseCSV(fileA));
			arffB = new ARFFData(CSVParser.parseCSV(fileB));
		}
		catch(FileNotFoundException e){
			System.out.println("the csv file to read does not exist");
			System.exit(1);
		}
		return convertWithStat(arffA, arffB);
	}
	
	public static List<ARFFData> convertWithStat (ARFFData arffA, ARFFData arffB){
		/**
		 * 27 attributes normally.
		 * delete timestamp and  current pattern -2
		 * =25
		 * don't calculate mean/stdev for x, y, and counter
		 * 22*2=44 for mean/stdev for everything else
		 */
		final int NEW_ATTRIBUTE_COUNT = 47;//22*2 + 2 + 1;
		attributePreprocessing(arffA, arffB);
		
		String[] newAttribute = new String[NEW_ATTRIBUTE_COUNT];
		int newAttributeIndex = 0;
		for (int i = 0; i<arffA.getAttributes().length; i++){
			if (shouldCalculateStats(arffA.getAttributes()[i])){
				newAttribute[newAttributeIndex] = arffA.getAttributes()[i] + "_MEAN";
				newAttributeIndex++;
				newAttribute[newAttributeIndex] = arffA.getAttributes()[i] + "_STANDARD_DEVIATION";
				newAttributeIndex++;
			}
			else{
				//for x,y, and counter
				newAttribute[newAttributeIndex] = arffA.getAttributes()[i];
				newAttributeIndex++;
			}
		}
		List<String[]> arffAData = new ArrayList<String[]>();
		List<String[]> arffBData = new ArrayList<String[]>();
		
		/**
		 * 0. initialize arffAData with column and row sizes that makes sense
		 * 1. for each attribute,
		 * 		a. for each counter, put all the data in a list
		 * 		b. pass that list to StatLibrary to calculate mean/stdev
		 * 		c. populate the proper rows with that mean/stdev 
		 */
		initializeData(arffAData, NEW_ATTRIBUTE_COUNT, arffA.getData().size());
		initializeData(arffBData, NEW_ATTRIBUTE_COUNT, arffB.getData().size());
		populateWithStat(arffAData, arffA);
		populateWithStat(arffBData, arffB);
		ARFFData newArffA = new ARFFData(newAttribute, arffAData);
		ARFFData newArffB = new ARFFData(newAttribute, arffBData);
		
		
		return convert(newArffA, newArffB);
	}
	
	private static void populateWithStat (List<String[]> newARFFData, ARFFData arff){
		String[] originalAttribuite = arff.getAttributes();
		List<String[]> arffData = arff.getData();
		int newColumnIndex = 0;
		for (int column = 0; column<originalAttribuite.length; column++){
			if (!shouldCalculateStats(originalAttribuite[column])){
				for (int row = 0; row<arffData.size(); row++){
					newARFFData.get(row)[newColumnIndex] = arffData.get(row)[column];
				}
				newColumnIndex++;
				continue;
			}
			final int counterIndex = arffData.get(0).length-1;
			List<String> sample = new ArrayList<String>();
			int counter = 0;
			int rowStart = 0;
			int rowEnd = 0;
			for (int row = 0; row<arffData.size(); row++){
				rowEnd = row;
				if (counter!= Integer.parseInt(arffData.get(row)[counterIndex])){//depends on counter being sequential
					double mean = StatLibrary.calculateMean(sample);
					double stdev = StatLibrary.calculateStandardDeviation(sample);
					recordStat(newARFFData, mean, stdev, rowStart, rowEnd, newColumnIndex);
					rowStart = rowEnd;
					sample = new ArrayList<String>();
					counter++;
				}
				sample.add(arffData.get(row)[column]);//adding each element in the counter
			}
			recordStat(newARFFData, StatLibrary.calculateMean(sample), StatLibrary.calculateStandardDeviation(sample), rowStart, rowEnd+1, newColumnIndex);
			newColumnIndex+=2;
		}
	}
	
	private static void recordStat (List<String[]> newARFFData, double mean, double stdev, int rowStart, int rowEnd, int column){
		for (int row = rowStart; row<rowEnd; row++){
			newARFFData.get(row)[column] = ""+mean;
			newARFFData.get(row)[column+1] = ""+stdev;
		}
	}
	
	private static void initializeData (List<String[]> data, int columns, int rows){
		for (int i = 0; i<rows; i++){
			data.add(new String[columns]);
		}
	}
	
	private static boolean shouldCalculateStats(String column){
		return !(column.equals("position_X") || column.equals("position_Y") || column.equals("Counter") || column.equals("Label") || column.equals("TimeStamp") || column.equals("mCurrentPattern"));
	}
	
	public static List<ARFFData> convert(File fileA, File fileB){
		ARFFData arffA = null;
		ARFFData arffB = null;
		try {
			arffA = new ARFFData(CSVParser.parseCSV(fileA));
			arffB = new ARFFData(CSVParser.parseCSV(fileB));
		}
		catch(FileNotFoundException e){
			System.out.println("the csv file to read does not exist");
			System.exit(1);
		}
		return convert(arffA, arffB);
	}
	
	public static List<ARFFData> convert(ARFFData arffA, ARFFData arffB){
		List<ARFFData> trainTestPair = new ArrayList<ARFFData>();
		final int ROWS_FOR_TRAIN = 35;

		attributePreprocessing(arffA, arffB);
		arffA.insertAttributeToEnd("Label", "A");
		arffB.insertAttributeToEnd("Label", "B");

		ARFFData arffATest = arffA.splitData(ROWS_FOR_TRAIN);
		ARFFData arffBTest = arffB.splitData(ROWS_FOR_TRAIN);
		
		//combine arffA = arffA + arffB
		arffA.combineData(arffB);
		arffATest.combineData(arffBTest);
		
		arffA.deleteAttribute("Counter");
		arffATest.deleteAttribute("Counter");
		trainTestPair.add(arffA);
		trainTestPair.add(arffATest);
		
		return trainTestPair;
	}

	private static void attributePreprocessing(ARFFData arffA, ARFFData arffB) {
		arffA.deleteAttribute("TimeStamp");
		arffA.deleteAttribute("mCurrentPattern");
		
		arffB.deleteAttribute("TimeStamp");
		arffB.deleteAttribute("mCurrentPattern");
	}
	
	public static void main(String[] args) {
		//MergedData_1_1_Anna.csv MergedData_1_1_Ted.csv
//		List<ARFFData> list = convert(new File(args[0]), new File(args[1]));
		List<ARFFData> list = convertWithStat(new File(args[0]), new File(args[1]));
		ARFFWriter.convertToARFF(new File("train.arff"), list.get(0));
		ARFFWriter.convertToARFF(new File("test.arff"), list.get(1));
		
		System.out.println("done");
	}
}