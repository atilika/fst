package com.atilika.kuromoji.fst.vm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VirtualMachine {

    private int pc;

    public VirtualMachine() {
        pc = 0;
    }

    public int run(Program program, String input) {

//        pc = 0;
//        pc = program.instruction.position() / Program.BYTES_PER_INSTRUCTIONS - 1; // Compiled in a reverse order
        pc = program.endOfTheProgram / Program.BYTES_PER_INSTRUCTIONS - 1; // Compiled in a reverse order

        int accumulator = 0; // CPU register

        int position = 0; // CPU register

        boolean done = false;

        while (!done) {

            Instruction i = program.getInstructionAt(pc);
//            System.out.println(i);

            short opcode = i.opcode;

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