package com.atilika.kuromoji.fst;

import org.junit.Test;

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

        VirtualMachine.Instruction[] instructions =
                new VirtualMachine.Instruction[] {instructionMatch, instructionAccept};
        program.addInstructions(instructions);

        assertEquals(1, vm.run(program, "a"));
    }

    @Test
    public void testMultipleMatches() throws Exception {


    }
}