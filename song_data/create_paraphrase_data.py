#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Processes google spreadsheet of paraphrases to create training, validation, and test data
"""
import inspect
import os
import pandas as pd
import numpy as np
import csv

filename = inspect.getframeinfo(inspect.currentframe()).filename
path = os.path.dirname(os.path.abspath(filename))

np.random.seed(5)

# Read in the google sheet
google_sheet = "https://docs.google.com/spreadsheets/d/1StM4qy-hG4FETenJirooV6IT8SN6Y1RAkYWGC3e8r0M/export?format=csv"
paraphrases = pd.read_csv(google_sheet)

# Create a single column with the format we need to write out
paraphrases["final"] = "(example\n  (utterance \"" + paraphrases["paraphrase"] + "\")\n  (original \"" + paraphrases["canonical_utterance"]+"\")\n  (targetFormula \n    " + paraphrases["do_not_edit"] + "\n  )\n)"

# Split into train, validation, and test data
paraphrases["random"]=np.random.rand(len(paraphrases))
paraphrases=paraphrases.sort_values("random").reset_index(drop=True)
test_size = len(paraphrases)/5
train = paraphrases.loc[0:3*test_size-1, "final"]
val = paraphrases.loc[3*test_size:4*test_size-1, "final"]
test = paraphrases.loc[4*test_size:, "final"]

# Drop missing and clean whitespace
for df in [train, val, test]:
    df.dropna(inplace=True)
    df.str.strip()
    
# Write to appropriate files
pd.set_option("display.max_colwidth", 10000)
datasets = [("train", train), ("val", val), ("test", test)]
for name, data in datasets:
    to_write=data.str.cat(sep="\n").replace('\\n', '\n')
    textfile = open(path+"/../lib/data/overnight/songs.paraphrases."+name+".examples", 'w')
    textfile.write(to_write)
    textfile.close()
    
