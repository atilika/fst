package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    private HashMap<String, ArrayList<State>> statesDictionaryHashList;
//    private HashMap<String, Integer> stateAddressHashMap = new HashMap<>();


    public List<VirtualMachine.Instruction> freezeState(State state,
                                                        HashMap<String, Integer> stateAddressHashMap) {
        // returns a list of instructions
        // instructions represent 1. state 2. state stransition (outgoing arcs from states)
        // Enough to assume that state itself already holds a transition string.

        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

        List<Character> transitionStrings = state.getAllTransitionStrings(); // all transition strings

        // since it is acyclic, always add to the front.
        if (state.isFinal) {
            instructionList.add(0, createInstructionAccept());
        }
        else {
            instructionList.add(0, createInstructionFail());
        }

        for (Character transitionString : transitionStrings) {

            VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
            instructionMatch.opcode = instructionMatch.MATCH;
            instructionMatch.arg1 = transitionString; // TODO: debug it!
            instructionMatch.arg3 = state.getTransitionArc(transitionString).getOutput(); // set output
//            instructionMatch.arg2 = stateAddressHashMap.get(transitionString.toString());
            String temp = state.getNextState(transitionString).getAllTransitionStrings().toString();
            instructionMatch.arg2 =
                    stateAddressHashMap.get(temp);

            instructionList.add(instructionMatch); // what will happen if one state accepts it?
        }

        return instructionList;
    }

    public VirtualMachine.Instruction createInstructionFail() {
        VirtualMachine.Instruction instructionFail = new VirtualMachine.Instruction();
        instructionFail.opcode = instructionFail.FAIL;
        return instructionFail;
    }

    public VirtualMachine.Instruction createInstructionAccept() {
        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        return instructionAccept;
    }

    public void
    addToStateAddressHashMap(String transition, State state) {
        ArrayList<State> states = statesDictionaryHashList.get(transition);
        states.add(state);
        statesDictionaryHashList.put(transition, states); // putting back the updated lists

    }

}