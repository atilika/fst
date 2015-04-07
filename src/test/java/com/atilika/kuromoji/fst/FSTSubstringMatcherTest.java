package com.atilika.kuromoji.fst;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FSTSubstringMatcherTest {
    // We want to test how fast the lookup method is.

    // input: wikipedia article
    // output: all of the entries in the dictionary


    @Test
    public void testExtractOneWord() throws Exception {
        String sampleSentence = "寿司が食べたい"; // "I want to eat sushi." in Japanese

//        List<String> words = new ArrayList<>();
//        words.add("寿司");
//        words.add("食べ");

        String[] words = {"寿司", "食べ"};
        int[] outputValues = {1, 2};

        FST fst = new FST();
        fst.createDictionary(words, outputValues);

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

        for (int i = 0; i < words.length; i++) {
            assertEquals(outputValues[i], vm.run(program, words[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));

        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher();

        fstSubstringMatcher.matchAllSubstrings(sampleSentence, vm, program);


    }
}
