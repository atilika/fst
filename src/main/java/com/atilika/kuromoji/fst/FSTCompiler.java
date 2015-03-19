package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    private HashMap<String, ArrayList<State>> statesDictionaryHashList;

    public List<VirtualMachine.Instruction> freezeState(State state) {
        // returns a list of instructions
        // instructions represent 1. state 2. state stransition (outgoing arcs from states)
        // Enough to assume that state itself already holds a transition string.

        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

        List<Character> transitionStrings = state.getAllTransitionStrings(); // all transition strings

        // TODO: remove ACCEPT and FAIL instruction when it is unnecessary

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

            if (state.getNextState(transitionString).isFinal) {
                instructionMatch.arg2 = instructionList.size() + 1;  // skip fail
                // to always make it

                // TODO: Currently, the jump address is just skipping the FAIL instruction
                // TODO: Moreover, we currently assume that there is only one unique accepting state, which is not true.

            }
            else {
                instructionMatch.arg2 = 1;
            }
//                instructionMatch.arg2 = findEquivalent(state.getNextState(transitionString)).hashCode();
                // making hashCode as a temporary address
                // TODO: All the destination states are not unique. Separate them.

            instructionList.add(0, instructionMatch); // what will happen if one state accepts it?
        }

        return instructionList;
    }

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

            instructionMatch.arg2 = stateAddressHashMap.get(transitionString.toString());

            instructionList.add(0, instructionMatch); // what will happen if one state accepts it?
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




    private State findEquivalent(State nextState) {
        // returns a equivalent state which is already in the stateDicitonary. nextState will be used when there is a collision
        List<Character> transitionStrings = nextState.getAllTransitionStrings();
        List<String> outputStrings = nextState.getAllOutputs(); // output of outgoing transition arcs

        String key = transitionStrings.toString() + outputStrings.toString();

        State newStateToDic = null;

        if (statesDictionaryHashList.containsKey(key)) {
            ArrayList<State> collidedStates = statesDictionaryHashList.get(key);

            if (nextState.getAllTransitionStrings().size() == 0) {
                // the dead end state (which is unique!)
                return collidedStates.get(0);
            }
            char transitionStringFocused = nextState.getAllTransitionStrings().get(0); // state which is not compiled yet
            State targetNextState = nextState.getNextState(transitionStringFocused);

            // Linear Probing the collidedStates!
            for (State collidedState : collidedStates) {
                if (collidedState.getNextState(transitionStringFocused).equals(targetNextState)) {
                    // OK, these states point to the same state. Equivalent!
                    return collidedState;
                }
            }
            // At this point, we know that there is no equivalent compiled (finalized) node
            newStateToDic = new State(nextState); // deep copy
            ArrayList<State> stateList = new ArrayList<State>();
            stateList.add(newStateToDic);
            statesDictionaryHashList.put(key, stateList);
        } else {
            // At this point, we know that there is no equivalent compiled (finalized) node
            newStateToDic = new State(nextState); // deep copy
            ArrayList<State> stateList = new ArrayList<State>();
            stateList.add(newStateToDic);
            statesDictionaryHashList.put(key, stateList);

        }
        return newStateToDic;
    }
}