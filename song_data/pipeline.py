#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue May  7 15:55:07 2019

@author: brinaseidel
"""
import inspect 
import os
import subprocess
 
filename = inspect.getframeinfo(inspect.currentframe()).filename
path = os.path.dirname(os.path.abspath(filename))

os.chdir(path)

# Create PPDB files
with open('logfile.txt', 'w') as f:
    for i in [50, 100, 150]:
        for j in range(0, 5):
            train_path = path + "/../lib/data/overnight/songs.paraphrases.train_size_"+str(i)+"_v"+str(j)+".examples"
            ppdb_path = path + "/../lib/data/overnight/train_size_"+str(i)+"_v"+str(j)+"_ppdb.txt"
            command = "python3 " + path + "/create_ppdb_file.py " + train_path + " " + "ppdb_path"
            p = subprocess.Popen(command.split(), stdout=f, cwd=path)
            output, error = p.communicate()
            print("Finished one!")
# process python script with args from command line and save the output in logfile
    