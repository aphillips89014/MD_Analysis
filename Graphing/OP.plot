lipid_1 = ARG1


set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 0.01 \
    pointtype 7 pointsize 1

set style line 2 \
	linecolor rgb '#ff0000'\
	linetype 1 linewidth 0.01 \
	pointtype 7 pointsize 1

set style line 3 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 1 \
    pointtype 7 pointsize 0.3

set style line 4 \
	linecolor rgb '#ff0000'\
	linetype 1 linewidth 1 \
	pointtype 7 pointsize 0.3

full_Title = lipid_1." OP"

set xrange [0:19]
set title full_Title
set xlabel "Carbon Index"
set ylabel "Order Parameter"

fileName_1 = "Data/".lipid_1."_chain_0_OP_H.dat"
fileName_2 = "Data/".lipid_1."_chain_1_OP_H.dat"
fileName_3 = "Data/".lipid_1."_chain_0_OP.dat"
fileName_4 = "Data/".lipid_1."_chain_1_OP.dat"


plot fileName_1 u 1:($2 * -1)  notitle with linespoints linestyle 1,\
fileName_1 u 1:($2 * -1):(($2 * -1) + $3):(($2 * -1) - $3) notitle with errorbars linestyle 3,\
fileName_2 u 1:($2 * -1) notitle  with linespoints linestyle 2,\
fileName_2 u 1:($2 * -1):(($2 * -1) + $3):(($2 * -1) - $3) notitle with errorbars linestyle 4,\
fileName_3 u 1:($2 * -1) title "Sn1" with linespoints linestyle 3,\
fileName_4 u 1:($2 * -1) title "Sn2" with linespoints linestyle 4
pause -1 "button"
