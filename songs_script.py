#!/usr/bin/env python3

# Check that interactive mode is off.
# the features specified in ./run.

import subprocess

def execute_command(command, stdout, cwd=None):
    if cwd:
        p = subprocess.Popen(command.split(), stdout=stdout, cwd=cwd)
    else:
        p = subprocess.Popen(command.split(), stdout=stdout)
    output, error = p.communicate()
    p.terminate()
    return 0


for num in [50, 100, 150]:
    for version in range(0, 5):
        # paths to train, test
        train_examples = "lib/data/overnight/songs.paraphrases.train_size_"+str(num)+"_v"+str(version)+".examples"
        test_examples = 'lib/data/overnight/songs.paraphrases.test.examples'
        ppdb = "lib/data/overnight/train_size_"+str(num)+"_v"+str(version)+"_ppdb.txt"
        # path to logfile
        logfile = 'logs/logfile_size_'+str(num)+'_v'+str(version)+'.txt'
        print('\n' + logfile + '\n')

        with open(logfile, 'w') as f:
            ## Berkeley Aligner
            # generate .e and .f files
            command = './run @mode=berkeley --BerkeleyInputMain.input {}'.format(train_examples)
            execute_command(command, f)
            print('.e and .f files are generated.')

            # generate the relevant files using the berkeley aligner
            execute_command('bash ./berkeley', f, 'berkeleyaligner/')
            print('Berkeleyaligner finished.')

            # generate the word_alignment file that has the actual features used by SEMPRE
            command = './run @mode=aligner --AlignerMain.input lib/data/overnight/songs.word_alignments.berkeley berkeley 2'
            execute_command(command, f)
            print('word_alignment file is generated.')

            ## Training the model
            params_dict = {
                'mode': 'songs', 
                'Dataset': ('train:' + train_examples + ' ' + 'test:' + test_examples),
                'ppdb_path': ppdb,
            }
            command = './run @mode={mode} -Dataset.inPaths {Dataset} -PPDBModel.ppdbModelPath {ppdb_path}'.format(**params_dict)
            execute_command(command, f)

print('end')