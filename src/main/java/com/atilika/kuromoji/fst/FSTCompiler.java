package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Instruction;
import com.atilika.kuromoji.fst.vm.Program;

import java.util.*;

public class FSTCompiler {

    public Program program;

    public FSTCompiler() {
        this.program = new Program();
    }

    /**
     * Assigning an target jump address to Arc b.
     *
     * @param b
     */
    public void assignTargetAddressToArcB(Arc b, boolean isStartState) {
        if (b.getDestination().arcs.size() == 0) {
            // an arc which points to dead end accepting state
            b.setTargetJumpAddress(0);// assuming dead-end accepting state is always at the address 0
        }
        else {
            // check whether equivalent destination state is already frozen
            int targetAddress = b.getTargetJumpAddress();
            if (targetAddress != -1) {
                b.setTargetJumpAddress(targetAddress); // equivalent state found

            } else {
                // First arc is regarded as a state
                int newAddress = makeNewInstructionsForFreezingState(b);
                b.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
            }
        }

        if (isStartState && b.getLabel() < program.cacheFirstAddresses.length) {
            program.cacheFirstAddresses[b.getLabel()] = b.getTargetJumpAddress();
            program.cacheFirstOutputs[b.getLabel()] = b.getOutput();
            program.cacheFirstIsAccept[b.getLabel()] = b.getDestination().isFinal;
        }
    }

    /**
     * Freeze a new arc since no frozen arcs transiting to the same state.
     *
     * @param b
     * @return the address of new instruction
     */
    public int makeNewInstructionsForFreezingState(Arc b){

        program.addInstruction(createInstructionFail());
        int newAddress = program.numInstructions;
        Instruction newInstructionForArcD;

        for (Arc d : b.getDestination().arcs) {
            newAddress = program.numInstructions;
            if (d.getDestination().isFinal) {
                newInstructionForArcD =
                        createInstructionMatchOrAccept(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            } else {
                newInstructionForArcD =
                        createInstructionMatch(d.getLabel(), d.getTargetJumpAddress(), d.getOutput());
            }

            program.addInstruction(newInstructionForArcD);
        }
        return newAddress;
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