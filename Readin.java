//Readin Java File

import java.io.*;
import java.util.Scanner;
import java.util.Arrays;

public class Readin implements Serializable{

	public Readin(){

	}	//Ends Constructor


	//Unserialize a Serialized Object, return said object
	public static Frame getFrame(int x){
		String frameNumber = Integer.toString(x);
		Frame newFrame = null;
		String fileName = "Frames/frame_" + frameNumber + ".ser";

		try{
			FileInputStream file = new FileInputStream(fileName);
			ObjectInputStream objectInput = new ObjectInputStream(file);

			newFrame = (Frame)objectInput.readObject();
			
			objectInput.close();
			file.close();

		}

		catch(ClassNotFoundException e){
			System.out.println("Frame Class not Found.");
		}	//Ends catch statement


		catch(IOException e){
			System.out.println("Issue occured accessing " + fileName);
			System.out.println(e);
			System.out.println("");

		}	//Ends catch statement

		return newFrame;
	}	//Ends getFrame



	public static int findMaximumID(File file) throws FileNotFoundException {
		//Works for specific input, needs to be adjustsed for new files (maybe)
		boolean keepGoing = true;

		Scanner Scan = new Scanner(file);
		Scan.useDelimiter(" ");

		int ID = 0;
		int currentFrame = 0;

		while (keepGoing){
			currentFrame = Scan.nextInt();

			if (currentFrame != 0) {
				keepGoing = false;
			}	//Ends if statement

			else{
				Scan.next();
				ID = Scan.nextInt();
				Scan.nextLine();		
			}	//Ends else statement
		}	//Ends while loop

		return ID;
	}	//Ends findMaximumId

	public static void serializeFrame(String fileName, int frameNumber, Frame Frame){

		//Setup a method of assigning fileNames based off an integer instead of a pre-processed fileName
		if (frameNumber < 2000){
			//Choose an arbitrarily large value such as 2000.

			String frameNumberString = Integer.toString(frameNumber);
			fileName = "Frames/frame_" + frameNumberString + ".ser";

		}	//Ends if statement

		try{
			FileOutputStream fileOutput = new FileOutputStream(fileName);
			ObjectOutputStream objectOutput = new ObjectOutputStream(fileOutput);

			objectOutput.writeObject(Frame);

			fileOutput.close();
			objectOutput.close();

		}       //Ends try statement

		catch (IOException e){
			//Don't do anything, give up.
		}       //Ends Catch statement



	}	//Ends Serialize Frame method


	public static Scanner readOPFile(Scanner Scan, Frame thisFrame, int lastID) throws FileNotFoundException {

		int currentFrame = 0;
		String Lipid;
		int ID = lastID;
		String Chain;
		int Member;
		int Hydrogen;
		float OP;
	
		//We can ignore Frame
		//We can ignore Lipid
		// ( ID - 1 ) Tells us what spot in the array the current lipid is.
		// Everything else is a specific atom.
		//Every new line is a new atom.


		int lastFrame = -1;

		//Scane the whole file
		boolean keepGoing = true;		
		int chainCount = 2;


		while (keepGoing){
			currentFrame = Scan.nextInt();
			Lipid = Scan.next();
			ID = Scan.nextInt();
			Chain = Scan.next();
			Member = Scan.nextInt();
			Hydrogen = Scan.nextInt();
			OP = Scan.nextFloat();

			Scan.nextLine();

			//The OP file and Coordinate File are different (Coords have only top leaflet).
			if (ID > 528){
				//Do Nothing, let the program scan Lines until it reaches a valid point
			}

			else{
				//There are two chains, with 3(2) Hydrogen on each. Once we see 3 hydrogen twice then keepGoing is set to false
				if (Hydrogen == 2){
					chainCount--;
					if (chainCount == 0){
						keepGoing = false;
					}	//Ends if statemetn
				}	//Ends if statement


				if (ID == lastID){
					thisFrame.allLipids[ID - 1].assignChainIdentifier(Chain);
					thisFrame.allLipids[ID - 1].createAtom(Chain, Member, Hydrogen, OP);
				}	//Ends if statement
			}	//Ends else statemetn
		}	//Ends while loop

		return Scan;
	}	//Ends readOPFile method



	public static int readFile() throws FileNotFoundException {

		File file = new File("Coordinates.dat");
		Scanner Scan = new Scanner(file);

		File file_1 = new File("OP_PSM.dat");
		File file_2 = new File("OP_PDPC.dat");
		
		Scanner Scan_1 = new Scanner(file_1);
		Scanner Scan_2 = new Scanner(file_2);

		Scan.useDelimiter(" ");
		Scan_1.useDelimiter(" ");
		Scan_2.useDelimiter(" ");

		Frame Frame = new Frame(9999, 1);

		int currentFrame = 0;
		int previousFrame = 1000;
		String Lipid;
		int ID;
		float X;
		float Y; 
	
		int totalFiles = 0;

		long start = System.currentTimeMillis();
		int maximumID = findMaximumID(file);

		System.out.println("Start File Reading");
		//Scane the whole file
		while (Scan.hasNextLine()){
			currentFrame = Scan.nextInt();
			Lipid = Scan.next();
			ID = Scan.nextInt();
			X = Scan.nextFloat();
			Y = Scan.nextFloat();
			Scan.nextLine();

			//Use these variables as you will.

			if (previousFrame != currentFrame){

				totalFiles++;

				//New Frame is required to be made.
				//Serialize the Old One.
				if (currentFrame == 0){
					//We won't have anything to serialize in this case.
					Frame = new Frame(currentFrame, maximumID);	
				}	//ends if statement

				else{
					//Serialize Old Frame
					//Create new Frame
					String frameString = Integer.toString(previousFrame);
					String fileName = "Frames/frame_" + frameString + ".ser";
	
					//Find the maximum X and Y length that is unique to every frame. Can only be done after every lipid has been viewed.
					Frame.findLength();
			
					serializeFrame(fileName, 9999, Frame);

					//Create new Frame
					Frame.resetFrame(currentFrame);

				}	//Ends else statement
			}	//Ends if statement	

			//Now, assign values to the lipid of interest
			Frame.createLipid(Lipid, ID, X, Y);

			//Now add OP Data to it.
			if (Lipid.equals("PSM")){
				Scan_1 = readOPFile(Scan_1, Frame, ID);	
			}	//Ends if statement


			else if (Lipid.equals("PDPC")){
				Scan_2 = readOPFile(Scan_2, Frame, ID);

			}	//ends else if statement


			previousFrame = currentFrame;
		}	//Ends while loop

		serializeFrame("falseName", (totalFiles-1), Frame);

		long end = System.currentTimeMillis();
		long totalTime = (end - start) / 1000;

		System.out.println("Finished Reading File in  " + totalTime + " seconds");
		
		return totalFiles;
	}	//Ends ReadFile
}	//End class definition
