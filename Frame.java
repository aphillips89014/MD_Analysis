//Frame Class
//Holds every Lipid
import java.io.Serializable;

public class Frame implements java.io.Serializable {
	private static final long serialVersionUID = 2;
	int frameNumber = 0;
	Lipid[] allLipids;
	int totalLipids = 0;
	float xLength = 0;
	float yLength = 0;

	public Frame(int frameNumber, int totalLipids){
		this.frameNumber = frameNumber;
		this.allLipids = new Lipid[totalLipids];
		this.totalLipids = totalLipids;

	}	//Ends Constructor


	public void createLipid(String Name, int ID, float X, float Y){
		this.allLipids[ID-1] = new Lipid(Name, ID, X, Y);

	}	//Ends createLipid Method

	public int getFrameNumber(){
		return this.frameNumber;
	}	//Ends getFrameNumber method

	public float getXLength(){
		return this.xLength;
	}	//ends getXLength method

	public float getYLength(){
		return this.yLength;
	}	//Ends getYLength method


	public void resetFrame(int frameNumber){
		this.allLipids = new Lipid[this.totalLipids];
		this.frameNumber = frameNumber;

	}	//Ends resetFrame

	//Find the maximum and minimum x and y lengths for this current frame
	//Iterate through every lipid and find the maximum x and y values.
	public void findLength(){
		float maxX = 0;
		float maxY = 0;
		float minX = 0;
		float minY = 0;

		float x = 0;
		float y = 0;

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

	}	//Ends findLength method


}	//Ends class defintion
