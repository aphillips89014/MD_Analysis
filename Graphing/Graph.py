#Python File for making graph files
#This File generates a makefile with a few commands

import subprocess as sp

def addLines(w, baseStatement, GraphName, Arguement_Reminder, Upper_Lower):

	tmp = sp.call('clear',shell=True)
	keepGoing = "true"

	print("Would you like to graph: " + GraphName + "    Y/N")

	while (keepGoing == "true"):

		response = input("Countine? (Y/N)")
		i = 0

		if (response != "N"):
			print(Arguement_Reminder)
			print("")
			print("Input Arguement Key:")
			Graphing_Key = input("")
			print("Input Title:")
			Title = input("")

			if (Upper_Lower == "True"):

				Up_Title = Title + "_Upper.png'\" "

				Graph_Input = baseStatement + Up_Title + "-c " + GraphName + "Upper " + Graphing_Key

				if (Graphing_Key != ""):
					w.write(Graph_Input +"\n")

					Down_Title = Title + "_Lower.png'\" "

					Graph_Input = baseStatement + Down_Title + "-c " + GraphName + "Lower " + Graphing_Key
					w.write(Graph_Input +"\n")

			else:
				Title = Title + ".png'\" "
				Graph_Input = baseStatement + Title + "-c " + GraphName + Graphing_Key
				if (Graphing_Key != ""):
					w.write(Graph_Input + "\n")
			

		else:
			keepGoing = "False"





w = open("makefile", "w")

w.write("clean:\n")
w.write("	rm Graphs/*\n")
w.write("Graph:\n")

baseStatement = "	gnuplot -e \"set terminal png size 960,720; set output 'Graphs/"
GraphNames = ["CosTheta_Histogram.plot ", "NN_Histogram.plot ", "OP.plot ", "OPvNN.plot ", "PCL.plot ", "Probability.plot ", "Thickness.plot ", "Registration.plot "]

addLines(w, baseStatement, GraphNames[0], "Lipid Chain", "True")
addLines(w, baseStatement, GraphNames[1], "Maximum_Lipids L1 ... LN", "True")
addLines(w, baseStatement, GraphNames[2], "Lipid", "True")
addLines(w, baseStatement, GraphNames[3], "L1 L2 L3 L4", "True")
addLines(w, baseStatement, GraphNames[4], "Lipid", "True")
addLines(w, baseStatement, GraphNames[5], "L1 L2 L3 L4", "True")
addLines(w, baseStatement, GraphNames[6], "Lipid", "False")
addLines(w, baseStatement, GraphNames[7], "Maximum_Lipids L1 ... LN", "False")


