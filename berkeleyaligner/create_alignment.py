import sys

filename1 = sys.argv[1]
file = open(filename1,'w')

e = open('./songs/.e','r')
f = open('./songs/.f','r')
align = open('./output/training.align','r')

e_lines = e.readlines()
f_lines = f.readlines()
align_lines = align.readlines()

for l in range(len(e_lines)):
    file.write("{}\t{}\t{}\n".format(f_lines[l].strip('\n'), e_lines[l].strip('\n'), align_lines[l].strip('\n')))
