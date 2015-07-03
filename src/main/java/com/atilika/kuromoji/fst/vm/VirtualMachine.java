package com.atilika.kuromoji.fst.vm;

public class VirtualMachine {

    private boolean useCache;

    public VirtualMachine() {
        this.useCache = true;
    }

    public VirtualMachine(boolean useCache) {
        this.useCache = useCache;
    }

    public int run(Program program, String input) {
        int pc; // Thread-safe

//        pc = 0;
        pc = program.endOfTheProgram / Program.BYTES_PER_INSTRUCTIONS - 1; // Compiled in a reverse order

        int accumulator = 0; // CPU register
        int position = 0; // CPU register

        boolean done = false;
        boolean isFirstArc = true;

        while (!done) {

            // Referring to the cache
            if (useCache && isFirstArc && input.charAt(position) < program.getCacheFirstAddresses().length) {

                if (program.getCacheFirstAddresses()[input.charAt(position)] == -1) {
                    accumulator = -1;
                    break;
                }

                pc = program.getCacheFirstAddresses()[input.charAt(position)];
                accumulator += program.getCacheFirstOutputs()[input.charAt(position)];

                if (input.length() == position + 1 && program.cacheFirstIsAccept[input.charAt(position)]) {
                    // last character
                    done = true;
                }

                position++;

                isFirstArc = false;
                continue;
            }

            Instruction i = program.getInstructionAt(pc);
//            System.out.println(i);

            short opcode = i.getOpcode();

            switch (opcode) {

                case Program.MATCH:

                    char arg1 = i.getArg1();

                    if (position >= input.length()) {
                        break;
                    }

                    if (arg1 == input.charAt(position)) {
//                        pc += i.arg2 - 1; // pc is always incremented!
//                        pc = i.arg2 - 1; // JUMP to Address i.arg2
                        pc = i.getArg2() + 1; // JUMP to Address i.arg2
                        accumulator += i.getArg3();
                        position += 1; // move the input char pointer
                    }

                    break;

                case Program.HELLO:
                    System.out.println("hello!");
                    break;

                case Program.ACCEPT:
                    if (input.length() == position) {
                        done = true;
                    }
                    break;

                case Program.ACCEPT_OR_MATCH:
                    arg1 = i.getArg1();

                    if (input.length() == position + 1 && arg1 == input.charAt(position)) {
                        // last character
                        accumulator += i.getArg3();
                        done = true;
                    }
                    else {
                        if (position < input.length() && arg1 == input.charAt(position)) {
//                        pc += i.arg2 - 1; // pc is always incremented!
                            pc = i.getArg2() + 1; // JUMP to Address i.arg2
                            accumulator += i.getArg3();
                            position += 1; // move the input char pointer
                        }
                    }
                    break;

                case Program.FAIL:
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
