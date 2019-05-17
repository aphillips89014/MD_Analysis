//Processes Data and Interprets it.

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


	//Shift valid point over by the given Length value
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



	public static void calculateNN(Frame currentFrame, int searchRadius, Readin tempReadin){
	
		boolean alreadyCalculated = currentFrame.allLipids[0].checkForNN();
	
		if (alreadyCalculated) {
			//Do nothing, this job has already been done.
		}	//Ends if statement

		else{
			int length = currentFrame.allLipids.length;
			int frame = currentFrame.getFrameNumber();	

			for (int i = 0; i < length; i++){
				float x = currentFrame.allLipids[i].getX();
				float y = currentFrame.allLipids[i].getY();
				String Name = currentFrame.allLipids[i].getName();

				float xLength = currentFrame.getXLength();
				float yLength = currentFrame.getYLength();

				int shiftY = checkBoundary(x, xLength, searchRadius);
				int shiftX = checkBoundary(y, yLength, searchRadius);


				int PSM = 0;
				int PDPC = 0;
				int CHL1 = 0;

				for (int j = 0; j < length; j++){
					float x2 = currentFrame.allLipids[j].getX();
					float y2 = currentFrame.allLipids[j].getY();
					String Name2 = currentFrame.allLipids[j].getName();


					x2 = applyPBC(x2, shiftX, xLength);
					y2 = applyPBC(y2, shiftY, yLength);				


					double radius = calculateRadius(x, y, x2, y2);

					if (radius <= searchRadius && radius != 0){
						if (Name2.equals("PSM")) { PSM++; }
						else if (Name2.equals("PDPC")) { PDPC++; }
						else if (Name2.equals("CHL1")) { CHL1++; }
					}	//Ends if statement
				}	//Ends for loop


				currentFrame.allLipids[i].assignNN(0, PSM);
				currentFrame.allLipids[i].assignNN(1, PDPC);
				currentFrame.allLipids[i].assignNN(2, CHL1);
				
			}	//Ends for loop

			tempReadin.serializeFrame("falseName", frame, currentFrame);

		}	//Ends else statement
	}	//Ends CalcualteNN Method


	//Take the plateau of a lipid of both chains, then average it and set it as a property of the lipid and re-serialize it.
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


	public static double[][][][][] findOPvNN(Frame Frame, double[][][][][] OPvNN){
		
		int length = Frame.allLipids.length;
		for (int i = 0; i < length; i++){
			int currentLipid = Frame.allLipids[i].getIntName();
			
			for (int compLipid = 0; compLipid < 3; compLipid++){

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

		System.out.println("");
		System.out.println("----------------------------------");
		System.out.println("Initiated File Processor");
		System.out.println("");


		//Create a bunch of serialized objects so that the memory usage won't be as great.
		//If the files already exist then find out how many there are.
		boolean filesExist = checkForFiles(fileName);
		if (!filesExist) {
			try{
				totalFiles = ReadFile.readFile();

			}	//Ends try Statement

			catch(FileNotFoundException ex){
				System.out.println("Could not find initial input file");
			}	//Ends catch statement

		}	//Ends if statement

		else{
			totalFiles = new File("Frames/").list().length;
		}	//Ends if statement


		System.out.println("");


		long start = System.currentTimeMillis();
		System.out.println("Begun Various Calculations");

		//Create an array for calculating various things.
		double[][][][][] OPvNN = new double[3][3][3][2][20];
			//Let's describe this 4-d array.
			//First index is either NNCount Array (0), OP Array (1), OP^2 Array (2)
			//Second Index is the current Lipid, for this specifically: PSM (0), PDPC (1), CHL1 (2)
			//Third Index is the Comparing Lipid, same as before.
			//Fourth Index is the chain; Sn1 (0), Sn2 (1);
			//Fifth index is the value we are interested in (# of Neighbors, OP, OP^2).
				//The index indicates how many Comparing Lipid Neighbors they are.

			//There may be a better way to do this, but this is the simplest in terms of manageable code.
	
		for (int i = 0; i < totalFiles; i++){
			Frame currentFrame = ReadFile.getFrame(i);

			calculateNN(currentFrame, 10, ReadFile);
			averageOP(currentFrame, ReadFile);
			
			OPvNN = findOPvNN(currentFrame, OPvNN);

//			currentFrame.allLipids[0].getInformation();


		}	//Ends for loop


		long end = System.currentTimeMillis();
		long totalTime = (end - start) / 1000;

		System.out.println("Finished Calculation in  " + totalTime + " seconds");		

		System.out.println("");
		System.out.println("Started Creating Output Files");

		start = System.currentTimeMillis();
	
		Readin.createHistogramFiles(OPvNN);
		Readin.createOPvNNFiles(OPvNN);

		end = System.currentTimeMillis();
		totalTime = (end - start) / 1000;
		System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	


		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
