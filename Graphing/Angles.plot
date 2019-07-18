lipid_1 = ARG1
chain = ARG2
Member = ARG3

set xrange[0:90]

set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.2

set xlabel "Angle"
set ylabel ""

set xlabel font ",20"
set key font ",14"


fileName = "Data/Angle_Histogram_".lipid_1."_".chain."_".Member.".dat"

key_1 = "C-H Angles"

plot fileName with linespoints linestyle 1 t key_1


