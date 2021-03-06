Leaflet = ARG1

lipid_1 = ARG2
lipid_1_ls = 4
lipid_2 = ARG3
lipid_2_ls = 3
lipid_3 = ARG4
lipid_4 = ARG5

set yrange [0:1]
set xrange[-0.2:10.2]


set style line 1 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 10\
    pointtype 7 pointsize 7

set style line 2 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 10 \
    pointtype 7 pointsize 7

set style line 3 \
    linecolor rgb '#008cff' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2

set style line 4 \
    linecolor rgb '#ff0000' \
    linetype 1 linewidth 3 \
    pointtype 7 pointsize 2



set title "Probability of Having N Specific Neighbors"

xLabel_Name = "Number of Neighbors"

set xlabel xLabel_Name offset 0,-2
set ylabel "Probability" offset -3
set xtics offset 0,-1.5

set xlabel font ",10"
set ylabel font ",10"
set title font ",10"
set key font ",10"
set xtics font ",10"
set ytics font ",10"
set lmargin 13
set bmargin 6

file_One = "Data/".Leaflet."_Leaflet_".lipid_1."_Histogram_".lipid_2.".dat"
file_Two = "Data/".Leaflet."_Leaflet_".lipid_3."_Histogram_".lipid_4.".dat"

stats file_One nooutput
Max_X_1 = STATS_max_x
Max = Max_X_1

stats file_Two nooutput
Max_X_2 = STATS_max_x

if (Max_X_2 > Max) { 
        Max = Max_X_2
}


set xrange[-0.2:(Max+0.35)]






key_1 = lipid_1." Having N ".lipid_2." Neighbors"
key_2 = lipid_3." Having N ".lipid_4." Neighbors"

plot file_One with linespoints linestyle 4 title key_1,\
file_Two with linespoints linestyle 3 title key_2,\


