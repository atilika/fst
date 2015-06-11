package com.atilika.kuromoji.fst;

import com.atilika.kuromoji.fst.vm.Instruction;
import com.atilika.kuromoji.fst.vm.Program;

public class FSTCompiler {

    private static final int ADDRESS_FAIL = 0; // Address 0 stores the information about the buffer size

    public Program program;

    public FSTCompiler() {
        this.program = new Program();
    }

    /**
     * Assigning an target jump address to Arc b and make corresponding Instruction.
     *
     * @param b
     */
    public void compileArc(Arc b, boolean isStartState) {
        State state = b.getDestination();
        if (state.arcs.size() == 0) {
            // an arc which points to dead end accepting state
            state.setTargetJumpAddress(ADDRESS_FAIL);// assuming dead-end accepting state is always at the address 0
        }
        else {
            // check whether equivalent destination state is already frozen
            if (state.getTargetJumpAddress() == -1) {
                // First arc is regarded as a state
                int newAddress = makeNewInstructionsForFreezingState(state);
                state.setTargetJumpAddress(newAddress); // the last arc since it is run in reverse order
            }
        }

        if (isStartState && b.getLabel() < program.cacheFirstAddresses.length) {
            // making states for the arcs outgoing from starting state. Not necessary since cache is enabled.
//            program.addInstructionFail();
            compileArcToInstruction(b);
            cacheArcs(b, state);
        }
    }

    /**
     * Cache the outgoing arcs from the starting state
     * @param b
     * @param state
     */
    private void cacheArcs(Arc b, State state) {
        program.cacheFirstAddresses[b.getLabel()] = state.getTargetJumpAddress();
        program.cacheFirstOutputs[b.getLabel()] = b.getOutput();
        program.cacheFirstIsAccept[b.getLabel()] = state.isFinal();
    }

    /**
     * Freeze a new arc since no frozen arcs transiting to the same state.
     *
     * @param freezingState
     * @return the address of new instruction
     */
    public int makeNewInstructionsForFreezingState(State freezingState){
        program.addInstructionFail();

        for (Arc outgoingArc : freezingState.arcs) {
            compileArcToInstruction(outgoingArc);
        }

        int newAddress = program.getNumInstructions() - 1;
        freezingState.setTargetJumpAddress(newAddress);
        return newAddress;
    }

    private void compileArcToInstruction(Arc d) {
        if (d.getDestination().isFinal()) {
            program.addInstructionMatchOrAccept(d.getLabel(), d.getDestination().getTargetJumpAddress(), d.getOutput());
        } else {
            program.addInstructionMatch(d.getLabel(), d.getDestination().getTargetJumpAddress(), d.getOutput());
        }
    }

    /**
     * Compile Instructions for starting state
     *
     * @param state
     */
    public void compileStartingState(State state) {
        program.addInstructionFail();
        // TODO: Probably better to create a separate method for compiling arcs outgoing from starting state.
        for (Arc arc : state.arcs) {
            compileArc(arc, true);
        }
    }

    public Instruction createInstructionFail() {
        Instruction instructionFail = new Instruction();
        instructionFail.opcode = program.FAIL;
        return instructionFail;
    }

    public Instruction createInstructionAccept(int jumpAddress) {
        Instruction instructionAccept = new Instruction();
        instructionAccept.opcode = program.ACCEPT;
        instructionAccept.arg2 = jumpAddress;
        return instructionAccept;
    }

    public Instruction createInstructionMatch(char arg1, int jumpAddress, int output) {
        Instruction instructionMatch = new Instruction();
        instructionMatch.opcode = program.MATCH;
        instructionMatch.arg1 = arg1;
        instructionMatch.arg2 = jumpAddress;
        instructionMatch.arg3 = output;
        return instructionMatch;
    }

//    private Instruction createInstructionMatchOrAccept(char arg1, int jumpAddress, int output) {
//        Instruction instructionMatch = new Instruction();
//        instructionMatch.opcode = instructionMatch.ACCEPT_OR_MATCH;
//        instructionMatch.arg1 = arg1;
//        instructionMatch.arg2 = jumpAddress;
//        instructionMatch.arg3 = output;
//        return instructionMatch;
//    }

    public Program getProgram() {
        return this.program;
    }
}