//Lipid Java File
import java.io.Serializable;
import java.util.Arrays;

public class Lipid implements java.io.Serializable {

	private static final long serialVersionUID = 1;

	String Name;
	int ID;
	float X;
	float Y;
	String firstChainIdentifier = "null";
	String secondChainIdentifier = "null";

	float firstOP = 0;
	float secondOP = 0;

	int[] Neighbors = new int[3];

	Atom firstChain;
	Atom secondChain;


	public Lipid(String Name, int ID, float X, float Y){
		this.Name = Name;
		this.ID = ID;
		this.X = X;
		this.Y = Y;
		this.firstChain = new Atom(ID, "head", 0, 0, 0);
		this.secondChain = new Atom(ID, "head", 0, 0, 0);

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



	public boolean checkForOP(){
		boolean result = false;

		if (this.firstOP != 0){
			result = true;
		}	//Ends if statement

		return result;

	}	//Ends checkForOP method


	public void findOP(){

		float[] first = new float[2];
		first = firstChain.averageOP(first);		
		this.firstOP = (first[1] / first[0]);
	
		float[] second = new float[2];
		second = secondChain.averageOP(second);
		this.secondOP = (second[1] / second[0]);

	}	//Ends average OP method



	public void assignNN(int index, int value){
		this.Neighbors[index] = value;

	}	//Ends assignNN



	//Return Various Information
	public void getInformation(){
//		System.out.println(this.Name + " " + this.firstChainIdentifier + " " + this.secondChainIdentifier);
//		this.firstChain.printAllAtoms();
//		this.secondChain.printAllAtoms();
		System.out.println(this.firstOP + " " + this.secondOP);
		System.out.println(Arrays.toString(this.Neighbors));
		System.out.println("");


	} //Ends getInformation method



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



	public void createAtom(String Chain, int Member, int Hydrogen, float OP){
		Atom thisChain = null;
		if (Chain.equals(this.firstChainIdentifier)){
			thisChain = this.firstChain;
		}	//Ends if statement

		else if (Chain.equals(this.secondChainIdentifier)){
			thisChain = this.secondChain;
		}	// Ends else if statement

		else{
			System.out.println("Shouldn't Happen: " + Chain);
			System.out.println("1st/2nd: " + this.firstChainIdentifier + " / " + this.secondChainIdentifier + "     Passed: " + Chain);
		}	//Ends else statement


		Atom newAtom = new Atom(this.ID, Chain, Member, Hydrogen, OP);
		
		addAtom(thisChain, newAtom);

	}	//Ends createAtom method



	//Add a newAtom to the head of a Linked List
	public void addAtom(Atom head, Atom newAtom){
		if (head != null){
			newAtom.setNext(head.next);
			head.setNext(newAtom);
		}	//Ends if statement

		else{
			System.out.println("Passed head was null");
			//This should not happen
		}	//ends else statement

	}       //Ends addAtom



	public String getName(){
		return this.Name;
	}	//Ends getName Method

	public float getX(){
		return this.X;
	}	//Ends getX Method

	public float getY(){
		return this.Y;
	}	//Ends getY Methdo

	public int getID(){
		return this.ID;
	}	//Ends getID Method

}	//Ends Lipid Class Defintion
