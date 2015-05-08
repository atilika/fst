package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

//    HashMap<String, List<Integer>> arcDestinationAddressHashMap = new HashMap<>();
    HashMap<Character, List<Integer>> arcDestinationAddressHashMap = new HashMap<>(); // points to the starting Arc address of a state
    //    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap = new HashMap<>();
    public List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

    /**
     * Assuming Arc b already holds target jump address. Checks whether Arc b is already frozen.
     *
     * @param b
     * @return -1 if there is no Arc which input/output corresponds to key. Else return the address that corresponds to that arc.
     */
    public int referToFrozenArc(Arc b) {
        // remember that key only refers to where Arc b is in the Program.
        // so you have to check whether b's destination is compiled or not.
        char key = b.getLabel();
        if (!arcDestinationAddressHashMap.containsKey(key)) {
            return -1;
        }

        List<Integer> arcAddresses = arcDestinationAddressHashMap.get(key);

        for (Integer arcAddress : arcAddresses) {
//            int arcBtargetAddress = addressInstructionHashMap.get(arcAddress).arg2; //
//            int arcBtargetAddress = instructionList.get(arcAddress).arg2; //
            int arcBtargetAddress = arcAddress; //
            if (b.getTargetJumpAddress() == arcBtargetAddress) {
                return arcBtargetAddress; // transiting to the same state.
            }
        }

        return -1;
    }

    /**
     * Assigning an target jump address to Arc b.
     *
     * @param b
     */
    public void assignTargetAddressToArcB(Arc b) {
        if (b.getDestination().arcs.size() == 0) {
            // an arc which points to dead end accepting state
            b.setTargetJumpAddress(0);// assuming dead-end accepting state is always at the address 0
            return;
        }

        int targetAddress = referToFrozenArc(b);
        if (targetAddress != -1) {
            b.setTargetJumpAddress(targetAddress); // equivalent state found
        }
        else {
            // First arc is regarded as a state
            int newAddress = makeNewInstructionsForFreezingState(b); // TODO: this method is not fully implemented yet
            b.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
        }
    }

    public int makeNewInstructionsForFreezingState(Arc b){
        // No frozen arcs transiting to the same state. Freeze a new arc.

        char key = b.getLabel();
        instructionList.add(createInstructionFail());

        List<Integer> arcAddresses = new ArrayList<>();
        int newAddress = instructionList.size();

        // 1. Create a new List for a new key
        if (arcDestinationAddressHashMap.containsKey(key)) {
            arcAddresses = arcDestinationAddressHashMap.get(key);
        }
        arcAddresses.add(newAddress); // destination
        arcDestinationAddressHashMap.put(key, arcAddresses);

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
//            addressInstructionHashMap.put(newAddress, newInstructionForArcD);
        }
        return newAddress;
    }

    public void makeInstructionForDeadEndState() {
        if (instructionList.size() == 0) {
            char KEY_FOR_DEAD_END = ' ';
            VirtualMachine.Instruction instructionAccept = createInstructionAccept(-1);
            instructionList.add(instructionAccept); // TODO: refactor this
            List<Integer> arcAddresses = new ArrayList<>();
            if (arcDestinationAddressHashMap.containsKey(KEY_FOR_DEAD_END)) {
                arcAddresses = arcDestinationAddressHashMap.get(KEY_FOR_DEAD_END);
            }
            int newAddress = instructionList.size();
            arcAddresses.add(newAddress);
            arcDestinationAddressHashMap.put(KEY_FOR_DEAD_END, arcAddresses);
//            addressInstructionHashMap.put(newAddress, instructionAccept);
        }
        else {
            // already exists
            return;
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