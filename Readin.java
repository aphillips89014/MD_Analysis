//Readin Java File
//This class relates to all operations related towards reading/creating files.


import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.lang.Math;

public class Readin implements Serializable{

	String[] lipidNames;

	public Readin(){

	}	//Ends Constructor


	//Unserialize a Serialized Object, return said object
	public static Frame getFrame(int x){
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

		}

		catch(ClassNotFoundException e){
			System.out.println("Frame Class not Found.");
		}	//Ends catch statement


		catch(IOException e){
			System.out.println("Issue occured accessing " + fileName);
			System.out.println(e);
			System.out.println("");

		}	//Ends catch statement

		return newFrame;
	}	//Ends getFrame



	//Search forward in a specific file to find out how many total Molecules in a data file there is.
		//This method is unique to a specific File Format
	public static int findMaximumID(File file) throws FileNotFoundException {
		boolean keepGoing = true;

		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

		int ID = 0;
		int currentFrame = 0;

		while (keepGoing){
			currentFrame = Scan.nextInt();

			if (currentFrame != 0) {
				keepGoing = false;
			}	//Ends if statement

			else{
				Scan.next();
				ID = Scan.nextInt();
				Scan.nextLine();		
			}	//Ends else statement
		}	//Ends while loop

		return ID;
	}	//Ends findMaximumId

	//Parse forward through the file we are interested in
		//Find each lipid in the systme by searching through the first frame.
	public static String[] findLipidNames() throws FileNotFoundException {
		boolean keepGoing = true;

		File file = new File("/media/alex/Hermes/Anton/Coordinates.dat");
		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

		int currentFrame = 0;
		String lipidName = "";

		//Because we don't want to introduce vectors we will be forced to use a somewhat inefficent method
		//Errors will occur if you have more than 10 lipid types
		String[] lipidNames = new String[10];

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


	//Save an object to the Disk so that it can be accessed at a later dat.
	public static void serializeFrame(String fileName, int frameNumber, Frame Frame){

		//Setup a method of assigning fileNames based off an integer instead of a pre-processed fileName
		if (frameNumber < 2000){
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
			//Don't do anything, give up.
		}       //Ends Catch statement
	}	//Ends Serialize Frame method


	//Unique to specific systems
	public static String convertInteger(int x, String[] lipidNames){
		//Converts a given int to a specific string.
		String output = "null";
		output = lipidNames[x];		

		return output;
	}	//Ends convertInteger Method

	public static void createOrderParameterFiles(double[][][][][] OP, String[] lipidNames){
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
								double currentOP = OP[1][currentLipid][currentChain][0][i] / count;
								double squaredOP = OP[2][currentLipid][currentChain][0][i] / count;

								double deviation = findDeviation(currentOP, squaredOP);
								
								System.out.println(i + " " + currentOP + " " + squaredOP);

							}	//Ends if statement
						}	//Ends for loop

						fileName = "Graphing/Data/" + lipidNames[currentLipid] + "_chain_" + currentChain + "_OP_H.dat";
						PrintStream output2 = new PrintStream(new File(fileName));
						System.setOut(output2);

						for (int currentHydrogen = 1; currentHydrogen < 4; currentHydrogen++){
							for (int i = 0; i < length; i++){
								double count = OP[0][currentLipid][currentChain][currentHydrogen][i];

								if (count > 0) {
									double currentOP = OP[1][currentLipid][currentChain][currentHydrogen][i] / count;
									double squaredOP = OP[2][currentLipid][currentChain][currentHydrogen][i] / count;

									double deviation = findDeviation(currentOP, squaredOP);
									
									System.out.println(i + " " + currentOP + " " + squaredOP);

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

	}	//Ends createOrderParameterFiles method

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
						binSpot = (binSpot/10) - 40;
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



	public static void createPCLFiles(double[][][][] PCL, String[] lipidNames){
		PrintStream console = System.out;
		
		int totalLipids = lipidNames.length;
	 	double sum = 0;
		int length = PCL[0][0][1].length;

		for (int i = 0; i < totalLipids; i++){
			sum = 0;
			for (int j = 0; j < length; j++){
				sum = sum + PCL[0][i][1][j];
			}	//Ends for loop

			//Now we ignore lipids without phosphates.
			if (sum > 1){
				String fileName0 = "Graphing/Data/" + lipidNames[i] + "_chain_0_PCL.dat";
				String fileName1 = "Graphing/Data/" + lipidNames[i] + "_chain_1_PCL.dat";

				try{
					PrintStream output0 = new PrintStream(new File(fileName0));
					PrintStream output1 = new PrintStream(new File(fileName1));

					double count = 0;
					double carbonLength = 0;
					double squaredLength = 0;
					double deviation = 0;


					for (int j = 0; j < length; j++){
						System.setOut(console);

						count = PCL[0][i][0][j];
						if (count > 0) {
							if (j != 0) {
								carbonLength = PCL[1][i][0][j] / count;
								squaredLength = PCL[2][i][0][j] / count;
	
								deviation = findDeviation(carbonLength, squaredLength);
									
								System.setOut(output0);
								System.out.println(j + " " + carbonLength + " " + deviation);
							}	//Ends if statement
						}	// ends if statement


						count = PCL[0][i][1][j];
						if (count > 0) {
							if (j != 0) {
								carbonLength = PCL[1][i][1][j] / count;
								squaredLength = PCL[2][i][1][j] / count;
	
								deviation = findDeviation(carbonLength, squaredLength);
								
								System.setOut(output1);
								System.out.println(j + " " + carbonLength + " " + deviation);
							}	//Ends if statement
						}	// ends if statement
					}	//Ends for loop
				}	//Ends try statement

				catch (IOException e){
					System.setOut(console);
					System.out.println("Error in Creating " + fileName0);
					System.out.println("Error in Creating " + fileName1);
					System.out.println(e);
				}	//Ends catch
			}	//Ends if statement
		}	//Ends for loop

		System.setOut(console);
	}	//ends createPCLFiles Method

	//Finds Standard Deviation
	public static double findDeviation(double value, double squaredValue){
		value = value * value;

		double deviation = squaredValue - value;
	
		deviation = Math.pow(deviation, 0.5);

		return deviation;
	}	//ends find Deviation Method


	//Going to create an output file after manipulating and binning OPvNN
	public static void createOPvNNFiles(double[][][][][] OPvNN, String[] lipidNames){

		PrintStream console = System.out;

		int totalLipids = OPvNN[0].length;

		//Iterate through second index
		for (int i = 0; i < totalLipids; i++){
			String lipid = convertInteger(i, lipidNames);

			//Iterate through third index
			for (int j = 0; j < totalLipids; j++){
				String compLipid = convertInteger(j, lipidNames);

				//Iterate through fourth index
				for (int chain = 0; chain < 2; chain++){

					//Create a file specifically for this
					String fileName = "Graphing/Data/OP_NN_" + lipid + "_chain_" + chain + "_" + compLipid + ".dat";				

					try{
						PrintStream output = new PrintStream(new File(fileName));
						System.setOut(output);

						//First Sum the array we want to look at.
						double sum = 0;
						int length = OPvNN[0][i][j][chain].length;					

						for (int k = 0; k < length; k++){
							sum = sum + OPvNN[0][i][j][chain][k];
						}	//Ends for Loop


						//Then find the proportion that the values in the array given occur.
							//Then find the standard Deviation of this.						
						for (int k = 0; k < length; k++){
							double count = OPvNN[0][i][j][chain][k];
							double proportion = count / sum;
							if (proportion <= 0.001){
								proportion = 0;
							}	//ends if statement

							proportion = proportion * 100;
							String stringProportion = String.format("%.2f", proportion);

							double OP = OPvNN[1][i][j][chain][k];
							double OPSquared = OPvNN[2][i][j][chain][k];

							OP = OP / count;
							OPSquared = OPSquared / count;
							
							double OpAvgSquared = Math.pow(OP, 2);
			
							double Deviation = Math.pow((OPSquared - OpAvgSquared), 0.5);
							
							//Magnitude of OP
							if (OP < 0) { OP = OP * -1; }

							if (OP > 0) {
								if (proportion > 0.3){
									System.out.println(k + " " + OP + " " + Deviation + " " + stringProportion + "%");
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
		}	//Ends for loop
	}	//Ends createOutputFiles Methdo



	//Going to create an output file after manipulating and binning OPvNN
	public static void createHistogramFiles(double[][][][][] OPvNN, String[] lipidNames){

		PrintStream console = System.out;
		int totalLipids = OPvNN[0].length;

		//iterate through second index
		for (int i = 0; i < totalLipids; i++){
			String lipid = convertInteger(i, lipidNames);

			//iterate through third index
			for (int j = 0; j < totalLipids; j++){
				String compLipid = convertInteger(j, lipidNames);

				String fileName = "Graphing/Data/" + lipid + "_Histogram_" + compLipid + ".dat";				

				try{
					PrintStream output = new PrintStream(new File(fileName));
					System.setOut(output);

					//First Sum the array we want to look at.
					double sum = 0;
					int length = OPvNN[0][i][j][0].length;					

					for (int k = 0; k < length; k++){
						sum = sum + OPvNN[0][i][j][0][k];
					}	//Ends for Loop
					
					for (int k = 0; k < length; k++){
						double count = OPvNN[0][i][j][0][k];
						double proportion = count / sum;
						if (proportion > 0.0005){
							System.out.println(k + " " + proportion);
						}	//ends if statement
					}	//Ends for loop
				}	//end try statement

				catch (IOException e){
					System.out.println("Error in creating Histogram Output File");
				}	//Ends catch statement

				System.setOut(console);
			}	//Ends for loop
		}	//Ends for loop
	}	//Ends createOutputFiles Methdo


	//Read Various Files and create Lipids to be associated with specific Frames.
		//Unqiue for a specific set of file formats.
	public static int readFile(String[] lipidNames, boolean firstFrameOnly) throws FileNotFoundException {

		File file = new File("/media/alex/Hermes/Anton/Coordinates.dat");
		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

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

		long start = System.currentTimeMillis();

		//Probe Forward
		int maximumID = findMaximumID(file);
		
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


			//Use these variables as you will.

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
					String fileName = "Frames/frame_" + frameString + ".ser";
	
					//Find the maximum X and Y length that is unique to every frame. Can only be done after every lipid has been viewed.
					Frame.findLength();
			
					serializeFrame(fileName, 9999, Frame);

					//Create new Frame
					Frame.resetFrame(currentFrame);

				}	//Ends else statement
			}	//Ends if statement	

			//Now, assign values to the item it corresponds to.

			if ( (Chain.equals("null")) && (Element.equals("null")) ){
				//New lipid identifiers have no chain or element			
				Frame.createLipid(Lipid, ID, X, Y, Z, lipidNames);
			}	//Ends if statement

			else if ( (Hydrogen == -1) ){
				//This implies that it is either Carbon or a special element.
				if (Element.equals("C")) {

					Frame.allLipids[ID - 1].assignChainIdentifier(Chain);
					Frame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, Element, X, Y, Z);
				}	//Ends if statemenet
			
				else if (Element.equals("P")){
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
		serializeFrame("falseName", (totalFiles-1), Frame);
		
		return totalFiles;
	}	//Ends ReadFile
}	//End class definition
