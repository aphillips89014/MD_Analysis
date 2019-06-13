//Readin Java File
//This class relates to all operations related towards reading/creating files.


import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.lang.Math;

public class Readin implements Serializable{

	//Save an object to the Disk so that it can be accessed at a later dat.
	public static void serializeFrame(String fileName, int frameNumber, Frame Frame){

		//Setup a method of assigning fileNames based off an integer instead of a pre-processed fileName
		if (frameNumber < 8888){
			//Choose an arbitrarily large value such as 2000.
			String frameNumberString = Integer.toString(frameNumber);
			fileName = "Frames/frame_" + frameNumberString + ".ser";
		}	//Ends if statement

		try{
			FileOutputStream fileOutput = new FileOutputStream(fileName);
			ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);

			objectOutput.writeObject(Frame);

			fileOutput.close();
			objectOutput.close();

		}       //Ends try statement

		catch (IOException e){
			System.out.println("Frame could not be Serialized");
		}       //Ends Catch statement
	}	//Ends Serialize Frame method



	//Unserialize a Serialized Object, return said object
	public static Frame unserializeFrame(int x){
		String frameNumber = Integer.toString(x);
		Frame newFrame = null;
		
		//There is a specific file, so we get that specific file
		String fileName = "Frames/frame_" + frameNumber + ".ser";

		try{
			FileInputStream file = new FileInputStream(fileName);
			ObjectInputStream objectInput = new ObjectInputStream(file);

			newFrame = (Frame)objectInput.readObject();
			
			objectInput.close();
			file.close();
		}	//Ends try Statement

		catch(ClassNotFoundException e){
			System.out.println("Frame Class not Found.");
		}	//Ends catch statement


		catch(IOException e){
			System.out.println("Issue occured accessing " + fileName);
			System.out.println(e);
			System.out.println("");
		}	//Ends catch statement

		return newFrame;
	}	//Ends unserializeFrame

	public static boolean checkForFiles(String fileName){
		//Checks for a specific file, if it exists return true, otherwise return false.
		boolean result = false;

		File file = new File(fileName);
		if(file.exists() && !file.isDirectory()) {
			result = true;
		}       //Ends if statement

		return result;
	}       //Ends checkForFiles method

	//This method will determine if we are looking at an Atomistic, or a Coarse-Grained Simulation.
		//There should never be an occurance of a Hydrogen in a CG simulation.
		//There is a potential that a simulation of Cholesterol only could break this.
	public static boolean determineSimulationMethod(){
		Frame currentFrame = unserializeFrame(0);
		//Choose the first frame available.

		int totalLipids = currentFrame.allLipids.length;
		Atom probingAtom;		
		int totalHydrogen = 0;

		boolean result = true;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			probingAtom = currentFrame.allLipids[currentLipid].firstChain;
			
			while (probingAtom != null) {
				if (probingAtom.nextHydrogen != null){
					//If this happens then the atom is Atomistic, so the result should be false, since it corresponds to if it is CG or not.
						//So cancel both loops we are in, we don't need to go any futher
					result = false;
					probingAtom = null;
					currentLipid = totalLipids;
				}	//Ends if statement
			
				else{
					probingAtom = probingAtom.next;
				}	//Ends else statement
			}	//ends while Loop
		}	//Ends for loop
		
		return result;
	}	//Ends determineSimulationMethod


	//Search forward in a specific file to find out how many total Molecules in a data file there is.
		//This method is unique to a specific File Format
	public static int findMaximumID(Scanner Scan, int targetFrame) throws FileNotFoundException {
		boolean keepGoing = true;

		int totalLipids = 0;
		int currentFrame = 0;
		String Chain;
		String Element;

		//In Coarse-Grained Simulations there is a variant amount of lipids due to the inconsistent flip-flop of Cholesterol.
			//So therefore we must do some gymnastics to make sure we can figure this out exactly every time.

		while (keepGoing){

			try{
				currentFrame = Scan.nextInt();
			}	//Ends try Statement

			catch (Exception e){
				if (Scan.hasNextLine()){
					Scan.nextLine();
				}	//Ends if statement	

				totalLipids++;

			}	//ends catch statement


			if (currentFrame > targetFrame){
				keepGoing = false;
			}	//Ends if statement

			else if (currentFrame == targetFrame){
				Scan.next();
				Scan.next();
				Chain = Scan.next();
				Element = Scan.next();
				Scan.nextLine();
				
				if ((Chain.equals("null")) && (Element.equals("null"))){
					totalLipids++;
				}	//Ends if statement
			}	//Ends else statement

			else{
				Scan.nextLine();

			}	//Ends else statement

			if (!(Scan.hasNextLine())){
				keepGoing = false;
			}	//Ends if statement
		}	//Ends while loop

		return totalLipids;
	}	//Ends findMaximumId

	//Parse forward through the file we are interested in
		//Find each lipid in the systme by searching through the first frame.
	public static String[] findLipidNames(String fileName){
		boolean keepGoing = true;

		String[] lipidNames = {"null"};
		try{

			File file = new File(fileName);
			Scanner Scan = new Scanner(file);
			Scan.useDelimiter(" ");

			int currentFrame = 0;
			String lipidName = "";

			//Because we don't want to introduce vectors we will be forced to use a somewhat inefficent method
			//Errors will occur if you have more than 10 lipid types
			lipidNames = new String[10];

			while (keepGoing){
				currentFrame = Scan.nextInt();
				
				if (currentFrame != 0) {
					keepGoing = false;
				}	//Ends if statement

				else{
					lipidName = Scan.next();
					lipidNames = addName(lipidName, lipidNames);

					Scan.nextLine();
				}	//Ends else statement
			}	//ends while loop

			lipidNames = assignLipidNames(lipidNames);

		}	//Ends Try Statement

		catch(IOException e) {
			System.out.println("Error in Accessing Coordinates.dat");
		}	//Ends catch statement

		return lipidNames;
	}	//Ends findLipidNames


	//Add a name to a string array.
	public static String[] addName(String name, String[] array){

		int length = array.length;
		boolean keepGoing = true;
		int i = 0;

		while(keepGoing){
			if (i == length){
				keepGoing = false;
				System.out.println("Too Many Lipids, Adjust the default lipidNames in Readin.java");
			}	//Ends if statement

			else if (array[i] == null){
				array[i] = name;
				keepGoing = false;
			}	//Ends if statement

			else if (array[i].equals(name)){
				//Name already exists in the array, no need to add it.
				keepGoing = false;				
			}	//Ends if statement
	
			else{
				i++;
			}	//Ends else statement
		}	//Ends while loop


		return array;
	}	//Ends addName method

	//Take a larger array and condense it to a smaller size.	
	public static String[] assignLipidNames(String[] array){
		int length = array.length;
		int realLength = 0;

		for (int i = 0; i < length; i++){
			if (!(array[i] == null)){
				//If the array is NOT null.
				realLength++;
			}
		}	//ends for loop

		String[] lipidNames = new String[realLength];

		for (int i = 0; i < realLength; i++){
			lipidNames[i] = array[i];

		}	//Ends for loop

		return lipidNames;
	}	//Ends if statement


	public static void createOPFiles(double[][][][][] OP, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
	
		int totalLipids = lipidNames.length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			for (int currentChain = 0; currentChain < 2; currentChain++){
				//Check to see if there is valid data.
				double sum = 0;
				int length = OP[0][currentLipid][currentChain][0].length;
				for (int i = 0; i < length; i++){
					sum = sum + OP[0][currentLipid][currentChain][0][i];

				}	//ends for loop

				if (sum > 0){
					String fileName = "Graphing/Data/" + lipidNames[currentLipid] + "_chain_" + currentChain + "_OP.dat";

					try{
						PrintStream output = new PrintStream(new File(fileName));
						System.setOut(output);

						for (int i = 0; i < length; i++){
							double count = OP[0][currentLipid][currentChain][0][i];

							if (count > 0) {
								double currentOP = OP[1][currentLipid][currentChain][0][i] / totalFiles;
								double squaredOP = OP[2][currentLipid][currentChain][0][i] / totalFiles;

								double deviation = Mathematics.calculateDeviation(currentOP, squaredOP);
								
								System.out.println(i + " " + currentOP + " " + deviation);

							}	//Ends if statement
						}	//Ends for loop

						fileName = "Graphing/Data/" + lipidNames[currentLipid] + "_chain_" + currentChain + "_OP_H.dat";
						PrintStream output2 = new PrintStream(new File(fileName));
						System.setOut(output2);

						for (int currentHydrogen = 1; currentHydrogen < 4; currentHydrogen++){
							for (int i = 0; i < length; i++){
								double count = OP[0][currentLipid][currentChain][currentHydrogen][i];

								if (count > 0) {
									double currentOP = OP[1][currentLipid][currentChain][currentHydrogen][i] / totalFiles;
									double squaredOP = OP[2][currentLipid][currentChain][currentHydrogen][i] / totalFiles;

									double deviation = Mathematics.calculateDeviation(currentOP, squaredOP);
									
									System.out.println(i + " " + currentOP + " " + deviation);

								}	//Ends if statement
							}	//Ends for loop
						}	//Ends for loop

					}	//Ends try statement

					catch (IOException e){
						System.setOut(console);
						System.out.println("Error in Creating " + fileName);
						System.out.println(e);
					}	//Ends catch
				}	//Ends if statement
			}	//Ends for loop
		}	//Ends for loop

		System.setOut(console);

	}	//Ends createOPFiles method

	public static void createThicknessFiles(int[][] Thickness, String[] lipidNames){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
		int length = Thickness[0].length;
		int sum = 0;

		for (int i = 0; i < totalLipids; i++){
			for (int j = 0; j < length; j++){
				sum = sum + Thickness[i][j];
			}	//Ends for loop

			//Now we ignore lipids without phosphates.
			if (sum > 1){
				String fileName = "Graphing/Data/" + lipidNames[i] + "_Thickness.dat";

				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					for (int j = 0; j < length; j++){
						float binSpot = (float) j;
						binSpot = (binSpot/10) - 100;
						int count = Thickness[i][j];
						String firstValue = String.format("%.1f", binSpot);
						System.out.println(firstValue + " " + count);

					}	//Ends for loop
				}	//Ends try statement

				catch (IOException e){
					System.setOut(console);
					System.out.println("Error in Creating " + fileName);
					System.out.println(e);
				}	//Ends catch
			}	//Ends if statement
		}	//Ends for loop
		System.setOut(console);
	}	//Ends createThicknessFile



	public static void createPCLFiles(double[][][][] PCL, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
	 	double sum = 0;
		int length = PCL[0][0][1].length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			sum = 0;
			for (int carbonIndex = 0; carbonIndex < length; carbonIndex++){
				sum = sum + PCL[0][currentLipid][1][carbonIndex];
			}	//Ends for loop

			//Now we ignore lipids without 2 chains
			if (sum > 1){
				for (int chainCount = 0; chainCount < 2; chainCount++) {
					String fileName = "Graphing/Data/" + lipidNames[currentLipid] + "_chain_" + chainCount + "_PCL.dat";

					try{
						PrintStream output = new PrintStream(new File(fileName));

						for (int carbonIndex = 0; carbonIndex < length; carbonIndex++){
							System.setOut(console);
						
							double count = PCL[0][currentLipid][chainCount][carbonIndex];
							if (count > 0) {
								if (carbonIndex != 0) {
									double carbonLength = PCL[1][currentLipid][chainCount][carbonIndex] / totalFiles;
									double squaredLength = PCL[2][currentLipid][chainCount][carbonIndex] / totalFiles;
		
									double deviation = Mathematics.calculateDeviation(carbonLength, squaredLength);
										
									System.setOut(output);
									System.out.println(carbonIndex + " " + carbonLength + " " + deviation);
								}	//Ends if statement
							}	// ends if statement
						}	//Ends for loop
					}	//Ends try statement

					catch (IOException e){
						System.setOut(console);
						System.out.println("Error in Creating " + fileName);
						System.out.println(e);
					}	//Ends catch
				}	//Ends for Loop
			}	//Ends if statement
		}	//Ends for loop
		System.setOut(console);
	}	//ends createPCLFiles Method


	//Going to create an output file after manipulating and binning OPvNN
	public static void createOPvNNFiles_CG(double[][][][] OPvNN, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
		int totalLipids = OPvNN[0].length;

		//Iterate through second index
		for (int lipid = 0; lipid < totalLipids; lipid++){
			String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

			//Iterate through third index
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){
				String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

				//Create a file specifically for this
				String fileName = "Graphing/Data/OP_NN_" + lipidName + "_" + compLipidName + ".dat";

				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					//First Sum the array we want to look at.
					double sum = 0;
					int length = OPvNN[0][lipid][compLipid].length;					

					for (int neighbors = 0; neighbors < length; neighbors++){
						sum = sum + OPvNN[0][lipid][compLipid][neighbors];
					}	//Ends for Loop


					//Then find the proportion that the values in the array given occur.
						//Then find the standard Deviation of this.						
					for (int neighbors = 0; neighbors < length; neighbors++){
						double count = OPvNN[0][lipid][compLipid][neighbors];
						double proportion = count / sum;

						if (proportion <= 0.001){
							proportion = 0;
						}	//ends if statement

						proportion = proportion * 100;
						String stringProportion = String.format("%.2f", proportion);

						double OP = OPvNN[1][lipid][compLipid][neighbors] / totalFiles;
						double OPSquared = OPvNN[2][lipid][compLipid][neighbors] / totalFiles;
						double Deviation = Mathematics.calculateDeviation(OP, OPSquared);

						//Magnitude of OP
						if (OP < 0) { OP = OP * -1; }

						if (OP > 0) {
							if (proportion > 3){
								System.out.println(neighbors + " " + OP + " " + Deviation + " " + stringProportion + "%");
							}	//Ends if statement
						}	//Ends if statement
					}	//Ends for loop
				}	//end try statement

				catch (IOException e){
					System.out.println("Error in creating Histogram Output File");
				}	//Ends catch statement
				System.setOut(console);
			}	//Ends for loop
		}	//Ends for loop
	}	//Ends createOutputFiles Methdo



	//Going to create an output file after manipulating and binning OPvNN
	public static void createOPvNNFiles_AA(double[][][][][] OPvNN, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
		int totalLipids = OPvNN[0].length;

		//Iterate through second index
		for (int lipid = 0; lipid < totalLipids; lipid++){
			String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

			//Iterate through third index
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){
				String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

				//Iterate through fourth index
				for (int chain = 0; chain < 2; chain++){

					//Create a file specifically for this
					String fileName = "Graphing/Data/OP_NN_" + lipidName + "_chain_" + chain + "_" + compLipidName + ".dat";

					try{
						PrintStream output = new PrintStream(new File(fileName));
						System.setOut(output);

						//First Sum the array we want to look at.
						double sum = 0;
						int length = OPvNN[0][lipid][compLipid][chain].length;					

						for (int neighbors = 0; neighbors < length; neighbors++){
							sum = sum + OPvNN[0][lipid][compLipid][chain][neighbors];
						}	//Ends for Loop



						//Then find the proportion that the values in the array given occur.
							//Then find the standard Deviation of this.						
						for (int neighbors = 0; neighbors < length; neighbors++){
							double count = OPvNN[0][lipid][compLipid][chain][neighbors];
							double proportion = count / sum;

							if (proportion <= 0.001){
								proportion = 0;
							}	//ends if statement

							proportion = proportion * 100;
							String stringProportion = String.format("%.2f", proportion);

							double OP = OPvNN[1][lipid][compLipid][chain][neighbors] / totalFiles;
							double OPSquared = OPvNN[2][lipid][compLipid][chain][neighbors] / totalFiles;
							double Deviation = Mathematics.calculateDeviation(OP, OPSquared);
							
							//Magnitude of OP
							if (OP < 0) { OP = OP * -1; }

							if (OP > 0) {
								if (proportion > 1){
									System.out.println(neighbors + " " + OP + " " + Deviation + " " + stringProportion + "%");
								}	//Ends if statement
							}	//Ends if statement
						}	//Ends for loop
					}	//end try statement

					catch (IOException e){
						System.out.println("Error in creating OPvNN Output File");
					}	//Ends catch statement
					System.setOut(console);
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop
	}	//Ends createOutputFiles Methdo

	//Get the AvgOP for the entire system (for each lipid)
	//Get the avg NN for the entire system
	//Show only that
	//Not meant to be graphed.
	public static void createStandardDataFiles_CG(double[][][][] OPvNN, double[][] OP_CG, String[] lipidNames){
		PrintStream console = System.out;
		int totalLipids = lipidNames.length;
		double sum = 0;
		double overallSum = 0;
		String fileName = "Graphing/Data/Standard_Data.dat";

		try{

			PrintStream output = new PrintStream(new File(fileName));
			System.setOut(output);

			//iterate through second index
			for (int lipid = 0; lipid < totalLipids; lipid++){
				String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

				//iterate through third index
				for (int compLipid = 0; compLipid < totalLipids; compLipid++){
					String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

					sum = 0;
					int length = OPvNN[0][lipid][compLipid].length;					
					double avgNN = 0;

					for (int neighbors = 0; neighbors < length; neighbors++){
						sum = sum + OPvNN[0][lipid][compLipid][neighbors];
						overallSum = overallSum + OPvNN[0][lipid][compLipid][neighbors];
					}	//Ends for Loop
					
					//Divide each count by the sum so we can find a proportion.
					for (int neighbors = 0; neighbors < length; neighbors++){
						double count = OPvNN[0][lipid][compLipid][neighbors];
						double proportion = count / sum;
						avgNN = avgNN + (proportion * neighbors);


					}	//Ends for loop

					System.out.println(lipidName + " has " + avgNN + " neighbors of " + compLipidName);

				}	//Ends for loop
			}	//Ends for loop

			//iterate through second index
			for (int lipid = 0; lipid < totalLipids; lipid++){
				String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

				double OP = OP_CG[1][lipid] / OP_CG[0][lipid];
				double OP_Squared = OP_CG[2][lipid] / OP_CG[0][lipid];


				double deviation = Mathematics.calculateDeviation(OP, OP_Squared);
				System.out.println("OP of " + lipidName + " is " + OP + "    +-" + deviation);

			}	//Ends for loop
		}	//Ends try statement

		catch(IOException e){
			System.setOut(console);
			System.out.println("Error in creating Standard Data File");
		}	//Ends catch statement

		System.setOut(console);
	}	//Ends createStandardDataFiles_CG


	//Going to create an output file after manipulating and binning OPvNN
	public static void createNNFiles_CG(double[][][][] OPvNN, String[] lipidNames){
		PrintStream console = System.out;
		int totalLipids = lipidNames.length;

		double[][] barGraphNN = new double[totalLipids][totalLipids];

		//iterate through second index
		for (int lipid = 0; lipid < totalLipids; lipid++){
			String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

			//iterate through third index
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){
				String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

				String fileName = "Graphing/Data/" + lipidName + "_Histogram_" + compLipidName + ".dat";				


				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					//First Sum the array we want to look at.
					double sum = 0;
					int length = OPvNN[0][lipid][compLipid].length;					

					//Create a second File for Bar Graphs, 
					double avgNN = 0;					

					for (int neighbors = 0; neighbors < length; neighbors++){
						sum = sum + OPvNN[0][lipid][compLipid][neighbors];
					}	//Ends for Loop
					
					//Divide each count by the sum so we can find a proportion.
					for (int neighbors = 0; neighbors < length; neighbors++){
						double count = OPvNN[0][lipid][compLipid][neighbors];
						double proportion = count / sum;

						avgNN = avgNN + (proportion * neighbors);

						//Probabilities cant be negative, and we want to not write zero probs.
						if (proportion > 0.0001){
							System.out.println(neighbors + " " + proportion);
						}	//Ends if statement
					}	//Ends for loop

					barGraphNN[lipid][compLipid] = avgNN;

				}	//end try statement

				catch (IOException e){
					System.out.println("Error in creating Histogram Output File");
				}	//Ends catch statement

				System.setOut(console);
			}	//Ends for loop
		}	//Ends for loop

		//Now, create the bar graph graph.
		try{
			String fileName = "Graphing/Data/NN_Bar_Graph.dat";
			PrintStream output = new PrintStream(new File(fileName));
			System.setOut(output);

			double seperator = 0.33;

			for (int i = 0; i < totalLipids; i++){
				for (int j = 0; j < totalLipids; j++){
					seperator = 0.33 + (i*1.33) + (0.33 * j);
					
					String Lipid = Mathematics.IntToLipid(i, lipidNames);
					String compLipid = Mathematics.IntToLipid(j, lipidNames);

					System.out.println(Lipid + " " + compLipid + " " + seperator + " " + barGraphNN[i][j]);

				}	//Ends for loop
			}	//Ends for loop

		}	//Ends try statement

		catch (IOException e){
			System.out.println("Error in Creating NN Bar Graph File");
		}	//Ends catch statement

		System.setOut(console);
	}	//Ends createOutputFiles Methdo



	//Going to create an output file after manipulating and binning OPvNN
	public static void createNNFiles_AA(double[][][][][] OPvNN, String[] lipidNames){
		PrintStream console = System.out;
		int totalLipids = lipidNames.length;

		double[][] barGraphNN = new double[totalLipids][totalLipids];

		//iterate through second index
		for (int lipid = 0; lipid < totalLipids; lipid++){
			String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

			//iterate through third index
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){
				String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

				String fileName = "Graphing/Data/" + lipidName + "_Histogram_" + compLipidName + ".dat";				

				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					//First Sum the array we want to look at.
					double sum = 0;
					int length = OPvNN[0][lipid][compLipid][0].length;					
					double avgNN = 0;

					for (int neighbors = 0; neighbors < length; neighbors++){
						sum = sum + OPvNN[0][lipid][compLipid][0][neighbors];
					}	//Ends for Loop
					
					//Divide each count by the sum so we can find a proportion.
					for (int neighbors = 0; neighbors < length; neighbors++){
						double count = OPvNN[0][lipid][compLipid][0][neighbors];
						double proportion = count / sum;
						avgNN = avgNN + (proportion * neighbors);

						if (proportion > 0.00001){
							System.out.println(neighbors + " " + proportion);
						}	//ends if statement
					}	//Ends for loop

					barGraphNN[lipid][compLipid] = avgNN;
				}	//end try statement

				catch (IOException e){
					System.out.println("Error in creating Histogram Output File");
				}	//Ends catch statement

				System.setOut(console);
			}	//Ends for loop
		}	//Ends for loop

		//Now, create the bar graph graph.
		try{
			String fileName = "Graphing/Data/NN_Bar_Graph.dat";
			PrintStream output = new PrintStream(new File(fileName));
			System.setOut(output);

			double seperator = 0.33;

			for (int i = 0; i < totalLipids; i++){
				for (int j = 0; j < totalLipids; j++){
					seperator = 0.33 + (i*1.33) + (0.33 * j);
					
					String Lipid = Mathematics.IntToLipid(i, lipidNames);
					String compLipid = Mathematics.IntToLipid(j, lipidNames);

					System.out.println(Lipid + " " + compLipid + " " + seperator + " " + barGraphNN[i][j]);

				}	//Ends for loop
			}	//Ends for loop

		}	//Ends try statement

		catch (IOException e){
			System.out.println("Error in Creating NN Bar Graph File");
		}	//Ends catch statement

		System.setOut(console);

	}	//Ends createOutputFiles Methdo


	//Read Various Files and create Lipids to be associated with specific Frames.
		//Unqiue for a specific set of file formats.
	public static int readFile(String[] lipidNames, boolean firstFrameOnly, String fileName) throws FileNotFoundException {

		File file = new File(fileName);
		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

		file = new File(fileName);
		Scanner Scout = new Scanner(file);
		Scout.useDelimiter(" ");


		Frame Frame = new Frame(9999, 1);

		int currentFrame = 0;
		int previousFrame = 1000;
		String Lipid;
		int ID;
		String Chain;
		String Element;
		int Member;
		int Hydrogen;
		double X;
		double Y; 
		double Z;
	
		int totalFiles = 0;

		//Probe Forward
		int maximumID = findMaximumID(Scout, 0);
		
		boolean keepGoing = true;

		//Scane the whole file
		while (keepGoing){
			currentFrame = Scan.nextInt();
			Lipid = Scan.next();
			ID = Scan.nextInt();
			Chain = Scan.next();
			Element = Scan.next();
			Member = Scan.nextInt();
			Hydrogen = Scan.nextInt();
			X = Scan.nextDouble();
			Y = Scan.nextDouble();
			Z = Scan.nextDouble();
			Scan.nextLine();
			
			if (!(Scan.hasNextLine())) {
				keepGoing = false;
			}	//Ends if statement

			if (previousFrame != currentFrame){
				totalFiles++;

				//New Frame is required to be made.
				//Serialize the Old One.
				if (currentFrame == 0){
					//We won't have anything to serialize in this case.
					Frame = new Frame(currentFrame, maximumID);	
				}	//ends if statement

				else{
					if (firstFrameOnly == true){
						keepGoing = false;
					}	//Ends if statement					

					//Serialize Old Frame
					//Create new Frame
					String frameString = Integer.toString(previousFrame);
					fileName = "Frames/frame_" + frameString + ".ser";
	
					//Find the maximum X and Y length that is unique to every frame. Can only be done after every lipid has been viewed.
					Frame.findDimensions();

					//Find the bilayer Center
					Frame.findBilayerCenter();


					//Serialize the updated Frame			
					serializeFrame(fileName, 9999, Frame);

					maximumID = findMaximumID(Scout, currentFrame);

					//Create new Frame
					Frame.resetFrame(currentFrame, maximumID);
				}	//Ends else statement
			}	//Ends if statement	

			//Now, assign values to the item it corresponds to.

			if ( (Chain.equals("null")) && (Element.equals("null")) ){
				//New lipid identifiers have no chain or element

				Frame.createLipid(Lipid, ID, X, Y, Z, lipidNames);
			}	//Ends if statement

			else if ( (Hydrogen == -1) ){
				//This implies that it is either Carbon or a special element.
				if (Element.equals("C") || (Element.equals("C3")) || (Element.equals("H3"))) {

					Frame.allLipids[ID - 1].assignChainIdentifier(Chain);
					Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);
				}	//Ends if statemenet
			
				else if (Element.equals("P")){
					Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);

				}	//ends if statement

				else if ((Element.equals("C-Bead")) || (Element.equals("R3")) || (Element.equals("ROH"))){
					//This elese statement will group up all the Coarse-Grained Atoms.
					Frame.allLipids[Frame.nextAvailableLipid - 1].assignChainIdentifier(Chain);
					Frame.allLipids[Frame.nextAvailableLipid - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);

				}	//Ends else statement
			}	//Ends if statment

			else {
				//It must be a Hydrogen in this case.
				Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);

			}	//Ends else statement

			previousFrame = currentFrame;
		}	//Ends while loop

		//Save the last Frame.
		serializeFrame("falseName", (totalFiles-1), Frame);
		
		return totalFiles;
	}	//Ends ReadFile
}	//End class definition
