//Atom Class
//Currently tailored for file reading a specific file format
//Change later


import java.io.Serializable;
import java.lang.Math;

public class Atom implements Serializable{
	
	String Chain;
	String Name;
	int Member;
	int Hydrogen;
	double X;
	double Y;
	double Z;
	double OP;
	int ID;

	//Atom is a Node in a Linked List.
	Atom next = null;
	Atom nextHydrogen = null;

	//Assings some attributes
	public Atom(int ID, String Chain, int Member, int Hydrogen, String Name, double X, double Y, double Z){
		this.ID = ID;
		this.Chain = Chain;
		this.Member = Member;
		this.Hydrogen = Hydrogen;
		this.Name = Name;
		this.X = X;
		this.Y = Y;
		this.Z = Z;


	}	//Ends constructor

	//Set the next value in the Linked List
	public void setNext(Atom newAtom){
		this.next = newAtom;

	}	//Ends setNext method

	//Set the next value in the linked list.
	public void setNextHydrogen(Atom newAtom){
		if (this.nextHydrogen == null){
			this.nextHydrogen = newAtom;
		}	//ends if statemnt

		else{
			this.nextHydrogen.setNextHydrogen(newAtom);
		}	//Ends else statement
	}	//ends setNextHydrogen method


	//Calculate the OP using the second order legrange polynomial. and some unseen algebra.
	public void determineOP(double carbonX, double carbonY, double carbonZ){
		
		//These first couple of if statements are for Atomistic Simulations only.
		
		if ((this.Name.equals("C"))) {
			this.nextHydrogen.determineOP(this.X, this.Y, this.Z);

			double[] OParray = new double[2];
			OParray = this.nextHydrogen.averageHydrogenOP(OParray);
			this.OP = OParray[1] / OParray[0];


			if (this.next != null){
				this.next.determineOP(0,0,0);
			}	//Ends if statement
		}	//Ends if statement


		else if (this.Name.equals("H")) {
			this.OP = Mathematics.calculateOP(carbonX, carbonY, carbonZ, this.X, this.Y, this.Z);

			if (this.nextHydrogen != null){
				this.nextHydrogen.determineOP(carbonX, carbonY, carbonZ);
			}	//Ends if statement
		}	//Ends if statement
		

		//Skip over the head
		else if (this.Name.equals("head")){
			if (this.next != null){
				this.next.determineOP(0,0,0);
			}	//Ends if statement
		}	//Ends if statement


		//Now we have some "Special" Order parameters


		//First the OP for Cholesterol in Atomistic Simualations
			//Its the OP between C3 and H3
		else if (this.Name.equals("H3")) {
			//Next item is always C3
			this.OP = Mathematics.calculateOP(this.next.X, this.next.Y, this.next.Z, this.X, this.Y, this.Z);

		}	//Ends if statement



		//Iterate through the linked list until we find a suitable value
		else{
			if (this.next != null){
				this.next.determineOP(0,0,0);
			}	//ends if statement
		}	//ends else statement
	}	//Ends determineOP

	
	public double[] averageHydrogenOP(double[] array){
		if (this.OP != 0) {
			array[0]++;
			array[1] = array[1] + this.OP;

		}	//Ends if statement
		
		if (this.nextHydrogen != null){
			array = this.nextHydrogen.averageHydrogenOP(array);
		}	//ends if statement

		return array;
	}	//Ends averageHydrogenOP



	//Iterater through the Linked list and average the OP
	//Keep track of the total number of iterations in the first index, then the summed value itself in the second index.
	public double[] averageOP(double[] array){
		
		//Come back to this later, currently it is intentionally broken
		if (this.OP != 0){
			array[0]++;
			array[1] = array[1] + this.OP;
		}	//Ends if statement

		if (this.next != null){
			array = this.next.averageOP(array);
		}	//Ends

		return array;
	}	//Ends averageOp

	public double getPhosphateThickness(){
		double result = 0;
		if ((this.Name).equals("P")){
			result = this.Z;

		}	//Ends if statement

		else if (this.next != null){
			result = this.next.getPhosphateThickness();

		}	//Ends if statement

		return result;
	}	//Ends getPhosphateThickness	


	//System for checking debugging
	public void printAllAtoms(){
		System.out.println(this.ID + " " + this.Chain + " " + this.Name + " " + this.Member + " " + this.Hydrogen + " " + this.X + " " + this.Y + " " + this.Z + " " + this.OP);
	
		if (this.next != null){
			this.next.printAllAtoms();
		}	//Ends if statement
	
		if (this.nextHydrogen != null){
			this.nextHydrogen.printAllAtoms();
		}	//Ends if statement
	}	//Ends printAllAtoms

	public int getMember(){
		return this.Member;
	}	//Ends getMember Method

	public double getOP(){
		return this.OP;
	}	//Ends getOP
}	//Ends class definitiom
