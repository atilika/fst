package com.atilika.kuromoji.fst;

import org.junit.Test;

public class FSTFormatterTest {
    @Test
    public void testFormat() throws Exception {
//        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend", "padata"};
//        int outputValues[] = {0, 1, 2, 3, 4, 20, 42};

        String inputValues[] = {"さかな", "寿", "寿司"};
        int outputValues[] = {0, 1, 2};

        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        FSTFormatter fstFormatter = new FSTFormatter();
        fstFormatter.format(fst, "LinearSearchFiniteStateTransducerOutputSushi.txt");
    }
}