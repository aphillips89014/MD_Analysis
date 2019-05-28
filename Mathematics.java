//Mathematics Class

//Class used for the various methods that are needed for various situations.
//Everything will be static, this object will never be instantiated.


//Access a method here by typing::: Mathematics.methodName()
public class Mathematics{

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


	//For the data sets we are concerned with the points exist in a box. We need to preform a special operation whenever these are within a searchRadius
	//This method returns 0, -1, or 1. This indicates which boundary it is close to.
	// 0 --> Not near a boundary
	// 1 --> Near the right (positive) boundary
	// -1 --> Near the left (negative) boundary
	public static int checkBoundary(double point, double length, int searchRadius){

		int result = 0;
		double halfLength = length / 2;
		boolean negative = false;

		if (point < 0) {
			negative = true;
			point = point * -1;
		}       //Ends if statement

		//This equation makes the point negative if it is outside the searchRadius, and makes it positive if it is within the SearchRadius
		//Only works is point and searchRadius are less than length
		point = point - (length - searchRadius);

		if (point < 0) {
			result = 0;
		}       //Ends if statement

		else{
			result = 1;
		}       //Ends else statement


		if (negative == true){
			result = result * -1;
		}       //Ends if statement

		return result;
	}       //Ends checkBoundary method

	//If a point is within the searchRadius of a Boundary of the box, this function is executed.
	//This shifts the point to a new location purely for a simpler more accurate calculation.
	public static double applyPBC(double coordinate, int modifier, double length){
		//PBC stands for Periodic Boundary Condition
		//modifier can only be 1, -1, or 0.

		double result = coordinate;

		//If the modifier is 0 then dont do a thing.

		if (modifier != 0){
			if (coordinate < 0){
				if (modifier == 1){
					result = result + length;
				}       //Ends if statement

				else if (modifier == -1){
					//Do nothing.
				}       //Ends if statement
			}       //Ends if statement

			else if (coordinate >= 0){
				if (modifier == -1){
					result = result - length;
				}       //Ends if statement

				else if (modifier == 1){
					//Do Nothing
				}       //Ends if statement
			}       //Ends if statement
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



	public static double calculateOP(double x1, double y1, double z1, double x2, double y2, double z2){

		double xDiff = Math.pow((x1 - x2), 2);
		double yDiff = Math.pow((y1 - y2), 2);
		double zDiff = Math.pow((z1 - z2), 2);

		double magnitude = Math.pow((xDiff + yDiff + zDiff), 0.5);
		double cosTheta = (z1 - z2) / magnitude;

		cosTheta = Math.pow(cosTheta, 2);

		double OP = (3*cosTheta - 1 ) / 2;
		return OP;
	}       //Ends calculateOP method

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
