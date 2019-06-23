lipid_1 = ARG1
lipid_1_ls = 1

lipid_2 = ARG2
lipid_2_ls = 2

lipid_3 = ARG3
lipid_3_ls = 3

lipid_4 = ARG4
lipid_4_ls = 4

set style line 1 lc rgb "red"
set style line 2 lc rgb "blue"
set style line 3 lc rgb "green"
set style line 4 lc rgb "purple"

set yrange[0:3]

set boxwidth 0.33
set style fill solid


set title "Nearest Neighbors"
set xlabel "Lipids"
set ylabel "Number of Neighbors"

fileName = "NN_Bar_Graph.dat"
full_FileName = "Data/".fileName

#plot full_FileName every 2 u 3:4 title lipid_1 with boxes ls lipid_1_ls,\
#full_FileName every 2::1 u 3:4:xtic(1) title lipid_2 with boxes ls lipid_2_ls,\


plot full_FileName every 3 u 3:4 title lipid_1 with boxes ls lipid_1_ls,\
full_FileName every 3::1 u 3:4:xtic(1) title lipid_2 with boxes ls lipid_2_ls,\
full_FileName every 3::2 u 3:4 title lipid_3 with boxes ls lipid_3_ls

#plot full_FileName every 4 u 3:4 title lipid_1 with boxes ls lipid_1_ls,\
#full_FileName every 4::1 u 3:4:xtic(1) title lipid_2 with boxes ls lipid_2_ls,\
#full_FileName every 4::2 u 3:4 title lipid_3 with boxes ls lipid_3_ls,\
#full_FileName every 4::3 u 3:4 title lipid_4 with boxes ls lipid_4_ls




pause -1 ""
