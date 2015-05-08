package com.atilika.kuromoji.fst;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class VirtualMachineTest {

    @Test
    public void testHelloVM()
    {
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();

        VirtualMachine.Instruction instruction = new VirtualMachine.Instruction();
        instruction.opcode = instruction.HELLO;

        VirtualMachine.Instruction instructionFail = new VirtualMachine.Instruction();
        instructionFail.opcode = instructionFail.FAIL;

        program.addInstruction(instruction);
        program.addInstruction(instructionFail);

        vm.run(program, "");

    }

    @Test
    public void testMatch() throws Exception {
        // testing the input string "a" being accepted or not
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();
        VirtualMachine.Instruction instructionMatch = new VirtualMachine.Instruction();
        instructionMatch.opcode = instructionMatch.MATCH;
        instructionMatch.arg1 = 'a'; // transition string
        instructionMatch.arg2 = 1;  // target address, delta coded
        instructionMatch.arg3 = 1; // output, value to be accumulated;

        VirtualMachine.Instruction instructionAccept = new VirtualMachine.Instruction();
        instructionAccept.opcode = instructionAccept.ACCEPT;

//        VirtualMachine.Instruction[] instructions =
//                new VirtualMachine.Instruction[] {instructionMatch, instructionAccept};
        program.addInstructions(
                Arrays.asList(instructionAccept, instructionMatch)
        );

        assertEquals(1, vm.run(program, "a"));
    }

    @Test
    public void testAddInstructions() throws Exception {
        FSTCompiler fstCompiler = new FSTCompiler();
        VirtualMachine.Instruction instructionAccept = fstCompiler.createInstructionAccept(0);
        VirtualMachine.Instruction instructionFail = fstCompiler.createInstructionFail();
        VirtualMachine.Instruction instructionMatch = fstCompiler.createInstructionMatch('a', 1, 1);

        VirtualMachine.Program program = new VirtualMachine.Program();
        program.addInstructions(Arrays.asList(instructionAccept, instructionFail, instructionMatch));
//        program.addInstruction(instructionAccept);
//        program.addInstruction(instructionFail);
//        program.addInstruction(instructionMatch);

        VirtualMachine.Instruction storedMatchInstruction = program.getInstructionAt(2);

        assertEquals(storedMatchInstruction.arg1, instructionMatch.arg1);
        assertEquals(storedMatchInstruction.arg2, instructionMatch.arg2);
        assertEquals(storedMatchInstruction.arg3, instructionMatch.arg3);

        VirtualMachine.Instruction storedAcceptInstruction = program.getInstructionAt(0);
        assertEquals(storedAcceptInstruction.toString(), instructionAccept.toString());

        VirtualMachine.Instruction storedFailInstruction = program.getInstructionAt(1);
        assertEquals(storedFailInstruction.toString(), instructionFail.toString());
    }
}