//Processes Data and Interprets it.

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
			int totalLipids = currentFrame.allLipids.length;		//Total Lipids in the sample

			//Find the X and Y for a single point, compare it to every other point.
				//If it is within a given radius of the first point then it can be defined as a Neighbor, so add 1 to a counter.
			for (int lipid1 = 0; lipid1 < totalLipids; lipid1++){			//Iterate over all Lipids
				double x1 = currentFrame.allLipids[lipid1].getX();			//X Coordinate, to be used to calculate radius later on.
				double y1 = currentFrame.allLipids[lipid1].getY();			//Y Coordinate, to be used to calculate radius later on.
				boolean Leaflet1 = currentFrame.allLipids[lipid1].getLeaflet();		//Upper/Lower Leafter (T/F)

				double xLength = currentFrame.getXLength();				//X And Y Length for the frame itself, helps with precise PBC.
				double yLength = currentFrame.getYLength();

				//There are periodic boundaries, so special things happen when a point is near one. Check for that here.
				int shiftX = Mathematics.checkBoundary(x1, xLength, searchRadius, canLengthBeNegative);
				int shiftY = Mathematics.checkBoundary(y1, yLength, searchRadius, canLengthBeNegative);

				int totalLipidTypes = lipidNames.length;				//Find # of unique lipid species
				int[] lipidCount = new int[totalLipidTypes];				//Array for tracking # of neighbors found for unique species

				for (int lipid2 = 0; lipid2 < totalLipids; lipid2++){			//Iterate over all Lipids
					boolean Leaflet2 = currentFrame.allLipids[lipid2].getLeaflet();		
					
					if (Leaflet1 == Leaflet2){						//Leaflets need to be the same.

						double x2 = currentFrame.allLipids[lipid2].getX();
						double y2 = currentFrame.allLipids[lipid2].getY();
						String Name2 = currentFrame.allLipids[lipid2].getName();		//Find out which lipid we are looking at.
						int lipidType = Mathematics.LipidToInt(lipidNames, Name2);		//Find out the int associted w/ that lipid.

						//In case the points given are special and near a boundary, fully account for it here.
						x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);
						y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);

						double radius = Mathematics.calculateRadius(x1, y1, x2, y2);

						if ((radius <= searchRadius) && (radius != 0)){		//If it is within the search radius, and not itself.
							lipidCount[lipidType]++;			//Add one to a counter.
						}	//Ends if statement
					}	//Ends if statement
				}	//Ends for loop

				//Save the counter we just found in the lipid we found it for.
				for (int lipidType = 0; lipidType < totalLipidTypes; lipidType++){
					currentFrame.allLipids[lipid1].setNN(lipidType, lipidCount[lipidType]);
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends else statement
	}	//Ends CalcualteNN Method



	public static int[][][] generateRegistration(Frame currentFrame, int[][][] Registration, String[] lipidNames, double searchRadius, boolean canLengthBeNegative){
	//Go through every lipid in the upper leaflet only and see if the lower leaflet around the same spot holds the same Lipid Type (Determined by Name)

		int totalLipids = currentFrame.allLipids.length;		//Total Lipids in the sample
		int frameNumber = currentFrame.getFrameNumber();		//Current Frame, (To be used to generate a graph over time)

		//Registration[ Total Count / Registered Count ][ Frame Number ][ Lipid ]

		for (int lipid1 = 0; lipid1 < totalLipids; lipid1++){			//Iterate over all lipids
			if (currentFrame.allLipids[lipid1].getLeaflet() == true) {		//If it's in the upper leaflet

				int lipidSpecies1 = Mathematics.LipidToInt(lipidNames, currentFrame.allLipids[lipid1].getName());// # Associated w/ the lipid species.
				double x1 = currentFrame.allLipids[lipid1].getX();			//X Coordinate, to be used in radius calculation later.
				double y1 = currentFrame.allLipids[lipid1].getY();			

				double xLength = currentFrame.getXLength();
				double yLength = currentFrame.getYLength();

				//There are periodic boundaries, so special things happen when a point is near one.
				int shiftX = Mathematics.checkBoundary(x1, xLength, searchRadius, canLengthBeNegative);
				int shiftY = Mathematics.checkBoundary(y1, yLength, searchRadius, canLengthBeNegative);


				for (int lipid2 = 0; lipid2 < totalLipids; lipid2++){	//Iterate over all Lipids
					if (currentFrame.allLipids[lipid2].getLeaflet() == false) {	//If it's in the lower leaflet

						int lipidSpecies2 = Mathematics.LipidToInt(lipidNames, currentFrame.allLipids[lipid2].getName());
						double x2 = currentFrame.allLipids[lipid2].getX();
						double y2 = currentFrame.allLipids[lipid2].getY();

						x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);	//Apply PBC
						y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);
						
						double radius = Mathematics.calculateRadius(x1, y1, x2, y2);		//Calculate Radius

						if (radius <= 50) {
							Registration[0][frameNumber][lipidSpecies1]++;			//Save that this calculation occured
							
							if (lipidSpecies1 == lipidSpecies2) {				//If the species are identical
								Registration[1][frameNumber][lipidSpecies1]++;			//Count it as Registered.
							}	//Ends if statement
						}	//Ends if statement
					}	//Ends if statement
				}	//Ends for loop
			}	//Ends if statement
		}	//Ends for loop

		return Registration;
	}	//Ends generateRegistration Method


	public static double[][][][][] generateDipoleField(Frame currentFrame, double[][][][][] DipoleField, String[] lipidNames, double searchRadius, boolean canLengthBeNegative, double Spacing){
		//Compare the vectors generated by the head groups of two lipids and take the dot product of both and save this.

		int totalLipids = currentFrame.allLipids.length;	//Total Lipids in the Sample
		int totalLipidTypes = lipidNames.length;		//Total Species in the sample
		int totalSpaces = DipoleField[0][0][0][0].length;	//Total given spaces to assign

		double[][][][][] frameDipoleField = new double[2][2][totalLipidTypes][totalLipidTypes][totalSpaces];	//Get the avg for the Frame, then add overall.
		//DipoleField[ Count / Dipole / Dipole^2 ][ Leaflet ][ Lipid ][ Comparting Lipid ][ Radial Distance ]

		double xLength = currentFrame.getXLength();		//To be used for accounting for PBC
		double yLength = currentFrame.getYLength();
	
		boolean[] validDipoles = new boolean[totalLipids];	//Use this to store all valid dipoles, to be used later on (save some time).

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){			//Iterate through all Lipids
			boolean validDipole = currentFrame.allLipids[currentLipid].checkForDipole(false);	//Check to see if it has a valid dipole
			validDipoles[currentLipid] = validDipole;						//Save the result

			if (validDipole){
				validDipole = currentFrame.allLipids[currentLipid].checkForDipole(true);	//Check to see if the valid dipole has been calcualted.
				if (!(validDipole)){
					currentFrame.allLipids[currentLipid].setDipoleVector();			//If it hasn't, calculate it.
				}	//Ends if statement
			}	//Ends if statemetn
		}	//Ends for loop

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){	//Iterate through all Lipids
			boolean validDipole = validDipoles[currentLipid];			//Check to see if it has a vlaid dipole

			if (validDipole){
				int lipid1 = Mathematics.LipidToInt(lipidNames, currentFrame.allLipids[currentLipid].getName());	// # for specific Lipid Species
				int Leaflet1 = Mathematics.LeafletToInt(currentFrame.allLipids[currentLipid].getLeaflet());		// # For Specicfic Leaflet
				
				double x1 = currentFrame.allLipids[currentLipid].X;							//X and Y Value
				double y1 = currentFrame.allLipids[currentLipid].Y;							//Used to calculate Radius

				double[] dipole1 = currentFrame.allLipids[currentLipid].getDipole();					//Get Dipole Vector

				int shiftX = Mathematics.checkBoundary(x1, xLength, searchRadius, canLengthBeNegative);			//Account for PBC
				int shiftY = Mathematics.checkBoundary(y1, yLength, searchRadius, canLengthBeNegative);

				for (int compLipid = 0; compLipid < totalLipids; compLipid++){ 	//Iterate through all Lipids
					validDipole = validDipoles[compLipid];				//Check to see if it has a valid dipole
					if (validDipole){
						int Leaflet2 = Mathematics.LeafletToInt(currentFrame.allLipids[currentLipid].getLeaflet());	//# for Specific Leaflet
						if (Leaflet1 == Leaflet2){									//If Same Leaflet

							int lipid2 = Mathematics.LipidToInt(lipidNames, currentFrame.allLipids[compLipid].getName());
							double x2 = currentFrame.allLipids[compLipid].X;
							double y2 = currentFrame.allLipids[compLipid].Y;
							double[] dipole2 = currentFrame.allLipids[compLipid].getDipole();		//Get Dipole Vector

							x2 = Mathematics.applyPBC(x2, shiftX, xLength, canLengthBeNegative);		//Account for PBC
							y2 = Mathematics.applyPBC(y2, shiftY, yLength, canLengthBeNegative);

							double radius = Mathematics.calculateRadius(x1, y1, x2, y2);			//Calculate radius

							if ((radius <= searchRadius) && (radius != 0)) {	//If within a Search Radius and not itself
								double dotProduct = Mathematics.calculateDotProduct(dipole1, dipole2);	//Dot Product of 2 Vectors.
								int radialIndex = (int) (radius / Spacing);		//Quik equation for an index of radiual distance
							
								frameDipoleField[0][Leaflet1][lipid1][lipid2][radialIndex]++;
								frameDipoleField[1][Leaflet1][lipid1][lipid2][radialIndex] = frameDipoleField[1][Leaflet1][lipid1][lipid2][radialIndex] + dotProduct;
							}	//Ends if statement
						}	//Ends if statement
					}	//Ends if statement
				}	//Ends for loop
			}	//Ends if statement
		}	//Ends for loop

		//Now go through the frame speciifc dipoleField and average to add across the entire system for an accurate Std. Deviation
		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			for (int lipid = 0; lipid < totalLipidTypes; lipid++){
				for (int compLipid = 0; compLipid < totalLipidTypes; compLipid++){
					for (int radial = 0; radial < totalSpaces; radial++){
						double count = frameDipoleField[0][Leaflet][lipid][compLipid][radial];
						double dipole = frameDipoleField[1][Leaflet][lipid][compLipid][radial];
	
						if (count == 0) { count = 1; }	//Prevent NaN from occuring, instead return 0.
						double dipoleAvg = dipole / count;
	
						DipoleField[0][Leaflet][lipid][compLipid][radial] = DipoleField[0][Leaflet][lipid][compLipid][radial] + count;
						DipoleField[1][Leaflet][lipid][compLipid][radial] = DipoleField[1][Leaflet][lipid][compLipid][radial] + dipoleAvg;
						DipoleField[2][Leaflet][lipid][compLipid][radial] = DipoleField[2][Leaflet][lipid][compLipid][radial] + (dipoleAvg * dipoleAvg);
					}	//Ends for loop
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return DipoleField;
	}	//Ends generateDipoleField Method


	//Calculate the OP for a lipid if needed, then/or bin it into an appropriate spot.
	public static double[][][][] generateOP_CG(Frame currentFrame, double[][][][] OP_CG, String[] lipidNames){
		
		boolean OPCalculated = currentFrame.allLipids[0].checkForOP();		//Make sure we aren't looking at an old object that already has OP calculated.

		int totalLipids = currentFrame.allLipids.length;			
		int frameNumber = currentFrame.getFrameNumber();
		int totalLipidTypes = lipidNames.length;

		double xLength = currentFrame.getXLength();
		double yLength = currentFrame.getYLength();

		//Get the OP for this frame, avg it, then avg that over the total frames.
		double[][][][] frameOP_CG = new double[2][2][3][totalLipids];

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			if (!(OPCalculated)){
				currentFrame.allLipids[currentLipid].setOP_CosTheta(xLength, yLength);		//If OP isn't calculated, then calculate it.
			}	//ends if statement

			String lipidName = currentFrame.allLipids[currentLipid].getName();
			int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);			//Integer equivalent of a unique lipid
			int Leaflet = Mathematics.LeafletToInt(currentFrame.allLipids[currentLipid].getLeaflet());	//Integer form of Leaflet

			double firstOP = currentFrame.allLipids[currentLipid].getFirstOP();				//First Chain OP
			double secondOP = currentFrame.allLipids[currentLipid].getSecondOP();				//Second Chain OP
			double avgOP = firstOP;									//Avg OP

			//Save those OP in specific spots.
			frameOP_CG[0][Leaflet][0][lipidNumber]++;
			frameOP_CG[1][Leaflet][0][lipidNumber] = frameOP_CG[1][Leaflet][0][lipidNumber] + firstOP;;

			//If the second OP is 2 then it is Cholesterol or ATOC, because these only have one 'chain'.
			if (secondOP != -2) { 
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
	public static double[][][][][][] generateOP_AA(Frame currentFrame, double[][][][][][] OP, String[] lipidNames){
		
		boolean OPCalculated = currentFrame.allLipids[0].checkForOP();

		int totalLipids = currentFrame.allLipids.length;			//Total Lipids
		int frameNumber = currentFrame.getFrameNumber();
		int totalLipidTypes = lipidNames.length;			//Total Lipids Types
		int totalChains = OP[0][0][0].length;				//Total Chains
		int totalMembers = OP[0][0][0][0][0].length;			//Total Members
		int totalAtoms = 4;	//There are at most 4 atoms involved with a single carbon: Carbon itself, and 3 Seperate Hydrogen (typically just 2 hydrgogen)

		//The Array OP can be Defined as:
		//OP[ Count / OP / OP^2 ][ Leaflet ][ Lipid ID ][ Chain Number ][ Carbon / H1 / H2 / H3 ][ Carbon Index ]

		//We Want to do a system average for a single frame, then average all the frames togethor.
		double[][][][][][] frameOP = new double[2][2][totalLipidTypes][totalChains][totalAtoms][totalMembers];

		for (int Lipid = 0; Lipid < totalLipids; Lipid++){			//Iterate through all lipids.
			if (!(OPCalculated)) { 
				currentFrame.allLipids[Lipid].setOP_CosTheta(0,0);		//In case the OP is not calculated then calculate it, otherwise, continue.
			}	//Ends if statement

			String lipidName = currentFrame.allLipids[Lipid].getName();
			int lipidNumber = Mathematics.LipidToInt(lipidNames, lipidName);		// # Associated with a unique lipid
			int Leaflet = Mathematics.LeafletToInt(currentFrame.allLipids[Lipid].getLeaflet());	// # Associated with a Leaflet
			Atom currentAtom = currentFrame.allLipids[Lipid].firstChain;				// # Head for an Atom

			boolean keepGoing = true;
			int chainCount = 0;

			//Now we will Bin all the data in one spot so analysis can be done.
			while (keepGoing) {						//Continue the loop until you no longer want to keep Going.
				if (currentAtom == null) {					//If at the end of a chain / Linked List.
					chainCount++;
					currentAtom = currentFrame.allLipids[Lipid].secondChain;					

					if (chainCount == 2) {
						keepGoing = false;				//Stop doing the overall loop when both chains are iterated through.
					}	//ends if statement
				}	//Ends if statement

				else {
					double currentOP = currentAtom.getOP();

					//Get the average for the Carbon Atom		
					if (currentOP != -2) {
						int member = currentAtom.getMember();
						if (member == -1) { member = 0; }	//If the member is -1, then it implies that it must be Carbon, which is index 0.

						frameOP[0][Leaflet][lipidNumber][chainCount][0][member]++;
						frameOP[1][Leaflet][lipidNumber][chainCount][0][member] = frameOP[1][Leaflet][lipidNumber][chainCount][0][member] + currentOP;

						Atom hydrogenAtom = currentAtom.nextHydrogen;
						int currentHydrogen = 1;

						//Get the average for the Hydrogen Atoms seperately.
						while (hydrogenAtom != null){				//While not at the end of the Linked List, keep going.
							currentOP = hydrogenAtom.getOP();
					
							frameOP[0][Leaflet][lipidNumber][chainCount][currentHydrogen][member]++;
							frameOP[1][Leaflet][lipidNumber][chainCount][currentHydrogen][member] = frameOP[1][Leaflet][lipidNumber][chainCount][currentHydrogen][member] + currentOP;
							currentHydrogen++;
							hydrogenAtom = hydrogenAtom.nextHydrogen;	// Next node in a Linked List
						}	//Ends while loop
					}	//Ends if statement
				
					currentAtom = currentAtom.next;					// Next node in a Linked List
				}	//ends else statement
			}	//Ends while loop
		}	//Ends for Loop

		//Now average the array given and add it into the overall Array.
		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			for (int Lipid = 0; Lipid < totalLipidTypes; Lipid++){
				for (int Chain = 0; Chain < totalChains; Chain++){
					for (int Atom = 0; Atom < totalAtoms; Atom++){
						for (int Member = 0; Member < totalMembers; Member++){

							double count = frameOP[0][Leaflet][Lipid][Chain][Atom][Member];

							OP[0][Leaflet][Lipid][Chain][Atom][Member] = OP[0][Leaflet][Lipid][Chain][Atom][Member] + count;

							double currentOP = frameOP[1][Leaflet][Lipid][Chain][Atom][Member] / count;

							if (currentOP != 0){
								OP[1][Leaflet][Lipid][Chain][Atom][Member] = OP[1][Leaflet][Lipid][Chain][Atom][Member] + currentOP;
								OP[2][Leaflet][Lipid][Chain][Atom][Member] = OP[2][Leaflet][Lipid][Chain][Atom][Member] + (currentOP * currentOP);
							}	//Ends if statement
						}	//Ends for loop
					}	//Ends for loop
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return OP;
	}	//Ends AverageOP method


	//Generate a Histogram of the given angle of a given C-H Bond.
	//This must manually be created because there are too many options for it to be done.
	public static int[] generateAngleHistogram(Frame currentFrame, int[] Angles, String correctLipid, boolean firstChain, int carbonIndex){
		int totalLipids = currentFrame.allLipids.length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String Name = currentFrame.allLipids[currentLipid].getName();

			//Only look at one specific Lipid
			if (Name.equals(correctLipid)){
				Atom probingAtom;
				
				//Look at either the first or second chain.
				if (firstChain) { probingAtom = currentFrame.allLipids[currentLipid].firstChain; }
				else { probingAtom = currentFrame.allLipids[currentLipid].secondChain; }

				//Look at a specific carbon index.
				while (probingAtom.getMember() != carbonIndex) {
					probingAtom = probingAtom.next;
				}	//Ends while loop
				
				//Now, view every C-H Bond, and get the Angle of the bond by reverse engineering the OP associated with the approporiate Hydrogen.
				while (probingAtom != null) {
					if (probingAtom.Hydrogen != -1) {
						double cosTheta = probingAtom.getCosTheta();			//Get Cos Theta
						double Angle = Math.acos(cosTheta) * (180/Math.PI);		//Change it into Degrees

						int index = (int) Math.round(Angle*20);				// Dirty equation to find index
						
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
	public static double[][][][][][] generateOPvNN(Frame currentFrame, double[][][][][][] OPvNN, String[] lipidNames){

		int totalLipidTypes = lipidNames.length;
		int totalNeighbors = OPvNN[0][0][0][0][0].length;
		int totalChains = 2;
		int totalLeaflets = 2;
		int totalLipids = currentFrame.allLipids.length;

		//OPvNN Array can be Described as
		//OPvNN[ Count / OP / OP^2 ][ Leaflet ][ Current Lipid ][ Comparing Lipid ][ Chain ][ # of Neighbors ]
		
		//Create a frame Array and avg this and add it to the overall array.
		double[][][][][][] frameOPvNN = new double[2][totalLeaflets][totalLipidTypes][totalLipidTypes][totalChains][totalNeighbors];


		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String lipidName = currentFrame.allLipids[currentLipid].getName();
			int Lipid = Mathematics.LipidToInt(lipidNames, lipidName);					// # Associated with a unique lipid.
			
			for (int compLipid = 0; compLipid < totalLipidTypes; compLipid++){

				int neighborIndex = currentFrame.allLipids[currentLipid].Neighbors[compLipid];			// # of Comp(aring) Lipid Neighbors.
				int Leaflet = Mathematics.LeafletToInt(currentFrame.allLipids[currentLipid].getLeaflet());	// # Associated with a Leaflet
				
				double firstOP = currentFrame.allLipids[currentLipid].getFirstOP();
				double secondOP = currentFrame.allLipids[currentLipid].getSecondOP();
				
				//Add 1 to the NN Count.
				frameOPvNN[0][Leaflet][Lipid][compLipid][0][neighborIndex]++;
				if (secondOP != -2) { frameOPvNN[0][Leaflet][Lipid][compLipid][1][neighborIndex]++; }
				
				//Add the OP
				frameOPvNN[1][Leaflet][Lipid][compLipid][0][neighborIndex] = frameOPvNN[1][Leaflet][Lipid][compLipid][0][neighborIndex] + firstOP;
				if (secondOP != -2) { frameOPvNN[1][Leaflet][Lipid][compLipid][1][neighborIndex] = frameOPvNN[1][Leaflet][Lipid][compLipid][1][neighborIndex] + secondOP; }

			}	//Ends for Loop
		}	//Ends for loop

		//Now average the frame array and put it into the overall array.
		for (int Leaflet = 0; Leaflet < totalLeaflets; Leaflet++){
			for (int Lipid = 0; Lipid < totalLipidTypes; Lipid++){
				for (int compLipid = 0; compLipid < totalLipidTypes; compLipid++){
					for (int Chain = 0; Chain < totalChains; Chain++){
						for (int NN = 0; NN < totalNeighbors; NN++){
							double count = frameOPvNN[0][Leaflet][Lipid][compLipid][Chain][NN];
							double OP = frameOPvNN[1][Leaflet][Lipid][compLipid][Chain][NN] / count;

							if (OP > -2) {
								OPvNN[0][Leaflet][Lipid][compLipid][Chain][NN] = OPvNN[0][Leaflet][Lipid][compLipid][Chain][NN] + count;
								OPvNN[1][Leaflet][Lipid][compLipid][Chain][NN] = OPvNN[1][Leaflet][Lipid][compLipid][Chain][NN] + OP;
								OPvNN[2][Leaflet][Lipid][compLipid][Chain][NN] = OPvNN[2][Leaflet][Lipid][compLipid][Chain][NN] + (OP*OP);
							}	//Ends if statement
						}	//Ends for loop
					}	//ends for loop
				}	//Ends for loop
			}	//Ends for loop
		}	//Ends for loop

		return OPvNN;
	}	//Ends OPvNN


	public static double[][][][] generateCosThetaHistogram(Frame currentFrame, double[][][][] CosTheta_Histogram, String[] lipidNames){

		int totalLipids = currentFrame.allLipids.length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			String lipidName = currentFrame.allLipids[currentLipid].getName();
			int LipidType = Mathematics.LipidToInt(lipidNames, lipidName);
			int Leaflet = Mathematics.LeafletToInt(currentFrame.allLipids[currentLipid].getLeaflet());

			double firstCosTheta = currentFrame.allLipids[currentLipid].getFirstCosTheta();
			double secondCosTheta = currentFrame.allLipids[currentLipid].getSecondCosTheta();

			double avgCosTheta;
			avgCosTheta = (firstCosTheta + secondCosTheta) / 2;

			int firstIndex = (int) Math.round((firstCosTheta + 1) * 2000);			//Special equation for assigning an index.
			CosTheta_Histogram[Leaflet][LipidType][0][firstIndex]++;

			//Cholesterol and ATOC only have one 'chain' so special case there.
			if (secondCosTheta < -1) {
				avgCosTheta = firstCosTheta;
			}	//Ends if statement
		
			else {
				int secondIndex = (int) Math.round((secondCosTheta + 1) * 2000);
				CosTheta_Histogram[Leaflet][LipidType][1][secondIndex]++;
			}	//Ends else statemnt

			int avgIndex = (int) Math.round((avgCosTheta + 1) * 2000);
			CosTheta_Histogram[Leaflet][LipidType][2][avgIndex]++;
		}	//Ends for loop
	
		return CosTheta_Histogram;
	}	//ends generateCosThetaHistogram



	public static int[][] generateThickness(Frame currentFrame, int[][] Thickness, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;
		double BilayerCenter = currentFrame.getBilayerCenter();

		for (int i = 0; i < totalLipids; i++){
			String lipidName = currentFrame.allLipids[i].getName();
			int LipidType = Mathematics.LipidToInt(lipidNames, lipidName);
			double Z = currentFrame.allLipids[i].findPhosphateThickness();
			double newZ = Z - BilayerCenter;

			//Apply a unique equation to Z and then use a unique equation to find the index for it.
			if (Z != 0){
				newZ = newZ + 100;
				int index = (int) Math.round(newZ * 10);
				Thickness[LipidType][index]++;
			}	//Ends if statement
		}	//Ends for loop

		return Thickness;
	}	//Ends generateThickness 


	//PCL Stands for Project Chain Length, it a a measurement of the height of each carbon on a carbon chain.
	public static double[][][][][] generatePCL(Frame currentFrame, double[][][][][] PCL, String[] lipidNames){
		int totalLipids = currentFrame.allLipids.length;
		double BilayerCenter = currentFrame.getBilayerCenter();
		int totalLipidTypes = lipidNames.length;		
		int totalChains = 2;
		int totalCarbonIndex = PCL[0][0][0][0].length;
		double Z = 0;

		//The PCL Array can be Defined as
		// PCL[ Count / PCL / PCL^2 ][ Leaflet ][ Lipid ][ Chain ][ Carbon Index ]

		//Now average it for every frame and add this to an overall Array.
		double[][][][][] framePCL = new double[2][2][totalLipidTypes][totalChains][totalCarbonIndex];

		for (int Lipid = 0; Lipid < totalLipids; Lipid++){
			//Skip any lipid that does not have 2 chains.
				//Because of how chains are assigned we only need to check the second chain	
			if ((currentFrame.allLipids[Lipid].secondChainIdentifier).equals("null")) {
				//Do Nothing, basically skip this lipid
			}	//ends if statement

			else {
				String lipidName = currentFrame.allLipids[Lipid].getName();
				int LipidType = Mathematics.LipidToInt(lipidNames, lipidName);				// # Associated with a unique Lipid
				int Leaflet = Mathematics.LeafletToInt(currentFrame.allLipids[Lipid].getLeaflet());	// # Associated with a Leaflet

				Atom firstChain = currentFrame.allLipids[Lipid].firstChain;				// Head for the first Chain
				Atom secondChain = currentFrame.allLipids[Lipid].secondChain;				// head for the Second Chain

				while (firstChain != null){				// While not at the end of a Linked List
					int Member = firstChain.getMember();		// Carbon Index
					Z = firstChain.Z;				// Z
					Z = Z - BilayerCenter;				// Shift Z based off the bilayer center.

					framePCL[0][Leaflet][LipidType][0][Member]++;
					framePCL[1][Leaflet][LipidType][0][Member] = framePCL[1][Leaflet][LipidType][0][Member] + Z;

					firstChain = firstChain.next;			// Go to the next node of the Linked List
				}	//Ends while loop

				while (secondChain != null){
					int Member = secondChain.getMember();
					Z = secondChain.Z;
					Z = Z - BilayerCenter;

					framePCL[0][Leaflet][LipidType][1][Member]++;
					framePCL[1][Leaflet][LipidType][1][Member] = framePCL[1][Leaflet][LipidType][1][Member] + Z;

					secondChain = secondChain.next;
				}	//Ends while loop
			}	//ends else statement
		}	//Ends for loop

		//Now average the frame and add it into the overall.
		for (int Leaflet = 0; Leaflet < 2; Leaflet++){
			for (int Lipid = 0; Lipid < totalLipidTypes; Lipid++){
				for (int Chain = 0; Chain < totalChains; Chain++){
					for (int Carbon = 0; Carbon < totalCarbonIndex; Carbon++){
						double count = framePCL[0][Leaflet][Lipid][Chain][Carbon];
						double currentPCL = framePCL[1][Leaflet][Lipid][Chain][Carbon] / count;
						if (Leaflet == 1) { currentPCL = currentPCL * -1; } 	//If its the lower leaflet invert the Z axis to be positive

						PCL[0][Leaflet][Lipid][Chain][Carbon] = PCL[0][Leaflet][Lipid][Chain][Carbon] + count;
						PCL[1][Leaflet][Lipid][Chain][Carbon] = PCL[1][Leaflet][Lipid][Chain][Carbon] + currentPCL;
						PCL[2][Leaflet][Lipid][Chain][Carbon] = PCL[2][Leaflet][Lipid][Chain][Carbon] + (currentPCL * currentPCL);

					}	//Ends for loop
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
			totalTime = (currentTime - givenTime) / 1000;			//Equation to see how much time has passed (and make it readable by humans)
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


	//Lipids are given names based off the Data File initially given/supplied. If those names are difficult to read (CHL1 for Cholesterol for example), we can change that here, now.
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
		int startingFrame = 0;					// Start at 0
		int finalFrame = -1;					// End when its null
		boolean userResponse = false;
		String coordinateFile = "Coordinates.dat";		// This could be changed, but the associated TCL Script outputs a file called this by default.

		double dipoleSearchModifier = 2;					// Amount of times larger than the searhc radius that will be searched
		double dipoleSearchRadius = searchRadius * dipoleSearchModifier;
		double dipoleSpacing = 0.5;						// The spacing for the binning of Dipole Values
		int dipoleSpaces = (int) (dipoleSearchRadius * (1 / dipoleSpacing));


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
				frameSeperator = Readin.findFrameSeperator();
				totalFiles = Readin.findTotalFrames(frameSeperator);	
			}	//Ends if statement



			//Determine what Method of Simulation is used so more accurate analysis can be done. Tell the user what we are assuming it is.
			coarseGrained = Readin.determineSimulationMethod();
			if (coarseGrained) { System.out.println("System is assumed to be Coarse-Grained"); }
			else { System.out.println("System is assumed to be Atomistic"); }



			if (finalFrame != -1) { totalFiles = finalFrame; } //If we are given a set final frame, set it now.

			//We may have chosen to only look at specific frames, so lets modify how many we actually looked at for an accurate averaging of the values.
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
	
			time = progressStatement(time, "End_Read");
			//File reading has finished, move on to Analysis.
	





			//Now we will do the actual Analysis.
			time = progressStatement(time, "Start_Calculation");



			//Arrays for binning data so that it can be organized into a nice output file later.


			double[][][][][][] OP_AA = new double[3][2][totalLipids][2][4][30];
			//OP_AA[ Count / OP / OP^2 ][ Leaflet ][ Lipid ][ Chain ][ Carbon / Hydrogen 1-3 ][ Carbon Index ]

			double[][][][] OP_CG = new double[3][2][3][totalLipids];
			//OP_CG[ Count / OP / OP^2 ][ Leaflet ][ Chain 1,2,Avg ][ Lipid ]

			double[][][][][][] OPvNN = new double[3][2][totalLipids][totalLipids][2][Neighbors];
			//OPvNN[ Count / OP / OP^2 ][ Leaflet ][ Current Lipid ][ Comparing Lipid ][ Chain ][ Number of Neighbors ]

			double[][][][] CosTheta_Histogram = new double[2][totalLipids][3][4001];
			//CosTheta_Histogram[ Leaflet ][ Lipid ][ Chain 1,2,Avg ][ Bin Spot ]

			double[][][][][] DipoleField = new double[3][2][totalLipids][totalLipids][dipoleSpaces + 1];
			//DipoleField[ Count / Dipole / Dipole^2 ][ Leaflet ][ Lipid ][ Comparting Lipid ][ Radial Distance ]

			int[] Angle_Histogram_AA = new int[3601];
			//Angle_Histogram_AA[ Bin Spot ]

			int[][] Thickness = new int[totalLipids][2000];
			//Thickness[ Lipid ][ Bin Spot ]			

			int[][][] Registration = new int[2][totalFiles + 1][totalLipids];
			//Registration[ Total Count / Registered Count ][ Frame Number ][ Lipid ]

			double[][][][][] PCL = new double[3][2][totalLipids][2][30];
			// PCL[ Count / PCL / PCL^2 ][ Leaflet ][ Lipid ][ Chain ][ Carbon Index ]

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
					Registration = generateRegistration(currentFrame, Registration, lipidNames, (searchRadius / 2), false); //Bin Registration
					DipoleField = generateDipoleField(currentFrame, DipoleField, lipidNames, dipoleSearchRadius, false, dipoleSpacing);	//Bin all Dipoles
				}	//ends if statemetn

				else {
					generateNN(currentFrame, searchRadius, lipidNames, true);					//Find Nearest Neighbors
					OP_AA = generateOP_AA(currentFrame, OP_AA, lipidNames);						//Find Order Parameter
					OPvNN = generateOPvNN(currentFrame, OPvNN, lipidNames);						//Plot the previous 2
					Thickness = generateThickness(currentFrame, Thickness, lipidNames);				//Bin all Thicknesses
					PCL = generatePCL(currentFrame, PCL, lipidNames);						//Bin all PCL
					CosTheta_Histogram = generateCosThetaHistogram(currentFrame, CosTheta_Histogram, lipidNames);	//Bin all Cos(Theta) values
					Angle_Histogram_AA = generateAngleHistogram(currentFrame, Angle_Histogram_AA, "PSM", true, 16);	//Bin all Angle Values
					Registration = generateRegistration(currentFrame, Registration, lipidNames, (searchRadius / 2), true);	//Bin Registration
					DipoleField = generateDipoleField(currentFrame, DipoleField, lipidNames, dipoleSearchRadius, true, dipoleSpacing); 	//Bin all Dipoles

				}	//Ends else statement

				//Special if statement for when we reach the final frame
				if (currentFrame.frameNumber == (finalFrame-1)) {

//					currentFrame = currentFrame.setFirstFrame();				//Go to beginning of LL
//					Readin.serializeFrame("falseName", FrameTracker, currentFrame);		//Serialize all Changes Made

					currentFrame = null;							//End the entire while loop.
				}	//Ends if statement

				else {
					if (currentFrame.nextFrame != null) { currentFrame = currentFrame.nextFrame; }		//If not at the end of the LL, go next.
					else { 
//						currentFrame = currentFrame.setFirstFrame();					//Go to beginning of LL
//						Readin.serializeFrame("falseName", FrameTracker, currentFrame);			//Save/Serialize all Changes Made

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
	
			//Create different files for the different simulation types.
			if (coarseGrained) {
				Readin.createOPvNNFiles(OPvNN, lipidNames, totalReadFrames);
				Readin.createNNFiles(OPvNN, lipidNames);
				Readin.createStandardDataFiles_CG(OPvNN, OP_CG, lipidNames);
				Readin.createCosThetaHistogramFiles(CosTheta_Histogram, lipidNames, coarseGrained);
				Readin.createRegistrationFiles(Registration, lipidNames);
				Readin.createDipoleFiles(DipoleField, dipoleSpacing, lipidNames, totalReadFrames);
			}	//Ends if statement

			else{
				Readin.createNNFiles(OPvNN, lipidNames);
				Readin.createOPFiles(OP_AA, lipidNames, totalReadFrames);
				Readin.createOPvNNFiles(OPvNN, lipidNames, totalReadFrames);
				Readin.createThicknessFiles(Thickness, lipidNames);
				Readin.createPCLFiles(PCL, lipidNames, totalReadFrames);
				Readin.createCosThetaHistogramFiles(CosTheta_Histogram, lipidNames, coarseGrained);
				Readin.createAngleHistogramFile(Angle_Histogram_AA, "PSM", true, 16);		//This needs to be set manually. (Too ambiguous)
				Readin.createRegistrationFiles(Registration, lipidNames);
				Readin.createDipoleFiles(DipoleField, dipoleSpacing, lipidNames, totalReadFrames);
			}	//Ends else statement

			time = progressStatement(time, "End_Output");
			//Output finished.
	
		}	//Ends else statement
		System.out.println("");
	}	//Ends Main
}	//Ends Class Definition
