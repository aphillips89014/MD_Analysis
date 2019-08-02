Leaflet = ARG1
lipid_1 = ARG2

set xrange [0:]

first_File_Chain = "SN1"
second_File_Chain = "SN2"

set style line 1 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 4 \
    pointtype 7 pointsize 4

set style line 2 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 4 \
    pointtype 7 pointsize 4

set style line 3 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 4 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

full_Title = "Projected Chain Length of ".lipid_1

set title full_Title
set key top right
set xlabel "Carbon Index" offset 0,-2
set ylabel "Height" offset -3

set arrow from 0,0 to 200,0

set key font ",20"
set title font ",20"
set xlabel font ",20"
set ylabel font ",20"

set xtics font ",20"
set ytics font ",20"
set lmargin 13
set bmargin 6

fileName_1 = "Data/".Leaflet."_Leaflet_".lipid_1."_chain_0_PCL.dat"
fileName_2 = "Data/".Leaflet."_Leaflet_".lipid_1."_chain_1_PCL.dat"

plot fileName_1 using 1:2:($2-$3):($2+$3) with errorbars linestyle 3 title first_File_Chain,\
fileName_1 using 1:2:($2-$3):($2+$3) with linespoints linestyle 1 notitle,\
fileName_2 using 1:2:($2-$3):($2+$3) with errorbars linestyle 4 title second_File_Chain,\
fileName_2 using 1:2:($2-$3):($2+$3) with linespoints linestyle 2 notitle,\

