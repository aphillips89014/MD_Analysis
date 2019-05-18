Need multiple files and directories when this is ran.

Wherever you run it, you need three Data Files:
	A Coordinates File
		Organized Like:
			Frame LipidName MoleculeID X Y

	OP File for one lipid
		Organized Like:
			Frame LipidName MoleculeID ChainIdentifier CarbonIndex HydrogenNumber OrderParameter

	OP File for another lipid
		Organized in the Same way 


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


