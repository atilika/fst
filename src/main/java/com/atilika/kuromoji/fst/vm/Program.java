package com.atilika.kuromoji.fst.vm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Program {

    int endOfTheProgram = 0; // end of the pc;
    public int numInstructions = 0;

    public final static int BYTES_PER_INSTRUCTIONS = 12;

    //        int instructionsSize = BYTES_PER_INSTRUCTIONS * 1000000;
    int instructionsSize = BYTES_PER_INSTRUCTIONS * 5000000;
    ByteBuffer instruction = ByteBuffer.allocate(instructionsSize); // init
    int instructionIndex = 0;
//        List<Instruction> instructions = new ArrayList<>();

    public Instruction getInstructionAt(int pc) {
        int internalIndex = pc * BYTES_PER_INSTRUCTIONS;
//            short opcode = (short) (instruction.get(internalIndex) << 8 | instruction.get(internalIndex + 1));
//            char arg1 = (char) (instruction.get(internalIndex + 2) << 8 | instruction.get(internalIndex + 3));
//            int arg2 = instruction.get(internalIndex + 4) << 24 | instruction.get(internalIndex + 5) << 16 | instruction.get(internalIndex + 6) << 8 | instruction.get(internalIndex + 7);
//            int arg3 = instruction.get(internalIndex + 8) << 24 | instruction.get(internalIndex + 9) << 16 | instruction.get(internalIndex + 10) << 8 | instruction.get(internalIndex + 11);

        instruction.position(internalIndex);

        Instruction i = new Instruction();
        i.opcode = instruction.getShort();
        i.arg1 = instruction.getChar();
        i.arg2 = instruction.getInt();
        i.arg3 = instruction.getInt();

        return i;
    }

    public void addInstruction(Instruction i) {
//            int internalIndex = instructionIndex * BYTES_PER_INSTRUCTIONS;

//            if (internalIndex > instructionsSize ) {
//                // grow byte array by doubling the size of it.
//                ByteBuffer newInstructions = ByteBuffer.allocate()
//
//            }
        // write instruction into bytearray as bytes..

        instruction.putShort(i.opcode);
        instruction.putChar(i.arg1);
        instruction.putInt(i.arg2);
        instruction.putInt(i.arg3);

        endOfTheProgram += BYTES_PER_INSTRUCTIONS;
        numInstructions += 1; // TODO: integrate this variable with the above.
    }

    public void addInstructions(List<Instruction> instructions) {

        for (Instruction i : instructions) {
            addInstruction(i);
        }
    }

    public List<Instruction> debugInstructions() {
        List<Instruction> instructions = new ArrayList<>();
        int pc = 0;
        int end = this.instruction.position() / Program.BYTES_PER_INSTRUCTIONS - 1;
        while (pc <= end) {
            instructions.add(this.getInstructionAt(pc));
            pc++;
        }
        return instructions;
    }
}
