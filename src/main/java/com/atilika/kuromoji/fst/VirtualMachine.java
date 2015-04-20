package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualMachine {

    private int pc;

    public static class Program {

        List<Instruction> instructions = new ArrayList<>();

        Instruction getInstructionAt(int pc) {
            return instructions.get(pc);
        }

        void addInstruction(Instruction instruction) {
            instructions.add(instruction);
        }

        void addInstructions(Instruction[] instructions) {
            this.instructions = Arrays.asList(instructions);
        }

        void addInstructions(List<Instruction> instructions) {
            this.instructions =instructions;
        }
    }

    public static class Instruction {

        public static final int MATCH = 1;
        public static final int NOP = 2;
        public static final int FAIL = 3;
        public static final int HELLO = 4;
        public static final int ACCEPT = 5;
        public static final int ACCEPT_OR_MATCH = 6;

        int opcode;

        char arg1;

        int arg2;

        int arg3; // used as a output for a FST arc
    }

    public VirtualMachine() {
        pc = 0;
    }

    public int run(Program program, String input) {

//        pc = 0;
        pc = program.instructions.size() - 1; // Compiled in a reverse order

        int accumulator = 0; // CPU register

        int position = 0; // CPU register

        boolean done = false;

        while (!done) {

            Instruction i = program.getInstructionAt(pc);

            int opcode = i.opcode;

            switch (opcode) {


                case Instruction.MATCH:

                    char arg1 = i.arg1;

                    if (position >= input.length()) {
                        break;
                    }

                    if (arg1 == input.charAt(position)) {
//                        pc += i.arg2 - 1; // pc is always incremented!
//                        pc = i.arg2 - 1; // JUMP to Address i.arg2
                        pc = i.arg2 + 1; // JUMP to Address i.arg2
                        accumulator += i.arg3;
                        position += 1; // move the input char pointer
                    }

                    break;

//                case Instruction.ACCUMULATE:
//                    accumulator += i.arg2;
//                    break;


                case Instruction.HELLO:
                    System.out.println("hello!");
                    break;

                case Instruction.NOP:
                    break;

                case Instruction.ACCEPT:
                    if (input.length() == position) {
                        done = true;
                    }
                    break;

                case Instruction.ACCEPT_OR_MATCH:
                    arg1 = i.arg1;

                    if (input.length() == position + 1 && arg1 == input.charAt(position)) {
                        // last character
                        accumulator += i.arg3;
                        done = true;
                    }
                    else {
                        if (position < input.length() && arg1 == input.charAt(position)) {
//                        pc += i.arg2 - 1; // pc is always incremented!
//                        pc = i.arg2 - 1; // JUMP to Address i.arg2
                            pc = i.arg2 + 1; // JUMP to Address i.arg2
                            accumulator += i.arg3;
                            position += 1; // move the input char pointer
                        }
                    }
                    break;

                case Instruction.FAIL:
                    done = true;
                    accumulator = -1;
                    break;

                default:
                    throw new RuntimeException("You have screwed up badly, please go away!");
            }

//            pc++;
            pc--;
        }

        return accumulator;

    }

}
