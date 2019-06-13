Process: Mathematics.java Atom.java Lipid.java Frame.java Readin.java Process.java
	javac Mathematics.java Atom.java Lipid.java Frame.java Readin.java Process.java
clean:
	rm -f *.class
	
fullClean:
	rm -f *.class
	rm -f Frames/*
	rm -f Graphing/Data/*

dataClean:
	rm -f Graphing/Data/*

jar:
	rm MD_Analysis.jar
	jar cvfm MD_Analysis.jar manifest.mf *.*

run: Process
	java Process AllFrames

firstFrame: Process
	java Process FirstFrame
