package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.List;

public class FSTSubstringMatcher {
    private String sentence;

    FSTSubstringMatcher(String sentence) {
        this.sentence = sentence;
    }

    /**
     * Matching all possible substrings
     *
     * @param vm
     * @param program
     * @return List of extracted tokens that are in a dictionary
     */
    public List matchAllSubstrings(VirtualMachine vm, VirtualMachine.Program program) {
        List extractedTokens = new ArrayList();

        for (int i = 0; i < sentence.length(); i++) {
            for (int j = i + 1; j < sentence.length(); j++) {
                if (vm.run(program, sentence.substring(i, j)) != -1) {
//                    System.out.println(sentence.substring(i, j));
                    extractedTokens.add(sentence.substring(i, j));
                }
            }
        }

        return extractedTokens;
    }
}
