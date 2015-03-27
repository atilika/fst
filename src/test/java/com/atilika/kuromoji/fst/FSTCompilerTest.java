package com.atilika.kuromoji.fst;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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
        assertEquals(2, newArc.getTargetJumpAddress()); // because the instructionList size is 2
    }

    private VirtualMachine.Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

    //    @Test
//    public void testCollidedCase() throws Exception {
//        HashMap<String, List<Integer>> stateAddressHashMap = new HashMap<>();
//        List<Integer> listAccept = new ArrayList<>();
//        listAccept.add(0);
//        stateAddressHashMap.put("[]", listAccept); // accepting state, stores the address of instruction
//
//        List<Integer> listA = new ArrayList<>();
//        listAccept.add(2);
//        listAccept.add(4);
//        listAccept.add(6);
//
//        stateAddressHashMap.put("[a]", listA); // shall I change it to list of integers?
//
//        State acceptState = new State();
//        acceptState.isFinal = true;
//
//        State stateA = new State();
//
//        State stateAA = new State();
//        stateA.setArc('a', 1, stateAA);
//        stateAA.setArc('a', 1, acceptState);
//
//        State startState = new State();
//        startState.setArc('a', 1, stateA);
//
//        FSTCompiler fstCompiler = new FSTCompiler();
//        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
//        instructionList.addAll(fstCompiler.freezeState(acceptState, stateAddressHashMap));
//
//
//
//        VirtualMachine vm = new VirtualMachine();
//        VirtualMachine.Program program = new VirtualMachine.Program();
//        program.addInstructions(instructionList);
//
//        assertEquals(1, vm.run(program, "a"));
//
//    }
}