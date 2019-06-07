Need multiple files and directories when this is ran.

Wherever you run it, you need one Data Files:
	A Coordinates File
		Organized Like:
			Frame Lipid_Name Residue_ID Chain_Identifier Element Member Hydrogen_Number X Y Z

			Where new lipids are defined as:
				Frame Lipid Molecule_ID Null Null -1 -1 X Y Z
					X Y Z indicate the CoM for the Lipid.

	File should be called Coordinates.dat





You also need Two Directories and one Sub Directory
	Need a Directory Labelled Frames
	Need a Directory Labelled Graphing
		Need a Sub Directory in Graphing called Data

	Or in other words, have access to the following files:

	~/Frames/
	~/Graphing/
	~/Graphing/Data/

When ran, for every Frame in your data set, an object will be created and Serialized. These serialized objects will be stored in the Frames/ directory.
The output designed for Graphing will be output to the Graphing/Data/ directory.

All input data should be held in the same directory as where the program is executed.

To Run the Program:
Just Activate the MD_Analysis.jar File in the Directory with the Input Files and Output Directories, and you're good.



