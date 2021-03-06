//Mathematics Class

//Class used for the various methods that are needed for various situations.
//Everything will be static, this object will never be instantiated.


//Access a method here by typing::: Mathematics.methodName()
public class Mathematics{

	public static double[] normalizeVector(double[] vector){
		//Take a given vector of any dimensions and normalize it.
		int totalCoordinates = vector.length;
		double magnitude = 0;	

		for (int i = 0; i < totalCoordinates; i++){
			double squared = vector[i] * vector[i];
			magnitude = magnitude + squared;

		}	//Ends for loop

		magnitude = Math.pow(magnitude, 0.5);

		for (int i = 0; i < totalCoordinates; i++){
			vector[i] = vector[i] / magnitude;

		}	//Ends for loop

		return vector;
	}	//Ends normalizeVector method

	public static double calculateDotProduct(double[] vector1, double[] vector2){
		double dotProduct = -5;
		int length1 = vector1.length;
		int length2 = vector2.length;
		if (length1 == length2){
			dotProduct = 0;
			for (int index = 0; index < length1; index++){
				dotProduct = dotProduct + (vector1[index] * vector2[index]);

			}	//Ends for loop
		}	//Ends if statement
		else{
			System.out.println("Coordinate Systems do not match up...");
			System.out.println("Failed Calculating Dot Product.");
		}	//Ends else statement
	

		return dotProduct;
	}	//Ends calculateDotProduct Method



	//Find the length (radius) between two points
	public static double calculateRadius(double firstX, double firstY, double secondX, double secondY){

		double xDiff = (firstX - secondX);
		double yDiff = (firstY - secondY);

		xDiff = xDiff * xDiff;
		yDiff = yDiff * yDiff;

		double radius = xDiff + yDiff;
		radius = Math.pow(radius, 0.5);

		return radius;
	}       //End calculateRadius Method


	public static int checkBoundary(double point, double length, double searchRadius, boolean LengthCanBeNegative){
		//Two Scenarios, those being if Length can be Negative
		// Typically if your origin is in the bottom left corner of your simulation length can NOT be negative.
		//		If it's in the center, length can be negative

		int result = 0;
		//If result is 0, then it is NOT near the boundary.
		//if result is 1, then it is near the RIGHT (positive) side of the boundary)
		//if result is -1, then it near the LEFT (negative) side of the boundary.


		if (LengthCanBeNegative){
			boolean isPointNegative = false;

			//Make sure point is a positive value.
				//Keep track of if it was ever negative.			
			if (point < 0) {
				isPointNegative = true;
				
				point = point * -1;

			}	//Ends if statement

			//Now, we will use a special equation

			double distanceFromBoundary = point - (length - searchRadius);
				//This equation will be POSITIVE if it is NEAR the boundary.
				//This equation will be NEGATIVE if it is NOT near the boundary.

			if (distanceFromBoundary >= 0){
				result = 1;

				if (isPointNegative) { result = -1; }
			}	//Ends if statement
		}	//Ends if statment

		else{
			//In case the box stops at 0 and L.

			//Basically iteratively figure out if it is within the edges.
			if ((point + searchRadius) >= length){
				//IN this case its at the RIGHT (L) Boundary.
				result = 1;				

			}	//Ends if statement

			else if ((point - searchRadius) <= 0){
				//In this case its at the LEFT (0) Boundary.
				result = -1;
			}	//ends if statement

			else{
				result = 0;
			}	//ends else statement
		}	//Ends else statement

		return result;
	}       //Ends checkBoundary method

	//If a point is within the searchRadius of a Boundary of the box, this function is executed.
	//This shifts the point to a new location purely for a simpler more accurate c44alculation.
	public static double applyPBC(double coordinate, double modifier, double length, boolean LengthCanBeNegative){
		//PBC stands for Periodic Boundary Condition
		//modifier can only be 1, -1, or 0.

		double result = coordinate;

		//If the modifier is 0 then dont do a thing.

		if (modifier != 0){

			//Length goes from -(1/2)L to (1/2)L.
			if (LengthCanBeNegative) {

				//Coordinate is on the left side
				if (coordinate < 0){

					//Modifier is on the right side
					if (modifier == 1){
						result = coordinate + length;
					}       //Ends if statement

					//Modifier is on the left side
					else if (modifier == -1){
						//Do nothing.
					}       //Ends if statement
				}       //Ends if statement

				//Coordinate is on the right side
				else if (coordinate >= 0){
					
					//Modifier is on the left side
					if (modifier == -1){
						result = coordinate - length;
					}       //Ends if statement

					//Modifier is on the right side.
					else if (modifier == 1){
						//Do Nothing
					}       //Ends if statement
				}       //Ends if statement
			}	//Ends if statement



			//Length goes from 0 to L.
			else{
				double halfLength = length / 2;
	
				//Left Side
				if (modifier == -1){

					//Coordinate is on the right side.
						//Don't do anything if Coordinate is on the left side.
					if (coordinate >= halfLength) {
						coordinate = coordinate - length;
						result = coordinate;

					}	//Ends if statement
				}	//Ends if statement

				//Right side
				else if (modifier == 1) {

					//Coordinate is on the left side
						//Dont do anything if the coordinate is on the right side.
					if (coordinate <= halfLength){
						coordinate = coordinate + length;
						result = coordinate;

					}	//Ends if statement
				}	//Ends else if satement

			}	//Ends else statement
		}       //Eends if statement

		return result;
	}       //Ends applyPBC method

	//Finds Standard Deviation
	public static double calculateDeviation(double value, double squaredValue){
		value = value * value;

		double deviation = squaredValue - value;

		deviation = Math.pow(deviation, 0.5);

		return deviation;
	}       //ends find Deviation Method


	public static double calculateCosTheta(double x1, double y1, double z1, double x2, double y2, double z2){
	
		double xDiff = x1 - x2;
		double yDiff = y1 - y2;
		double zDiff = z1 - z2;

		double xDiff2 = xDiff * xDiff;
		double yDiff2 = yDiff * yDiff;
		double zDiff2 = zDiff * zDiff;

		double magnitude = Math.pow((xDiff2 + yDiff2 + zDiff2), 0.5);
		double cosTheta = (z1 - z2) / magnitude;
	
		return cosTheta;
	}	//Ends calculateOP method

	public static double calculateOP(double cosTheta){
		double cosThetaSquared = Math.pow(cosTheta, 2);

		double OP = ((3*cosThetaSquared) - 1 ) / 2;
		return OP;
	}       //Ends calculateOP method

	public static int sumArray(int[] array){
		int length = array.length;
		int sum = 0;

		for (int index = 0; index < length; index++){
			sum = sum + array[index];

		}	//Ends for loop

		return sum;
	}	//Ends average array method
	
	public static double sumArray(double[] array){
		int length = array.length;
		double sum = 0;

		for (int index = 0; index < length; index++){
			sum = sum + array[index];

		}	//Ends for loop

		return sum;
	}	//Ends average array method
	


	public static boolean isValidLipid(String name, String[] validLipidArray){
		//Checks the given name against the given array, if the given name is inside the given array it is TRUE
		int length = validLipidArray.length;		
		boolean result = false;
		
		for (int i = 0; i < length; i++){
			if (name.equals(validLipidArray[i])){
				result = true;
			}	//Ends if statement
		}	//Ends for loop


		return result;
	}	//Ends isValidLipid

	public static int LeafletToInt(boolean Leaflet){
		int result = -1;
		if (Leaflet == true){ result = 0; }
		else if (Leaflet == false) { result = 1; }
		else { System.out.println("NOT UPPER OR LOWER LEAFLET!!!"); }

		return result;
	}	//Ends LeafletToInt Method

	public static String IntToLeaflet_STR(int x){
		String result = "";
		if (x == 0) { result = "Upper";}
		else if (x == 1) { result = "Lower";}
		else { System.out.println("NOT UPPER OR LOWER"); }

		return result;
	}	//Ends IntToLeaflet Method

	public static boolean IntToLeaflet_BOOL(int x){
		boolean result = false;

		if (x == 0) { result = true; }
		else if (x == 1) { result = false; }		

		return result;
	}	//Ends IntToLeaflet Method


	public static String IntToLipid(int x, String[] lipidNames){
		//Converts a given int to a specific string.
		String output = "null";
		output = lipidNames[x];

		return output;
	}       //Ends IntToLipid Method

	//Return a specific integer based off lipid name
	public static int LipidToInt(String[] lipidNames, String Name){
		int result = 99;

		int length = lipidNames.length;

		for (int i = 0; i < length; i++){
			if (Name.equals(lipidNames[i])) { result = i; }

		}       //ends for loop

		return result;
	}       //Ends LipidToInt
}	//Ends class Defintion
