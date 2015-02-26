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
        VirtualMachine vm = new VirtualMachine();
        VirtualMachine.Program program = new VirtualMachine.Program();

    }
}