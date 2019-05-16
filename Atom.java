//Atom Class
//Currently tailored for file reading a specific file format
//Change later


import java.io.Serializable;

public class Atom implements Serializable{
	
	String Chain;
	int Member;
	int Hydrogen;
	float OP;
	int ID;
	Atom next = null;

	public Atom(int ID, String Chain, int Member, int Hydrogen, float OP){
		this.ID = ID;
		this.Chain = Chain;
		this.Member = Member;
		this.Hydrogen = Hydrogen;
		this.OP = OP;


	}	//Ends constructor

	public void setNext(Atom newAtom){
		this.next = newAtom;

	}	//Ends setNext method

	
	public void printAllAtoms(){
		System.out.println(this.ID + " " + this.Chain + " " + this.Member + " " + Hydrogen + " " + OP);
	
		if (this.next != null){
			this.next.printAllAtoms();
		}	//Ends if statement

	}	//Ends printAllAtoms


}	//Ends class definitiom
