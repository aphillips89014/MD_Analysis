//Lipid Java File

//Lipids have attributes, such as the location of its Center of Mass, it's averageOP, and the neighbors it has.

import java.io.Serializable;
import java.util.Arrays;

public class Lipid implements java.io.Serializable {

	private static final long serialVersionUID = 1;
		//This variable is to help with serialization, won't be used specifically.

	String Name;				// Name of the Lipid
	int ID;					// Numeric ID, this is unique and only 1 lipid at a time can have a single ID.
	double X;				// X, Y, Z Coordinates.
	double Y;
	double Z;
	String firstChainIdentifier = "null";	// Each chain has a unique name, such as 1 and 2, or A and B. Save those two unqiue names here
	String secondChainIdentifier = "null";

	boolean Leaflet;			//True Leaflet implies upper leaflet, false implies Lower
	boolean FlipFloppable;			// Can the given lipid Flip-Flop?

	double firstOP = -2;			// Avg OP of the first chain
	double secondOP = -2;			// Avg OP of the second chain

	double firstCosTheta = -2;		// Avg Cos Theta value of the first chain
	double secondCosTheta = -2;		// "" of the second chain

	int[] Neighbors;			// Array of the # of neighbors a lipid has. The indexes in the array implies which lipid the Neighbors refer to
						// 	 The indexes are kept consistent throughout the entire program and are determined from reading the file.

	double[] dipoleVector = {0,0,0};	// Vector that determines the vector between the Phosphourous and Nitrogen atoms/bead equivalents. Some lipids dont have this.

	Atom firstChain;			// Head of a Linked List for the First Chain of atoms
	Atom secondChain;			// Head of a linked list for the second chain of atoms

	//Special atoms such as Phosphourous, Nitrogen
	Atom specialAtoms;			// Head of a Linked List for every 'other' atoms.

	//Assign some attributes
	public Lipid(String Name, int ID, double X, double Y, double Z, String Leaflet, String FlipFloppable, String[] lipidNames){
		this.Name = Name;
		this.ID = ID;
		this.X = X;
		this.Y = Y;
		this.Z = Z;

		if (Leaflet.equals("Upper")) { 
			this.Leaflet = true; 
		}	//Ends If Statement
		else{ 
			this.Leaflet = false; 
		}	//Ends else statement

		if (FlipFloppable.equals("Yes")) {
			this.FlipFloppable = true;
		}	//Ends if statement
		else{
			this.FlipFloppable = false;
		}	//Ends else statement

		this.firstChain = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);		// Initialize all the atoms with Heads.
		this.secondChain = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);
		this.specialAtoms = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);

		int length = lipidNames.length;						// The # of unique lipids in a sample.
		this.Neighbors = new int[length];

	}	//Ends Constructor


	//Checks if Neighbors has been calculated yet.
	public boolean checkForNN(){
		// ** Lives under that assumption that any lipid species will have at least one of any type of neighbor.

		boolean result = false;
		int sum = Mathematics.sumArray(this.Neighbors);

		if (sum > 0){
			result = true;
		}

		return result;
	}	//Ends checkForNN Method



	public boolean checkForDipole(boolean numeric){
		//Check to see if special atoms has a P and an N atom.
		//If it doesn't return false
		//If we are looking for a numeric value, ensure if the dipole has been calculated yet.
		boolean result = false;

		if (!(numeric)){
			Atom head = this.specialAtoms;

			boolean N = false;		// Nitrogen
			boolean P = false;		// Phosphourous

			while (head != null){				// Iterate through the entire Linked List
				if ((head.Name.equals("N") || (head.Name.equals("NC3")))){		// If we have seen a Nitrogen, let us know.
					N = true;
				}	//Ends if statement
				
				else if ((head.Name.equals("P") || (head.Name.equals("PO4")))){		// If we have seen a Phosphourous, let us know.
					P = true;
				}	//ends if statement

				head = head.next;			// Go to next Node in the Linked List
			}	//Ends while loop

			if (P && N) { result = true; }			// If we have a Phosphorous and a Nitrogen, then we can calculate a dipole, so return true.

		}	//Ends if statement

		else if (numeric){		// If we want to know if we have calculated a dipole (not if we legally can but rather have we at all)
			double sum = Mathematics.sumArray(this.dipoleVector);
	
			if (sum == 0) { result = false; }
			else if (sum != 0) { result = true; }

		}	//Ends if statment

		return result;
	}	//ends checkForDipole Method


	//Checks if OP has been Calculated yet.
	public boolean checkForOP(){
		boolean result = false;

		if (this.firstOP != -2){		// If there is an OP for the second chain, there MUST be one for the first.
			result = true;			// 	If the OP is -2 this implies it is still the default value.
		}	//Ends if statement

		return result;

	}	//Ends checkForOP method


	public void setDipoleVector(){
		//If the lipid has a Phosphorous and Nitrogen, use that to generate the vector in question.
		
		if (this.specialAtoms.next != null){
			double[] dipoleVector = new double[3];
			Atom head = this.specialAtoms.next;

			double x1 = 0;
			double y1 = 0;
			double z1 = 0;

			double x2 = 0;
			double y2 = 0;
			double z2 = 0;

			// We want to subtract phosphorous from nitrogen ideally. But there is no guarntee the order they will appear in the Linked List (rules aren't too strict here.)
			while (head != null){
				if ((head.Name.equals("N")) || (head.Name.equals("NC3"))){ 
					x1 = head.X;
					y1 = head.Y;
					z1 = head.Z;

				}	//ends if statement

				if ((head.Name.equals("P")) || (head.Name.equals("PO4"))){ 
					x2 = head.X;
					y2 = head.Y;
					z2 = head.Z;

				}	//Ends else if statement

				else{

				}	//Ends if statement

				head = head.next;
			}	//Ends while loop

			dipoleVector[0] = x1 - x2;
			dipoleVector[1] = y1 - y2;
			dipoleVector[2] = z1 - z2;

			dipoleVector = Mathematics.normalizeVector(dipoleVector);
			this.dipoleVector = dipoleVector;

		}	//Ends if statement
	}	//Ends setDipoleVector method

	//Average the Order Parameter for the first Chain and the seocnd chain.
	//averageOP is  recursive so it calls itself until its iterated through each lipid.
	public void setOP_CosTheta(double xLength, double yLength){

		double[] first_CosTheta = new double[2];		// We are going to use this array as a way to count the # of times we have caluclated CosTheta and the sum of each of those values. Then we will take the Sum and divide it by the #.
		double[] first_OP = new double[2];
	
		//The orientation is mathematically flipped if a lipid is in the lower leaflet.
		//We can fix that here so we can perform comparitive analysis easier.
		//May want to change this in the future to be more elegant...
		double modifier = 1;
		if (this.Leaflet == false) { modifier = -1; }
		

		firstChain.determineOP(0, 0, 0, xLength, yLength);
		first_OP = firstChain.averageOP(first_OP);		
		first_CosTheta = firstChain.averageCosTheta(first_CosTheta);

		if (first_CosTheta[0] > 0){		// If we actually have a valid # of occurances ( basically we want to avoid division by zero. )
			this.firstOP = (first_OP[1] / first_OP[0]);
			this.firstCosTheta = (first_CosTheta[1] / first_CosTheta[0]) * modifier;

		}	//Ends if statement	


		double[] second_OP = new double[2];
		double[] second_CosTheta = new double[2];

		secondChain.determineOP(0,0,0, xLength, yLength);
		second_OP = secondChain.averageOP(second_OP);
		second_CosTheta = secondChain.averageCosTheta(second_CosTheta);		

		if (second_CosTheta[0] > 0){
			this.secondOP = (second_OP[1] / second_OP[0]);
			this.secondCosTheta = (second_CosTheta[1] / second_CosTheta[0]) * modifier;
		}	//ends if statement

	}	//Ends average OP method

	public void setNN(int index, int value){
		this.Neighbors[index] = value;		// Assign the given index the given value.

	}	//Ends setNN


	//Return Various Information
	public void getInformation(){					// For Debugging purposes only, pay no attention to the man behind the curtain.
		System.out.println("");
//		System.out.println("Get Info:");


//		System.out.println(this.Name + " " + this.ID + " " +  this.X + " " +  this.Y + " " + this.Z);
		System.out.println(this.Name + " " + this.ID + " ");

//		System.out.println("firstChain:");
//		this.firstChain.printAllAtoms();
//		System.out.println("secondChain:");
//		this.secondChain.printAllAtoms();

		System.out.println("SpecialAtoms:");
		this.specialAtoms.printAllAtoms();

//		System.out.println("CosTheta: " + this.firstCosTheta + " " + this.secondCosTheta);
//		System.out.println("OP: " + this.firstOP + " " + this.secondOP);
//		System.out.println(Arrays.toString(this.Neighbors));
		System.out.println(Arrays.toString(this.dipoleVector));


		System.out.println("");
	} //Ends getInformation method


	//Allows the lipid to be defined on the fly instead of having to be defined by hand every time.
	public void assignChainIdentifier(String givenChain){

		if (this.firstChainIdentifier.equals("null")){
			this.firstChainIdentifier = givenChain;
		}	//ends if staetment

		else if (this.secondChainIdentifier.equals("null")){
			if (!(givenChain.equals(this.firstChainIdentifier))){
				this.secondChainIdentifier = givenChain;
			}	//Ends if statement
		}	//Ends else if statement

		else{
	
		}	//Ends else statement
	}	//Ends assignChain Method


	//Creates a specific atom on a specific chain.
	public void createAtom(String Chain, int Member, int Hydrogen, String Name, double X, double Y, double Z){

		Atom thisChain = null;

		if (Chain.equals(this.firstChainIdentifier)){
			thisChain = this.firstChain;
		}	//Ends if statement

		else if (Chain.equals(this.secondChainIdentifier)){
			thisChain = this.secondChain;
		}	// Ends else if statement

		else if (Chain.equals("null")){
			//Do Nothing, this should currently only occur if a CG PO4 or CG NC3 is being looked at.
		}	//ends if statement

		else{
			System.out.println("Error in creating atom");
			//This shouldn't ever happen.
		}	//Ends else statement



		//Create new Atom
		Atom newAtom = new Atom(this.ID, Chain, Member, Hydrogen, Name, X, Y, Z);
	

		// Now based off the name of the given atom, choose where to place it.
		// 	 Some of these if statements may be able to be grouped up, but being redundant is a bit more clear at times.
		if (Name.equals("C")){
			//Add it to a linked list.

			addAtom(thisChain, newAtom);
		}	//Ends if statement

		else if (Name.equals("H")) {
			addHydrogen(thisChain, newAtom);

		}	//Ends if statement

		else if ((Name.equals("P")) || (Name.equals("PO4"))) {
			addAtom(this.specialAtoms, newAtom);	

		}	//Ends if statement
		
		else if ((Name.equals("N")) || (Name.equals("NC3"))) {
			addAtom(this.specialAtoms, newAtom);

		}	//Ends if statement

		else {
			//Group up all the Coarse-Grained Beads in this else statement
			addAtom(thisChain, newAtom);

		}	//Ends else statement
	}	//Ends createAtom method



	public static void addHydrogen(Atom head, Atom newAtom){
		
		if (head == null){
			System.out.println("Add Hydrogen has been passed a null head");
		}	//Ends if statement

		if ((head.getMember()) != newAtom.getMember()){		// Make sure we are looking at the correct carbon on the chain.
			addHydrogen(head.next, newAtom);
		}	//ends addHydrogen

		else{							// We found the correct Carbon
			head.setNextHydrogen(newAtom);
		}	//Ends else statement
	}	//ends addHydrogen


	//Add a newAtom to the head of a Linked List
	public static void addAtom(Atom head, Atom newAtom){
		if (head != null){
			newAtom.setNext(head.next);
			head.setNext(newAtom);
		}	//Ends if statement

		else{
			System.out.println("Passed head was null");
			//This should not happen
		}	//ends else statement

	}       //Ends addAtom

	
	public double findPhosphateThickness(){
		double result = 0;
		result = this.specialAtoms.getPhosphateThickness();
		
		return result;

	}	//Ends findPhosphateThickness


	public double getTerminalCarbonHeight(){
		//Go to the terminal Carbon and get its height.
		Atom tempAtom = this.firstChain;
		double result = 0;
		boolean keepGoing = true;

		while (keepGoing){
			int currentMember = tempAtom.getMember();
			
			if (tempAtom.next != null) {
				int nextMember = tempAtom.next.getMember();
				
				if (nextMember > currentMember){
					tempAtom = tempAtom.next;
				}	//ends if statement

				else{
					keepGoing = false;
				}	//Ends else statement
			}	//Ends if statement
		
			else{
				keepGoing = false;
			}	//Ends else statement
		}	//Endswhile loop

		result = tempAtom.Z;

		//If its invalid, return an absurd value, there may be a better way to do this.
		if (tempAtom.getMember() == 0) { 
			result = 5000; 
			System.out.println("ERROR in finding Terminal Carbon Height");
		}	// Ends if statement


		return result;
	}	//Ends getTerminalCarbonHeight Method


	public String getName(){
		return this.Name;
	}	//Ends getName Method

	public boolean getLeaflet(){
		return this.Leaflet;
	}	//Ends getLeaflet Method

	public double getX(){
		return this.X;
	}	//Ends getX Method

	public double getY(){
		return this.Y;
	}	//Ends getY Methdo

	public int getID(){
		return this.ID;
	}	//Ends getID Method

	public double[] getDipole(){
		return this.dipoleVector;
	}	//ends getDipoleMethod

	public double getFirstCosTheta(){
		return this.firstCosTheta;
	}	//ends getFirstCosTheta

	public double getSecondCosTheta(){
		return this.secondCosTheta;
	}	//ends getFirstCosTheta

	public double getFirstOP(){
		return this.firstOP;
	}	//Ends getFirstOP()

	public double getSecondOP(){
		return this.secondOP;
	}	//Ends getFirstOP()
}	//Ends Lipid Class Defintion
