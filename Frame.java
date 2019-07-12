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
	double BilayerCenter = 0;
	int nextAvailableLipid = 0;
	
	//Frame is a 2 directional LL, it is 2 Directional because the programmer wanted to be lazy.
	Frame nextFrame = null;
	Frame prevFrame = null;


	//Assign the attributes of the class directly from the Constructor.
	public Frame(int frameNumber, int totalLipids){
		this.frameNumber = frameNumber;
		this.allLipids = new Lipid[totalLipids];
		this.totalLipids = totalLipids;

	}	//Ends Constructor


	//Create a lipid with the given attributes
	public void createLipid(String Name, int ID, double X, double Y, double Z, String Leaflet, String FlipFloppable, String[] lipidNames){
		this.allLipids[this.nextAvailableLipid] = new Lipid(Name, ID, X, Y, Z, Leaflet, FlipFloppable, lipidNames);
		this.nextAvailableLipid++;
	}	//Ends createLipid Method

	//Return the given String of lipids
	public void getSpecificLipids(String givenLipid){
		int totalLipids = this.allLipids.length;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			if ((this.allLipids[currentLipid].Name).equals(givenLipid)){
				this.allLipids[currentLipid].getInformation();

			}	//Ends if statement
		}	//Ends for loop
	}	//Ends getSpecificLipids Method

	public double getBilayerCenter(){
		return this.BilayerCenter;
	}	//ends getBilayerCenter

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
	public void resetFrame(int frameNumber, int totalLipids){
		this.allLipids = new Lipid[totalLipids];
		this.frameNumber = frameNumber;
		this.nextAvailableLipid = 0;
		this.nextFrame = null;
		this.prevFrame = null;

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
	

	//Find the cetner of the bilayer by averaging all lipids terminal carbon Z location. 
	public void findBilayerCenter(){
		double currentZ = 0;
		int totalLipids = this.allLipids.length;		
		double sum = 0;
		double validPoints = 0;

		for (int currentLipid = 0; currentLipid < totalLipids; currentLipid++){
			currentZ = this.allLipids[currentLipid].getTerminalCarbonHeight();
			
			if (currentZ < 4000) {
			//If it is a valid height

				sum = sum + currentZ;
				validPoints++;

			}	//Ends if statement		
		}	//Ends for loop

		this.BilayerCenter = sum / validPoints;
		
	}	//Ends findBilayerCenter Method

	public Frame setFirstFrame(){
		//Return the first item in the LL.
		Frame firstFrame = this;
		if (this.prevFrame != null) {
			firstFrame = this.prevFrame.setFirstFrame();
		}	//ends if statement

		return firstFrame;
	}	//Ends setFirstFrame
}	//Ends class defintion
