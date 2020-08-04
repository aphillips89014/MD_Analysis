Leaflet = ARG1

lipid_1 = ARG2
chain = ARG3

set xrange[0:90]

set style line 1 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 2 \
    pointtype 7 pointsize 0.2


set style line 2 \
    linecolor rgb '#0060ad' \
    linetype 1 linewidth 0.3 \
    pointtype 7 pointsize 0.75


set xlabel "Cos(Theta)"

set xlabel font ",20"
set key left top
set key font ",14"


fileName = "Data/".Leaflet."_Leaflet_".lipid_1."_chain_".chain."_CosTheta_Histogram.dat"

key_1 = lipid_1." Cos Theta"

set angles degrees

plot fileName u (acos($1)):2 with linespoints linestyle 2 t key_1
#plot fileName with linespoints linestyle 2 t key_1

