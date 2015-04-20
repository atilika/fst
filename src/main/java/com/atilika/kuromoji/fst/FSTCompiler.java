package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    HashMap<String, List<Integer>> arcAddressHashMap = new HashMap<>();
    HashMap<Integer, VirtualMachine.Instruction> addressInstructionHashMap = new HashMap<>();
    public List<VirtualMachine.Instruction> instructionList = new ArrayList<>();

    /**
     * Assuming Arc b already holds target jump address. Checks whether Arc b is already frozen.
     *
     * @param b
     * @param key
     * @return -1 if there is no Arc which input/output corresponds to key. Else return the address that corresponds to that arc.
     */
    public int referToFrozenArc(Arc b, String key) {
        // remember that key only refers to where Arc b is in the Program.
        // so you have to check whether b's destination is compiled or not.
        if (!arcAddressHashMap.containsKey(key)) {
            return -1;
        }

        List<Integer> arcAddresses = arcAddressHashMap.get(key);

        for (Integer arcAddress : arcAddresses) {
            int arcBtargetAddress = addressInstructionHashMap.get(arcAddress).arg2; //
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
     * @param key
     */
    public void assignTargetAddressToArcB(Arc b, String key) {
        if (b.getDestination().arcs.size() == 0) {
            // an arc which points to dead end accepting state
            b.setTargetJumpAddress(0);// assuming dead-end accepting state is always at the address 0
            return;
        }

        int targetAddress = referToFrozenArc(b, key);
        if (targetAddress != -1) {
            b.setTargetJumpAddress(targetAddress); // equivalent state found
        }
        else {

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

    public void makeInstructionForDeadEndState() {
        if (instructionList.size() == 0) {
            VirtualMachine.Instruction instructionAccept = createInstructionAccept(-1);
            instructionList.add(instructionAccept); // TODO: refactor this
            List<Integer> arcAddresses = new ArrayList<>();
            if (arcAddressHashMap.containsKey(" ")) {
                arcAddresses = arcAddressHashMap.get(" ");
            }
            int newAddress = instructionList.size();
            arcAddresses.add(newAddress);
            arcAddressHashMap.put(" ", arcAddresses);
            addressInstructionHashMap.put(newAddress, instructionAccept);
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