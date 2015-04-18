package com.atilika.kuromoji.fst;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class FSTCompilerTest {

    @Test
    public void testReferToFrozenArc() throws Exception {
        // testing for {2: match 'a', 1: FAIL, 0: ACCEPT}

        FSTCompiler fstCompiler = new FSTCompiler();

        State acceptState = new State();
        acceptState.isFinal = true;

        Arc b = new Arc(1, acceptState, 'a');
        String key = "a";

        List<Integer> addresses = new ArrayList<>();
        addresses.add(2);
        fstCompiler.arcAddressHashMap.put(key, addresses);

        VirtualMachine.Instruction instruction = createInstructionMatch('a', 0, 1);
        fstCompiler.addressInstructionHashMap.put(2, instruction);
        assertEquals(0, fstCompiler.referToFrozenArc(b, key)); // returns the address of already frozen instruction

        Arc c = new Arc(1, acceptState, 'b');
        key = "b";
        assertEquals(-1, fstCompiler.referToFrozenArc(c, key));
    }

    @Test
    public void testAssignTargetAddressToArcB() throws Exception {
        // Testing the equivalence freezing
        FSTCompiler fstCompiler = new FSTCompiler();

        // set up testing environment
        State acceptState = new State();
        acceptState.isFinal = true;
        acceptState.setArc(' ', 0, acceptState); // self loop

        int INSTRUCTION_ACCEPT_ADDRESS = 0;
        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        instructionAccept.arg2 = 0; // target address, self loop: VERY IMPORTANT for equivalent state detection.
        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_ACCEPT_ADDRESS, instructionAccept);

//        Arc b = new Arc(1, acceptState, 'a');
        String key = "a";
        List<Integer> addresses = new ArrayList<>();
        int INSTRUCTION_ADDRESS = 2;
        addresses.add(INSTRUCTION_ADDRESS); // Instruction of an address
        fstCompiler.arcAddressHashMap.put(key, addresses);

        // making an instruction that corresponds to an arc
        VirtualMachine.Instruction instructionMatch = createInstructionMatch('a', 0, 1);
        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_ADDRESS, instructionMatch);

//        fstCompiler.assignTargetAddressToArcB(b, key); // set the address of already frozen instruction
//        assertEquals(0, b.getTargetJumpAddress());

        // freeze a new arc
        Arc newArc = new Arc(2, acceptState, 'b');
        addresses = new ArrayList<>();
        INSTRUCTION_ADDRESS = 3;
        addresses.add(INSTRUCTION_ADDRESS);

        // create an instruction
        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
        instructionList.add(instructionAccept);
        instructionList.add(instructionMatch);

        fstCompiler.instructionList = instructionList;
        key = "b";
        fstCompiler.assignTargetAddressToArcB(newArc, key);
        // TargetJumpAddress = 3, because the instructionList size is 2 + 1 selfloop = 3
        assertEquals(3, newArc.getTargetJumpAddress());

        fstCompiler.assignTargetAddressToArcB(newArc, key);
    }

    private VirtualMachine.Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

    @Test
    public void testCreateDictionaryWithFSTCompiler() throws Exception {
        // referring to https://lucene.apache.org/core/4_3_0/core/org/apache/lucene/util/fst/package-summary.html to make a simple test
        String inputValues[] = {"cat", "cats", "dog", "dogs", "friday", "friend", "pydata"};
        int outputValues[] = {1, 2, 3, 4, 20, 42, 43};


        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fst.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);
        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }

        assertEquals(-1, vm.run(program, "thursday"));
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

        FST fst = new FST();

        fst.createDictionary(sortedInput, outputValues);

        for (int i = 0; i < sortedInput.length; i++) {
            assertEquals(outputValues[i], fst.transduce(sortedInput[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);
        for (int i = 0; i < sortedInput.length; i++) {
            assertEquals(outputValues[i], vm.run(program, sortedInput[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));
    }

    @Test
    public void testKotobuki() throws Exception {
        String inputValues[] = {"さかな", "寿", "寿司"};
        int outputValues[] = {0, 1, 2};

        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fst.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

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

        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fst.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], vm.run(program, inputValues[i]));
        }
        assertEquals(-1, vm.run(program, "まぐろ"));

    }

    @Test
    public void testRepeatedCharacter() throws Exception {
        String inputValues[] = {"!", "! -attention-", "!!!", "!!!F You!!!", "!［ai-ou］"};
        int outputValues[] = {0, 1, 2, 3, 4};

        FST fst = new FST();
        fst.createDictionary(inputValues, outputValues);

        for (int i = 0; i < inputValues.length; i++) {
            assertEquals(outputValues[i], fst.transduce(inputValues[i]));
        }

        // Test whether the program is correctly made.
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

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
        FST fst = fstTestHelper.readIncremental(resource);

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

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