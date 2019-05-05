# Current process for generating lexical alignment features

Based on the issues response: https://github.com/percyliang/sempre/issues/109#issuecomment-487223390

## To generate aligner features
#### `lexical`
1. Start from the main `sempre` directory. First generate .e and .f files from the LispTree. From the run mode, enter berkeley followed by the filepath to the current training examples. If it runs correctly, the output from the console will be a boolean true.

       ./run @mode=features
       # berkeley <trainingFile1>
       # For example:
       berkeley lib/data/overnight/songs.paraphrases.train.examples

2. Then generate the relevant files using the berkeley aligner. Note that the files in this folder are designed to get overwritten every time (but they can always be regenerated from the training example file).
        cd berkeleyaligner/
        bash ./berkeley
        cd ..
3. Finally, generate the word_alignment file that has the actual features used by SEMPRE. The output from the console will be a boolean true.
       ./run @mode=features
       # align <outputFileName> <berkeley> <threshold>
       # For example:
       align lib/data/overnight/songs.word_alignments.berkeley berkeley 2
