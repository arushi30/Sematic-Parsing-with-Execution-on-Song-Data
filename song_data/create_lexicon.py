import pandas as pd
import re

path_to_file_db = 'database/songs_data.txt'
path_to_lexicon = '../lib/data/overnight/songs.lexicon'

# read the db
df = pd.read_csv(path_to_file_db, sep='\t', header=None)

# set of unique formulas from columns 1 and 2
list_unique_formulas = list(set(df[1]).union(set(df[2])))

# open the file
f = open(path_to_lexicon,"w+")
for formula in list_unique_formulas:
    # check it's not date / number
    if formula[0] != '(':
        typ, lexeme = formula.rsplit('.', 1)
        lexeme_initial = lexeme.replace("_", " ")
        # create lexeme without info in brackets
        lexeme_no_brackets = re.sub("[\(\[].*?[\)\]]", "", lexeme_initial).rstrip()
        for lexeme in set([lexeme_initial, lexeme_no_brackets]):
            f.write('{"lexeme": "%s", "formula": "%s", "type": "%s"}\n' 
                    % (lexeme, formula, typ))
f.close()