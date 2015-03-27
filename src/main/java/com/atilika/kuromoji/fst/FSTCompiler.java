package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {
    HashMap<String, List<Integer>> arcAddressHashMap = new HashMap<>();
    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap = new HashMap<>();
    public List<VirtualMachine.Instruction> instructionList = new ArrayList<>();


//    public List<VirtualMachine.Instruction> freezeState(State state,
//                                                        HashMap<String, Integer> stateAddressHashMap) {
//        // returns a list of instructions
//        // instructions represent 1. state 2. state stransition (outgoing arcs from states)
//        // Enough to assume that state itself already holds a transition string.
//
//        List<VirtualMachine.Instruction> instructionList = new ArrayList<>();
//
//        List<Character> transitionStrings = state.getAllTransitionStrings(); // all transition strings
//
//        // since it is acyclic, always add to the front.
//        if (state.isFinal) {
//            instructionList.add(0, createInstructionAccept());
//        }
//        else {
//            instructionList.add(0, createInstructionFail());
//        }
//
//        for (Character transitionString : transitionStrings) {
//
//            VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
//            instructionMatch.opcode = instructionMatch.MATCH;
//            instructionMatch.arg1 = transitionString; // TODO: debug it!
//            instructionMatch.arg3 = state.getTransitionArc(transitionString).getOutput(); // set output
////            instructionMatch.arg2 = stateAddressHashMap.get(transitionString.toString());
//            String temp = state.getNextState(transitionString).getAllTransitionStrings().toString();
//            instructionMatch.arg2 =
//                    stateAddressHashMap.get(temp);
//
//            state.getNextArc(transitionString).setTargetJumpAddress(stateAddressHashMap.get(temp));
//
//            instructionList.add(instructionMatch); // what will happen if one state accepts it?
//        }
//
//        return instructionList;
//    }


    // To be unit tested in the following methods....

    public boolean isJumpToSameAddress(int jumpAddress, Arc arc) {
        if (arc.getTargetJumpAddress() == jumpAddress) {
            // getTargetJumpAddress is Arc d
            return true;
        }
        return false;
    }

    public int referToFrozenArc(Arc b, String key) {
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

    public void assignTargetAddressToArcB(Arc b, String key) {
        int targetAddress = referToFrozenArc(b, key);
        if (targetAddress != -1) {
            b.setTargetJumpAddress(targetAddress); // equivalent state found
        }
        else {
            // No frozen arcs transiting to the same state. Freeze a new arc.
            instructionList.add(createInstructionFail());

            List<Integer> arcAddresses = new ArrayList<>();
            int newAddress = instructionList.size();

            // 1. Create a new List for a new key
            if (arcAddressHashMap.containsKey(key)) {
                arcAddresses = arcAddressHashMap.get(key);
            }
            arcAddresses.add(newAddress);
            arcAddressHashMap.put(key, arcAddresses);

            Arc d = b.getDestination().arcs.get(0);
            // First arc is regarded as a state
            // TODO: Assumed only single arc
            VirtualMachine.Instruction newInstructionForArcD = new VirtualMachine.Instruction();
            if (d.getLabel() == ' ') {
                // TODO: Accepting state sometimes jumps to next state. Self loop is not always true. Handle e.g. "dog" vs. "dogs"
                newInstructionForArcD = createInstructionAccept(newAddress); // self loop
            }
            else {
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }
            instructionList.add(newInstructionForArcD);

            addressInstructionHashMap.put(newAddress, newInstructionForArcD);
            b.setTargetJumpAddress(newAddress);

            // rest of the arcs
            for (int i = 1; i < b.getDestination().arcs.size(); i++) {
                newAddress = instructionList.size();
                d = b.getDestination().arcs.get(i);
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
                instructionList.add(newInstructionForArcD);

                addressInstructionHashMap.put(newAddress, newInstructionForArcD);
            }
        }
    }

    public VirtualMachine.Instruction createInstructionFail() {
        VirtualMachine.Instruction instructionFail = new VirtualMachine.Instruction();
        instructionFail.opcode = instructionFail.FAIL;
        return instructionFail;
    }

    public VirtualMachine.Instruction createInstructionAccept(int jumpAddress) {
        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        instructionAccept.arg2 = jumpAddress;
        return instructionAccept;
    }

    private VirtualMachine.Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

}