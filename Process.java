//Processes Data and Interprets it.
//Processes very specific data files, check README for more information.

//Added this comment ot test gitHub

import java.util.Arrays;
import java.io.*;
import java.lang.Math;

public class Process implements Serializable {
	
	//Calculate Nearest Neighbors for a Frame that consists of many Points (Lipids).
	public static void generateNN(Frame currentFrame, double searchRadius, String[] lipidNames, boolean canLengthBeNegative){
	
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

				int shiftX = Mathematics.checkBoundary(x, xLength, searchRadius, canLengthBeNegative);
				int shiftY = Mathematics.checkBoundary(y, yLength, searchRadius, canLengthBeNegative);

				int totalLipids = lipidNames.length;
				int[] lipidCount = new int[totalLipids];

				for (int j = 0; j < length; j++){
					double x2 = currentFrame.allLipids[j].getX();
					double y2 = currentFrame.allLipids[j].getY();
					String Name2 = currentFrame.allLipids[j].getName();

					x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);
					y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);

					double radius = Mathematics.calculateRadius(x, y, x2, y2);

					if ((radius <= searchRadius) && (radius != 0)){
						for (int k = 0; k < totalLipids; k++){
							if (Name2.equals(lipidNames[k])) { lipidCount[k]++;}
						}	//Ends for loop
					}	//Ends if statement
				}	//Ends for loop

				for (int j = 0; j < totalLipids; j++){
					currentFrame.allLipids[i].setNN(j, lipidCount[j]);
				}	//Ends for loop
			}	//Ends for loop

			//We need to update the frame as we have just done a calculation and more importantly we want to save that calculation.
			Readin.serializeFrame("falseName", frame, currentFrame);

		}	//Ends else statement
	}	//Ends CalcualteNN Method

	//Use the method setOP to average the OP of all Atoms
	//Do this for every lipid, then save these calculation.
	public static double[][] generateOP_CG(Frame Frame, double[][] OP_CG, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		if (OPCalculated){
			//Do Nothing, you've already calculated the Average OP
		}	//Ends if statement

		else{
			int totalLipids = Frame.allLipids.length;
			int frameNumber = Frame.getFrameNumber();
			int totalLipidTypes = lipidNames.length;

			double xLength = Frame.getXLength();
			double yLength = Frame.getYLength();

			//Get the OP for this frame, avg it, then avg that over the total frames.
			double[][] frameOP_CG = new double[2][totalLipids];

			for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
				Frame.allLipids[currentLipid].setOP(xLength, yLength);


				String lipidName = Frame.allLipids[currentLipid].getName();
				int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);
	

		
				double firstOP = Frame.allLipids[currentLipid].getFirstOP();
				double secondOP = Frame.allLipids[currentLipid].getSecondOP();

				double totalChains = 2;
				if (secondOP == 0) { totalChains = 1; }

				double OP = (firstOP + secondOP) / totalChains;
	
				frameOP_CG[0][lipidNumber]++;
				frameOP_CG[1][lipidNumber] = frameOP_CG[1][lipidNumber] + OP;

			}	//Ends for loop


			//Avg OP for this frame.
			for (int currentLipid = 0; currentLipid < totalLipidTypes; currentLipid++){
				OP_CG[0][currentLipid]++;
				
				double OP = frameOP_CG[1][currentLipid] / frameOP_CG[0][currentLipid];
				
				OP_CG[1][currentLipid] = OP_CG[1][currentLipid] + OP;
				OP_CG[2][currentLipid] = OP_CG[2][currentLipid] + (OP * OP);

			}	//Ends for loop

			Readin.serializeFrame("falseName", frameNumber, Frame);

		}	//Ends else statement
	
		return OP_CG;
	}	//Ends AverageOP method



	//Each lipid has 2 chains, each chain has many Atoms. Each atom has an OP.
		//Use the method setOP to average the OP of all Atoms per Chain
		//Do this for every lipid, then save these calculation.
	public static double[][][][][] generateOP_AA(Frame Frame, double[][][][][] OP, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		if (OPCalculated){
			//Do Nothing, you've already calculated the Average OP
		}	//Ends if statement

		else{

			int length = Frame.allLipids.length;
			int frameNumber = Frame.getFrameNumber();
			int totalLipids = lipidNames.length;
			int totalChains = OP[0][0].length;
			int totalMembers = OP[0][0][0][0].length;
			int totalAtoms = 4;

			//The Array OP can be Defined as:
			//OP[ Count / OP / OP^2 ][ Lipid ID ][ Chain Number ][ Carbon / H1 / H2 / H3 ][ Carbon Index ]
			//We Want to do a system average for a single frame, then average all the frames togethor.

			double[][][][][] frameOP = new double[2][totalLipids][totalChains][totalAtoms][totalMembers];


			for (int i = 0; i < length; i++){
				Frame.allLipids[i].setOP(0,0);

				String lipidName = Frame.allLipids[i].getName();
				int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);
				Atom currentAtom = Frame.allLipids[i].firstChain;

				boolean keepGoing = true;
				int chainCount = 0;

				//Now we will Bin all the data in one spot so analysis can be done.
				while (keepGoing) {
					
					if (currentAtom == null) {
						chainCount++;
						currentAtom = Frame.allLipids[i].secondChain;						

						if (chainCount == 2) {
							keepGoing = false;
						}	//ends if statement

					}	//Ends if statement

					else {
						double currentOP = currentAtom.getOP();
						
						if (currentOP != 0) {
							int member = currentAtom.getMember();
							if (member == -1) { member = 0; }

							frameOP[0][lipidNumber][chainCount][0][member]++;
							frameOP[1][lipidNumber][chainCount][0][member] = frameOP[1][lipidNumber][chainCount][0][member] + currentOP;


							Atom hydrogenAtom = currentAtom.nextHydrogen;
							int currentHydrogen = 1;

							while (hydrogenAtom != null){
								currentOP = hydrogenAtom.getOP();
						
								frameOP[0][lipidNumber][chainCount][currentHydrogen][member]++;
								frameOP[1][lipidNumber][chainCount][currentHydrogen][member] = frameOP[1][lipidNumber][chainCount][currentHydrogen][member] + currentOP;
								currentHydrogen++;
								hydrogenAtom = hydrogenAtom.nextHydrogen;
							}	//Ends while loop
						}	//Ends if statement
					
						currentAtom = currentAtom.next;
					}	//ends else statement
				}	//Ends while loop
			}	//Ends for Loop

			Readin.serializeFrame("falseName", frameNumber, Frame);

			
			//Now average the array given and add it into the overall Array.

			for (int i = 0; i < totalLipids; i++){
				for (int j = 0; j < totalChains; j++){
					for (int k = 0; k < totalAtoms; k++){
						for (int h = 0; h < totalMembers; h++){

							double count = frameOP[0][i][j][k][h];

							OP[0][i][j][k][h] = OP[0][i][j][k][h] + count;

							double currentOP = frameOP[1][i][j][k][h] / count;

							if (currentOP != 0){
								OP[1][i][j][k][h] = OP[1][i][j][k][h] + currentOP;
								OP[2][i][j][k][h] = OP[2][i][j][k][h] + (currentOP * currentOP);

							}	//Ends if statement
						}	//Ends for loop
					}	//Ends for loop
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends else statement

		return OP;
	}	//Ends AverageOP method

	//Generate a Histogram of the given angle of a given C-H Bond.
	public static int[] generateAngleHistogram(Frame Frame, int[] Angles, String correctLipid, boolean firstChain, int carbonIndex){
		int totalLipids = Frame.allLipids.length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String Name = Frame.allLipids[currentLipid].getName();

			//Only look at one specific Lipid
			if (Name.equals(correctLipid)){
				Atom probingAtom;
				
				//Look at either the first or second chain.
				if (firstChain) { probingAtom = Frame.allLipids[currentLipid].firstChain; }
				else { probingAtom = Frame.allLipids[currentLipid].secondChain; }

				//Look at a specific carbon index.
				while (probingAtom.getMember() != carbonIndex) {
					probingAtom = probingAtom.next;
				}	//Ends while loop
				
				//Now, view every C-H Bond, and get the Angle of the bond by reverse engineering the OP associated with the approporiate Hydrogen.
				while (probingAtom != null) {
					if (probingAtom.Hydrogen != -1) {
						double Angle = Mathematics.reverseOP(probingAtom.OP);

						double newAngle = Angle + 180;	
						int index = (int) Math.round(newAngle * 10);
						
						Angles[index]++;
					}	//Ends if statement
					
					probingAtom = probingAtom.nextHydrogen;
				}	//Ends while loop
			}	//Ends if statement
		}	//Ends for loop	

		return Angles;
	}	//ends generateAngleHistogram



	public static double[][][][] generateOPvNN_CG(Frame Frame, double[][][][] OPvNN, String[] lipidNames){
		//The OPvNN Passed into the array is for all frames.	
			//Generate a frame specific OPvNN, avg it out and then add to the passed in OPvNN Array.

		int length = Frame.allLipids.length;
		int totalLipids = lipidNames.length;
		int totalNeighbors = OPvNN[0][0][0].length;

		double[][][][] frameOPvNN = new double[2][totalLipids][totalLipids][totalNeighbors];

		for (int lipid = 0; lipid < length; lipid++){
			String lipidName = Frame.allLipids[lipid].getName();
			int currentLipid = Mathematics.LipidToInt(lipidNames, lipidName);
			
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){

				int neighborIndex = Frame.allLipids[lipid].Neighbors[compLipid];

				double firstOP = Frame.allLipids[lipid].getFirstOP();
				double secondOP = Frame.allLipids[lipid].getSecondOP();

				double totalChains = 2;
				if (secondOP == 0) { totalChains = 1; }

				double OP = (firstOP + secondOP) / totalChains;
				

				//Add 1 to the NN Count.
				frameOPvNN[0][currentLipid][compLipid][neighborIndex]++;
				
				//Add the OP
				frameOPvNN[1][currentLipid][compLipid][neighborIndex] = frameOPvNN[1][currentLipid][compLipid][neighborIndex] + OP;

			}	//Ends for Loop
		}	//Ends for loop

		//Average the frame Specific Array and add the avg to the overall Frame array.
		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){
				for (int neighbor = 0; neighbor < totalNeighbors; neighbor++){

					double frameOP = frameOPvNN[1][currentLipid][compLipid][neighbor] / frameOPvNN[0][currentLipid][compLipid][neighbor];



					OPvNN[0][currentLipid][compLipid][neighbor] = OPvNN[0][currentLipid][compLipid][neighbor] + frameOPvNN[0][currentLipid][compLipid][neighbor];
					if (frameOP > 0){
						OPvNN[1][currentLipid][compLipid][neighbor] = OPvNN[1][currentLipid][compLipid][neighbor] + frameOP;
						OPvNN[2][currentLipid][compLipid][neighbor] = OPvNN[2][currentLipid][compLipid][neighbor] + (frameOP * frameOP);

					}	//Ends if statement
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return OPvNN;
	}	//Ends OPvNN



	//At this point every lipid should have an amount of Nearest Neighbors, and an Averaged OP.
		//Bin these data points such that we can average the OP for when there are specifically 2 (for example) Neighbors only.
		//This will be done in a large 5d array.
	public static double[][][][][] generateOPvNN_AA(Frame Frame, double[][][][][] OPvNN_AA, String[] lipidNames){

		//OPvNN Array can be Described as
		//OPvNN[ Count / OP / OP^2 ][ Current Lipid ][ Comparing Lipid ][ Chain ][ # of Neighbors ]
		
		//Create a frame Array and avg this and add it to the overall array.

		int totalLipids = lipidNames.length;
		int totalNeighbors = OPvNN_AA[0][0][0][0].length;
		int totalChains = 2;

		double[][][][][] frameOPvNN_AA = new double[2][totalLipids][totalLipids][totalChains][totalNeighbors];

		int length = Frame.allLipids.length;

		for (int i = 0; i < length; i++){
			String lipidName = Frame.allLipids[i].getName();
			int currentLipid = Mathematics.LipidToInt(lipidNames, lipidName);
			
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){

				int neighborIndex = Frame.allLipids[i].Neighbors[compLipid];

				
				double firstOP = Frame.allLipids[i].getFirstOP();
				double secondOP = Frame.allLipids[i].getSecondOP();

				double firstOPSquared = firstOP * firstOP;
				double secondOPSquared = secondOP * secondOP;

				//Add 1 to the NN Count.
				frameOPvNN_AA[0][currentLipid][compLipid][0][neighborIndex]++;
				frameOPvNN_AA[0][currentLipid][compLipid][1][neighborIndex]++;
				
				//Add the OP
				frameOPvNN_AA[1][currentLipid][compLipid][0][neighborIndex] = frameOPvNN_AA[1][currentLipid][compLipid][0][neighborIndex] + firstOP;
				frameOPvNN_AA[1][currentLipid][compLipid][1][neighborIndex] = frameOPvNN_AA[1][currentLipid][compLipid][1][neighborIndex] + secondOP;

			}	//Ends for Loop
		}	//Ends for loop

		//Now average the frame array and put it into the overall array.

		for (int i = 0; i < totalLipids; i++){
			for (int j = 0; j < totalLipids; j++){
				for (int k = 0; k < totalChains; k++){
					for (int h = 0; h < totalNeighbors; h++){
						double count = frameOPvNN_AA[0][i][j][k][h];
						double OP = frameOPvNN_AA[1][i][j][k][h] / count;
						if (OP > 0) {
							OPvNN_AA[0][i][j][k][h] = OPvNN_AA[0][i][j][k][h] + count;
							OPvNN_AA[1][i][j][k][h] = OPvNN_AA[1][i][j][k][h] + OP;
							OPvNN_AA[2][i][j][k][h] = OPvNN_AA[2][i][j][k][h] + (OP*OP);
						}	//Ends if Statemetn
					}	//Ends for loop
				}	//ends for loop
			}	//Ends for loop
		}	//Ends for loop


		return OPvNN_AA;
	}	//Ends OPvNN_AA


	public static double[][] generateOPHistogram(Frame currentFrame, double[][] OP_Histogram, String[] lipidNames){
		//First index is the lipid type, second index is the binned OP
		int totalLipids = currentFrame.allLipids.length;
		
		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String lipidName = currentFrame.allLipids[currentLipid].getName();
			int lipid = Mathematics.LipidToInt(lipidNames, lipidName);

			double firstOP = currentFrame.allLipids[currentLipid].getFirstOP();
			double secondOP = currentFrame.allLipids[currentLipid].getSecondOP();

			double totalChains = 2;
			if (secondOP == 0) { totalChains = 1; }

			double OP = (firstOP + secondOP) / totalChains;
		
			int index = (int) Math.round(OP * 2000);

			OP_Histogram[lipid][index]++;

		}	//Ends for loop
	
		return OP_Histogram;
	}	//ends generateOPHistogram

	public static int[][] generateThickness(Frame currentFrame, int[][] Thickness, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;
		double BilayerCenter = currentFrame.getBilayerCenter();

		for (int i = 0; i < totalLipids; i++){
			String lipidName = currentFrame.allLipids[i].getName();
			int lipid = Mathematics.LipidToInt(lipidNames, lipidName);
			double Z = currentFrame.allLipids[i].findPhosphateThickness();
			double newZ = Z - BilayerCenter;

			if (Z != 0){
				newZ = newZ + 100;
				int index = (int) Math.round(newZ * 10);
				Thickness[lipid][index]++;
			}	//Ends if statement
		}	//Ends for loop

		return Thickness;
	}	//Ends generateThickness 


	//PCL Stands for Project Chain Length, it a a measurement of the height of each carbon on a carbon chain.
		//For this program it can be a simple binning algorithm.
	public static double[][][][] generatePCL(Frame currentFrame, double[][][][] PCL, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;
		double BilayerCenter = currentFrame.getBilayerCenter();

		int totalLipidTypes = lipidNames.length;		
		int totalChains = 2;
		int totalCarbonIndex = PCL[0][0][0].length;
		double Z = 0;

		//The PCL Array can be Defined as
		// PCL[ Count / PCL / PCL^2 ][ Lipid ][ Chain ][ Carbon Index ]

		//Now average it for every frame and add this to an overall Array.

		double[][][][] framePCL = new double[2][totalLipidTypes][totalChains][totalCarbonIndex];

		for (int i = 0; i < totalLipids; i++){
			//Skip any lipid that does not have 2 chains.
				//Because of how chains are assigned we only need to check the second chain	
			if ((currentFrame.allLipids[i].secondChainIdentifier).equals("null")) {
				//Do Nothing, basically skip this lipid
			}	//ends if statement

			else {
				String lipidName = currentFrame.allLipids[i].getName();
				int currentLipid = Mathematics.LipidToInt(lipidNames, lipidName);
				Atom firstChain = currentFrame.allLipids[i].firstChain;
				Atom secondChain = currentFrame.allLipids[i].secondChain;


				while (firstChain != null){
					int Member = firstChain.getMember();
					Z = firstChain.Z;
					Z = Z - BilayerCenter;

					framePCL[0][currentLipid][0][Member]++;
					framePCL[1][currentLipid][0][Member] = framePCL[1][currentLipid][0][Member] + Z;

					firstChain = firstChain.next;
				}	//Ends while loop

				while (secondChain != null){
					int Member = secondChain.getMember();
					Z = secondChain.Z;
					Z = Z - BilayerCenter;

					framePCL[0][currentLipid][1][Member]++;
					framePCL[1][currentLipid][1][Member] = framePCL[1][currentLipid][1][Member] + Z;

					secondChain = secondChain.next;
				}	//Ends while loop
			}	//ends else statement
		}	//Ends for loop

		//Now average the frame and add it into the overall.

		for (int i = 0; i < totalLipidTypes; i++){
			for (int j = 0; j < totalChains; j++){
				for (int k = 0; k < totalCarbonIndex; k++){
					double count = framePCL[0][i][j][k];
					double currentPCL = framePCL[1][i][j][k] / count;

					PCL[0][i][j][k] = PCL[0][i][j][k] + count;
					PCL[1][i][j][k] = PCL[1][i][j][k] + currentPCL;
					PCL[2][i][j][k] = PCL[2][i][j][k] + (currentPCL * currentPCL);

				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop


		return PCL;
	}	//Ends calculate PCL

	//Prints out the progress of the System.
	public static long progressStatement(long givenTime, String Step){
		long totalTime = 0;		
		long currentTime = System.currentTimeMillis();
		boolean end = false;

		if (givenTime == 0){
			totalTime = currentTime;
		}	//ends if statement

		else {
			end = true;
			totalTime = (currentTime - givenTime) / 1000;
		}	//Ends else statement

		if (Step.equals("Start_Read")) {
			System.out.println("");
			System.out.println("----------------------------------");
			System.out.println("Initiated File Processor");
			System.out.println("");
			System.out.println("Started Reading Files");

		}	//Ends if statement

		else if (Step.equals("End_Read")){
			System.out.println("Finished Reading File in " + totalTime + " seconds.");
			System.out.println("");
		}	//Ends if statement

		else if (Step.equals("Start_Calculation")){
			System.out.println("Begun Various Calculations");
		}	//ends if statement

		else if (Step.equals("End_Calculation")){
			System.out.println("Finished Calculations in  " + totalTime + " seconds");		
			System.out.println("");
		}	//ends if statement

		else if (Step.equals("Start_Output")){
			System.out.println("Started Creating Output Files");
		}	//ends if statement

		else if (Step.equals("End_Output")){
			System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	
		}	//ends if statement

		if (end) { totalTime = 0; }

		return totalTime;
	}	//Ends progressStatement method



	public static void main(String[] args){
		boolean firstFrameOnly = false;
		int startingFrame = 0;
		int finalFrame = -1;

		double searchRadius = 10;
		int Neighbors = 20;
		long time = 0;

		boolean coarseGrained = false;

		String coordinateFile = "Coordinates.dat";

		//Determine if there is a specific set number of frames to use.
		if (args.length > 0){
			String CommandLineArguement = args[0];
			if (CommandLineArguement.equals("FirstFrame")) { firstFrameOnly = true; }
			else {
				//Set the beginning and end Frame
				startingFrame = Integer.parseInt(CommandLineArguement);
				finalFrame = Integer.parseInt(args[1]);
			 }	// Ends else statement
		}	//Ends if statement


		//Read an input file.
		time = progressStatement(0, "Start_Read");

		String[] lipidNames = Readin.findLipidNames(coordinateFile);
		int totalLipids = lipidNames.length;

		if (lipidNames[0].equals("null")){
			//There would be an error accessing the file, so the whole program must end.
		}	//Ends if statement

		else{
			int totalFiles = 0;
			String fileName = "Frames/frame_0.ser";

			//Create a bunch of serialized objects so that the memory usage won't be as great.
			//If the files already exist then find out how many there are.
			boolean filesExist = Readin.checkForFiles(fileName);
			if (!filesExist) {
				try{
					totalFiles = Readin.readFile(lipidNames, firstFrameOnly, coordinateFile);

				}	//Ends try Statement

				catch(FileNotFoundException ex){
					System.out.println("Could not find initial input file");
				}	//Ends catch statement
			}	//Ends if statement

			else{
				totalFiles = new File("Frames/").list().length;
			}	//Ends if statement

			//Determine what Method of Simulation is used so more accurate analysis can be done. Tell the user what we are assuming it is.
			coarseGrained = Readin.determineSimulationMethod();
			if (coarseGrained) { System.out.println("System is assumed to be Coarse-Grained"); }
			else { System.out.println("System is assumed to be Atomistic"); }

			time = progressStatement(time, "End_Read");
	

			//Now we will do the actual Analysis.
			time = progressStatement(time, "Start_Calculation");

			//Arrays for binning data so that it can be organized into a nice output file later.
			//The DOCUMENTATION.txt file describes each of these arrays in the methods they are used in.
			double[][][][][] OP_AA = new double[3][totalLipids][2][4][30];
			double[][] OP_CG = new double[3][totalLipids];
			double[][][][][] OPvNN_AA = new double[3][totalLipids][totalLipids][2][Neighbors];
			double[][][][] OPvNN_CG = new double[3][totalLipids][totalLipids][Neighbors];
			double[][] OP_Histogram = new double[totalLipids][2001];
			int[] Angle_Histogram_AA = new int[3601];
			int[][] Thickness = new int[totalLipids][2000];
			double[][][][] PCL = new double[3][totalLipids][2][30];

			if (firstFrameOnly) { totalFiles = 1; } //Allows us to skip a lot of work, this is a debugging tool.
			else if (finalFrame != -1) { totalFiles = finalFrame; }

			//Perform calculations for each Frame.
			for (int i = startingFrame; i < totalFiles; i++){
				Frame currentFrame = Readin.unserializeFrame(i);
				
				if (coarseGrained){
					generateNN(currentFrame, searchRadius, lipidNames, false);
					OP_CG = generateOP_CG(currentFrame, OP_CG, lipidNames);
					OPvNN_CG = generateOPvNN_CG(currentFrame, OPvNN_CG, lipidNames);
					OP_Histogram = generateOPHistogram(currentFrame, OP_Histogram, lipidNames);

				}	//ends if statemetn

				else {
					generateNN(currentFrame, searchRadius, lipidNames, true);
					OP_AA = generateOP_AA(currentFrame, OP_AA, lipidNames);
					OPvNN_AA = generateOPvNN_AA(currentFrame, OPvNN_AA, lipidNames);
					Thickness = generateThickness(currentFrame, Thickness, lipidNames);
					PCL = generatePCL(currentFrame, PCL, lipidNames);
					OP_Histogram = generateOPHistogram(currentFrame, OP_Histogram, lipidNames);
					Angle_Histogram_AA = generateAngleHistogram(currentFrame, Angle_Histogram_AA, "PSM", true, 3);

				}	//Ends else statement
			}	//Ends for loop
			time = progressStatement(time, "End_Calculation");
			
			//Now Create output files for graphing.
			time = progressStatement(time, "Start_Output");

			//We may have chosen to only look at specific frames, so lets modify how many we actually looked at for binning purposes.
			int totalReadFrames = totalFiles;
			if ((startingFrame != 0) || (finalFrame != -1)) {
				if ((startingFrame != 0) && (finalFrame == -1)){
					totalReadFrames = totalFiles - startingFrame;
				}	//Ends if statement

				else if ((startingFrame == 0) && (finalFrame != -1)){
					totalReadFrames = finalFrame;
				}	//Ends else if statement

				else if ((startingFrame != 0) && (finalFrame != -1)){
					totalReadFrames = finalFrame - startingFrame;
				}	//ends else if statement
			}	
		
			if (coarseGrained) {
				Readin.createOPvNNFiles_CG(OPvNN_CG, lipidNames, totalReadFrames);
				Readin.createNNFiles_CG(OPvNN_CG, lipidNames);
				Readin.createStandardDataFiles_CG(OPvNN_CG, OP_CG, lipidNames);
				Readin.createOPHistogramFiles(OP_Histogram, lipidNames);
			}	//Ends if statement

			else{
				Readin.createNNFiles_AA(OPvNN_AA, lipidNames);
				Readin.createOPFiles(OP_AA, lipidNames, totalReadFrames);
				Readin.createOPvNNFiles_AA(OPvNN_AA, lipidNames, totalReadFrames);
				Readin.createThicknessFiles(Thickness, lipidNames);
				Readin.createPCLFiles(PCL, lipidNames, totalReadFrames);
				Readin.createOPHistogramFiles(OP_Histogram, lipidNames);
				Readin.createAngleHistogramFile(Angle_Histogram_AA, "PSM", true, 3);
			}	//Ends else statement

			time = progressStatement(time, "End_Output");
		}	//Ends else statement
		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
