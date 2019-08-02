#Python File for making graph files
#This File generates a makefile with a few commands

import subprocess as sp

def addLines(w, baseStatement, Pairs, GraphName, Arguement_Reminder, Upper_Lower):

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

			length = 1
			counter = 0
			usePairs = "false"
			if (Graphing_Key == "Pairs"):
				counter = 1
				length = len(Pairs)
				usePairs = "true"

			keepGoingInside = "true"
	
			while (keepGoingInside == "true"):
				if (counter == length):
					keepGoingInside = "false"

				else:
					if (usePairs == "true"):
						print("Current Pair: " + str(Pairs[counter]))
						Graphing_Key = Pairs[counter]

					print("Input Title")
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


					counter = counter + 1					


		else:
			keepGoing = "False"


def getPairs():
	# This function will gather any pairs of names and save them for later.
	response = input("Would you like to use pairs? (Y/N)")
	result = [""]
	if (response == "N"):
		result[0] = "No Pair"
		print("No pair based graphs will be generated")
	else:
		keepGoing = "true"
		print("Input your pairs as:")
		print("L1 L2 L3 L4")
		print("Type END when done.")
		while (keepGoing == "true"):
			response = input("")

			if (response == "END"):
				keepGoing = "false"

			else:
				result.append(response)
			

	return result


w = open("makefile", "w")

w.write("clean:\n")
w.write("	rm Graphs/*\n")
w.write("Graph:\n")

baseStatement = "	gnuplot -e \"set terminal png size 960,720; set output 'Graphs/"
GraphNames = ["CosTheta_Histogram.plot ", "NN_Histogram.plot ", "OP.plot ", "OPvNN.plot ", "PCL.plot ", "Probability.plot ", "Thickness.plot ", "Registration.plot ", "Dipole.plot ", "Angles.plot "]

Pairs = getPairs()

addLines(w, baseStatement, Pairs, GraphNames[0], "Lipid Chain", "True")
addLines(w, baseStatement, Pairs, GraphNames[1], "Maximum_Lipids L1 ... LN", "True")
addLines(w, baseStatement, Pairs, GraphNames[2], "Lipid", "True")
addLines(w, baseStatement, Pairs, GraphNames[3], "Pairs", "True")
addLines(w, baseStatement, Pairs, GraphNames[4], "Lipid", "True")
addLines(w, baseStatement, Pairs, GraphNames[5], "Pairs", "True")
addLines(w, baseStatement, Pairs, GraphNames[6], "Lipid", "False")
addLines(w, baseStatement, Pairs, GraphNames[7], "Maximum_Lipids L1 ... LN", "False")
addLines(w, baseStatement, Pairs, GraphNames[8], "L1 L2", "True")
addLines(w, baseStatement, Pairs, GraphNames[9], "", "False")
