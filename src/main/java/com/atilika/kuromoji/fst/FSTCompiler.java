package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    public char KEY_FOR_DEADEND_ARC = '|';

    HashMap<String, List<Integer>> arcAddressHashMap = new HashMap<>();
    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap = new HashMap<>();
    public List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

    // To be unit tested in the following methods....

    /**
     * Checks whether a given arc's jump address is same as the input jumpAddress
     *
     * @param jumpAddress
     * @param arc
     * @return
     */
    public boolean isJumpToSameAddress(int jumpAddress, Arc arc) {
        if (arc.getTargetJumpAddress() == jumpAddress) {
            // getTargetJumpAddress is Arc d
            return true;
        }
        return false;
    }

    /**
     * Checks whether Arc b is already frozen
     *
     * @param b
     * @param key
     * @return -1 if there is no Arc which input/output corresponds to key. Else return the address that corresponds to that arc.
     */
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
            VirtualMachine.Instruction newInstructionForArcD = new VirtualMachine.Instruction();
            if (d.getLabel() == KEY_FOR_DEADEND_ARC) {
                newInstructionForArcD = createInstructionAccept(newAddress); // self loop
            }
            else if (d.getDestination().isFinal) {
                // Accepting state sometimes jumps to next state. Self loop is not always true. Handle e.g. "dog" vs. "dogs"
                newInstructionForArcD = createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }
            else {
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }
            instructionList.add(newInstructionForArcD);
            addressInstructionHashMap.put(newAddress, newInstructionForArcD);

            // rest of the arcs
            for (int i = 1; i < b.getDestination().arcs.size(); i++) {
                newAddress = instructionList.size();
                d = b.getDestination().arcs.get(i);
                if (d.getDestination().isFinal) {
                    newInstructionForArcD =
                            createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
                }
                else {
                    newInstructionForArcD =
                            createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
                }

                instructionList.add(newInstructionForArcD);
                addressInstructionHashMap.put(newAddress, newInstructionForArcD);
            }
            b.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
        }
    }

    private VirtualMachine.Instruction createInstructionFail() {
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

    private VirtualMachine.Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
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