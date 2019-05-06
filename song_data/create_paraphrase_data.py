#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Processes google spreadsheet of paraphrases to create training, validation, and test data
"""
import inspect
import os
import pandas as pd
import csv
from sklearn.model_selection import GroupShuffleSplit

filename = inspect.getframeinfo(inspect.currentframe()).filename
path = os.path.dirname(os.path.abspath(filename))

# Read in the google sheet
google_sheet = "https://docs.google.com/spreadsheets/d/1StM4qy-hG4FETenJirooV6IT8SN6Y1RAkYWGC3e8r0M/export?format=csv"
paraphrases = pd.read_csv(google_sheet)
paraphrases.dropna(inplace=True)
paraphrases.reset_index(inplace=True)

# Create a single column with the format we need to write out
paraphrases["final"] = "(example\n  (utterance \"" + paraphrases["paraphrase"] + "\")\n  (original \"" + paraphrases["canonical_utterance"]+"\")\n  (targetFormula \n    " + paraphrases["do_not_edit"] + "\n  )\n)"
paraphrases=paraphrases.apply(lambda x: x.str.strip() if x.dtype == "object" else x)

# Split into train and test
train_inds, test_inds = next(GroupShuffleSplit(test_size=.20, n_splits=2, random_state = 7).split(paraphrases, groups=paraphrases['canonical_utterance']))
train, test = paraphrases.loc[train_inds, :], paraphrases.loc[test_inds, 'final']

# Split training data into train and validation
train_inds, val_inds = next(GroupShuffleSplit(test_size=.25, n_splits=2, random_state = 7).split(train, groups=train['canonical_utterance']))
train, val = paraphrases.loc[train_inds, 'final'], paraphrases.loc[val_inds, 'final']

    
# Write to appropriate files
pd.set_option("display.max_colwidth", 10000)
datasets = [("train", train), ("val", val), ("test", test)]
for name, data in datasets:
    to_write=data.str.cat(sep="\n").replace('\\n', '\n')
    textfile = open(path+"/../lib/data/overnight/songs.paraphrases."+name+".examples", 'w')
    textfile.write(to_write)
    textfile.close()
    
