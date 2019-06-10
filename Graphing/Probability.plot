lipid_1 = ARG1
lipid_1_ls = 4

lipid_2 = ARG2
lipid_2_ls = 3

lipid_3 = ARG3
lipid_4 = ARG4


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

set yrange [0:0.8]
set xrange[-0.2:7.2]

set file_One = "Data/".lipid_1."_Histogram_".lipid_3.".dat"
set file_Two = "Data/".lipid_2."_Histogram_".lipid_4.".dat"

plot file_One with linespoints linestyle 4 title lipid_1,\
file_Two with linespoints linestyle 3 title lipid_2,\


pause -1 "Press button"
