//Processes Data and Interprets it.
//Processes very specific data files, check README for more information.


import java.util.Arrays;
import java.io.*;
import java.lang.Math;

public class Process implements Serializable {


	public static boolean checkForFiles(String fileName){
		//Checks for a specific file, if it exists return true, otherwise return false.
		boolean result = false;

		File file = new File(fileName);
		if(file.exists() && !file.isDirectory()) { 
		    result = true;
		}	//Ends if statement

		return result;
	}	//Ends checkForFiles method

	
	//Find the length (radius) between two points
	public static double calculateRadius(float firstX, float firstY, float secondX, float secondY){
		
		float xDiff = (firstX - secondX);
		float yDiff = (firstY - secondY);

		double dxDiff = xDiff;
		double dyDiff = yDiff;

		dxDiff = Math.pow(dxDiff, 2);
		dyDiff = Math.pow(dyDiff, 2);


		double radius = dxDiff + dyDiff;
		radius = Math.pow(radius, 0.5);

		return radius;
	}	//End calculateRadius Method



	//For the data sets we are concerned with the points exist in a box. We need to preform a special operation whenever these are within a searchRadius
	//This method returns 0, -1, or 1. This indicates which boundary it is close to.
	// 0 --> Not near a boundary
	// 1 --> Near the right (positive) boundary
	// -1 --> Near the left (negative) boundary
	public static int checkBoundary(float point, float length, int searchRadius){

		int result = 0;
		float halfLength = length / 2;
		boolean negative = false;

		if (point < 0) {
			negative = true;
			point = point * -1;
		}	//Ends if statement

		//This equation makes the point negative if it is outside the searchRadius, and makes it positive if it is within the SearchRadius
		//Only works is point and searchRadius are less than length
		point = point - (length - searchRadius);

		if (point < 0) {
			result = 0;
		}	//Ends if statement

		else{
			result = 1;
		}	//Ends else statement


		if (negative == true){
			result = result * -1;
		}	//Ends if statement

		return result;
	}	//Ends checkBoundary method



	//If a point is within the searchRadius of a Boundary of the box, this function is executed.
	//This shifts the point to a new location purely for a simpler more accurate calculation.
	public static float applyPBC(float coordinate, int modifier, float length){
		//PBC stands for Periodic Boundary Condition
		//modifier can only be 1, -1, or 0.

		float result = coordinate;
	
		//If the modifier is 0 then dont do a thing.

		if (modifier != 0){
			if (coordinate < 0){
				if (modifier == 1){
					result = result + length;
				}	//Ends if statement
				
				else if (modifier == -1){
					//Do nothing.
				}	//Ends if statement
			}	//Ends if statement

			else if (coordinate >= 0){
				if (modifier == -1){
					result = result - length;
				}	//Ends if statement

				else if (modifier == 1){
					//Do Nothing
				}	//Ends if statement
			}	//Ends if statement
		}	//Eends if statement

		return result;
	}	//Ends applyPBC method


	
	//Calculate Nearest Neighbors for a Frame that consists of many Points (Lipids).
	public static void calculateNN(Frame currentFrame, int searchRadius, Readin tempReadin, String[] lipidNames){
	
		//May already be done, so lets try to skip this lengthy calculation if it is already done.
		boolean alreadyCalculated = currentFrame.allLipids[0].checkForNN();
	
		if (alreadyCalculated) {
			//Do nothing, this job has already been done.
		}	//Ends if statement

		else{
			int length = currentFrame.allLipids.length;
			int frame = currentFrame.getFrameNumber();	

			//Find the X and Y for a single point, compare it to every other point.
				//If it is within a radius of 10 of the first point then it can be defined as a Neighbor, so add 1 to a counter.
			for (int i = 0; i < length; i++){
				float x = currentFrame.allLipids[i].getX();
				float y = currentFrame.allLipids[i].getY();
				String Name = currentFrame.allLipids[i].getName();

				float xLength = currentFrame.getXLength();
				float yLength = currentFrame.getYLength();

				int shiftY = checkBoundary(x, xLength, searchRadius);
				int shiftX = checkBoundary(y, yLength, searchRadius);

				//These names are unique for a specific system.
					//Will return at a future data for an easier way of tracking this.

				int totalLipids = lipidNames.length;
				int[] lipidCount = new int[totalLipids];

				for (int j = 0; j < length; j++){
					float x2 = currentFrame.allLipids[j].getX();
					float y2 = currentFrame.allLipids[j].getY();
					String Name2 = currentFrame.allLipids[j].getName();

					x2 = applyPBC(x2, shiftX, xLength);
					y2 = applyPBC(y2, shiftY, yLength);				

					double radius = calculateRadius(x, y, x2, y2);

					if (radius <= searchRadius && radius != 0){
						for (int k = 0; k < totalLipids; k++){
							if (Name2.equals(lipidNames[k])) { lipidCount[k]++;}

						}	//Ends for loop
					}	//Ends if statement
				}	//Ends for loop

				for (int j = 0; j < totalLipids; j++){
					currentFrame.allLipids[i].assignNN(j, lipidCount[j]);
				}	//Ends for loop
			}	//Ends for loop

			//We need to update the frame as we have just done a calculation and more importantly we want to save that calculation.
			tempReadin.serializeFrame("falseName", frame, currentFrame);

		}	//Ends else statement
	}	//Ends CalcualteNN Method


	//Each lipid has 2 chains, each chain has many Atoms. Each atom has an OP.
		//Use the method findOP to average the OP of all Atoms per Chain
		//Do this for every lipid, then save these calculation.
	public static void averageOP(Frame Frame, Readin Readin){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		if (OPCalculated){
			//Do Nothing, you've already calculated the Average OP
		}	//Ends if statement

		else{
			int length = Frame.allLipids.length;
			int frameNumber = Frame.getFrameNumber();

			for (int i = 0; i < length; i++){
				Frame.allLipids[i].findOP();

			}	//Ends for Loop

			Readin.serializeFrame("falseName", frameNumber, Frame);

		}	//Ends else statement
	}	//Ends AverageOP method


	//At this point every lipid should have an amount of Nearest Neighbors, and an Averaged OP.
		//Bin these data points such that we can average the OP for when there are specifically 2 (for example) Neighbors only.
		//This will be done in a large 5d array.
	public static double[][][][][] findOPvNN(Frame Frame, double[][][][][] OPvNN, String[] lipidNames){
		
		int length = Frame.allLipids.length;
		int totalLipids = OPvNN[0].length;

		for (int i = 0; i < length; i++){
			int currentLipid = Frame.allLipids[i].getIntName(lipidNames);
			
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){

				int neighborIndex = Frame.allLipids[i].Neighbors[compLipid];
				
				double firstOP = Frame.allLipids[i].getFirstOP();
				double secondOP = Frame.allLipids[i].getSecondOP();

				double firstOPSquared = Math.pow(firstOP, 2);
				double secondOPSquared = Math.pow(secondOP, 2);

				//Add 1 to the NN Count.
				OPvNN[0][currentLipid][compLipid][0][neighborIndex]++;
				OPvNN[0][currentLipid][compLipid][1][neighborIndex]++;
				
				//Add the OP
				OPvNN[1][currentLipid][compLipid][0][neighborIndex] = OPvNN[1][currentLipid][compLipid][0][neighborIndex] + firstOP;
				OPvNN[1][currentLipid][compLipid][1][neighborIndex] = OPvNN[1][currentLipid][compLipid][1][neighborIndex] + secondOP;

				//Add the OP^2
				OPvNN[2][currentLipid][compLipid][0][neighborIndex] = OPvNN[2][currentLipid][compLipid][0][neighborIndex] + firstOPSquared;
				OPvNN[2][currentLipid][compLipid][1][neighborIndex] = OPvNN[2][currentLipid][compLipid][1][neighborIndex] + secondOPSquared;

			}	//Ends for Loop
		}	//Ends for loop

		return OPvNN;
	}	//Ends OPvNN



	public static void main(String[] args){
		Readin ReadFile = new Readin();
		String fileName = "Frames/frame_1.ser";
		int totalFiles = 0;

	
		//Lets create some variables that will be used intermittenly.
		
		int searchRadius = 10;

		System.out.println("");
		System.out.println("----------------------------------");
		System.out.println("Initiated File Processor");
		System.out.println("");


		long start = System.currentTimeMillis();
		System.out.println("Started Reading Files");

		String[] lipidNames = new String[1];
		int totalLipids = lipidNames.length;

		try{
			lipidNames = ReadFile.findLipidNames();
			totalLipids = lipidNames.length;
		}	//Ends try statement

		catch(FileNotFoundException e){
			System.out.println("Cannot find Coordinates.dat");
		}	//Ends catch statement



		//Create a bunch of serialized objects so that the memory usage won't be as great.
		//If the files already exist then find out how many there are.
		boolean filesExist = checkForFiles(fileName);
		if (!filesExist) {
			try{
				totalFiles = ReadFile.readFile(lipidNames);

			}	//Ends try Statement

			catch(FileNotFoundException ex){
				System.out.println("Could not find initial input file");
			}	//Ends catch statement

		}	//Ends if statement

		else{
			totalFiles = new File("Frames/").list().length;
		}	//Ends if statement

		long end = System.currentTimeMillis();
		long totalTime = (end - start) / 1000;

		System.out.println("Finished Reading File in " + totalTime + " seconds.");


		System.out.println("");







		start = System.currentTimeMillis();
		System.out.println("Begun Various Calculations");

		//Create an array for calculating various things.
		double[][][][][] OPvNN = new double[3][totalLipids][totalLipids][2][20];
			//Let's describe this 5-d array.
			//First index is either NNCount Array (0), OP Array (1), OP^2 Array (2)
				//AKA Various Calculations that we will eventually need Simultaneously.

			//Second Index is the current Lipid, for this specifically: PSM (0), PDPC (1), CHL1 (2)

			//Third Index is the Comparing Lipid, same as second Index.

			//Fourth Index is the chain; Sn1 (0), Sn2 (1);

			//Fifth index is the value we are interested in (# of Neighbors, OP, OP^2).
				//The index indicates how many Comparing Lipid Neighbors they are.

			//There may be a better way to do this, but this is the simplest in terms of manageable code.
	

		//Preform calculations for each Frame.
		for (int i = 0; i < totalFiles; i++){
			Frame currentFrame = ReadFile.getFrame(i);

			calculateNN(currentFrame, searchRadius, ReadFile, lipidNames);
			averageOP(currentFrame, ReadFile);
			
			OPvNN = findOPvNN(currentFrame, OPvNN, lipidNames);

		}	//Ends for loop


		end = System.currentTimeMillis();
		totalTime = (end - start) / 1000;

		System.out.println("Finished Calculation in  " + totalTime + " seconds");		



		System.out.println("");
		System.out.println("Started Creating Output Files");
		start = System.currentTimeMillis();

		//Create the output Files	
		Readin.createHistogramFiles(OPvNN, lipidNames);
		Readin.createOPvNNFiles(OPvNN, lipidNames);

		end = System.currentTimeMillis();
		totalTime = (end - start) / 1000;
		System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	


		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
