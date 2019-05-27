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
	public static double calculateRadius(double firstX, double firstY, double secondX, double secondY){
		
		double xDiff = (firstX - secondX);
		double yDiff = (firstY - secondY);

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
	public static int checkBoundary(double point, double length, int searchRadius){

		int result = 0;
		double halfLength = length / 2;
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
	public static double applyPBC(double coordinate, int modifier, double length){
		//PBC stands for Periodic Boundary Condition
		//modifier can only be 1, -1, or 0.

		double result = coordinate;
	
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
				double x = currentFrame.allLipids[i].getX();
				double y = currentFrame.allLipids[i].getY();
				String Name = currentFrame.allLipids[i].getName();

				double xLength = currentFrame.getXLength();
				double yLength = currentFrame.getYLength();

				int shiftY = checkBoundary(x, xLength, searchRadius);
				int shiftX = checkBoundary(y, yLength, searchRadius);

				int totalLipids = lipidNames.length;
				int[] lipidCount = new int[totalLipids];

				for (int j = 0; j < length; j++){
					double x2 = currentFrame.allLipids[j].getX();
					double y2 = currentFrame.allLipids[j].getY();
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
	public static double[][][][][] getOP(Frame Frame, Readin Readin, double[][][][][] OP, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		if (OPCalculated){
			//Do Nothing, you've already calculated the Average OP
		}	//Ends if statement

		else{
			int length = Frame.allLipids.length;
			int frameNumber = Frame.getFrameNumber();
			int totalMembers = OP[0][0][0][0].length;
			int totalChains = OP[0][0].length;

			for (int i = 0; i < length; i++){
				Frame.allLipids[i].findOP();

				int lipidNumber = Frame.allLipids[i].getIntName(lipidNames);
				Atom carbonAtom = Frame.allLipids[i].firstChain;

				while (carbonAtom != null) {
					
					double currentOP = carbonAtom.getOP();

					if (currentOP != 0) {
						int member = carbonAtom.getMember();

						OP[0][lipidNumber][0][0][member]++;
						OP[1][lipidNumber][0][0][member] = OP[1][lipidNumber][0][0][member] + currentOP;
						OP[2][lipidNumber][0][0][member] = OP[2][lipidNumber][0][0][member] + (currentOP * currentOP);


						Atom hydrogenAtom = carbonAtom.nextHydrogen;
						int currentHydrogen = 1;

						while (hydrogenAtom != null){
							currentOP = hydrogenAtom.getOP();
					
							OP[0][lipidNumber][0][currentHydrogen][member]++;
							OP[1][lipidNumber][0][currentHydrogen][member] = OP[1][lipidNumber][0][currentHydrogen][member] + currentOP;
							OP[2][lipidNumber][0][currentHydrogen][member] = OP[2][lipidNumber][0][currentHydrogen][member] + (currentOP * currentOP);
							currentHydrogen++;
							hydrogenAtom = hydrogenAtom.nextHydrogen;
						}	//Ends while loop
					}	//Ends if statement
				
					carbonAtom = carbonAtom.next;

				}	//Ends while loop

				carbonAtom = Frame.allLipids[i].secondChain;

				while (carbonAtom != null) {
					
					double currentOP = carbonAtom.getOP();

					if (currentOP != 0) {
						int member = carbonAtom.getMember();

						OP[0][lipidNumber][1][0][member]++;
						OP[1][lipidNumber][1][0][member] = OP[1][lipidNumber][1][0][member] + currentOP;
						OP[2][lipidNumber][1][0][member] = OP[2][lipidNumber][1][0][member] + (currentOP * currentOP);


						Atom hydrogenAtom = carbonAtom.nextHydrogen;
						int currentHydrogen = 1;

						while (hydrogenAtom != null){
							currentOP = hydrogenAtom.getOP();
					
							OP[0][lipidNumber][1][currentHydrogen][member]++;
							OP[1][lipidNumber][1][currentHydrogen][member] = OP[1][lipidNumber][1][currentHydrogen][member] + currentOP;
							OP[2][lipidNumber][1][currentHydrogen][member] = OP[2][lipidNumber][1][currentHydrogen][member] + (currentOP * currentOP);
							currentHydrogen++;
							hydrogenAtom = hydrogenAtom.nextHydrogen;
						}	//Ends while loop
					}	//Ends if statement
				
					carbonAtom = carbonAtom.next;

				}	//Ends while loop




			}	//Ends for Loop

			Readin.serializeFrame("falseName", frameNumber, Frame);

		}	//Ends else statement

		return OP;
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

	public static void countLipids(Lipid[] allLipids, String[] lipidNames){
		
		int length = allLipids.length;
		int[] totalLipids = new int[lipidNames.length];

		for (int i = 0; i < length; i++){
			int currentLipid = allLipids[i].getIntName(lipidNames);
			totalLipids[currentLipid]++;

		}	//ends for loop

		System.out.println(Arrays.toString(totalLipids));

	}	//Ends countLipids Method


	public static int[][] calculateThickness(Frame currentFrame, int[][] Thickness, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;

		for (int i = 0; i < totalLipids; i++){
			int lipid = currentFrame.allLipids[i].getIntName(lipidNames);
			double Z = currentFrame.allLipids[i].getPhosphateThickness();

			if (Z != 0){
				Z = Z + 40;
				int index = (int) Math.round(Z * 10);
				Thickness[lipid][index]++;
			}	//Ends if statement
		}	//Ends for loop

		return Thickness;
	}	//Ends calculateThickness 


	//PCL Stands for Project Chain Length, it a a measurement of the height of each carbon on a carbon chain.
		//For this program it can be a simple binning algorithm.
	public static double[][][][] calculatePCL(Frame currentFrame, double[][][][] PCL, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;

		for (int i = 0; i < totalLipids; i++){
			//Skip any lipid that does not have 2 chains.
				//Because of how chains are assigned we only need to check the second chain	
			if ((currentFrame.allLipids[i].secondChainIdentifier).equals("null")) {
				//Do Nothing, basically skip this lipid
			}	//ends if statement

			else {
				int currentLipid = currentFrame.allLipids[i].getIntName(lipidNames);
				Atom firstChain = currentFrame.allLipids[i].firstChain;
				Atom secondChain = currentFrame.allLipids[i].secondChain;

				while (firstChain != null){
					int Member = firstChain.getMember();
					double Z = firstChain.Z;

					PCL[0][currentLipid][0][Member]++;
					PCL[1][currentLipid][0][Member] = PCL[1][currentLipid][0][Member] + Z;
					PCL[2][currentLipid][0][Member] = PCL[2][currentLipid][0][Member] + (Z * Z);

					firstChain = firstChain.next;
				}	//Ends while loop

				while (secondChain != null){
					int Member = secondChain.getMember();
					double Z = secondChain.Z;

					PCL[0][currentLipid][1][Member]++;
					PCL[1][currentLipid][1][Member] = PCL[1][currentLipid][1][Member] + Z;
					PCL[2][currentLipid][1][Member] = PCL[2][currentLipid][1][Member] + (Z * Z);

					secondChain = secondChain.next;
				}	//Ends while loop
			}	//ends else statement
		}	//Ends for loop

		return PCL;
	}	//Ends calculate PCL



	public static void main(String[] args){
		Readin ReadFile = new Readin();
		String fileName = "Frames/frame_0.ser";
		int totalFiles = 0;
		boolean firstFrameOnly = false;
	
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
				totalFiles = ReadFile.readFile(lipidNames, firstFrameOnly);

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

		double[][][][][] OP = new double[3][totalLipids][2][4][30];
			//First Index is Count (0), Avg (1), Squared Average (2)
			//Second index is current Lipid
			//Third index is Chain One (0), Two (1),
			//Fourth index is the corresponding OP
				//Carbon/CarbonBead/CHOL OP (0)
				//H1,H2,H3 (1,2,3)
			//Fifth index is carbon index.


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
	
		int[][] Thickness = new int[totalLipids][800];
			//First index is the lipid we are interested in, if it doesn't have a Phosphate then it will not have a thicknes
			//Second Index are the bins.
				//Starting at -40 it goes to; -39.9, -39.8, -39.7, ... , 0 , 0.1, 0.2, ..., 39.9, 40.
		
		double[][][][] PCL = new double[3][totalLipids][2][30];
			//First index can be: Number of Occurance (0), PCL (1), or PCL^2 (2)
			//Second index is the current lipid
			//Third index is the chain, since there are only 2 chains we can assign an exact value
			//Fourht index relates to carbon index, we overestimate this just to be extremely safe.



		if (firstFrameOnly) { totalFiles = 1; }

		//Preform calculations for each Frame.
		for (int i = 0; i < totalFiles; i++){
			Frame currentFrame = ReadFile.getFrame(i);

//			countLipids(currentFrame.allLipids, lipidNames);

			calculateNN(currentFrame, searchRadius, ReadFile, lipidNames);
			OP = getOP(currentFrame, ReadFile, OP, lipidNames);
			
			OPvNN = findOPvNN(currentFrame, OPvNN, lipidNames);
			Thickness = calculateThickness(currentFrame, Thickness, lipidNames);
			PCL = calculatePCL(currentFrame, PCL, lipidNames);

//			currentFrame.allLipids[0].getInformation();

		}	//Ends for loop

//		System.out.println(Arrays.toString(lipidNames));

		end = System.currentTimeMillis();
		totalTime = (end - start) / 1000;

		System.out.println("Finished Calculation in  " + totalTime + " seconds");		



		System.out.println("");
		System.out.println("Started Creating Output Files");
		start = System.currentTimeMillis();

		//Create the output Files	
		Readin.createHistogramFiles(OPvNN, lipidNames);
		Readin.createOrderParameterFiles(OP, lipidNames);
		Readin.createOPvNNFiles(OPvNN, lipidNames);
		Readin.createThicknessFiles(Thickness, lipidNames);
		Readin.createPCLFiles(PCL, lipidNames);
		end = System.currentTimeMillis();
		totalTime = (end - start) / 1000;
		System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	


		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
