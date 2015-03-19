package com.atilika.kuromoji.fst;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class FSTCompilerTest {

    @Test
    public void testFreezeStateTwoStates() throws Exception {

        // currently, supported by storing where the destination address is.
        HashMap<String, Integer> stateAddressHashMap = new HashMap<>();
        stateAddressHashMap.put("ACCEPT", 3); // looping to itself
        stateAddressHashMap.put("a", 3); // stores the address of a next state
        stateAddressHashMap.put("b", 3); // next state is 3

        State acceptState = new State();
        acceptState.isFinal = true;

        State startState = new State();
        startState.setArc('a', 1, acceptState);
        startState.setArc('b', 2, acceptState);

        FSTCompiler fstCompiler = new FSTCompiler();
        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
        instructionList.addAll(0, fstCompiler.freezeState(acceptState, stateAddressHashMap));
        instructionList.addAll(0, fstCompiler.freezeState(startState, stateAddressHashMap));

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(instructionList);

        assertEquals(1, vm.run(program, "a"));
        assertEquals(2, vm.run(program, "b"));
        assertEquals(-1, vm.run(program, "c"));
    }

    @Test
    public void testFreezeStateThreeStates() throws Exception {

        HashMap<String, Integer> stateAddressHashMap = new HashMap<>();
        stateAddressHashMap.put("ACCEPT", 5); // looping to itself
        stateAddressHashMap.put("c", 5);
        stateAddressHashMap.put("a", 5); // stores the address of a next state
        stateAddressHashMap.put("b", 3); // next state is 3

        // this map will be unsupervised in the higher level method.

        State acceptState = new State();
        acceptState.isFinal = true;

        State startState = new State();
        startState.setArc('a', 1, acceptState);

        State destStateB = new State();
        startState.setArc('b', 2, destStateB);

        destStateB.setArc('c', 1, acceptState);

        FSTCompiler fstCompiler = new FSTCompiler();
        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
        instructionList.addAll(0, fstCompiler.freezeState(acceptState, stateAddressHashMap));
        instructionList.addAll(0, fstCompiler.freezeState(destStateB, stateAddressHashMap));
        instructionList.addAll(0, fstCompiler.freezeState(startState, stateAddressHashMap));
        // note that startState is always freezed in the last.

        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(instructionList);

        assertEquals(1, vm.run(program, "a"));
        assertEquals(-1, vm.run(program, "b"));
        assertEquals(3, vm.run(program, "bc"));
    }
}