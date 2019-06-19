lipid_1 = ARG1

set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.2


set style line 2 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 0.02 \
    pointtype 7 pointsize 2


set xlabel "Cosine Theta"

set xlabel font ",20"
set key font ",14"


fileName = "Data/".lipid_1."_OP_Histogram.dat"

key_1 = lipid_1." cos(Theta)"

plot fileName with linespoints linestyle 2 t key_1


pause -1 "Press button"
