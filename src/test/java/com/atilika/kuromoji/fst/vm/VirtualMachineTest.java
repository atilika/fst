package com.atilika.kuromoji.fst.vm;

import com.atilika.kuromoji.fst.FSTCompiler;
import com.atilika.kuromoji.fst.vm.Instruction;
import com.atilika.kuromoji.fst.vm.Program;
import com.atilika.kuromoji.fst.vm.VirtualMachine;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class VirtualMachineTest {

    @Test
    public void testHelloVM()
    {
        VirtualMachine vm = new VirtualMachine(false);
        Program program = new Program();

        Instruction instruction = new Instruction();
        instruction.opcode = Program.HELLO;

        Instruction instructionFail = new Instruction();
        instructionFail.opcode = Program.FAIL;

        program.addInstruction(instruction);
        program.addInstruction(instructionFail);

        vm.run(program, "");

    }

    @Test
    public void testMatch() throws Exception {
        // testing the input string "a" being accepted or not
        VirtualMachine vm = new VirtualMachine(false);
        Program program = new Program();
        Instruction instructionMatch = new Instruction();
        instructionMatch.opcode = Program.MATCH;
        instructionMatch.arg1 = 'a'; // transition string
        instructionMatch.arg2 = 1;  // target address, delta coded
        instructionMatch.arg3 = 1; // output, value to be accumulated;

        Instruction instructionAccept = new Instruction();
        instructionAccept.opcode = Program.ACCEPT;

        program.addInstructions(
                Arrays.asList(instructionAccept, instructionMatch)
        );

        assertEquals(1, vm.run(program, "a"));
    }

    @Test
    public void testAddInstructions() throws Exception {
        FSTCompiler fstCompiler = new FSTCompiler();
        Instruction instructionAccept = fstCompiler.createInstructionAccept(0);
        Instruction instructionFail = fstCompiler.createInstructionFail();
        Instruction instructionMatch = fstCompiler.createInstructionMatch('a', 1, 1);

        Program program = new Program();
        program.addInstructions(Arrays.asList(instructionAccept, instructionFail, instructionMatch));

        List<Instruction> instructions = program.dumpInstructions();

        Instruction storedMatchInstruction = program.getInstructionAt(2);

        assertEquals(storedMatchInstruction.arg1, instructionMatch.arg1);
        assertEquals(storedMatchInstruction.arg2, instructionMatch.arg2);
        assertEquals(storedMatchInstruction.arg3, instructionMatch.arg3);

        Instruction storedAcceptInstruction = program.getInstructionAt(0);
        assertEquals(storedAcceptInstruction.toString(), instructionAccept.toString());

        Instruction storedFailInstruction = program.getInstructionAt(1);
        assertEquals(storedFailInstruction.toString(), instructionFail.toString());
    }
}