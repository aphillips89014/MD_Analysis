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



	public static void calculateNN(Frame currentFrame, PrintStream output, PrintStream console, int searchRadius){
		
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

			//Output to a file
			System.setOut(output);

			System.out.println(frame + " " + Name + " " + i + " " + "PSM" + " " + PSM);
			System.out.println(frame + " " + Name + " " + i + " " + "PDPC" + " " + PDPC);
			System.out.println(frame + " " + Name + " " + i + " " + "CHL1" + " " + CHL1);

			System.setOut(console);

		}	//Ends for loop
	}	//Ends CalcualteNN Method


	public static void main(String[] args){
		Readin ReadFile = new Readin();
		String fileName = "Frames/frame_1.ser";
		int totalFiles = 0;


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



		PrintStream output = null;
		PrintStream console = null;

		try{
			output = new PrintStream(new File("NN.dat"));
			console = System.out;
		}	//Ends try Statement

		catch(Exception e){
			System.out.println("Error in Openning Data File");
			System.out.println(e);
			System.out.println("");

		}	//Ends catch statement


		
		long start = System.currentTimeMillis();
		System.out.println("Started Calculating NN");
	
		for (int i = 0; i < totalFiles; i++){
//			Frame currentFrame = ReadFile.getFrame(i);

//			calculateNN(currentFrame, output, console, 10);

//			System.out.println(currentFrame.getFrameNumber() + " " + currentFrame.allLipids[2].firstChainIdentifier);
//			currentFrame.allLipids[2].firstChain.printAllAtoms();
//			System.out.println("");

		}	//Ends for loop

		long end = System.currentTimeMillis();
		long totalTime = (end - start) / 1000;

		System.out.println("Finished NN Calculation in  " + totalTime + " seconds");		


	}	//Ends Main
}	//Ends Class Definition
