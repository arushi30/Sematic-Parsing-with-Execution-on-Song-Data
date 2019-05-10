#!/usr/bin/env python3

# Check that interactive mode is off.
# Specify the features in ./run.
# Make sure there are ppdb files.

import subprocess
from joblib import Parallel, delayed

def execute_command(command, stdout, cwd=None):
    if cwd:
        p = subprocess.Popen(command.split(), stdout=stdout, cwd=cwd)
    else:
        p = subprocess.Popen(command.split(), stdout=stdout)
    output, error = p.communicate()
    p.terminate()
    return 0


def f(n_str, n_int):
    # paths to train, test
    train_examples = "lib/data/overnight/songs.paraphrases.train_{}_utterance.examples".format(n_int) # 1,2,3
    test_examples = 'lib/data/overnight/songs.paraphrases.val.examples'
    ppdb = 'lib/data/overnight/train_{}_utterance-ppdb.txt'.format(n_int) # 1,2,3
    berkely = 'lib/data/overnight/songs.word_alignments_{}utterance.berkeley'.format(n_str) # one, two, three
    # path to logfile
    logfile = 'logs/logfile_{}_utterance.txt'.format(n_int) # 1,2,3
    print('\n' + logfile + '\n')

    with open(logfile, 'w') as f:
        ## Training the model
        params_dict = {
            'mode': 'songs', 
            'Dataset': ('train:' + train_examples + ' ' + 'test:' + test_examples),
            'ppdb_path': ppdb,
            'berkely_path': berkely,
        }
        command = './run @mode={mode} -Dataset.inPaths {Dataset} -PPDBModel.ppdbModelPath {ppdb_path} -wordAlignmentPath {berkely_path}'\
                    .format(**params_dict)
        print('\n' + command + '\n')
        execute_command(command, f)
            
            
numbers_str = ['one', 'two', 'three']
numbers_int = [1, 2, 3]

Parallel(n_jobs=3)(
    delayed(f)(numbers_str[i], numbers_int[i])
    for i in range(3)
)

print('end')