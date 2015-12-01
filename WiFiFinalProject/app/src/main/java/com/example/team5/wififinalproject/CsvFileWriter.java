package com.example.team5.wififinalproject;


/*Function that accepts array list of strings(MAC Address) and ENUM

design an enum
glennan 1st-8th floors
White
Olin

Write MAC address and enum in one line to a CSV file
*/

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@SuppressWarnings("unchecked")
public class CsvFileWriter {

	//Delimiters for CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	//CSV file header
	private static final String FILE_HEADER = "enum Location, item1, item2, item3, ... ,item50";

	//CSV file name
	private static final String FILE_NAME = "testdoc";

	private Location location;

	//public static void writeCsvFile(enum, Arraylist list){
	public static void writeCsvFile(Location location, ArrayList addresses) {
		//Will take in a list with 50 values


		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(FILE_NAME);

			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());

			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR.toString());

			fileWriter.append(location.toString());

			//Write a new student object list to the CSV file
			for (int i = 0; i < 50; i++) {
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(addresses.get(i).toString());
			}
			fileWriter.append(NEW_LINE_SEPARATOR);
			System.out.println("CSV file was created successfully !!!");

		} catch (Exception e) {
			System.out.println("Error in CSV file Writer");
			e.printStackTrace();

		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/clsoing fileWriter");
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		ArrayList addresses = new ArrayList();
		for (int i = 0; i < 50; i++) {
			addresses.add("address " + i);
		}
		writeCsvFile(Location.GLENNAN5, addresses);
	}


	public enum Location {
		GLENNAN1 {
			@Override
			public String toString() {
				return "Glennan1";
			}
		},
		GLENNAN2 {
			@Override
			public String toString() {
				return "Glennan2";
			}
		},
		GLENNAN3 {
			@Override
			public String toString() {
				return "Glennan3";
			}
		},
		GLENNAN4 {
			@Override
			public String toString() {
				return "Glennan4";
			}
		},
		GLENNAN5 {
			@Override
			public String toString() {
				return "Glennan5";
			}
		},
		GLENNAN6 {
			@Override
			public String toString() {
				return "Glennan6";
			}
		},
		GLENNAN7 {
			@Override
			public String toString() {
				return "Glennan7";
			}
		},
		GLENNAN8 {
			@Override
			public String toString() {
				return "Glennan8";
			}
		}
	}

}