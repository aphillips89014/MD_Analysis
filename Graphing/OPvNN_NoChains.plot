
lipid_1 = ARG1
lipid_2 = ARG2
lipid_3 = ARG3
lipid_4 = ARG4

set style line 1 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 5 \
    pointtype 7 pointsize 3

set style line 2 \
    linecolor rgb '#000000' \
    linetype 1 linewidth 5 \
    pointtype 7 pointsize 3

set style line 3 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 4 \
    linecolor rgb '#000000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 5 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 5 \
    pointtype 7 pointsize 3

set style line 6 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

full_Title = lipid_1."'s Order Parameter vs ".lipid_1."'s Nearest Neighbors"

set title full_Title
set key bottom left
set xlabel "Number of Neighbors" offset 0,-2
set ylabel "Order Parameter" offset -3

set key font ",20"
set title font ",20"
set xlabel font ",20"
set ylabel font ",20"
set xrange[-0.1:7.25]
set yrange[0.3:1]

set xtics font ",20"
set ytics font ",20"
set lmargin 13
set bmargin 6

set arrow 1 from -0.13,0.35 to -0.07,0.33 nohead front lw 4
set arrow 2 from -0.13,0.33 to -0.07,0.31 nohead front lw 4

fileName_1 = "Data/OP_NN_".lipid_1."_".lipid_2.".dat"
fileName_2 = "Data/OP_NN_".lipid_3."_".lipid_4.".dat"

key_1 = lipid_1." v ".lipid_2
key_2 = lipid_3." v ".lipid_4

plot fileName_1 using 1:2:($2-$3):($2+$3) with errorbars linestyle 3 title key_1,\
fileName_1 using 1:2:($2-$3):($2+$3) with linespoints linestyle 1 notitle,\
fileName_1 using 1:2:4 with labels offset 4,-1.5 tc ls 1 font ",10" notitle,\
fileName_2 using 1:2:($2-$3):($2+$3) with errorbars linestyle 6 title key_2,\
fileName_2 using 1:2:($2-$3):($2+$3) with linespoints linestyle 5 notitle,\
fileName_2 using 1:2:4 with labels offset 4,2 tc ls 5 font ",10" notitle


pause -1 "button"
