Need multiple files and directories when this is ran.

Wherever you run it, you need one Data Files:
	A Coordinates File
		Organized Like:
			Frame Lipid_Name Residue_ID Leaflet FlipFloppable(Yes/No) Chain_Identifier Element Member Hydrogen_Number X Y Z

			Where new lipids are defined as:
				Frame Lipid Molecule_ID Leaflet FlipFlop Null Null -1 -1 X Y Z
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

When the file is ran frames from the coordinate file will be packaged togethor and stored as a serialized object. This is so in the future it can quickly be accessed again.

The output designed for Graphing will be output to the Graphing/Data/ directory.

All input data should be held in the same directory as where the program is executed.

To Run the Program:
Just Activate the MD_Analysis.jar File in the Directory with the Input Files and Output Directories, and you're good.



Side Note:
In the Graphing Directory there are a series of GNUPLOT Scripts and a Python script called Graph.py
	These are there to make process of interpreting the outputted data files as easy as possible.

In the TCL Directory:
	These are the scripts associated with looking at a system and extracting the relevant Coordinates.dat file.
	To modify one:
		Open it up and change the number of entries in each array to match the relevant aspect of a system.
		Most of the information required can be found in the Structure file for the system.





