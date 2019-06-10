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
	public static void generateOP_CG(Frame Frame, String[] lipidNames){
		
		boolean OPCalculated = Frame.allLipids[0].checkForOP();

		if (OPCalculated){
			//Do Nothing, you've already calculated the Average OP
		}	//Ends if statement

		else{
			int totalLipids = Frame.allLipids.length;
			int frameNumber = Frame.getFrameNumber();

			double xLength = Frame.getXLength();
			double yLength = Frame.getYLength();

			for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
				Frame.allLipids[currentLipid].setOP(xLength, yLength);

			}	//Ends for loop

			Readin.serializeFrame("falseName", frameNumber, Frame);
			
		}	//Ends else statement
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

//		System.out.println(OP[0][0][0][0][5]);
//		System.out.println(OP[1][0][0][0][5]);
//		System.out.println("");

		return OP;
	}	//Ends AverageOP method



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
				
				double OPSquared = OP * OP;

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

						OPvNN_AA[0][i][j][k][h] = OPvNN_AA[0][i][j][k][h] + count;
						OPvNN_AA[1][i][j][k][h] = OPvNN_AA[1][i][j][k][h] + OP;
						OPvNN_AA[2][i][j][k][h] = OPvNN_AA[2][i][j][k][h] + (OP*OP);

					}	//Ends for loop
				}	//ends for loop
			}	//Ends for loop
		}	//Ends for loop


		return OPvNN_AA;
	}	//Ends OPvNN_AA

	public static int[][] generateThickness(Frame currentFrame, int[][] Thickness, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;

		for (int i = 0; i < totalLipids; i++){
			String lipidName = currentFrame.allLipids[i].getName();
			int lipid = Mathematics.LipidToInt(lipidNames, lipidName);
			double Z = currentFrame.allLipids[i].findPhosphateThickness();

			if (Z != 0){
				Z = Z + 100;
				int index = (int) Math.round(Z * 10);
				Thickness[lipid][index]++;
			}	//Ends if statement
		}	//Ends for loop

		return Thickness;
	}	//Ends generateThickness 


	//PCL Stands for Project Chain Length, it a a measurement of the height of each carbon on a carbon chain.
		//For this program it can be a simple binning algorithm.
	public static double[][][][] generatePCL(Frame currentFrame, double[][][][] PCL, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;

		int totalLipidTypes = lipidNames.length;		
		int totalChains = 2;
		int totalCarbonIndex = PCL[0][0][0].length;


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
					double Z = firstChain.Z;

					framePCL[0][currentLipid][0][Member]++;
					framePCL[1][currentLipid][0][Member] = framePCL[1][currentLipid][0][Member] + Z;

					firstChain = firstChain.next;
				}	//Ends while loop

				while (secondChain != null){
					int Member = secondChain.getMember();
					double Z = secondChain.Z;

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



	public static void main(String[] args){
		boolean firstFrameOnly = false;
//		boolean firstFrameOnly = true;
		double searchRadius = 10;
		int Neighbors = 20;

		//String coordinateFile = "/media/alex/Hermes/Anton/Coordinates.dat";
		String coordinateFile = "Coordinates.dat";
		//Gonna do the groundwork for the whole program, it will be a bit messy in this statement due to all the Console Ouput Messages.

		boolean coarseGrained = false;

		System.out.println("");
		System.out.println("----------------------------------");
		System.out.println("Initiated File Processor");
		System.out.println("");
		long start = System.currentTimeMillis();
		System.out.println("Started Reading Files");



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

			coarseGrained = Readin.determineSimulationMethod();
			
			if (coarseGrained) { System.out.println("System is assumed to be Coarse-Grained"); }
			else { System.out.println("System is assumed to be Atomistic"); }

			long end = System.currentTimeMillis();
			long totalTime = (end - start) / 1000;
			System.out.println("Finished Reading File in " + totalTime + " seconds.");
			System.out.println("");



			//Now we will do the actual Analysis.

			start = System.currentTimeMillis();
			System.out.println("Begun Various Calculations");

			double[][][][][] OP_AA = new double[3][totalLipids][2][4][30];
				//First Index is Count (0), Avg (1), Squared Average (2)
				//Second index is current Lipid
				//Third index is Chain One (0), Two (1),
				//Fourth index is the corresponding OP
					//Carbon/CarbonBead/CHOL OP (0)
					//H1,H2,H3 (1,2,3)
				//Fifth index is carbon index.


			//Create an array for calculating various things.
			double[][][][][] OPvNN_AA = new double[3][totalLipids][totalLipids][2][Neighbors];
				//First index is either NNCount Array (0), OP Array (1), OP^2 Array (2)
					//AKA Various Calculations that we will eventually need Simultaneously.

				//Second Index is the current Lipid, for this specifically: PSM (0), PDPC (1), CHL1 (2)

				//Third Index is the Comparing Lipid, same as second Index.

				//Fourth Index is the chain; Sn1 (0), Sn2 (1);

				//Fifth index is the value we are interested in (# of Neighbors, OP, OP^2).
					//The index indicates how many Comparing Lipid Neighbors they are.

				//There may be a better way to do this, but this is the simplest in terms of manageable code.
		
			double[][][][] OPvNN_CG = new double[3][totalLipids][totalLipids][Neighbors];
				//First is Count (0), OP (1), OP^2 (2)
				//Second Index is current Lipid
				//Third index is the comparing lipid
				//fourth index is the # of Neighbors of COmparign Lipid


			int[][] Thickness = new int[totalLipids][2000];
				//First index is the lipid we are interested in, if it doesn't have a Phosphate then it will not have a thicknes
				//Second Index are the bins.
					//Starting at -40 it goes to; -39.9, -39.8, -39.7, ... , 0 , 0.1, 0.2, ..., 39.9, 40.
			
			double[][][][] PCL = new double[3][totalLipids][2][30];
				//First index can be: Number of Occurance (0), PCL (1), or PCL^2 (2)
				//Second index is the current lipid
				//Third index is the chain, since there are only 2 chains we can assign an exact value
				//Fourht index relates to carbon index, we overestimate this just to be extremely safe.


			if (firstFrameOnly) { totalFiles = 1; } //Allows us to skip a lot of work, this is a debugging tool.

			//Preform calculations for each Frame.
			for (int i = 0; i < totalFiles; i++){
				Frame currentFrame = Readin.unserializeFrame(i);
				
				if (coarseGrained){
					generateNN(currentFrame, searchRadius, lipidNames, false);
					generateOP_CG(currentFrame, lipidNames);
					OPvNN_CG = generateOPvNN_CG(currentFrame, OPvNN_CG, lipidNames);

				}	//ends if statemetn

				else {
					generateNN(currentFrame, searchRadius, lipidNames, true);
					OP_AA = generateOP_AA(currentFrame, OP_AA, lipidNames);
					OPvNN_AA = generateOPvNN_AA(currentFrame, OPvNN_AA, lipidNames);
					Thickness = generateThickness(currentFrame, Thickness, lipidNames);
					PCL = generatePCL(currentFrame, PCL, lipidNames);
				}	//Ends else statement
			}	//Ends for loop


			end = System.currentTimeMillis();
			totalTime = (end - start) / 1000;
			System.out.println("Finished Calculation in  " + totalTime + " seconds");		
			System.out.println("");


			



			System.out.println("Started Creating Output Files");
			start = System.currentTimeMillis();
			
			if (coarseGrained) {
				Readin.createOPvNNFiles_CG(OPvNN_CG, lipidNames, totalFiles);
				Readin.createNNFiles_CG(OPvNN_CG, lipidNames);

			}	//Ends if statement

			else{
				Readin.createNNFiles_AA(OPvNN_AA, lipidNames);
				Readin.createOPFiles(OP_AA, lipidNames, totalFiles);
				Readin.createOPvNNFiles_AA(OPvNN_AA, lipidNames, totalFiles);
				Readin.createThicknessFiles(Thickness, lipidNames);
				Readin.createPCLFiles(PCL, lipidNames, totalFiles);
			}	//Ends else statement

			end = System.currentTimeMillis();

			totalTime = (end - start) / 1000;
			System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	

		}	//Ends else statement

		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
