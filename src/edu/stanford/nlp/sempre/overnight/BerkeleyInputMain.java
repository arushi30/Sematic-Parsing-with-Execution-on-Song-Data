package edu.stanford.nlp.sempre.overnight;

import fig.basic.Option;
import fig.exec.Execution;
import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.io.IOUtils;
import fig.basic.LispTree;
import fig.basic.LogInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;


/**
 * Created by joberant on 1/27/15.
 * Generating canonical utterances from grammar with various depths
 */
public class BerkeleyInputMain implements Runnable {
  @Option
  public boolean interactive = false;
  @Option
  public String input = "";

  @Override
  public void run() {
    SongCreateBerkeleyAlignerInputFromLispTree creator = new SongCreateBerkeleyAlignerInputFromLispTree();
    creator.main(input);
  }

  public static void main(String[] args) {
    Execution.run(args, "BerkeleyInputMain", new BerkeleyInputMain(), Master.getOptionsParser());
  }
}
