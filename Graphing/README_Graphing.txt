All of the data should be help in the Data Directory and is ready to be plotted.
This directory is for the actual files for plotting, access the data files by having the prefix Data/ on each filename String.

The graphing files available are to be plotted in gnuplot.

They should be plotted by using the following command directly in the command line:

	gnuplot -c GRAPH.plot LIPID_1 LIPID_2 ...

	Where GRAPH.plot is the plotting file you want to plot
	And LIPID_1 LIPID_2 ... are the arguements that will be accepted into each graphing file. Just use the same names as used for the Lipids and it will choose those files. This allows for fast and precise graphing whenever you want.

	There are a number of arguements for each command though.

	NN_Histogram has 3
	OP has 1
	PCL has 1
	Thickness has 1
	OPvNN Chains has 4
	OPvNN NoChains has 4
	Probabillity has 4

	If there is a plot where there are typically pairs of data (A compared to B)(OPvNN, Probabillity) just set the pairs sequentially.
	

	For example, if I want to plot the OPvNN of A as a function of the NN of B and C I would do the following:

	gnuplot -c OPvNN_NoChains.plot A B A C

That's it.

