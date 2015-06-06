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

//    @Test
//    public void testReferToFrozenArc() throws Exception {
//        // testing for {2: match 'a', 1: FAIL, 0: ACCEPT}
//
//        FSTCompiler fstCompiler = new FSTCompiler();
//
//        State acceptState = new State();
//        acceptState.isFinal = true;
////        fstCompiler.instructionList.add(0, fstCompiler.createInstructionAccept(0));
//        fstCompiler.program.addInstruction(fstCompiler.createInstructionAccept(0));
////        fstCompiler.instructionList.add(1, fstCompiler.createInstructionFail());
//        fstCompiler.program.addInstruction(fstCompiler.createInstructionFail());
//
//        Arc b = new Arc(1, acceptState, 'a');
////        String key = "a";
//        char key = 'a';
//        b.setTargetJumpAddress(0); // to accepting state
//
//        List<Integer> addresses = new ArrayList<>();
//        addresses.add(0);
//        fstCompiler.arcDestinationAddressHashMap.put(key, addresses);
//
//        Instruction instruction = fstCompiler.createInstructionMatch('a', 0, 1);
////        fstCompiler.addressInstructionHashMap.put(2, instruction);
////        fstCompiler.instructionList.add(2, instruction);
//        fstCompiler.program.addInstruction(instruction);
//        assertEquals(0, fstCompiler.referToFrozenArc(b)); // returns the address of already frozen instruction
//
//        Arc c = new Arc(1, acceptState, 'b');
//        key = 'b';
//        assertEquals(-1, fstCompiler.referToFrozenArc(c));
//    }
//
//    @Test
//    public void testAssignTargetAddressToArcB() throws Exception {
//        // Testing assigning jump address to an arc works
//        // {{3: match 'b', 2: match 'a', 1: FAIL, 0: ACCEPT}}
//        FSTCompiler fstCompiler = new FSTCompiler();
//
//        // set up testing environment
//        State acceptState = new State();
//        acceptState.isFinal = true;
////        acceptState.setArc(' ', 0, acceptState); // self loop
//
//        int INSTRUCTION_ACCEPT_ADDRESS = 0;
//        Instruction instructionAccept = new Instruction();
//        instructionAccept.opcode = instructionAccept.ACCEPT;
////        instructionAccept.arg2 = 0; // target address, self loop: VERY IMPORTANT for equivalent state detection.
//        instructionAccept.arg2 = -1; // target address not set.
////        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_ACCEPT_ADDRESS, instructionAccept);
//
//        Instruction instructionFail = fstCompiler.createInstructionFail();
//
//        int INSTRUCTION_MATCH_A_ADDRESS = 0;
//        char keyA = 'a';
//        List<Integer> addresses = new ArrayList<>();
//        addresses.add(INSTRUCTION_MATCH_A_ADDRESS); // Instruction of an address
//        fstCompiler.arcDestinationAddressHashMap.put(keyA, addresses); // only stores corresponding arcs
//        // making an instruction that corresponds to an arc
//        Instruction instructionMatchA =
//                fstCompiler.createInstructionMatch('a', 0, 1); // trans. char='a', target address: 0, output: 1
////        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_MATCH_A_ADDRESS, instructionMatchA);
//
//        // freeze a new arc
//        int INSTRUCTION_MATCH_B_ADDRESS = 0;
//        char keyB ='b';
//        Arc arcB = new Arc(2, acceptState, 'b'); // output=2, dest. to Accepting state, transition char='b'
////        addresses = new ArrayList<>();
////        addresses.add(INSTRUCTION_MATCH_B_ADDRESS);
////        Instruction instructionMatchB =
////                createInstructionMatch('b', 0, 2); // trans. char='b', target address: 0, output: 2
////        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_MATCH_B_ADDRESS, instructionMatchB);
//
//        // create an instruction
//        List<Instruction> instructionList = new ArrayList<>();
//        instructionList.add(instructionAccept);
//        instructionList.add(instructionFail);
//        instructionList.add(instructionMatchA);
////        instructionList.add(instructionMatchB); //
////        fstCompiler.instructionList = instructionList;
//        fstCompiler.program.addInstructions(instructionList);
//
//        fstCompiler.compileArc(arcB);
//        // TargetJumpAddress = 0, to the dead-end accepting state
//        assertEquals(0, arcB.getTargetJumpAddress());
//
//        List<Integer> addressesForKeyB = new ArrayList<>();
//        addressesForKeyB.add(INSTRUCTION_MATCH_B_ADDRESS);
//        Instruction instructionMatchB =
//                fstCompiler.createInstructionMatch('b', 0, 2); // trans. char='b', target address: 0, output: 2
////        fstCompiler.addressInstructionHashMap.put(INSTRUCTION_MATCH_B_ADDRESS, instructionMatchB);
//        fstCompiler.arcDestinationAddressHashMap.put(keyB, addressesForKeyB);
//
//        instructionList.add(instructionMatchB); // add to Program for VM
//
//        // There should exist an instruction MATCH 'b'
//        assertNotEquals(-1, fstCompiler.referToFrozenArc(arcB));
//    }
//
//    @Test
//    public void testAssignTargetAddressToArcBFour() throws Exception {
//
//
//    }

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

    @Test
    public void testReadCompiledFST() throws Exception {


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