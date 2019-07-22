set overrideTotalFrames 0

##	CG_GC Stands for Coarse_Grained_GrabCoordinates
##	TCL Script designned to grab specific coordinates system to system
##	Slightly Different between AA and CG
##	This is the Coarse-Grained Version



#Set System Specififcs Here.

############## BEGIN SYSTEM INFO #####################

set totalFiles 3
set filePath "/N/dc2/scratch/aqphilli/TCLScript/DPPUCH/"
set groName "DPPUCH.gro"

set LipidNames { "DPPC" "PUPC" "CHOL" }
set FlipFloppable { "No" "No" "Yes" }

#Total lipids in sample
set maximumID 1350
#Maximum ID of lipid in upper leaflet that cannot flip-flop. This assumes that all flip-floppable lipids come after this point.
set middleID 500

set Beads {
	{ "GL1" "C1A" "C3A" "C1B" "C3B" }
	{ "GL1" "D1A" "D3A" "C1B" "C3B" }
	{ "R3" "R3" "ROH" }
}

set BeadNames {
	{ "null" "C-Bead" "C-Bead" "C-Bead" "C-Bead" }
	{ "null" "C-Bead" "C-Bead" "C-Bead" "C-Bead" }
	{ "null" "R3" "ROH" }
}

set chainNames {
	{ "null" "A" "A" "B" "B" }
	{ "null" "A" "A" "B" "B" }
	{ "null" "1" "1" }
}

set members {
	{ "-1" "1" "3" "1" "3" }
	{ "-1" "1" "3" "1" "3" }
	{ "-1" "1" "1" }
}

########### END SYSTEM INFO #############

mol delete all
set groFinal $filePath$groName

set outputFileName "Coordinates.dat"
set outputFinal $filePath$outputFileName
set File [open $outputFinal w]
set writingFrame 0

set totalLipids [llength $LipidNames]

proc getXYZ { selection args } {
	set x [$selection get {x}]
	set y [$selection get {y}]
	set z [$selection get {z}]

	set output "$x $y $z"

	return $output
}

proc createOutput { currentFrame lipid resID Leaflet FlipFloppable chain atom member hydrogen xyz File args} {
        #currentFrame == Current Frame
        #lipid == name of the lipid we want.
        #resID == Residue ID
        #atom == string for identifying atoms
        #chain == string for the chain we reside on
        #member == current member along a chain
        #hydrogen == current hydrogen, can be 1-3 (represented as 0,1,2, for easy array implementation)
        #xyz == x,y,z Coordinates

        #invalid integers are saved as -1 (save x,y,z, these are never invalid)

        set output "$currentFrame $lipid $resID $Leaflet $FlipFloppable $chain $atom $member $hydrogen $xyz "
        puts $File $output

}


proc validateID { mainList highestValue args} {
        #Check one list and if each value is beneath an acceptable range (below 342), then we return true (1)
        set result "Upper"

        set mainLength [llength $mainList]

        for {set i 0} {$i < $mainLength} {incr i} {
                set currentItem [lindex $mainList $i]
                if {$currentItem > $highestValue} {
                        set result "Lower"
                }
        }

        return $result
}

proc determineLipid { mol currentFrame currentResID LipidNames } {
	set result -1
	set length [llength $LipidNames]

	for {set i 0} {$i < $length} {incr i} {
		set name [lindex $LipidNames $i]
		set selection [atomselect $mol "resname $name and resid $currentResID"]
		
		set x [$selection get {x}]

		if { $x != "" } {
			set result $i
			set i [expr ($length + 1)]
		}
	}

	return $result
}


proc determineLeaflet { mol currentFrame lipidName resID middleID FlipFloppable } {

	set Leaflets "Upper"

	if { $FlipFloppable == "Yes" } {
	      
		#Select everything within a 1 nm sphere around Cholesterol. That has the bead GL1/AM1, aka all non-flip-floppable lipids
		set mainSel [atomselect $mol "(within 10 of (resname $lipidName and resid $resID)) and (name eq GL1 or name eq AM1)" frame $currentFrame]

		#Recieve a list of Res ID
		set mainList [$mainSel get resid]
		$mainSel delete

		#The res ID indicates what leafelet any lipid is in (becuase lipids dont flip-flop)
		set Leaflet [validateID $mainList $middleID 0]

	} else {
		if { $resID > $middleID } { set Leaflets "Lower" }
	}	

        return $Leaflet
}



set currentFile 1
set totalFiles [expr $totalFiles + 1]

while {$currentFile < $totalFiles} {

	set xtcFinal $filePath
	append xtcFinal "skipped_traj_$currentFile"
	append xtcFinal ".xtc"

	set mol [mol new $groFinal]
	mol addfile $xtcFinal step 50 waitfor all

	set totalFrames [molinfo top get numframes]
	set currentFrame 0
	
	if {$overrideTotalFrames == 1} {set totalFrames 1}

	while {$currentFrame < $totalFrames} {
		for {set currentID 0} {$currentID < $maximumID} {incr currentID} {

			set currentLipid [determineLipid $mol $currentFrame $currentID $LipidNames]
				
			set currentLipidName [lindex $LipidNames $currentLipid]
			set totalBeads [llength [lindex $Beads $currentLipid]]
			set flipFlop [lindex $FlipFloppable $currentLipid]
			set Leaflet [determineLeaflet $mol $currentFrame $currentLipidName $currentID $middleID $flipFlop]

			for {set currentBead 0} {$currentBead < $totalBeads} {incr currentBead} {
				
					set currentBeadName [lindex $Beads $currentLipid $currentBead]
					set currentAtomName [lindex $BeadNames $currentLipid $currentBead]
					set currentChain [lindex $chainNames $currentLipid $currentBead]
					set currentMember [lindex $members $currentLipid $currentBead]

					set selection [atomselect $mol "resname $currentLipidName and resid $currentID and name eq $currentBeadName" frame $currentFrame]
					set xyz [getXYZ $selection 0]
					$selection delete

					createOutput $writingFrame $currentLipidName $currentID $Leaflet $flipFlop $currentChain $currentAtomName $currentMember "-1" $xyz $File 0
			}
		}
		set writingFrame [expr $writingFrame + 1]
		set currentFrame [expr $currentFrame + 1]
	}
	set currentFile [expr $currentFile + 1]
	mol delete all
}


close $File
mol delete all
