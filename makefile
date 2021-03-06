Process: Mathematics.java Atom.java Lipid.java Frame.java Readin.java Process.java
	javac Mathematics.java Atom.java Lipid.java Frame.java Readin.java Process.java
clean:
	rm -f *.class
	
fullClean:
	rm -f *.class
	rm -f Frames/*
	rm -f Graphing/Data/*
	rm -f Graphing/Graphs/*
	clear

dataClean:
	rm -f Graphing/Data/*

jar:
	rm MD_Analysis.jar
	jar cvfm MD_Analysis.jar manifest.mf *.*

run: Process
	java Process

nFrames: Process
	java Process $(s) $(f)

firstFrame: Process
	java Process 0 1
