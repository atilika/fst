package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

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

            state.getNextArc(transitionString).setTargetJumpAddress(stateAddressHashMap.get(temp));

            instructionList.add(instructionMatch); // what will happen if one state accepts it?
        }

        return instructionList;
    }


    // To be unit tested in the following methods....

    public boolean isJumpToSameAddress(int jumpAddress, Arc arc) {
        if (arc.getTargetJumpAddress() == jumpAddress) {
            return true;
        }
        return false;
    }

    public int referToFrozenArc(Arc b, String key, HashMap<String, List<Integer>> arcAddressHashMap,
                                 HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap) {
        if (!arcAddressHashMap.containsKey(key)) {
            return -1;
        }

        List<Integer> arcAddresses = arcAddressHashMap.get(key);

        for (Integer arcAddress : arcAddresses) {
            int arcCtargetAddress = addressInstructionHashMap.get(arcAddress).arg2;
            if (isJumpToSameAddress(arcCtargetAddress, b)) {
                return arcCtargetAddress; // transiting to the same state.
            }
        }

        return -1;
    }

    public void assignTargetAddressToArcB(Arc b, String key, HashMap<String, List<Integer>> arcAddressHashMap,
                                    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap) {
        int targetAddress = referToFrozenArc(b, key, arcAddressHashMap, addressInstructionHashMap);
        if (targetAddress != -1) {
            b.setTargetJumpAddress(targetAddress); // equivalent state found
        }
        else {
            // No frozen arcs transiting to the same state. Freeze a new arc.
            List<Integer> arcAddresses = arcAddressHashMap.get(key);
            int newAddress = 0; // TODO: this should allocate to new address
            VirtualMachine.Instruction newInstruction = new VirtualMachine.Instruction(); // TODO: Assign new instruction
            arcAddresses.add(newAddress);
            arcAddressHashMap.put(key, arcAddresses);
            addressInstructionHashMap.put(newAddress, newInstruction);
            b.setTargetJumpAddress(newAddress);
        }
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

}