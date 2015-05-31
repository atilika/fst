package com.atilika.kuromoji.fst;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FSTTest {

    @Test
    public void testCreateDictionary() throws Exception {
        // referring to https://lucene.apache.org/core/4_3_0/core/org/apache/lucene/util/fst/package-summary.html to make a simple test
        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend", "pydata"};
        int outputValues[] = {1, 2, 3, 4, 20, 42, 43};


        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fst.transduce(inputValues[i]));
        }
    }

    @Ignore("Enable when you want to try out with the whole JA wikipedia titles by incremental approach.")
    @Test
    public void testJAWikipediaIncremental() throws Exception {
        FSTTestHelper fstTestHelper = new FSTTestHelper();
        fstTestHelper.readIncremental("jawikititles.txt");
    }

    @Ignore("Enable when you want to try out with the whole JA wikipedia titles.")
    @Test
    public void testJAWikipedia() throws Exception {
        read(getResource("jawikititles.txt"));
    }


    public static FST read(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        ArrayList<String> titles = new ArrayList<String>();
        int maxWordLength = 0;

        while ((line = reader.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            titles.add(line.trim());
            maxWordLength = Math.max(maxWordLength, line.trim().length());
        }

        reader.close();

        String[] inputValues = titles.toArray(new String[titles.size()]);
        int[] outputValues = new int[titles.size()];
        for (int i = 0; i < outputValues.length; i++) {
            outputValues[i] = i;
        }

        FST fst = new FST();
        fst.MAX_WORD_LENGTH = maxWordLength;
        fst.createDictionary(inputValues, outputValues);

        return fst;
    }

    @Test
    public void testExtractFromWikipediaArticle() throws Exception {
        // Read the dictionary from a file
        // Extract words from dictionaries

        String resource = "ipadic-allwords_uniq_sorted.csv";
        testJAWikipediaIncremental(resource);
    }

    private void testJAWikipediaIncremental(String resource) throws Exception {
        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FST fst = fstTestHelper.readIncremental(resource);

        for (List<State> listOfStates : fst.getStatesDictionary().values()) {
            if (listOfStates.size() == 2) {
                System.out.println();
            }
//            assertEquals(1, listOfStates.size());
        }

        int wordIDExpected = 1;

        // Read all words
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResource(resource), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            assertEquals(wordIDExpected, fst.transduce(line));
            wordIDExpected++;
        }
        reader.close();
    }

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }
}