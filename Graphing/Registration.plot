MaxLipids = ARG1

lipid_1 = ARG2
lipid_1_ls = 1

lipid_2 = ARG3
lipid_2_ls = 2

lipid_3 = ARG4
lipid_3_ls = 3

lipid_4 = ARG5
lipid_4_ls = 4

set style line 1 \
    linecolor rgb 'blue' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 2 \
    linecolor rgb 'red' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 3 \
    linecolor rgb 'green' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 4 \
    linecolor rgb 'purple' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set yrange[0:1]


set style fill solid

set title "Registration"
set xlabel "Time"
set ylabel "Registration %"

fileName_1 = "Data/".lipid_1."_Registration.dat"
fileName_2 = "Data/".lipid_2."_Registration.dat"
fileName_3 = "Data/".lipid_3."_Registration.dat"
fileName_4 = "Data/".lipid_4."_Registration.dat"


if (MaxLipids == 2) {

	plot fileName_1 title lipid_1 ls lipid_1_ls,\
	fileName_2 title lipid_2 ls lipid_2_ls
}

if (MaxLipids == 3) {

	plot fileName_1 title lipid_1 ls lipid_1_ls,\
	fileName_2 title lipid_2 ls lipid_2_ls,\
	fileName_3 title lipid_3 ls lipid_3_ls
}

if (MaxLipids == 4) {

	plot fileName_1 title lipid_1 ls lipid_1_ls,\
	fileName_2 title lipid_2 ls lipid_2_ls,\
	fileName_3 title lipid_3 ls lipid_3_ls,\
	fileName_4 title lipid_4 ls lipid_4_ls
}



