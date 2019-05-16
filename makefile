Process: Atom.java Lipid.java Frame.java Readin.java Process.java
	javac Atom.java Lipid.java Frame.java Readin.java Process.java
clean:
	rm -f *.class
	
fullClean:
	rm -f *.class
	rm -f Frames/*

run: Process
	java Process
