package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Program;
import com.atilika.kuromoji.fst.vm.VirtualMachine;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
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

        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(tokens, outputValues);

        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.getFstCompiler().getProgram();

        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher();
        List extractedTokens = fstSubstringMatcher.matchAllSubstrings(sampleSentence, vm, program);

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

        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(tokens, outputValues);

        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.getFstCompiler().getProgram();

        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher();
        List extractedTokens = fstSubstringMatcher.matchAllSubstrings(sampleSentence, vm, program);
        String[] expectedTokens = {"寿司", "寿司", "寿司", "寿司", "寿司"};
        assertEquals(Arrays.asList(expectedTokens), extractedTokens);
    }

    @Ignore("Enable by providing external dictionary file")
    @Test
    public void testExtractFromWikipediaArticle() throws Exception {
        // Read the dictionary from a file
        // Extract words from dictionaries

//        String resource = "jawikititlesHead1000.txt";
//        String resource = "ipadic-allwords_uniqHead5000.csv";
//        String resource = "ipadic-allwords_uniqHead100000.csv";
//        String resource = "ipadic-allwords_uniqHead200000.csv";
//        String resource = "ipadic-allwords_uniq_sorted.csv";
//        String resource = "jawikititles.txt";
        String resource = "jawikititles_sorted.txt";
//        String resource = "jawikititlesHead1000000.txt";

        testJAWikipediaIncremental(resource);
    }

    private void testJAWikipediaIncremental(String resource) throws Exception {
        FSTSubstringMatcher fstSubstringMatcher = new FSTSubstringMatcher();
        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FSTBuilder fstBuilder = fstTestHelper.readIncremental(resource);

//        VirtualMachine vm = new VirtualMachine(false);
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.getFstCompiler().getProgram();

//        program.outputProgramToFile(); // outputting bytebuffer to a file
//        program.readProgramFromFile();
//        List<Instruction> instructions = program.dumpInstructions();


//        FSTFormatter fstFormatter = new FSTFormatter();
//        fstFormatter.format(fst, "ipadic-allwords_uniq_sorted_Head1070_tail65.txt");

        // Enable when you want to confirm that the built (compiled) FST is correct
        fstTestHelper.checkOutputWordByWord(resource, program, vm);

        List<String> sentences = readSampleWikipediaArticle();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 1; i++) {
            fstSubstringMatcher.lookupSentences(vm, program, sentences);
        }

        long end = System.currentTimeMillis();
        System.out.println("Running time:" + (end - start) + " milliseconds");
        System.out.println("Number of lookups:" + fstSubstringMatcher.getNumLookups());
        System.out.println("MilliSec. per lookup:" + (1.0 * (end - start)) / fstSubstringMatcher.getNumLookups());
    }

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }


    private List<String> readSampleWikipediaArticle() throws IOException {

        List<String> sentences = new ArrayList<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResource("jaWikiSampleArticle.txt"), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }

            sentences.add(line);
        }
        return sentences;
    }
}
