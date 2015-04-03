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

//    @Test
//    public void testFreezeStateTwoStates() throws Exception {
//
//        // currently, supported by storing where the destination address is.
//        HashMap<String, Integer> arcAddressHashMap = new HashMap<>();
//        arcAddressHashMap.put("[]", 0); // ACCEPT
//        arcAddressHashMap.put("[a]", 2);
//        arcAddressHashMap.put("[b]", 3);
//
//        State acceptState = new State();
//        acceptState.isFinal = true;
//
//        State startState = new State();
//        startState.setArc('a', 1, acceptState);
//        startState.setArc('b', 2, acceptState);
//
//        FSTCompiler fstCompiler = new FSTCompiler();
//        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
//        instructionList.addAll(fstCompiler.freezeState(acceptState, arcAddressHashMap));
//        instructionList.addAll(fstCompiler.freezeState(startState, arcAddressHashMap));
//
//        VirtualMachine vm = new VirtualMachine();
//        VirtualMachine.Program program = new VirtualMachine.Program();
//        program.addInstructions(instructionList);
//
//
//        assertEquals(1, vm.run(program, "a"));
//        assertEquals(2, vm.run(program, "b"));
//        assertEquals(-1, vm.run(program, "c"));
//    }
//
//    @Test
//    public void testFreezeStateThreeStates() throws Exception {
//
//        HashMap<String, Integer> arcAddressHashMap = new HashMap<>(); // corresponds to each arc
//        arcAddressHashMap.put("[]", 0); // accepting state, stores the address of instruction
//        arcAddressHashMap.put("[c]", 2);
//        arcAddressHashMap.put("[a]", 4);
//        arcAddressHashMap.put("[b]", 5);
//
//        // this map will be unsupervised in the higher level method.
//
//        State acceptState = new State();
//        acceptState.isFinal = true;
//
//        State startState = new State();
//        startState.setArc('a', 1, acceptState);
//
//        State destStateB = new State();
//        startState.setArc('b', 2, destStateB);
//
//        destStateB.setArc('c', 1, acceptState);
//
//        FSTCompiler fstCompiler = new FSTCompiler();
//        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
//        instructionList.addAll(fstCompiler.freezeState(acceptState, arcAddressHashMap));
//        instructionList.addAll(fstCompiler.freezeState(destStateB, arcAddressHashMap));
//        instructionList.addAll(fstCompiler.freezeState(startState, arcAddressHashMap));
//        // note that startState is always freezed in the last.
//
//
//        VirtualMachine vm = new VirtualMachine();
//        VirtualMachine.Program program = new VirtualMachine.Program();
//        program.addInstructions(instructionList);
//
//        assertEquals(1, vm.run(program, "a"));
//        assertEquals(-1, vm.run(program, "b"));
//        assertEquals(3, vm.run(program, "bc"));
//    }

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
        acceptState.setArc(' ', 0, acceptState);

        int INSTRUCTION_ACCEPT_ADDRESS = 0;
        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        instructionAccept.arg2 = 0; // target address, self loop: VERY IMPORTANT for equivalent state detection.
        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_ACCEPT_ADDRESS, instructionAccept);

        // making an arc itself
        Arc b = new Arc(1, acceptState, 'a');
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
        key = "b";
        addresses = new ArrayList<>();
        INSTRUCTION_ADDRESS = 3;
        addresses.add(INSTRUCTION_ADDRESS);

        // create an instruction
        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
        instructionList.add(instructionAccept);
        instructionList.add(instructionMatch);

        fstCompiler.instructionList = instructionList;
        fstCompiler.assignTargetAddressToArcB(newArc, key);
        // TargetJumpAddress = 3, because the instructionList size is 2 + 1 selfloop = 3
        assertEquals(3, newArc.getTargetJumpAddress());
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

        FST fst = readIncremental(getResource(resource));

        // TODO: Check if each word is mapped correctly

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(fst.fstCompiler.instructionList);

        int wordID = 1;

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
            int wordIDExpected = vm.run(program, line);
            assertEquals(wordID, wordIDExpected);
        }
        reader.close();
    }

    private FST readIncremental(InputStream is) throws IOException {
        FST fst = new FST();
        fst.MAX_WORD_LENGTH = getMaxWordLength(getResource("jawikititles.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        fst.createDictionaryIncremental(reader);

        return fst;
    }

    // The following methods should ideally be included in FST or FSTCompiler class
    private int getMaxWordLength (InputStream is) throws IOException {
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

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }
}