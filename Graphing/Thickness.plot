lipid_1 = ARG1

set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.2

set xlabel "Distance from Center of Bilayer (A)"

set xlabel font ",20"
set key font ",14"


fileName = "Data/".lipid_1."_Thickness.dat"

plot fileName with linespoints linestyle 1 t fileName


pause -1 "Press button"
