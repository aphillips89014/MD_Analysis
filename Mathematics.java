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


	public static int checkBoundary(double point, double length, double searchRadius, boolean LengthCanBeNegative){
		//Two Scenarios, those being if Length can be Negative

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
		
//		if (deviation < 0) { deviation = deviation * -1; }

		deviation = Math.pow(deviation, 0.5);

		return deviation;
	}       //ends find Deviation Method


	public static double calculateCosTheta(double x1, double y1, double z1, double x2, double y2, double z2){
	
		double xDiff = Math.pow((x1 - x2), 2);
		double yDiff = Math.pow((y1 - y2), 2);
		double zDiff = Math.pow((z1 - z2), 2);

		double magnitude = Math.pow((xDiff + yDiff + zDiff), 0.5);
		double cosTheta = (z1 - z2) / magnitude;
	
		return cosTheta;
	}	//Ends calculateOP method

	public static double calculateOP(double cosTheta){
		double cosThetaSquared = Math.pow(cosTheta, 2);

		double OP = ((3*cosThetaSquared) - 1 ) / 2;
		return OP;
	}       //Ends calculateOP method

	//Take an OP and transform it into the appropraite angle.
	public static double reverseOP(double OP){
		double Angle = ((OP * 2) + 1) / 3;	//Undo OP equation
		Angle = Math.pow(Angle, 0.5);		//Make it not squared
		Angle = Math.acos(Angle);		//Make it an angle (radians)
		Angle = Angle * (180 / Math.PI);	//Make it in terms of degrees.

		return Angle;
	}	//Ends reverseOP

	//Get the OP, but in terms of cos Theta
	public static double reverseOP_CosTheta(double OP){
		double cosTheta = ((OP * 2) + 1) /3;
		cosTheta = Math.pow(cosTheta, 0.5);

		return cosTheta;
	}	//Ends reverseOP_CostTheta


	public double sumArray(double[] array){
		int length = array.length;
		double sum = 0;

		for (int index = 0; index < length; index++){
			sum = sum + array[index];

		}	//Ends for loop

		return sum;
	}	//Ends average array method


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
