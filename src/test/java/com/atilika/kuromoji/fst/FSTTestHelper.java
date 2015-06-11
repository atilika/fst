package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Program;
import com.atilika.kuromoji.fst.vm.VirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class FSTTestHelper {

    // The following methods should ideally be included in FST or FSTCompiler class
    public int getMaxWordLength (InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int maxWordLength = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            maxWordLength = Math.max(maxWordLength, line.trim().length());
        }
        reader.close();
        return maxWordLength;
    }


    public FSTBuilder readIncremental(String resource) throws IOException {
        InputStream is = getResource(resource);
        FSTBuilder fstBuilder = new FSTBuilder();
        FSTTestHelper fstTestHelper = new FSTTestHelper();
        fstBuilder.MAX_WORD_LENGTH = fstTestHelper.getMaxWordLength(getResource(resource));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        fstBuilder.createDictionaryIncremental(reader);

        return fstBuilder;
    }

    public void checkOutputWordByWord(String resource, Program program, VirtualMachine vm) throws IOException {
        int wordIDExpected = 1;
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
            int wordID = vm.run(program, line);
            assertEquals(wordIDExpected, wordID);
            wordIDExpected++;
        }
        reader.close();
    }

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }

}
