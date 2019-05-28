//Lipid Java File

//Lipids have attributes, such as the location of its Center of Mass, it's averageOP, and the neighbors it has.

import java.io.Serializable;
import java.util.Arrays;

public class Lipid implements java.io.Serializable {

	private static final long serialVersionUID = 1;
		//This variable is to help with serialization, won't be used specifically.

	String Name;
	int ID;
	double X;
	double Y;
	double Z;
	String firstChainIdentifier = "null";
	String secondChainIdentifier = "null";

	double firstOP = 0;
	double secondOP = 0;

	int[] Neighbors;

	Atom firstChain;
	Atom secondChain;

	//Special atoms such as Phosphourous
	Atom specialAtoms;

	//Assign some attributes
	public Lipid(String Name, int ID, double X, double Y, double Z, String[] lipidNames){
		this.Name = Name;
		this.ID = ID;
		this.X = X;
		this.Y = Y;
		this.Z = Z;
		this.firstChain = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);
		this.secondChain = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);
		this.specialAtoms = new Atom(ID, "head", 0, 0, "head", 0, 0, 0);

		int length = lipidNames.length;
		this.Neighbors = new int[length];


	}	//Ends Constructor


	//Checks if Neighbors has been calculated yet.
	public boolean checkForNN(){

		int sum = 0;
		int length = this.Neighbors.length;
		boolean result = false;

		for (int i = 0; i < length; i++){
			sum = sum + this.Neighbors[i];
		}	//Ends for loop

		if (sum > 0){
			result = true;
		}

		return result;
	}	//Ends checkForNN Method


	//Checks if OP has been Calculated yet.
	public boolean checkForOP(){
		boolean result = false;

		if (this.firstOP != 0){
			result = true;
		}	//Ends if statement

		return result;

	}	//Ends checkForOP method


	//Average the Order Parameter for the first Chain and the seocnd chain.
	//averageOP is  recursive so it calls itself until its iterated through each lipid.
	public void setOP(){

		double[] first = new double[2];

		firstChain.determineOP(0, 0, 0);
		first = firstChain.averageOP(first);		
		this.firstOP = (first[1] / first[0]);
	
		double[] second = new double[2];

		secondChain.determineOP(0,0,0);
		second = secondChain.averageOP(second);
		this.secondOP = (second[1] / second[0]);

	}	//Ends average OP method


	//Assigns a specific attribute.
	public void setNN(int index, int value){
		this.Neighbors[index] = value;

	}	//Ends setNN


	//Return Various Information
	public void getInformation(){
//		System.out.println(this.Name + " " + this.firstChainIdentifier + " " + this.secondChainIdentifier);
		this.firstChain.printAllAtoms();
//		this.secondChain.printAllAtoms();
		this.specialAtoms.printAllAtoms();

//		System.out.println(this.firstOP + " " + this.secondOP);
//		System.out.println(Arrays.toString(this.Neighbors));
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

		else{
			System.out.println("Error in creating atom");
			//This shouldn't ever happen.
		}	//Ends else statement

		//Create new Atom
		Atom newAtom = new Atom(this.ID, Chain, Member, Hydrogen, Name, X, Y, Z);
	
		if (Name.equals("C")){
			//Add it to a linked list.

			addAtom(thisChain, newAtom);
		}	//Ends if statement

		else if (Name.equals("H")) {
			addHydrogen(thisChain, newAtom);

		}	//Ends if statement

		else if (Name.equals("P")) {
			addAtom(this.specialAtoms, newAtom);	

		}	//Ends if statement


	}	//Ends createAtom method

	public static void addHydrogen(Atom head, Atom newAtom){
		
		if (head == null){
			System.out.println("Add Hydrogen has been passed a null head");
		}	//Ends if statement

		//Make sure we are looking at the correct carbon
		if ((head.getMember()) != newAtom.getMember()){
			addHydrogen(head.next, newAtom);
		}	//ends addHydrogen

		else{
			//We found the correct Carbon
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


	public String getName(){
		return this.Name;
	}	//Ends getName Method

	public double getX(){
		return this.X;
	}	//Ends getX Method

	public double getY(){
		return this.Y;
	}	//Ends getY Methdo

	public int getID(){
		return this.ID;
	}	//Ends getID Method

	public double getFirstOP(){
		return this.firstOP;
	}	//Ends getFirstOP()

	public double getSecondOP(){
		return this.secondOP;
	}	//Ends getFirstOP()

}	//Ends Lipid Class Defintion
