package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Instruction;
import com.atilika.kuromoji.fst.vm.Program;
import com.atilika.kuromoji.fst.vm.VirtualMachine;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class FSTCompilerTest {

    @Test
    public void testCreateDictionaryWithFSTCompiler() throws Exception {
        // referring to https://lucene.apache.org/core/4_3_0/core/org/apache/lucene/util/fst/package-summary.html to make a simple test
        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend", "pydata"};
        int outputValues[] = {1, 2, 3, 4, 20, 42, 43};


        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fstBuilder.transduce(inputValues[i]));
        }


        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();
        List<Instruction> instructionsForDebug = program.dumpInstructions();
        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }

        assertEquals(-1, vm.run(program, "thursday"));
    }

    @Test
    public void testSuffixStatesMerged() throws Exception {
        String inputValues[] = {"cat", "cats", "dog", "dogs"};
        int outputValues[] = {1, 2, 3, 4};
        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(inputValues, outputValues);

        Program program = fstBuilder.fstCompiler.getProgram();
        List<Instruction> instructionsForDebug = program.dumpInstructions();

//      There should be only one instruction with the label 's' and the output 1
        List<Instruction> instructionsToSameState = new ArrayList<>();

        int numInstructionWithTransitionCharS = 0;
        for (Instruction instruction : instructionsForDebug) {
            if (instruction.arg1 == 's') {
                numInstructionWithTransitionCharS++;
            }
            if (instruction.arg1 == 'g' || instruction.arg1 == 't') {
                instructionsToSameState.add(instruction);
            }
        }
        assertEquals(1, numInstructionWithTransitionCharS); // 1, since states are equivalent.
        // pointing to the same Instruction address
        assertEquals(instructionsToSameState.get(0).arg2, instructionsToSameState.get(1).arg2);
    }

    @Test
    public void testJapaneseBasics() throws Exception {
        String inputValues[] = {"すし", "すめし", "さしみ", "寿司", "寿", "さんま", "さかな"};
        int outputValues[] = {1, 2, 3, 4, 20, 42, 43};

        List<String> inputs = Arrays.asList(inputValues);
        Collections.sort(inputs);

        String sortedInput[] = new String[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            sortedInput[i] = inputs.get(i);
        }

        FSTBuilder fstBuilder = new FSTBuilder();

        fstBuilder.createDictionary(sortedInput, outputValues);

        for (int i = 0; i < sortedInput.length; i++) {
            assertEquals(outputValues[i], fstBuilder.transduce(sortedInput[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();
        List<Instruction> instructionsDebug = program.dumpInstructions();
        for (int i = 0; i < sortedInput.length; i++) {
            assertEquals(outputValues[i], vm.run(program, sortedInput[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));
    }

    @Test
    public void testKotobuki() throws Exception {
        String inputValues[] = {"さかな", "寿", "寿司"};
        int outputValues[] = {0, 1, 2};

        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fstBuilder.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();

        List<Instruction> instructionsDebug = program.dumpInstructions();

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));
        assertEquals(-1, vm.run(program, "寿司が食べたい"));
        assertEquals(-1, vm.run(program, "寿司が"));
    }

    @Test
    public void testWordsWithWhiteSpace() throws Exception {
        String inputValues[] = {"!", "! -attention-"};
        int outputValues[] = {0, 1};

        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fstBuilder.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));

    }

    @Test
    public void testRepeatedCharacter() throws Exception {
        String inputValues[] = {"!", "! -attention-", "!!!", "!!!F You!!!", "!［ai-ou］"};
        int outputValues[] = {0, 1, 2, 3, 4};

        FSTBuilder fstBuilder = new FSTBuilder();
        fstBuilder.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fstBuilder.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();
        List<Instruction> instructions = program.dumpInstructions();

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));

    }

    //    @Ignore("Enable for testing simple VM-based FST")
    @Test
    public void testJAWikipediaIncremental10Words() throws Exception {
        String resource = "jawikititlesHead10.txt";
        testJAWikipediaIncremental(resource);
    }

    //    @Ignore("Enable for testing simple VM-based FST")
    @Test
    public void testJAWikipediaIncremental100Words() throws Exception {
        String resource = "jawikititlesHead100.txt";
        testJAWikipediaIncremental(resource);
    }

    private void testJAWikipediaIncremental(String resource) throws Exception {
        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FSTBuilder fstBuilder = fstTestHelper.readIncremental(resource);

        VirtualMachine vm = new VirtualMachine();
        Program program = fstBuilder.fstCompiler.getProgram();

        fstTestHelper.checkOutputWordByWord(resource, program, vm);
    }
}