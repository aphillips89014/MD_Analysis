set overrideTotalFrames 0

##	AA_CG Stands for All_Atom_GrabCoordinates
##	TCL Script designned to grab specific Coordinates System to System
##	Slight Differences Between AA and CG
##	This is the All-Atom Version



#Set System Specifics Here

############## BEGIN SYSTEM INFO #####################

set totalFiles 1
set filePath "/N/dc2/scratch/aqphilli/TCLScript/Anton/"
set psfName "step5_assembly.xplor_ext.psf"

set LipidNames { "PSM" "PDPC" "CHL1" }
set LipidType { "Normal" "Normal" "Special" }

set totalResID 1056
set middleResID 555
#Everything above the middleResID is in the Lower Leaflet

#Bead info is organized as the following:
#	First Chain Identifier, First Member, Last Member, H1, H2, H3, Second Chain Identifier, ...
# 	For special Atoms just make a list of the specific beads selected.
set BeadInformation {
	{ "F" "2" "16" "F" "G" "H" "S" "2" "18" "S" "T" "U" }
	{ "3" "2" "16" "X" "Y" "Z" "2" "3" "22" "R" "S" "T" }
	{ "C3" "H3" }
}

set CoMBeads {
	{ "P" "C3S" "C4S" "C2F" "C3F" }
	{ "P" "C22" "C23" "C32" "C33" }
	{ "C16" "C10" "C13" "C8" "C3" }
}

#Sometimes NAMD Forcefield generates odd names, so we may need to adjust lipid to lipid, do this in in resetSelection Method.
proc resetSelection {lipid element member suffix args} {
	#Say you want to look at H18T
	#Element == H
	#Member == 18
	#Suffix == T
		#Suffix also often corresponds to the specific chain.

	#specify lipid to determine the ordering method.
	#Changes system to system
	set selection ""

	if {$lipid == "PSM"} {
		set selection $element
		append selection $member $suffix

	} elseif {$lipid == "PDPC"} {

		if {$element == "C"} {
			set selection $element
			append selection $suffix $member

		} elseif {$element == "H"} {

			set selection $element
			append selection $member $suffix
		}

	} elseif {$lipid == "CHL1"} {


	}

	return $selection
}



############## END SYSTEM INFO ########################


set psfFinal $filePath$psfName

set outputPath "/N/dc2/scratch/aqphilli/TCLScript/Anton/"
set output "Coordinates.dat"
set outputFinal $outputPath$output
set File [open $outputFinal w]

set currentFrame 0
set zCarbon 0

proc createOutput { currentFrame lipid resID Leaflet FlipFloppable chain atom member hydrogen xyz File args} {
        #currentFrame == Current Frame
        #lipid == name of the lipid we want.
        #resID == Residue ID
 	#Leaflet == Upper or Lower Leaflet
	#FlipFloppable == Can it FlipFlop		(Always no in AA)
        #chain == string for the chain we reside on
        #atom == string for identifying atoms
        #member == current member along a chain
        #hydrogen == current hydrogen, can be 1-3 (represented as 0,1,2, for easy array implementation)
        #xyz == x,y,z Coordinates

        #invalid integers are saved as -1 (save x,y,z, these are never invalid)

        set output "$currentFrame $lipid $resID $Leaflet $FlipFloppable $chain $atom $member $hydrogen $xyz "
        puts $File $output

}

proc getCoordinates { atomSel args } {
	set x [$atomSel get {x}]
	set y [$atomSel get {y}]
	set z [$atomSel get {z}]

	set xyz "$x $y $z"

	return $xyz
}

proc determineLeaflet { resID middleResID } {
	set result "null"
	if {$resID <= $middleResID} {
		set result "Upper"
	} else {
		set result "Lower"
	}
	return $result
}

proc whichLipid { resID mol currentFrame lipidNames args } {
	set output ""

	set totalLipids [llength $lipidNames]

	for {set i 0} {$i < $totalLipids} {incr i} {
		set currentLipid [lindex $lipidNames $i]
		
		set selection [atomselect $mol "resname $currentLipid and resid $resID" frame $currentFrame]
		set coordinate [$selection get {x}]
		$selection delete

		if {$coordinate != ""} {
			set output $i
			set i $totalLipids
		}
	}

	return $output
}


proc findCenterOfMass { mol currentFrame selectionList lipid id args } {
	set x 0
	set y 0
	set z 0

	set totalSelections [llength $selectionList]

	for {set i 0} {$i < $totalSelections} {incr i} {
		set atom [lindex $selectionList $i]
		set selection [atomselect $mol "resname $lipid and resid $id and name eq $atom" frame $currentFrame]

		set selX [$selection get {x}]
		set selY [$selection get {y}]
		set selZ [$selection get {z}]

		set x [expr $x + $selX]
		set y [expr $y + $selY]
		set z [expr $z + $selZ]

		$selection delete
	}

	set x [expr $x / $totalSelections]
	set y [expr $y / $totalSelections]
	set z [expr $z / $totalSelections]

	set output "$x $y $z"

	return $output
}




set currentFile 1
set totalFiles [expr ($totalFiles + 1)]
set totalResID [expr ($totalResID + 1)]
set writingFrame 0

while {$currentFile < $totalFiles} {

	set dcdFinal $filePath
	append dcdFinal "step8.$currentFile"
	append dcdFinal "_production.dcd"
	
	set mol [mol new $psfFinal]
	mol addfile $dcdFinal step 50 waitfor all
	
	set totalFrames [molinfo top get numframes]
	set currentFrame 0

	if {$overrideTotalFrames == 1} { set totalFrames 1 }

	while {$currentFrame < $totalFrames} {
		set currentResID 1

		while {$currentResID < $totalResID} {
			
			set LipidIndex [whichLipid $currentResID $mol $currentFrame $LipidNames 0]

			set Leaflet [detmineLeaflet $currentResID $middleResID]			

			set Type [lindex $LipidType $LipidIndex]
			set Name [lindex $LipidNames $LipidIndex]
			set CoM_Beads [lindex $CoMBeads $LipidIndex]
			set Beads [lindex $BeadInformation $LipidIndex]

			set xyz [findCenterOfMass $mol $currentFrame $CoM_Beads $Name $currentResID 0]

			createOutput $writingFrame $Name $currentResID $Leaflet "No" "null" "null" "-1" "-1" $xyz $File 0

			if {$Type == "Normal"} {
				set lipid $Name

				set firstChain [lindex $Beads 0]
				set first_firstCarbon [lindex $Beads 1]
				set first_lastCarbon [lindex $Beads 2]
				set first_firstHydrogen [lindex $Beads 3]
				set first_secondHydrogen [lindex $Beads 4]
				set first_thirdHydrogen [lindex $Beads 5]

				set secondChain [lindex $Beads 6]
				set second_firstCarbon [lindex $Beads 7]
				set second_lastCarbon [lindex $Beads 8]
				set second_firstHydrogen [lindex $Beads 9]
				set second_secondHydrogen [lindex $Beads 10]
				set second_thirdHydrogen [lindex $Beads 11]


				#Find the XYZ for the Phosphate
				set phosphateSel [atomselect $mol "resname $lipid and resid $currentResID and name eq P" frame $currentFrame]

				set xyz [getCoordinates $phosphateSel 0]
				createOutput $writingFrame $lipid $currentResID $Leaflet "No" "null" "P" "-1" "-1" $xyz $File 0

				$phosphateSel delete



				set currentChain $firstChain

				while {$currentChain != $secondChain} {
					set currentMember $first_firstCarbon
					set lastMember $first_lastCarbon
				
					set firstHydrogen $first_firstHydrogen
					set secondHydrogen $first_secondHydrogen
					set thirdHydrogen $first_thirdHydrogen
				

					set keepGoing 1

					while {$keepGoing == 1} {
						set currentCarbon [resetSelection $lipid "C" $currentMember $currentChain 0]

						set carbonSel [atomselect $mol "resname $lipid and resid $currentResID and name $currentCarbon" frame $currentFrame]
						set zCarbon [$carbonSel get {z}]
			

						if {$zCarbon == ""} {
							if {$currentChain == $secondChain} {set keepGoing 0}
							set currentChain $secondChain

							set currentMember $second_firstCarbon
							set lastMember $second_lastCarbon

							set firstHydrogen $second_firstHydrogen
							set secondHydrogen $second_secondHydrogen
							set thirdHydrogen $second_thirdHydrogen


						} else {
		

							set xyz [getCoordinates $carbonSel 0]
							createOutput $writingFrame $lipid $currentResID $Leaflet "No" $currentChain "C" $currentMember "-1" $xyz $File 0
						
							#get First Hydrogen
							set currentHydrogen [resetSelection $lipid "H" $currentMember $firstHydrogen 0]
							set hydrogenSel [atomselect $mol "resname $lipid and resid $currentResID and name $currentHydrogen" frame $currentFrame]
							set xyz [getCoordinates $hydrogenSel 0]
							createOutput $writingFrame $lipid $currentResID $Leaflet "No" $currentChain "H" $currentMember "0" $xyz $File 0
							$hydrogenSel delete


							#get Second Hydrogen
							set currentHydrogen [resetSelection $lipid "H" $currentMember $secondHydrogen 0]
							set hydrogenSel [atomselect $mol "resname $lipid and resid $currentResID and name $currentHydrogen" frame $currentFrame]		
							set checkIfValid [$hydrogenSel get {x}]

							if {$checkIfValid != ""} {
								set xyz [getCoordinates $hydrogenSel 0]
								createOutput $writingFrame $lipid $currentResID $Leaflet "No" $currentChain "H" $currentMember "1" $xyz $File 0
							}

							$hydrogenSel delete


							#get Third Hydrogen
							if {$currentMember == $lastMember} {
								set currentHydrogen [resetSelection $lipid "H" $currentMember $thirdHydrogen 0]
								set hydrogenSel [atomselect $mol "resname $lipid and resid $currentResID and name $currentHydrogen" frame $currentFrame]
								set xyz [getCoordinates $hydrogenSel 0]
								createOutput $writingFrame $lipid $currentResID $Leaflet "No" $currentChain "H" $currentMember "2" $xyz $File 0
								$hydrogenSel delete
							}


							set currentMember [expr $currentMember + 1]
						}
						
						$carbonSel delete
					}
				}


			} elseif {$Type == "Special"} {
				set lipid $Name

				set totalBeads [llength $Beads]

				#Select every given bead.
				for {set i 0} {$i < $totalBeads} {incr i} {
					set currentBead [lindex $Beads $i]
					
					set selection [atomselect $mol "resname $lipid and resid $currentResID and name eq $currentBead" frame $currentFrame]
					set xyz [getCoordinates $selection 0]
					createOutput $writingFrame $lipid $currentResID $Leaflet "No" "null" $currentBead "-1" "-1" $xyz $File 0

					$selection delete
				}
			}

			set currentResID [expr $currentResID + 1]
		}
		set currentFrame [expr $currentFrame + 1]
		set writingFrame [expr $writingFrame + 1]
	}		
	set currentFile [expr $currentFile + 1]
}

close $File
mol delete all
