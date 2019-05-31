//Processes Data and Interprets it.
//Processes very specific data files, check README for more information.


import java.util.Arrays;
import java.io.*;
import java.lang.Math;

public class Process implements Serializable {
	
	//Calculate Nearest Neighbors for a Frame that consists of many Points (Lipids).
	public static void generateNN(Frame currentFrame, int searchRadius, String[] lipidNames, boolean canLengthBeNegative){
	
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

				int shiftY = Mathematics.checkBoundary(x, xLength, searchRadius, canLengthBeNegative);
				int shiftX = Mathematics.checkBoundary(y, yLength, searchRadius, canLengthBeNegative);

				int totalLipids = lipidNames.length;
				int[] lipidCount = new int[totalLipids];

				for (int j = 0; j < length; j++){
					double x2 = currentFrame.allLipids[j].getX();
					double y2 = currentFrame.allLipids[j].getY();
					String Name2 = currentFrame.allLipids[j].getName();

					x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);
					y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);

					double radius = Mathematics.calculateRadius(x, y, x2, y2);

					if (radius <= searchRadius && radius != 0){
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
			int totalMembers = OP[0][0][0][0].length;
			int totalChains = OP[0][0].length;

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

							OP[0][lipidNumber][chainCount][0][member]++;
							OP[1][lipidNumber][chainCount][0][member] = OP[1][lipidNumber][chainCount][0][member] + currentOP;
							OP[2][lipidNumber][chainCount][0][member] = OP[2][lipidNumber][chainCount][0][member] + (currentOP * currentOP);


							Atom hydrogenAtom = currentAtom.nextHydrogen;
							int currentHydrogen = 1;

							while (hydrogenAtom != null){
								currentOP = hydrogenAtom.getOP();
						
								OP[0][lipidNumber][chainCount][currentHydrogen][member]++;
								OP[1][lipidNumber][chainCount][currentHydrogen][member] = OP[1][lipidNumber][chainCount][currentHydrogen][member] + currentOP;
								OP[2][lipidNumber][chainCount][currentHydrogen][member] = OP[2][lipidNumber][chainCount][currentHydrogen][member] + (currentOP * currentOP);
								currentHydrogen++;
								hydrogenAtom = hydrogenAtom.nextHydrogen;
							}	//Ends while loop
						}	//Ends if statement
					
						currentAtom = currentAtom.next;
					}	//ends else statement
				}	//Ends while loop
			}	//Ends for Loop

			Readin.serializeFrame("falseName", frameNumber, Frame);

		}	//Ends else statement

		return OP;
	}	//Ends AverageOP method



	public static double[][][][] generateOPvNN_CG(Frame Frame, double[][][][] OPvNN, String[] lipidNames){
		
		int length = Frame.allLipids.length;
		int totalLipids = OPvNN[0].length;

		for (int i = 0; i < length; i++){
			String lipidName = Frame.allLipids[i].getName();
			int currentLipid = Mathematics.LipidToInt(lipidNames, lipidName);
			
			for (int compLipid = 0; compLipid < totalLipids; compLipid++){

				int neighborIndex = Frame.allLipids[i].Neighbors[compLipid];

				double firstOP = Frame.allLipids[i].getFirstOP();
				double secondOP = Frame.allLipids[i].getSecondOP();

				double totalChains = 2;
				if (secondOP == 0) { totalChains = 1; }

				double OP = (firstOP + secondOP) / totalChains;
				
				double OPSquared = OP * OP;


				//Add 1 to the NN Count.
				OPvNN[0][currentLipid][compLipid][neighborIndex]++;
				
				//Add the OP
				OPvNN[1][currentLipid][compLipid][neighborIndex] = OPvNN[1][currentLipid][compLipid][neighborIndex] + OP;

				//Add the OP^2
				OPvNN[2][currentLipid][compLipid][neighborIndex] = OPvNN[2][currentLipid][compLipid][neighborIndex] + OPSquared;

			}	//Ends for Loop
		}	//Ends for loop

		return OPvNN;
	}	//Ends OPvNN



	//At this point every lipid should have an amount of Nearest Neighbors, and an Averaged OP.
		//Bin these data points such that we can average the OP for when there are specifically 2 (for example) Neighbors only.
		//This will be done in a large 5d array.
	public static double[][][][][] generateOPvNN_AA(Frame Frame, double[][][][][] OPvNN_AA, String[] lipidNames){
		
		int length = Frame.allLipids.length;
		int totalLipids = OPvNN_AA[0].length;

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
				OPvNN_AA[0][currentLipid][compLipid][0][neighborIndex]++;
				OPvNN_AA[0][currentLipid][compLipid][1][neighborIndex]++;
				
				//Add the OP
				OPvNN_AA[1][currentLipid][compLipid][0][neighborIndex] = OPvNN_AA[1][currentLipid][compLipid][0][neighborIndex] + firstOP;
				OPvNN_AA[1][currentLipid][compLipid][1][neighborIndex] = OPvNN_AA[1][currentLipid][compLipid][1][neighborIndex] + secondOP;

				//Add the OP^2
				OPvNN_AA[2][currentLipid][compLipid][0][neighborIndex] = OPvNN_AA[2][currentLipid][compLipid][0][neighborIndex] + firstOPSquared;
				OPvNN_AA[2][currentLipid][compLipid][1][neighborIndex] = OPvNN_AA[2][currentLipid][compLipid][1][neighborIndex] + secondOPSquared;

			}	//Ends for Loop
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
				Z = Z + 40;
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
		boolean firstFrameOnly = false;
//		boolean firstFrameOnly = true;
		int searchRadius = 10;
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
			double[][][][][] OPvNN_AA = new double[3][totalLipids][totalLipids][2][20];
				//First index is either NNCount Array (0), OP Array (1), OP^2 Array (2)
					//AKA Various Calculations that we will eventually need Simultaneously.

				//Second Index is the current Lipid, for this specifically: PSM (0), PDPC (1), CHL1 (2)

				//Third Index is the Comparing Lipid, same as second Index.

				//Fourth Index is the chain; Sn1 (0), Sn2 (1);

				//Fifth index is the value we are interested in (# of Neighbors, OP, OP^2).
					//The index indicates how many Comparing Lipid Neighbors they are.

				//There may be a better way to do this, but this is the simplest in terms of manageable code.
		
			double[][][][] OPvNN_CG = new double[3][totalLipids][totalLipids][20];
				//First is Count (0), OP (1), OP^2 (2)
				//Second Index is current Lipid
				//Third index is the comparing lipid
				//fourth index is the # of Neighbors of COmparign Lipid


			int[][] Thickness = new int[totalLipids][800];
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

				}	//Do Stuff

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
				Readin.createOPvNNFiles_CG(OPvNN_CG, lipidNames);
				Readin.createNNFiles_CG(OPvNN_CG, lipidNames);

				System.out.println("CG");
			}	//Ends if statement

			else{
				Readin.createNNFiles_AA(OPvNN_AA, lipidNames);
				Readin.createOPFiles(OP_AA, lipidNames);
				Readin.createOPvNNFiles_AA(OPvNN_AA, lipidNames);
				Readin.createThicknessFiles(Thickness, lipidNames);
				Readin.createPCLFiles(PCL, lipidNames);
			}	//Ends else statement

			end = System.currentTimeMillis();

			totalTime = (end - start) / 1000;
			System.out.println("Finished Creating Output Files in " + totalTime + " seconds");	

		}	//Ends else statement

		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
