package com.atilika.kuromoji.fst;

import java.util.List;

public class FSTSubstringMatcher {

    // method to match all possible substrings
    public void matchAllSubstrings(String word, VirtualMachine vm, VirtualMachine.Program program) {
        for (int i = 0; i < word.length(); i++) {
            for (int j = i + 1; j < word.length(); j++) {
                if (vm.run(program, word.substring(i, j)) != -1) {
                    System.out.println(word.substring(i, j));
                    // current implementation does not fail immediately
                }
            }
        }
    }
}
