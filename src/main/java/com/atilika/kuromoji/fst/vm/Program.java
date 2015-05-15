package com.atilika.kuromoji.fst.vm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Program {

    int endOfTheProgram = 0; // end of the pc;
    public int numInstructions = 0;

    public final static int BYTES_PER_INSTRUCTIONS = 11;

    //        int instructionsSize = BYTES_PER_INSTRUCTIONS * 1000000;
//    int instructionsSize = BYTES_PER_INSTRUCTIONS * 1000000;
    int instructionsSize = BYTES_PER_INSTRUCTIONS * 1000000;
    ByteBuffer instruction = ByteBuffer.allocate(instructionsSize); // init

    int CACHED_CHAR_RANGE = 1 << 16;
    public int[] cacheFirstAddresses;
    public int[] cacheFirstOutputs;
    public boolean[] cacheFirstIsAccept;

    public Program() {
        this.cacheFirstAddresses = new int[CACHED_CHAR_RANGE];
        Arrays.fill(this.cacheFirstAddresses, -1);
        this.cacheFirstOutputs = new int[CACHED_CHAR_RANGE];
        this.cacheFirstIsAccept = new boolean[CACHED_CHAR_RANGE];
    }

    public Instruction getInstructionAt(int pc) {
        int internalIndex = pc * BYTES_PER_INSTRUCTIONS;
//            short opcode = (short) (instruction.get(internalIndex) << 8 | instruction.get(internalIndex + 1));
//            char arg1 = (char) (instruction.get(internalIndex + 2) << 8 | instruction.get(internalIndex + 3));
//            int arg2 = instruction.get(internalIndex + 4) << 24 | instruction.get(internalIndex + 5) << 16 | instruction.get(internalIndex + 6) << 8 | instruction.get(internalIndex + 7);
//            int arg3 = instruction.get(internalIndex + 8) << 24 | instruction.get(internalIndex + 9) << 16 | instruction.get(internalIndex + 10) << 8 | instruction.get(internalIndex + 11);

        instruction.position(internalIndex);

        Instruction i = new Instruction();
//        i.opcode = instruction.getShort();
        i.opcode = instruction.get();
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

//        instruction.putShort(i.opcode);
        instruction.put(i.opcode);
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

    public int[] getCacheFirstAddresses() {return this.cacheFirstAddresses;}

    public int[] getCacheFirstOutputs() {return this.cacheFirstOutputs;}

    public void outputProgramToFile() throws IOException {
        ByteBuffer bbuf = this.instruction;
        File file = new File("fstByteBuffer");

        boolean append = false;

        FileChannel wChannel = new FileOutputStream(file, append).getChannel();

        wChannel.write(bbuf);

        wChannel.close();
    }
}
