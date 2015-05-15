package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Instruction;
import com.atilika.kuromoji.fst.vm.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FSTCompiler {

    public Program program;

    public FSTCompiler() {
        this.program = new Program();
    }

    /**
     * Assuming Arc b already holds target jump address. Checks whether Arc b is already frozen.
     *
     * @param b
     * @return -1 if there is no Arc which input/output corresponds to key. Else return the address that corresponds to that arc.
     */
    public int referToFrozenArc(Arc b, HashMap<Integer, ArrayList<State>> statesDictionaryHashMap) {
        return b.getDestination().getInstructionAddress();
    }

    /**
     * Assigning an target jump address to Arc b.
     *
     * @param b
     */
    public void assignTargetAddressToArcB(Arc b, HashMap<Integer, ArrayList<State>> statesDictionaryHashList, boolean isStartState) {
        if (b.getDestination().arcs.size() == 0) {
            // an arc which points to dead end accepting state
            b.setTargetJumpAddress(0);// assuming dead-end accepting state is always at the address 0
//            return;
        }
        else {
            // whether equivlent destination state is already frozen

            int targetAddress = referToFrozenArc(b, statesDictionaryHashList);
            if (targetAddress != -1) {
                b.setTargetJumpAddress(targetAddress); // equivalent state found
                b.getDestination().setInstructionAddress(targetAddress);

            } else {
                // First arc is regarded as a state
                int newAddress = makeNewInstructionsForFreezingState(b, isStartState); // TODO: this method is not fully implemented yet
                b.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
                b.getDestination().setInstructionAddress(newAddress); // the last arc since it is run in reverse order
            }
        }


        if (isStartState && b.getLabel() < program.cacheFirstAddresses.length) {
            program.cacheFirstAddresses[b.getLabel()] = b.getTargetJumpAddress();
            program.cacheFirstOutputs[b.getLabel()] = b.getOutput();
            program.cacheFirstIsAccept[b.getLabel()] = b.getDestination().isFinal;
        }
    }

    public int makeNewInstructionsForFreezingState(Arc b, boolean isStartState){
        // No frozen arcs transiting to the same state. Freeze a new arc.

        program.addInstruction(createInstructionFail());
        int newAddress = program.numInstructions;
        Instruction newInstructionForArcD = new Instruction();

        for (int i = 0; i < b.getDestination().arcs.size(); i++) {

            newAddress = program.numInstructions;
            Arc d = b.getDestination().arcs.get(i);
            if (d.getDestination().isFinal) {
                newInstructionForArcD =
                        createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            } else {
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }
//
//            if (isStartState && d.getLabel() < program.cacheFirstAddresses.length) {
//                program.cacheFirstAddresses[d.getLabel()] = d.getTargetJumpAddress();
//                program.cacheFirstAddresses[d.getLabel()] = d.getOutput();
//                program.cacheFirstIsAccept[d.getLabel()] = d.getDestination().isFinal;
//            }

            program.addInstruction(newInstructionForArcD);
        }
        return newAddress;
    }

    public void makeInstructionForDeadEndState() {
        // TODO: this is currently not used. Delete it afterwards.
        if (program.numInstructions == 0) {
            char KEY_FOR_DEAD_END = ' ';
            Instruction instructionAccept = createInstructionAccept(-1);
            program.addInstruction(instructionAccept);
//            List<Integer> arcAddresses = new ArrayList<>();
//            if (arcDestinationAddressHashMap.containsKey(KEY_FOR_DEAD_END)) {
//                arcAddresses = arcDestinationAddressHashMap.get(KEY_FOR_DEAD_END);
//            }
//            int newAddress = program.numInstructions;
//            arcAddresses.add(newAddress);
//            arcDestinationAddressHashMap.put(KEY_FOR_DEAD_END, arcAddresses);
//            addressInstructionHashMap.put(newAddress, instructionAccept);
        }
        else {
            // already exists
            return;
        }
    }

    public Instruction createInstructionFail() {
        Instruction instructionFail = new Instruction();
        instructionFail.opcode = instructionFail.FAIL;
        return instructionFail;
    }

    public Instruction createInstructionAccept(int jumpAddress) {
        Instruction instructionAccept = new Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;
        instructionAccept.arg2 = jumpAddress;
        return instructionAccept;
    }

    public Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        Instruction instructionMatch = new Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

    private Instruction createInstructionMatchOrAccept(char arg1, int jumpAddress, int output) {
        Instruction instructionMatch = new Instruction();
        instructionMatch.opcode = instructionMatch.ACCEPT_OR_MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

    public Program getProgram() {
        return this.program;
    }
}