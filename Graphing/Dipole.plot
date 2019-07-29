Leaflet = ARG1

lipid_1 = ARG2
lipid_2 = ARG3

set yrange[-1:1]

#set arrow 1 from -0.13,0.35 to -0.07,0.33 nohead front lw 4
#set arrow 2 from -0.13,0.33 to -0.07,0.31 nohead front lw 4 


set style line 1 \
    linecolor rgb '#8c8c8c' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 2 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 3 \
    linecolor rgb '#8c8c8c' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 4 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 5 \
    linecolor rgb '#000000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 6 \
    linecolor rgb '#ff8080' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 7 \
    linecolor rgb '#000000' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

set style line 8 \
    linecolor rgb '#ff8080' \
    linetype 1 linewidth 1.5 \
    pointtype 7 pointsize 1

xLabel_Name = "Radial Distance"
yLabel_Name = "Field Alignment"

set title "Field Alignment"
set key top left
set xlabel xLabel_Name offset 0,-2
set ylabel yLabel_Name


set xlabel font ",10"
set ylabel font ",10"
set title font ",10"
set key font ",10"
set xtics font ",10"
set ytics font ",10"
set lmargin 13
set bmargin 6


fileName_1 = "Data/".Leaflet."_Leaflet_".lipid_1."_".lipid_2."_Dipole.dat"

stats fileName_1 nooutput
Max_X_1 = STATS_max_x
Max = Max_X_1

set xrange[-0.2:(Max+0.35)]

key_1 = lipid_1." Dipole with respect to ".lipid_2

plot fileName_1 using 1:2:($2-$3):($2+$3) with errorbars linestyle 3 title key_1,\
fileName_1 using 1:2:($2-$3):($2+3) with linespoints linestyle 1 notitle,\
fileName_1 using 1:2:4 with labels offset 3.5,-3 font ",10" tc ls 5 notitle,\
