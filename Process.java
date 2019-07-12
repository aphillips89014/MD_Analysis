//Processes Data and Interprets it.
//Processes very specific data files, check README for more information.

//Added this comment ot test gitHub

import java.util.Scanner;
import java.util.Arrays;
import java.io.*;
import java.lang.Math;

public class Process implements Serializable {
	


	//Calculate Nearest Neighbors for a Frame that consists of many Points (Lipids).
	public static void generateNN(Frame currentFrame, double searchRadius, String[] lipidNames, boolean canLengthBeNegative){
	
		//NN May have been calculated and we are looking at an old set of objects, so let's skip the lengthy process of finding NN.
		boolean alreadyCalculated = currentFrame.allLipids[0].checkForNN();
	
		if (alreadyCalculated) {
			//Do nothing, this job has already been done.
		}	//Ends if statement

		else{
			int length = currentFrame.allLipids.length;
			int frame = currentFrame.getFrameNumber();	

			//Find the X and Y for a single point, compare it to every other point.
				//If it is within a given radius of the first point then it can be defined as a Neighbor, so add 1 to a counter.
			for (int i = 0; i < length; i++){
				double x = currentFrame.allLipids[i].getX();
				double y = currentFrame.allLipids[i].getY();
				String Name = currentFrame.allLipids[i].getName();
				String Leaflet = currentFrame.allLipids[i].getLeaflet();

				double xLength = currentFrame.getXLength();
				double yLength = currentFrame.getYLength();

				//There are periodic boundaries, so special things happen when a point is near one. Check for that here.
				int shiftX = Mathematics.checkBoundary(x, xLength, searchRadius, canLengthBeNegative);
				int shiftY = Mathematics.checkBoundary(y, yLength, searchRadius, canLengthBeNegative);

				int totalLipids = lipidNames.length;
				int[] lipidCount = new int[totalLipids];

				for (int j = 0; j < length; j++){
					String Leaflet2 = currentFrame.allLipids[j].getLeaflet();
					
					if (Leaflet.equals(Leaflet2)){

						double x2 = currentFrame.allLipids[j].getX();
						double y2 = currentFrame.allLipids[j].getY();
						String Name2 = currentFrame.allLipids[j].getName();

						//In case the points given are special and near a boundary, fully account for it here.
						x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);
						y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);

						double radius = Mathematics.calculateRadius(x, y, x2, y2);

						if ((radius <= searchRadius) && (radius != 0)){
							for (int k = 0; k < totalLipids; k++){
								if (Name2.equals(lipidNames[k])) { lipidCount[k]++;}
							}	//Ends for loop
						}	//Ends if statement
					}	//Ends if statement
				}	//Ends for loop

				//Save your findings.
				for (int j = 0; j < totalLipids; j++){
					currentFrame.allLipids[i].setNN(j, lipidCount[j]);
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends else statement
	}	//Ends CalcualteNN Method

	//Use the method setOP_CosTheta to average the OP of all Atoms
	//Do this for every lipid, then save these calculation.
	public static double[][][][] generateOP_CG(Frame Frame, double[][][][] OP_CG, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();		//Make sure we aren't looking at an old object that already has OP calculated.

		int totalLipids = Frame.allLipids.length;
		int frameNumber = Frame.getFrameNumber();
		int totalLipidTypes = lipidNames.length;

		double xLength = Frame.getXLength();
		double yLength = Frame.getYLength();

		//Get the OP for this frame, avg it, then avg that over the total frames.
		double[][][][] frameOP_CG = new double[2][2][3][totalLipids];

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			if (!(OPCalculated)){
				//If the OP Is not calculated then we should set it.
				Frame.allLipids[currentLipid].setOP_CosTheta(xLength, yLength);
			}	//ends if statement

			String lipidName = Frame.allLipids[currentLipid].getName();
			int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);
			int Leaflet = Mathematics.LeafletToInt(Frame.allLipids[currentLipid].getLeaflet());

			double firstOP = Frame.allLipids[currentLipid].getFirstOP();
			double secondOP = Frame.allLipids[currentLipid].getSecondOP();
			double avgOP = firstOP;

			//Save those OP in specific spots.
			frameOP_CG[0][Leaflet][0][lipidNumber]++;
			frameOP_CG[1][Leaflet][0][lipidNumber] = frameOP_CG[1][Leaflet][0][lipidNumber] + firstOP;;

			if (secondOP != 0) { 
				avgOP = (firstOP + secondOP) / 2; 

				frameOP_CG[0][Leaflet][1][lipidNumber]++;
				frameOP_CG[1][Leaflet][1][lipidNumber] = frameOP_CG[1][Leaflet][1][lipidNumber] + secondOP;;
			}	//Ends if statement
	
			frameOP_CG[0][Leaflet][2][lipidNumber]++;
			frameOP_CG[1][Leaflet][2][lipidNumber] = frameOP_CG[1][Leaflet][2][lipidNumber] + avgOP;;
		}	//Ends for loop

		//Place the OP for the frame into the OP of the entire time frame.
		for (int currentLipid = 0; currentLipid < totalLipidTypes; currentLipid++){
			for (int Leaflet = 0; Leaflet < 2; Leaflet++){
				for (int chain = 0; chain < 3; chain++){
					OP_CG[0][Leaflet][chain][currentLipid]++;
					
					//Might be NaN in some circumstances
					double OP = frameOP_CG[1][Leaflet][chain][currentLipid] / frameOP_CG[0][Leaflet][chain][currentLipid];
					
					OP_CG[1][Leaflet][chain][currentLipid] = OP_CG[1][Leaflet][chain][currentLipid] + OP;
					OP_CG[2][Leaflet][chain][currentLipid] = OP_CG[2][Leaflet][chain][currentLipid] + (OP * OP);
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return OP_CG;
	}	//Ends AverageOP method



	//Each lipid has 2 chains, each chain has many Atoms. Each atom has an OP.
		//Use the method setOP_CosTheta to average the OP of all Atoms per Chain
		//Do this for every lipid, then save these calculation.
	public static double[][][][][][] generateOP_AA(Frame Frame, double[][][][][][] OP, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		int length = Frame.allLipids.length;
		int frameNumber = Frame.getFrameNumber();
		int totalLipids = lipidNames.length;
		int totalChains = OP[0][0].length;
		int totalMembers = OP[0][0][0][0].length;
		int totalAtoms = 4;	//There are at most 4 atoms involved with a single carbon: Carbon itself, and 3 Seperate Hydrogen (typically just 2 hydrgogen)

		//The Array OP can be Defined as:
		//OP[ Count / OP / OP^2 ][ Leaflet ][ Lipid ID ][ Chain Number ][ Carbon / H1 / H2 / H3 ][ Carbon Index ]
		//We Want to do a system average for a single frame, then average all the frames togethor.

		double[][][][][][] frameOP = new double[2][2][totalLipids][totalChains][totalAtoms][totalMembers];


		for (int i = 0; i < length; i++){
			if (!(OPCalculated)) { 
				//This takes some time, so lets try to minimalize it.
				Frame.allLipids[i].setOP_CosTheta(0,0);
			}	//Ends if statement

			String lipidName = Frame.allLipids[i].getName();
			int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);
			int Leaflet = Mathematics.LeafletToInt(Frame.allLipids[i].getLeaflet());
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
					//Get the average for the Carbon Atom		
					if (currentOP != 0) {
						int member = currentAtom.getMember();
						if (member == -1) { member = 0; }

						frameOP[0][Leaflet][lipidNumber][chainCount][0][member]++;
						frameOP[1][Leaflet][lipidNumber][chainCount][0][member] = frameOP[1][Leaflet][lipidNumber][chainCount][0][member] + currentOP;


						Atom hydrogenAtom = currentAtom.nextHydrogen;
						int currentHydrogen = 1;

						//Get the average for the Hydrogen Atoms seperately.
						while (hydrogenAtom != null){
							currentOP = hydrogenAtom.getOP();
					
							frameOP[0][Leaflet][lipidNumber][chainCount][currentHydrogen][member]++;
							frameOP[1][Leaflet][lipidNumber][chainCount][currentHydrogen][member] = frameOP[1][Leaflet][lipidNumber][chainCount][currentHydrogen][member] + currentOP;
							currentHydrogen++;
							hydrogenAtom = hydrogenAtom.nextHydrogen;
						}	//Ends while loop
					}	//Ends if statement
				
					currentAtom = currentAtom.next;
				}	//ends else statement
			}	//Ends while loop
		}	//Ends for Loop

		//Now average the array given and add it into the overall Array.
		for (int L = 0; L < 2; L++){
			for (int i = 0; i < totalLipids; i++){
				for (int j = 0; j < totalChains; j++){
					for (int k = 0; k < totalAtoms; k++){
						for (int h = 0; h < totalMembers; h++){

							double count = frameOP[0][L][i][j][k][h];

							OP[0][L][i][j][k][h] = OP[0][L][i][j][k][h] + count;

							double currentOP = frameOP[1][L][i][j][k][h] / count;

							if (currentOP != 0){
								OP[1][L][i][j][k][h] = OP[1][L][i][j][k][h] + currentOP;
								OP[2][L][i][j][k][h] = OP[2][L][i][j][k][h] + (currentOP * currentOP);

							}	//Ends if statement
						}	//Ends for loop
					}	//Ends for loop
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

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
						double cosTheta = probingAtom.getCosTheta();
						double Angle = Math.acos(cosTheta) * (180/Math.PI);

						int index = (int) Math.round(Angle*20);
						
						Angles[index]++;
					}	//Ends if statement
					
					probingAtom = probingAtom.nextHydrogen;
				}	//Ends while loop
			}	//Ends if statement
		}	//Ends for loop	

		return Angles;
	}	//ends generateAngleHistogram


	//At this point every lipid should have an amount of Nearest Neighbors, and an Averaged OP.
		//Bin these data points such that we can average the OP for when there are specifically 2 (for example) Neighbors only.
		//This will be done in a large 5d array.
	public static double[][][][][] generateOPvNN(Frame Frame, double[][][][][] OPvNN, String[] lipidNames){

		//OPvNN Array can be Described as
		//OPvNN[ Count / OP / OP^2 ][ Current Lipid ][ Comparing Lipid ][ Chain ][ # of Neighbors ]
		
		//Create a frame Array and avg this and add it to the overall array.

		int totalLipids = lipidNames.length;
		int totalNeighbors = OPvNN[0][0][0][0].length;
		int totalChains = 2;

		double[][][][][] frameOPvNN = new double[2][totalLipids][totalLipids][totalChains][totalNeighbors];

		int length = Frame.allLipids.length;

		for (int i = 0; i < length; i++){
			String lipidName = Frame.allLipids[i].getName();
			int currentLipid = Mathematics.LipidToInt(lipidNames, lipidName);
			
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){

				int neighborIndex = Frame.allLipids[i].Neighbors[compLipid];
				
				double firstOP = Frame.allLipids[i].getFirstOP();
				double secondOP = Frame.allLipids[i].getSecondOP();
		
				//Add 1 to the NN Count.
				frameOPvNN[0][currentLipid][compLipid][0][neighborIndex]++;
				if (secondOP != -2) { frameOPvNN[0][currentLipid][compLipid][1][neighborIndex]++; }
				
				//Add the OP
				frameOPvNN[1][currentLipid][compLipid][0][neighborIndex] = frameOPvNN[1][currentLipid][compLipid][0][neighborIndex] + firstOP;
				if (secondOP != -2) { frameOPvNN[1][currentLipid][compLipid][1][neighborIndex] = frameOPvNN[1][currentLipid][compLipid][1][neighborIndex] + secondOP; }

			}	//Ends for Loop
		}	//Ends for loop

		//Now average the frame array and put it into the overall array.

		for (int i = 0; i < totalLipids; i++){
			for (int j = 0; j < totalLipids; j++){
				for (int k = 0; k < totalChains; k++){
					for (int h = 0; h < totalNeighbors; h++){
						double count = frameOPvNN[0][i][j][k][h];
						double OP = frameOPvNN[1][i][j][k][h] / count;

						if (OP > -2) {
							OPvNN[0][i][j][k][h] = OPvNN[0][i][j][k][h] + count;
							OPvNN[1][i][j][k][h] = OPvNN[1][i][j][k][h] + OP;
							OPvNN[2][i][j][k][h] = OPvNN[2][i][j][k][h] + (OP*OP);
						}	//Ends if statement
					}	//Ends for loop
				}	//ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return OPvNN;
	}	//Ends OPvNN


	public static double[][][] generateCosThetaHistogram(Frame currentFrame, double[][][] CosTheta_Histogram, String[] lipidNames){
		//First index is the lipid type, second index is the binned OP
		int totalLipids = currentFrame.allLipids.length;
		
		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String lipidName = currentFrame.allLipids[currentLipid].getName();
			int lipid = Mathematics.LipidToInt(lipidNames, lipidName);

			double firstCosTheta = currentFrame.allLipids[currentLipid].getFirstCosTheta();
			double secondCosTheta = currentFrame.allLipids[currentLipid].getSecondCosTheta();

			double avgCosTheta;
			avgCosTheta = (firstCosTheta + secondCosTheta) / 2;

			int firstIndex = (int) Math.round((firstCosTheta + 1) * 2000);
			CosTheta_Histogram[lipid][0][firstIndex]++;

			if (secondCosTheta < -1) {
				avgCosTheta = firstCosTheta;
			}	//Ends if statement
		
			else {
				int secondIndex = (int) Math.round((secondCosTheta + 1) * 2000);
				CosTheta_Histogram[lipid][1][secondIndex]++;
			}	//Ends else statemnt

			int avgIndex = (int) Math.round((avgCosTheta + 1) * 2000);
			CosTheta_Histogram[lipid][2][avgIndex]++;

		}	//Ends for loop
	
		return CosTheta_Histogram;
	}	//ends generateCosThetaHistogram

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

	public static String[] changeLipidNames(String[] lipidNames){
		Scanner userInput = new Scanner(System.in);
		int length = lipidNames.length;

		for (int i = 0; i < length; i++){
			System.out.println("");
			System.out.println("The current lipid is called: " + lipidNames[i]);
			System.out.println("Would you like to change it? (Y/N)");
			System.out.println("");
			String response = userInput.nextLine();
			
			if (response.equals("Y")){
				System.out.println("Please input the new name.");
				response = userInput.nextLine();
				System.out.println("");
				lipidNames[i] = response;

			}	//Ends if statement

			System.out.println("The final name for the current lipid is: " + lipidNames[i]);
			System.out.println("-----");

		}	//Ends for loop
		return lipidNames;
	}	//Ends changeLipidNames

	public static void main(String[] args){

		//Some starter variables
		int frameSeperator = 10;	//Set to be 10 by default, different numbers could bring better results based of the Hard Drive Specs.
		double searchRadius = 10; 	//Set to be 10 by default, can change in the future.
		int Neighbors = 20;		//Also set to be 20 by default

		//These shouldn't be changed.
		long time = 0;
		boolean coarseGrained = false;
		int startingFrame = 0;
		int finalFrame = -1;
		boolean userResponse = false;
		String coordinateFile = "Coordinates.dat";




		//Determine if there is a specific set number of frames to use.
		//This will be checked via command line arguements
		if (args.length > 0){
			String CommandLineArguement = args[0];

			//Set the beginning and end Frame
			startingFrame = Integer.parseInt(CommandLineArguement);
			finalFrame = Integer.parseInt(args[1]);
		}	//Ends if statement






		//Get user input, use this to see if we should ask for input in the future (Input will be requested to aide in file naming process. This is optional)
		Scanner userInput = new Scanner(System.in);
		System.out.println("Would you like to edit settings as the program runs? (Y/N)");
		String response = userInput.nextLine();
		
		if (response.equals("Y")) {
			System.out.println("User response will be prompted occasionally.");
			userResponse = true;
		}	//ends if statement




		//Now we will read an Input File and begin the program
		time = progressStatement(time, "Start_Read");	//This function will be used intermittenly to aide in console output
								//Could be removed, is purely for visual effects so if the program crashes it can be seen when.
		String[] lipidNames = Readin.findLipidNames(coordinateFile);	//The default names in the coordinate file for each lipid
		int totalLipids = lipidNames.length;				//The total number of unique lipids in the system

		if (lipidNames[0].equals("null")){
			//There would be an error accessing the file, so the whole program must end.
		}	//Ends if statement

		else{
			
			int totalFiles = 0;		//Will tell us the total number of files we have.
			String fileName = "Frames/frame_0.ser";

			boolean filesExist = Readin.checkForFiles(fileName);	//Make sure we haven't already read the file in the past.

			if (!filesExist) {
				try{
					totalFiles = Readin.readFile(lipidNames, finalFrame, frameSeperator, coordinateFile);
				}	//Ends try Statement
				catch(FileNotFoundException ex){
					System.out.println("Could not find initial input file");
				}	//Ends catch statement
			}	//Ends if statement

			else{
				totalFiles = Readin.findTotalFrames(frameSeperator);	
			}	//Ends if statement



			//Determine what Method of Simulation is used so more accurate analysis can be done. Tell the user what we are assuming it is.
			coarseGrained = Readin.determineSimulationMethod();
			if (coarseGrained) { System.out.println("System is assumed to be Coarse-Grained"); }
			else { System.out.println("System is assumed to be Atomistic"); }

			time = progressStatement(time, "End_Read");
			//File reading has finished, move on to Analysis.
	





			//Now we will do the actual Analysis.
			time = progressStatement(time, "Start_Calculation");

			//Arrays for binning data so that it can be organized into a nice output file later.
			double[][][][][][] OP_AA = new double[3][2][totalLipids][2][4][30];
			double[][][][] OP_CG = new double[3][2][3][totalLipids];
			double[][][][][] OPvNN = new double[3][totalLipids][totalLipids][2][Neighbors];
			double[][][] CosTheta_Histogram = new double[totalLipids][3][4001];
			int[] Angle_Histogram_AA = new int[3601];
			int[][] Thickness = new int[totalLipids][2000];
			double[][][][] PCL = new double[3][totalLipids][2][30];

			if (finalFrame != -1) { totalFiles = finalFrame; } //If we are given a set final frame, set it now.

			int FrameTracker = (startingFrame / frameSeperator) * frameSeperator;	//Little equation that forces Java to return a whole rounded number.
			Frame currentFrame = Readin.unserializeFrame(FrameTracker);		//Use that number to unserialze an object associated with that number.

			//Perform calculations for each Frame/File.
			while (currentFrame != null){
				while (currentFrame.frameNumber < startingFrame) { currentFrame = currentFrame.nextFrame; } //If we are given a start point, go there.

				//Analysis for Coarse-Grained Systems vs AA Systems are uniquely different, sometimes we need to differentiate.
				if (coarseGrained){
					generateNN(currentFrame, searchRadius, lipidNames, false);					//Find Nearest Neighbors
					OP_CG = generateOP_CG(currentFrame, OP_CG, lipidNames);						//Find Order Parameter
					OPvNN = generateOPvNN(currentFrame, OPvNN, lipidNames);						//Plot the previous 2
					CosTheta_Histogram = generateCosThetaHistogram(currentFrame, CosTheta_Histogram, lipidNames);	//Bin all Cos(Theta) values


				}	//ends if statemetn

				else {
					generateNN(currentFrame, searchRadius, lipidNames, true);					//Find Nearest Neighbors
					OP_AA = generateOP_AA(currentFrame, OP_AA, lipidNames);						//Find Order Parameter
					OPvNN = generateOPvNN(currentFrame, OPvNN, lipidNames);						//Plot the previous 2
					Thickness = generateThickness(currentFrame, Thickness, lipidNames);				//Bin all Thicknesses
					PCL = generatePCL(currentFrame, PCL, lipidNames);						//Bin all PCL
					CosTheta_Histogram = generateCosThetaHistogram(currentFrame, CosTheta_Histogram, lipidNames);	//Bin all Cos(Theta) values
					Angle_Histogram_AA = generateAngleHistogram(currentFrame, Angle_Histogram_AA, "PSM", true, 3);	//Bin all Angle Values

				}	//Ends else statement


				//Special if statement for when we reach the final frame
				if (currentFrame.frameNumber == (finalFrame-1)) {

					currentFrame = currentFrame.setFirstFrame();				//Go to beginning of LL
					Readin.serializeFrame("falseName", FrameTracker, currentFrame);		//Serialize all Changes Made

					currentFrame = null;							//End the entire while loop.
				}	//Ends if statement

				else {
					if (currentFrame.nextFrame != null) { currentFrame = currentFrame.nextFrame; }		//If not at the end of the LL, go next.
					else { 
						currentFrame = currentFrame.setFirstFrame();					//Go to beginning of LL
						Readin.serializeFrame("falseName", FrameTracker, currentFrame);			//Save/Serialize all Changes Made

						FrameTracker = FrameTracker + frameSeperator;					//Get number for next Frame Set
						currentFrame = Readin.unserializeFrame(FrameTracker);				//Unserialize next frame set.
					}	//Ends else statement
				}	//Ends else statement
			}	//Ends while loop

			time = progressStatement(time, "End_Calculation");
			//Analysis finished.			



			if (userResponse) { lipidNames = changeLipidNames(lipidNames); }	//If the user wanted to change the names of the files, they do it now.



			//Begin File Output
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
		
			//Create different files for the different simulation types.
			if (coarseGrained) {
				Readin.createOPvNNFiles(OPvNN, lipidNames, totalReadFrames);
				Readin.createNNFiles(OPvNN, lipidNames);
				Readin.createStandardDataFiles_CG(OPvNN, OP_CG, lipidNames);
				Readin.createCosThetaHistogramFiles(CosTheta_Histogram, lipidNames, coarseGrained);
			}	//Ends if statement

			else{
				Readin.createNNFiles(OPvNN, lipidNames);
				Readin.createOPFiles(OP_AA, lipidNames, totalReadFrames);
				Readin.createOPvNNFiles(OPvNN, lipidNames, totalReadFrames);
				Readin.createThicknessFiles(Thickness, lipidNames);
				Readin.createPCLFiles(PCL, lipidNames, totalReadFrames);
				Readin.createCosThetaHistogramFiles(CosTheta_Histogram, lipidNames, coarseGrained);
				Readin.createAngleHistogramFile(Angle_Histogram_AA, "PSM", true, 3);		//This needs to be set manually. (Too ambiguous)
			}	//Ends else statement

			time = progressStatement(time, "End_Output");
			//Output finished.
	
		}	//Ends else statement
		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
