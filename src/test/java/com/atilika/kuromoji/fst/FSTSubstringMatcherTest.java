package com.atilika.kuromoji.fst;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FSTSubstringMatcherTest {
    // We want to test how fast the lookup method is.
    // input: wikipedia article
    // output: all of the entries in the dictionary

    @Test
    public void testExtractTwoTokens() throws Exception {
        String sampleSentence = "寿司が食べたい"; // "I want to eat sushi." in Japanese

        String[] tokens = {"寿司", "食べ"};
        int[] outputValues = {1, 2};

        FST fst = new FST();
        fst.createDictionary(tokens, outputValues);

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher(sampleSentence);
        List extractedTokens = fstSubstringMatcher.matchAllSubstrings(vm, program);

        assertEquals(Arrays.asList(tokens), extractedTokens);
    }

    @Test
    public void testExtractLongSentence() throws Exception {
        String sampleSentence = "寿司（すし、鮨、鮓[注釈 1]）と呼ばれる食品は、酢飯と主に魚介類を組み合わせた日本料理である。" +
                "大別すると、生鮮魚介を用いた「早鮨（早ずし）」と、魚介類を飯と塩で乳酸発酵させた「なれ鮨（なれずし）」に区分される。" +
                "そのなかでも代表的な寿司は前者の握り寿司（江戸前寿司）であり、英語圏では“sushi”で通じる料理となっている。" +
                "（詳細は、各々「江戸前寿司」「なれずし」を参照）"; // Explanation of sushi in Japanese

        String[] tokens = {"寿司", "食べ"};
        int[] outputValues = {1, 2};

        FST fst = new FST();
        fst.createDictionary(tokens, outputValues);

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher(sampleSentence);
        List extractedTokens = fstSubstringMatcher.matchAllSubstrings(vm, program);
        String[] expectedTokens = {"寿司", "寿司", "寿司", "寿司", "寿司"};
        assertEquals(Arrays.asList(expectedTokens), extractedTokens);
    }
}
