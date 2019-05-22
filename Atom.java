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
	float X;
	float Y;
	float Z;
	double OP;
	int ID;

	//Atom is a Node in a Linked List.
	Atom next = null;
	Atom nextHydrogen = null;

	//Assings some attributes
	public Atom(int ID, String Chain, int Member, int Hydrogen, String Name, float X, float Y, float Z){
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
	public void calculateOP(float carbonX, float carbonY, float carbonZ){

		if ((this.Name.equals("C"))) {
			this.nextHydrogen.calculateOP(this.X, this.Y, this.Z);

			double[] OParray = new double[2];
			OParray = this.nextHydrogen.averageHydrogenOP(OParray);
			this.OP = OParray[1] / OParray[0];


			if (this.next != null){
				this.next.calculateOP(0,0,0);
			}	//Ends if statement
		}	//Ends if statement

		else if (this.Name.equals("H")) {
			float hydrogenX = this.X;
			float hydrogenY = this.Y;
			float hydrogenZ = this.Z;

			double xDiff = Math.pow((carbonX - hydrogenX), 2);
			double yDiff = Math.pow((carbonY - hydrogenY), 2);
			double zDiff = Math.pow((carbonZ - hydrogenZ), 2);

			double magnitude = Math.pow((xDiff + yDiff + zDiff), 0.5);
			double cosTheta = zDiff / magnitude;
		
			cosTheta = Math.pow(cosTheta, 2);
			
			double OP = (3*cosTheta - 1 ) / 2;
			this.OP = OP;

			if (this.nextHydrogen != null){
				this.nextHydrogen.calculateOP(carbonX, carbonY, carbonZ);
			}	//Ends if statement
		}	//Ends if statement
		
		//Skip over the head
		else if (this.Name.equals("head")){
			if (this.next != null){
				this.next.calculateOP(0,0,0);
			}	//Ends if statement
		}	//Ends if statement


	}	//Ends calculateOP

	
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

	
	//System for checking debugging
	public void printAllAtoms(){
		System.out.println(this.ID + " " + this.Chain + " " + this.Name + " " + this.Member + " " + this.Hydrogen + " " + this.X + " " + this.Y + " " + this.Z + " " + this.OP);
	
		if (this.next != null){
			this.next.printAllAtoms();
		}	//Ends if statement
	
		if (this.nextHydrogen != null){
			this.nextHydrogen.printAllAtoms();
		}


	}	//Ends printAllAtoms

	public int getMember(){
		return this.Member;
	}	//Ends getMember Method


}	//Ends class definitiom
