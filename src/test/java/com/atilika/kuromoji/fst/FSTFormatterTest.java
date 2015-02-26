package com.atilika.kuromoji.fst;

import org.junit.Test;

public class FSTFormatterTest {
    @Test
    public void testFormat() throws Exception {
//        String inputValues[] = {"cat", "cats", "dog", "dogs"};
//        int outputValues[] = {0, 1, 2, 4};

//        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend"};
//        int outputValues[] = {1, 2, 3, 4, 20, 42};

//        TODO:fail with nextStateTransitionsToo, the "da" correspondance of "friday" and "padata"
        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend", "padata"};
        int outputValues[] = {0, 1, 2, 3, 4, 20, 42};

        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        FSTFormatter fstFormatter = new FSTFormatter();
        fstFormatter.format(fst, "LinearSearchFiniteStateTransducerOutput.txt");
    }
}