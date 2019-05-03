#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Fri May  3 14:39:39 2019

@author: brinaseidel
"""

import os
import sys
import inspect

def main(train_path, ppdb_path):
    

    # This should be the path to your SEMPRE directory

    # Get a list of words to use from the training canonical utterances and paraphrases
    song_words = set([])
    train_path = "lib/data/overnight/"+train_path
    fp = open(train_path)
    for i, line in enumerate(fp):
        if ("utterance" in line) or ("original" in line):
            phrase = line.split("\"")[1]
            phrase = phrase.replace(".", "").replace(",", "").replace(";", "").replace("?", "").replace("!", "")
            tokens = phrase.split(" ")
            for token in tokens:
                song_words.add(token)
    
    # Filter the ppdb data on these words
    lines_to_use = []
    fp = open("/Users/brinaseidel/Downloads/ppdb-2.0-s-all") # Downloaded from http://paraphrase.org/#/download
    for i, line in enumerate(fp):
        split = line.split("|||")
        phrase = split[1].strip()
        paraphrase = split[2].strip()
        if (phrase in song_words) or (paraphrase in song_words):
            lines_to_use.append(line)
        if i % 1000000 == 0:
            print("PPDB lines processed: ", i)
    fp.close()
    
    # Write the relevant ppdp 
    ppdb_path = "lib/data/overnight/"+ppdb_path
    with open(ppdb_path, 'w') as the_file:
        for line in lines_to_use:
            the_file.write(line)
    
if __name__ == '__main__':
  main(sys.argv[1], sys.argv[2])


