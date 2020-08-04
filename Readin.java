//Readin Java File
//This class relates to all operations related towards reading/creating files.


import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.lang.Math;

public class Readin implements Serializable{

	//Save an object to the Disk so that it can be accessed at a later date.
	public static void serializeFrame(String fileName, int frameNumber, Frame Frame){

		//Setup a method of assigning fileNames based off an integer instead of a manually chosen fileName
		if (frameNumber < 8888){		//8888 Means nothing, just choosing a very large number.
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
		}	//Ends catch statement

		return newFrame;
	}	//Ends unserializeFrame

	public static int findFrameSeperator(){
		String[] FileArray = new File("Frames/").list();
		int totalFiles = FileArray.length;
		int maxFrame = 0;

		for (int currentFile = 0; currentFile < totalFiles; currentFile++){
			int FileStringLength = FileArray[currentFile].length();
			FileArray[currentFile] = FileArray[currentFile].substring(6, (FileStringLength - 4));
	
			int currentFrame = Integer.parseInt(FileArray[currentFile]);
			
			if (currentFrame > maxFrame){
				maxFrame = currentFrame;
			}	//Ends if statement
		}	//Ends for loop

		int result = maxFrame / (totalFiles - 1);

		return result;
	}	//Ends findFrameSeperator

	public static int findTotalFrames(int seperator){
		int totalFiles = new File("Frames/").list().length;	//Find the total number of files in the Frames/ directory
		int lastFile = (totalFiles*seperator) - seperator;	//Find that total number and based off the seperator given figure out the total # of objects
	
		Frame currentFrame = Readin.unserializeFrame(lastFile);	//Unserialize the last file.

		while (currentFrame.nextFrame != null) { currentFrame = currentFrame.nextFrame; }	//Go to the last object in the Linked List
		
		int finalFrame = currentFrame.getFrameNumber();						//Get the last object's number

		return finalFrame;
	}	//Ends findTotalFrames


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
		//There is a potential that a simulation of Atomistic Cholesterol only could break this.
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

	public static void createTestFiles(double[] test, String[] lipidNames){
		PrintStream console = System.out;			// Save the current output device so we can revert anything done later
		int totalLipids = lipidNames.length;		// Total number of lipids

		String fileName = "Graphing/Data/test.dat";			// Name of our new file. We can get creative and make more but this is an example

		try{			// Use a try catch when we attempt to make a new file
			PrintStream output = new PrintStream(new File(fileName));			// Might as well imbed our new file inside our new printstream, both have to work.




		}
		catch(IOException e){			// Example Catch, really don't have to change this.
			System.setOut(console);
			System.out.println("Error in openning " + fileName);
			System.out.println(e);
		}

		System.setOut(console);			// Set the output of text back to what it was at the beginning.
	}	// Ends createTestFiles



	//Create files to output Order Parameters
	public static void createOPFiles(double[][][][][][] OP, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
	
		int totalLipids = lipidNames.length;
		
		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			String Leaflet_Name = Mathematics.IntToLeaflet_STR(Leaflet);

			for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
				for (int currentChain = 0; currentChain < 2; currentChain++){
					//Check to see if there is valid data.
					double sum = 0;
					int length = OP[0][Leaflet][currentLipid][currentChain][0].length;

					for (int i = 0; i < length; i++){
						sum = sum + OP[0][Leaflet][currentLipid][currentChain][0][i];
					}	//ends for loop

					if (sum > 0){		//If the given OP is valid.
						String fileName = "Graphing/Data/" + Leaflet_Name + "_" + lipidNames[currentLipid] + "_chain_" + currentChain + "_OP.dat";

						try{
							PrintStream output = new PrintStream(new File(fileName));
							System.setOut(output);

							for (int i = 0; i < length; i++){
								double count = OP[0][Leaflet][currentLipid][currentChain][0][i];

								if (count > 0) {
									double currentOP = OP[1][Leaflet][currentLipid][currentChain][0][i] / totalFiles;
									double squaredOP = OP[2][Leaflet][currentLipid][currentChain][0][i] / totalFiles;

									double deviation = Mathematics.calculateDeviation(currentOP, squaredOP);
									
									System.out.println(i + " " + currentOP + " " + deviation);

								}	//Ends if statement
							}	//Ends for loop

							fileName = "Graphing/Data/" + Leaflet_Name + "_" + lipidNames[currentLipid] + "_chain_" + currentChain + "_OP_H.dat";
							PrintStream output2 = new PrintStream(new File(fileName));
							System.setOut(output2);

							for (int currentHydrogen = 1; currentHydrogen < 4; currentHydrogen++){
								for (int i = 0; i < length; i++){
									double count = OP[0][Leaflet][currentLipid][currentChain][currentHydrogen][i];

									if (count > 0) {
										double currentOP = OP[1][Leaflet][currentLipid][currentChain][currentHydrogen][i] / totalFiles;
										double squaredOP = OP[2][Leaflet][currentLipid][currentChain][currentHydrogen][i] / totalFiles;

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
		}	//Ends for loop

		System.setOut(console);

	}	//Ends createOPFiles method

	public static void createDipoleFiles(double[][][][][] DipoleField, double Spacing, String[] lipidNames, double totalFrames){
		//DipoleField[ Count / Dipole / Dipole^2][ Leaflet ][ Lipid ][ CompLipid ][ Radial Distance ]

		PrintStream console = System.out;
		int totalLipids = lipidNames.length;
		int totalLeaflets = DipoleField[0].length;
		int totalRadialSpaces = DipoleField[0][0][0][0].length;
		
		for (int Leaflet = 0; Leaflet < totalLeaflets; Leaflet++){
			String LeafletName = Mathematics.IntToLeaflet_STR(Leaflet);			

			for (int Lipid = 0; Lipid < totalLipids; Lipid++){
				String LipidName = lipidNames[Lipid];

				for (int compLipid = 0; compLipid < totalLipids; compLipid++){
					String compLipidName = lipidNames[compLipid];

					String FileName = "Graphing/Data/" + LeafletName + "_Leaflet_" + LipidName + "_" + compLipidName + "_Dipole.dat";

					try{
						PrintStream output = new PrintStream(new File(FileName));
						System.setOut(output);


						for (int radial = 0; radial < totalRadialSpaces; radial++){
							double radius = radial * Spacing; 					
							double count = DipoleField[0][Leaflet][Lipid][compLipid][radial];

							double dipole = DipoleField[1][Leaflet][Lipid][compLipid][radial] / totalFrames;
							double dipoleSquared = DipoleField[2][Leaflet][Lipid][compLipid][radial] / totalFrames;

							System.out.println(radius + " " + dipole + " " + dipoleSquared);

						}	//Ends for loop

						System.setOut(console);
					}	//Ends try statement

					catch (IOException e){
						System.setOut(console);
						System.out.println("Error in Creating " + FileName);
						System.out.println(e);
					}	//Ends catch
				}	//Ends for loop
			}	//ends for loop
		}	//Ends for loop
	}	//Ends createDipoleFiles method

	public static void createRegistrationFiles(int[][][] Registration, String[] lipidNames){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
		int totalFrames = Registration[0].length;
		
		for (int Lipid = 0; Lipid < totalLipids; Lipid++){

			String LipidName = lipidNames[Lipid];
			String fileName = "Graphing/Data/" + LipidName + "_Registration.dat";

			try{
				PrintStream output = new PrintStream(new File(fileName));
				System.setOut(output);

				for (int Frame = 0; Frame < totalFrames; Frame++){
					double totalCounted = Registration[0][Frame][Lipid];
					double acceptedCounts = Registration[1][Frame][Lipid];
				
					if (totalCounted != 0) {
						double registrationValue = acceptedCounts / totalCounted;
						System.out.println(Frame + " " + registrationValue);
					}	//Ends if statement
				}	//Ends for loop
				System.setOut(console);
			}	//Ends try statement

			catch (IOException e){
				System.setOut(console);
				System.out.println("Error in Creating " + fileName);
				System.out.println(e);
			}	//Ends catch
		}	//Ends for loop	

		System.setOut(console);
	}	//Ends createRegistrationFiles



	public static void createCosThetaHistogramFiles(double[][][][] CosTheta_Histogram, String[] lipidNames, boolean Coarse_Grained){
		PrintStream console = System.out;

		int totalLipids = lipidNames.length;
		int totalLeaflets = 2;
		int length = CosTheta_Histogram[0][0][0].length;
		double sum = 0;

		for (int Leaflet = 0; Leaflet < totalLeaflets; Leaflet++){
			String LeafletName = Mathematics.IntToLeaflet_STR(Leaflet);

			for (int Lipid = 0; Lipid < totalLipids; Lipid++){
				for (int chain = 0; chain < 3; chain++){
					sum = 0;
					for (int j = 0; j < length; j++){
						sum = sum + CosTheta_Histogram[Leaflet][Lipid][chain][j];
					}	//Ends for loop

					if (sum > 1){		//If the value is valid.
						String fileName = "Graphing/Data/" + LeafletName + "_Leaflet_" + lipidNames[Lipid] + "_chain_" + (chain+1) + "_CosTheta_Histogram.dat";
						try{
							PrintStream output = new PrintStream(new File(fileName));
							System.setOut(output);

							for (int j = 0; j < length; j++){
								double binSpot = (double) j;
								binSpot = (binSpot / 2000) - 1;		//Special Equation to find the indexed spot and what it means.
								
								double count = CosTheta_Histogram[Leaflet][Lipid][chain][j];
								String firstValue = String.format("%.0005f", binSpot);
								System.out.println(firstValue + " " + count);

							}	//Ends for loop
							System.setOut(console);
						}	//Ends try statement

						catch (IOException e){
							System.setOut(console);
							System.out.println("Error in Creating " + fileName);
							System.out.println(e);
						}	//Ends catch
					}	//Ends if statement
					else{ chain = 3; }	//Quick way to skip ahead
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop
		System.setOut(console);
	}	//ends createCosThetaHistogram

	

	public static void createThicknessFiles(int[][] Thickness, String[] lipidNames){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
		int totalBinSpots = Thickness[0].length;
		int sum = 0;

		for (int Lipid = 0; Lipid < totalLipids; Lipid++){
			for (int currentBinSpot = 0; currentBinSpot < totalBinSpots; currentBinSpot++){
				sum = sum + Thickness[Lipid][currentBinSpot];
			}	//Ends for loop

			//Now we ignore lipids without phosphates.
			if (sum > 1){
				String fileName = "Graphing/Data/" + lipidNames[Lipid] + "_Thickness.dat";

				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					for (int currentBinSpot = 0; currentBinSpot < totalBinSpots; currentBinSpot++){
						float binSpot = (float) currentBinSpot;
						binSpot = (binSpot/10) - 100;
						int count = Thickness[Lipid][currentBinSpot];
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



	public static void createPCLFiles(double[][][][][] PCL, String[] lipidNames, double totalFiles){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
	 	double sum = 0;
		int length = PCL[0][0][0][1].length;

		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			String Leaflet_Name = Mathematics.IntToLeaflet_STR(Leaflet);

			for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
				sum = 0;
				for (int carbonIndex = 0; carbonIndex < length; carbonIndex++){
					sum = sum + PCL[0][Leaflet][currentLipid][1][carbonIndex];
				}	//Ends for loop

				//Now we ignore lipids without 2 chains
				if (sum > 1){
					for (int chainCount = 0; chainCount < 2; chainCount++) {
						String fileName = "Graphing/Data/" + Leaflet_Name + "_Leaflet_" + lipidNames[currentLipid] + "_chain_" + chainCount + "_PCL.dat";

						try{
							PrintStream output = new PrintStream(new File(fileName));

							for (int carbonIndex = 0; carbonIndex < length; carbonIndex++){
								System.setOut(console);
							
								double count = PCL[0][Leaflet][currentLipid][chainCount][carbonIndex];
								if (count > 0) {
									if (carbonIndex != 0) {
										double carbonLength = PCL[1][Leaflet][currentLipid][chainCount][carbonIndex] / totalFiles;
										double squaredLength = PCL[2][Leaflet][currentLipid][chainCount][carbonIndex] / totalFiles;
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
		}	//Ends for loop

		System.setOut(console);
	}	//ends createPCLFiles Method


	//Going to create an output file after manipulating and binning OPvNN
	public static void createOPvNNFiles(double[][][][][][] OPvNN, String[] lipidNames, double totalFrames){
		PrintStream console = System.out;
		int totalLipids = OPvNN[0][0].length;

		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			String LeafletName = Mathematics.IntToLeaflet_STR(Leaflet);

			for (int lipid = 0; lipid < totalLipids; lipid++){
				String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

				for (int compLipid = 0; compLipid < totalLipids; compLipid++){
					String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

					for (int chain = 0; chain < 2; chain++){

						//Create a file specifically for this
						String fileName = "Graphing/Data/" + LeafletName + "_Leaflet_OP_NN_" + lipidName + "_chain_" + chain + "_" + compLipidName + ".dat";

						try{
							//First Sum the array we want to look at.
							//OPvNN[ Count / OP / OP^2 ][ Leaflet ][ Current Lipid ][ Comparing Lipid ][ Chain ][ Number of Neighbors ]
							double sum = 0;
							int length = OPvNN[0][Leaflet][lipid][compLipid][chain].length;					

							for (int neighbors = 0; neighbors < length; neighbors++){
								sum = sum + OPvNN[0][Leaflet][lipid][compLipid][chain][neighbors];
							}	//Ends for Loop

							PrintStream output = new PrintStream(new File(fileName));
							System.setOut(output);

							if (sum == 0) {
								System.out.println("0 0 0 0.0%");
							}

							for (int neighbors = 0; neighbors < length; neighbors++){
								double count = OPvNN[0][Leaflet][lipid][compLipid][chain][neighbors];
								double proportion = count / sum;

								if (proportion <= 0.001){
									proportion = 0;
								}	//ends if statement

								proportion = proportion * 100;
								String stringProportion = String.format("%.2f", proportion);

								double OP = OPvNN[1][Leaflet][lipid][compLipid][chain][neighbors] / totalFrames;
								double OPSquared = OPvNN[2][Leaflet][lipid][compLipid][chain][neighbors] / totalFrames;
								double Deviation = Mathematics.calculateDeviation(OP, OPSquared);
							
								if (proportion > 2){
									if (OP > -1.01) {
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
		}	//Ends for loop
	}	//Ends createOPvNNFiles Method

	//Get the AvgOP for the entire system (for each lipid)
	//Get the avg NN for the entire system
	//Show only that
	//Not meant to be graphed.
	public static void createStandardDataFiles_CG(double[][][][][][] OPvNN, double[][][][] OP_CG, String[] lipidNames){
		PrintStream console = System.out;
		int totalLipids = lipidNames.length;
		double sum = 0;
		double overallSum = 0;
		String fileName = "Graphing/Data/Standard_Data.dat";

		try{

			PrintStream output = new PrintStream(new File(fileName));
			System.setOut(output);

			for (int Leaflet = 0; Leaflet < 2; Leaflet++){
				String Leaflet_Name = Mathematics.IntToLeaflet_STR(Leaflet);

				for (int lipid = 0; lipid < totalLipids; lipid++){
					String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

					for (int compLipid = 0; compLipid < totalLipids; compLipid++){
						String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

						sum = 0;
						int length = OPvNN[0][Leaflet][lipid][compLipid][0].length;					
						double avgNN = 0;

						for (int neighbors = 0; neighbors < length; neighbors++){
							sum = sum + OPvNN[0][Leaflet][lipid][compLipid][0][neighbors];
							overallSum = overallSum + OPvNN[0][Leaflet][lipid][compLipid][0][neighbors];
						}	//Ends for Loop
						
						//Divide each count by the sum so we can find a proportion.
						for (int neighbors = 0; neighbors < length; neighbors++){
							double count = OPvNN[0][Leaflet][lipid][compLipid][0][neighbors];
							double proportion = count / sum;
							avgNN = avgNN + (proportion * neighbors);


						}	//Ends for loop

						System.out.println(Leaflet_Name + " Leaflet: " + lipidName + " has " + avgNN + " neighbors of " + compLipidName);

					}	//Ends for loop

					System.out.println("");
				}	//Ends for loop
			}	//Ends for loop

			for (int lipid = 0; lipid < totalLipids; lipid++){
				for (int Leaflet = 0; Leaflet < 2; Leaflet++){
					for (int chain = 0; chain < 3; chain++){
						String lipidName = Mathematics.IntToLipid(lipid, lipidNames);
						String Leaflet_String = Mathematics.IntToLeaflet_STR(Leaflet);

						double OP = OP_CG[1][Leaflet][chain][lipid] / OP_CG[0][Leaflet][chain][lipid];
						double OP_Squared = OP_CG[2][Leaflet][chain][lipid] / OP_CG[0][Leaflet][chain][lipid];

						double deviation = Mathematics.calculateDeviation(OP, OP_Squared);
						System.out.println("OP of " + lipidName + " chain " + (chain+1) + " in the " + Leaflet_String + " Leaflet is " + OP + "    +-" + deviation);
					}	//Ends for loop

					System.out.println("");
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends try statement

		catch(IOException e){
			System.setOut(console);
			System.out.println("Error in creating Standard Data File");
		}	//Ends catch statement

		System.setOut(console);
	}	//Ends createStandardDataFiles_CG


	public static void createAngleHistogramFile(int[] Angles, String lipidName, boolean firstChain, int Member){
		PrintStream console = System.out;

		int length = Angles.length;
		int sum = 0;
		for (int i = 0; i < length; i++) { sum = sum + Angles[i]; }
		if (sum != 0){
			
			String chain;
			if (firstChain) { chain = "SN1"; }
			else { chain = "SN2"; }
			String fileName = "Graphing/Data/Angle_Histogram_" + lipidName + "_" + chain + "_" + Member + ".dat";
			
			try {
				PrintStream output = new PrintStream(new File(fileName));
				System.setOut(output);

				for (int i = 0; i < length; i++){
					int count = Angles[i];
					float givenAngle = (float) i;
					givenAngle = givenAngle / 20;
					String givenOutput = String.format("%.05f", givenAngle);		
					
					System.out.println(givenOutput + " " + count);
				}	//Ends for loop
			}	//Ends try statement

			catch(IOException e){
				System.setOut(console);
				System.out.println("Error in creating Angle Histogram file");

			}	//Ends catch
		}	//Ends if statement
		System.setOut(console);
	}	//Ends createAngleHistogramFile



	//Going to create an output file after manipulating and binning OPvNN
	public static void createNNFiles(double[][][][][][] OPvNN, String[] lipidNames){
		PrintStream console = System.out;
		int totalLipids = lipidNames.length;

		double[][][] barGraphNN = new double[2][totalLipids][totalLipids];
		//barGraphNN[ Leaflet ][ Curernt Lipid ][ Comp Lipid ]

		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			String LeafletName = Mathematics.IntToLeaflet_STR(Leaflet);

			for (int lipid = 0; lipid < totalLipids; lipid++){
				String lipidName = Mathematics.IntToLipid(lipid, lipidNames);

				for (int compLipid = 0; compLipid < totalLipids; compLipid++){
					String compLipidName = Mathematics.IntToLipid(compLipid, lipidNames);

					String fileName = "Graphing/Data/" + LeafletName + "_Leaflet_" + lipidName + "_Histogram_" + compLipidName + ".dat";				
					try{
						PrintStream output = new PrintStream(new File(fileName));
						System.setOut(output);

						//First Sum the array we want to look at.
						double sum = 0;
						int length = OPvNN[0][Leaflet][lipid][compLipid][0].length;					
						double avgNN = 0;

						for (int neighbors = 0; neighbors < length; neighbors++){
							sum = sum + OPvNN[0][Leaflet][lipid][compLipid][0][neighbors];
						}	//Ends for Loop
						
						//Divide each count by the sum so we can find a proportion.
						for (int neighbors = 0; neighbors < length; neighbors++){
							double count = OPvNN[0][Leaflet][lipid][compLipid][0][neighbors];
							double proportion = count / sum;
							avgNN = avgNN + (proportion * neighbors);

							if (proportion > 0.00001){
								System.out.println(neighbors + " " + proportion);
							}	//ends if statement
						}	//Ends for loop

						barGraphNN[Leaflet][lipid][compLipid] = avgNN;
					}	//end try statement

					catch (IOException e){
						System.out.println("Error in creating Histogram Output File");
					}	//Ends catch statement

					System.setOut(console);
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends For Loop


		//Now, create the bar graph graph.
		try{
			for (int Leaflet = 0; Leaflet < 2; Leaflet++){
				String LeafletName = Mathematics.IntToLeaflet_STR(Leaflet);

				String fileName = "Graphing/Data/" + LeafletName + "_Leaflet_NN_Bar_Graph.dat";
				PrintStream output = new PrintStream(new File(fileName));
				System.setOut(output);

				double doubleTotalLipids = totalLipids;
				double shiftValue = 1 / doubleTotalLipids;
				double seperator = 0;

				for (int i = 0; i < totalLipids; i++){
					for (int j = 0; j < totalLipids; j++){
						seperator = shiftValue + (i*(1+shiftValue)) + (shiftValue * j);
						
						String Lipid = Mathematics.IntToLipid(i, lipidNames);
						String compLipid = Mathematics.IntToLipid(j, lipidNames);

						System.out.println(Lipid + " " + compLipid + " " + seperator + " " + barGraphNN[Leaflet][i][j]);

					}	//Ends for loop
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
	public static int readFile(String[] lipidNames, int givenFinalFrame, int frameSeperator, String fileName) throws FileNotFoundException {

		File file = new File(fileName);
		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

		file = new File(fileName);
		Scanner Scout = new Scanner(file);	//Going to use this to help find out important information for the future, such as how many more IDs or Frames we need to expect.
		Scout.useDelimiter(" ");


		Frame Frame = new Frame(9999, 1);

		int currentFrame = 0;
		int previousFrame = 1000;
		String Lipid;
		int ID;
		String Leaflet;
		String FlipFloppable;
		String Chain;
		String Element;
		int Member;
		int Hydrogen;
		double X;
		double Y; 
		double Z;
		int totalFiles = 0;
		boolean keepGoing = true;

		int maximumID = findMaximumID(Scout, 0);	//Probe forward, find maximum ID

		//Now read the entire file.
		while (keepGoing){

			//Variables that are readin each line.
			currentFrame = Scan.nextInt();
			Lipid = Scan.next();
			ID = Scan.nextInt();
			Leaflet = Scan.next();
			FlipFloppable = Scan.next();
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
					if (currentFrame == givenFinalFrame) {
						keepGoing = false;
					}	//Ends if statement

	

					//Serialize Old Frame
					//Create new Frame


					String frameString = Integer.toString((previousFrame / frameSeperator)*frameSeperator);
					fileName = "Frames/frame_" + frameString + ".ser";
	
					Frame.findDimensions();			//Find X and Y Length of the entire system.
					Frame.findBilayerCenter();		//Find the generalized center of the system.

					if ((currentFrame % frameSeperator) == 0){
						Frame = Frame.setFirstFrame();			//Go to first frame in LL
						serializeFrame(fileName, 9999, Frame);		//Serialize/Save that Frame
						Frame.resetFrame(currentFrame, maximumID);	//Reset the frame you have and get ready for a new one.

					}	//Ends if statement

					else {
						Frame.nextFrame = new Frame(currentFrame, maximumID);	//Create next Frame, add to LL
						Frame.nextFrame.prevFrame = Frame;			//Set the nextFrame's previousFrame to be the current Frame.
						Frame = Frame.nextFrame;				//Set the currentFrame to be the next Frame

					}	//Ends else statement
				}	//Ends else statement
			}	//Ends if statement	





			//Now, assign values to the item it corresponds to.

			if ( (Chain.equals("null")) && (Element.equals("null")) ){
				//New lipid identifiers have no chain or element

				Frame.createLipid(Lipid, ID, X, Y, Z, Leaflet, FlipFloppable, lipidNames);
			}	//Ends if statement

			else if ( (Hydrogen == -1) ){
				//This implies that it is either a standard Element, or Special Element
				String[] standardElements = new String[]{ "C", "C3", "H3", "C-Bead", "R3", "ROH", "C1", "C10", "C13" };
				String[] nonStandardElements = new String[]{ "P", "N", "PO4", "NC3" };

				if (Mathematics.isValidLipid(Element, standardElements)) {

					Frame.allLipids[ID - 1].assignChainIdentifier(Chain);
					Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);
				}	//Ends if statemenet
			
				else if (Mathematics.isValidLipid(Element, nonStandardElements)){
					Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);

				}	//ends if statement
			}	//Ends if statment

			else {
				//It must be a Hydrogen in this case.
				Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);

			}	//Ends else statement

			previousFrame = currentFrame;
		}	//Ends while loop

		//Save the last Frame.
		Frame = Frame.setFirstFrame();
		serializeFrame("falseName", (((totalFiles-1)/frameSeperator)*frameSeperator), Frame);
		
		return totalFiles;
	}	//Ends ReadFile
}	//End class definition
