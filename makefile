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

run: Process
	java Process
