//Frame Class

//For every Frame there is a N number of Lipids.
	//This is where we store those Lipids

import java.io.Serializable;

public class Frame implements java.io.Serializable {

	private static final long serialVersionUID = 2;
	//This variable allows this class to actually be Serialziable. Won't be used directly.

	int frameNumber = 0;
	Lipid[] allLipids;
	int totalLipids = 0;
	double xLength = 0;
	double yLength = 0;


	//Assign the attributes of the class directly from the Constructor.
	public Frame(int frameNumber, int totalLipids){
		this.frameNumber = frameNumber;
		this.allLipids = new Lipid[totalLipids];
		this.totalLipids = totalLipids;

	}	//Ends Constructor


	//Create a lipid with the given attributes
	public void createLipid(String Name, int ID, double X, double Y, double Z, String[] lipidNames){
		this.allLipids[ID-1] = new Lipid(Name, ID, X, Y, Z, lipidNames);

	}	//Ends createLipid Method


	public int getFrameNumber(){
		return this.frameNumber;
	}	//Ends getFrameNumber method

	public double getXLength(){
		return this.xLength;
	}	//ends getXLength method

	public double getYLength(){
		return this.yLength;
	}	//Ends getYLength method


	//Quite literally delete the frame.
	//Since the frame (before this method is invoked) is always Serialized, if we want to access it we just Un-Serailize it. So we can remove it entirely from our memory. This is done using this method (kind of).
	public void resetFrame(int frameNumber){
		this.allLipids = new Lipid[this.totalLipids];
		this.frameNumber = frameNumber;

	}	//Ends resetFrame

	//Find the maximum and minimum x and y lengths for this current frame
	//Iterate through every lipid and find the maximum x and y values.
	//This is unique for every frame so it must be calculated every frame.
	public void findDimensions(){
		double maxX = 0;
		double maxY = 0;
		double minX = 0;
		double minY = 0;

		double x = 0;
		double y = 0;

		int length = this.allLipids.length;

		for (int i = 0; i < length; i++){
			x = this.allLipids[i].getX();
			y = this.allLipids[i].getY();

			if (x < minX) { minX = x; }
			else if (x > maxX) { maxX = x; }
			if (y < minY) { minY = y; }
			else if (y > maxY) { maxY = y; }

		}	//Ends for loop

		this.xLength = (maxX - minX);
		this.yLength = (maxY - minY);

	}	//Ends findDimensions method
}	//Ends class defintion
