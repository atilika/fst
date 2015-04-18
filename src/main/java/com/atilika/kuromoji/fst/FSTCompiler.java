package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    public char KEY_FOR_DEADEND_ARC = '|';

    HashMap<String, List<Integer>> arcAddressHashMap = new HashMap<>();
    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap = new HashMap<>();
    public List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

    /**
     * Checks whether Arc b is already frozen
     *
     * @param d
     * @param key
     * @return -1 if there is no Arc which input/output corresponds to key. Else return the address that corresponds to that arc.
     */
    public int referToFrozenArc(Arc d, String key) {
        if (!arcAddressHashMap.containsKey(key)) {
            return -1;
        }

        List<Integer> arcAddresses = arcAddressHashMap.get(key);

        for (Integer arcAddress : arcAddresses) {
            int arcCtargetAddress = addressInstructionHashMap.get(arcAddress).arg2;
            if (d.getTargetJumpAddress() == arcCtargetAddress) {
                return arcCtargetAddress; // transiting to the same state.
            }
        }

        return -1;
    }

    /**
     * Assigning an target jump address to Arc b.
     *
     * @param b
     * @param key
     */
    public void assignTargetAddressToArcB(Arc b, String key) {
        int targetAddress = referToFrozenArc(b, key);
        if (targetAddress != -1) {
            b.setTargetJumpAddress(targetAddress); // equivalent state found
        }
        else {
            if (b.getDestination().arcs.size() == 0) {
                // an arc which points to dead end accepting state
                b.setTargetJumpAddress(0);// assuming dead-end accepting state is always at the address 0
                return;
            }
            // First arc is regarded as a state
            int newAddress = makeNewInstructionForArcD(b, key); // TODO: this method is not fully implemented yet

            b.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
        }
    }

    public int makeNewInstructionForArcD(Arc b, String key){
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

//        Arc d = b.getDestination().arcs.get(0);

//        if (d.getLabel() == KEY_FOR_DEADEND_ARC) {
//            newInstructionForArcD = createInstructionAccept(newAddress); // self loop
//        } else if (d.getDestination().isFinal) {
//            // Accepting state sometimes jumps to next state. Self loop is not always true. Handle e.g. "dog" vs. "dogs"
//            newInstructionForArcD = createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
//        } else {
//            newInstructionForArcD =
//                    createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
//        }
//        instructionList.add(newInstructionForArcD);
//        addressInstructionHashMap.put(newAddress, newInstructionForArcD);

        // rest of the arcs
        VirtualMachine.Instruction newInstructionForArcD = new VirtualMachine.Instruction();

        for (int i = 0; i < b.getDestination().arcs.size(); i++) {
            newAddress = instructionList.size();
            Arc d = b.getDestination().arcs.get(i);
            if (d.getDestination().isFinal) {
                newInstructionForArcD =
                        createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            } else {
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }

            instructionList.add(newInstructionForArcD);
            addressInstructionHashMap.put(newAddress, newInstructionForArcD);
        }
        return newAddress;
    }

    public VirtualMachine.Instruction createInstructionFail() {
        VirtualMachine.Instruction instructionFail = new VirtualMachine.Instruction();
        instructionFail.opcode = instructionFail.FAIL;
        return instructionFail;
    }

    private VirtualMachine.Instruction createInstructionAccept(int jumpAddress) {
        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        instructionAccept.arg2 = jumpAddress;
        return instructionAccept;
    }

    public VirtualMachine.Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

    private VirtualMachine.Instruction createInstructionMatchOrAccept(char arg1, int jumpAddress, int output) {
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.ACCEPT_OR_MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

}