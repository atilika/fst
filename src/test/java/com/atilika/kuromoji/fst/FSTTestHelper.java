package com.atilika.kuromoji.fst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }

}
