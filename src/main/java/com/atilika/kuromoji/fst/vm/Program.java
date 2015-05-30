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

    public static final byte MATCH = 1;
    public static final byte FAIL = 3;
    public static final byte HELLO = 4;
    public static final byte ACCEPT = 5;
    public static final byte ACCEPT_OR_MATCH = 6;

    int endOfTheProgram = 0; // end of the pc;
    public int numInstructions = 0;

    public final static int BYTES_PER_INSTRUCTIONS = 11;
    int numInstructionsAllocated = 100000;

    int instructionsSize = BYTES_PER_INSTRUCTIONS * numInstructionsAllocated;
    ByteBuffer instruction = ByteBuffer.allocate(instructionsSize); // init

    int CACHED_CHAR_RANGE = 1 << 16; // 2bytes, range of whole char type.
    public int[] cacheFirstAddresses; // 4 bytes * 66536 = 262,144 = 262KB
    public int[] cacheFirstOutputs;  // 262KB
    public boolean[] cacheFirstIsAccept; // 1 bit * 66536 = 66536 bits = 8317 bits = 8KB

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
        i.opcode = instruction.get();
        i.arg1 = instruction.getChar();
        i.arg2 = instruction.getInt();
        i.arg3 = instruction.getInt();

        return i;
    }

//    public byte getOpcodeAt(int pc) {
//        return instruction.get();
//    }

    /**
     * Add an instruction to Bytebuffer. Doubling the size of buffer when the current size is not enough.
     *
     * @param i
     */
    public void addInstruction(Instruction i) {
        doubleBufferSize();

        instruction.put(i.opcode);
        instruction.putChar(i.arg1);
        instruction.putInt(i.arg2);
        instruction.putInt(i.arg3);

        endOfTheProgram += BYTES_PER_INSTRUCTIONS;
        numInstructions += 1; // TODO: integrate this variable with the above.
    }

    public void addInstruction(byte op, char label, int targetAddress, int output) {
        doubleBufferSize();
        instruction.put(op);
        instruction.putChar(label);
        instruction.putInt(targetAddress);
        instruction.putInt(output);

        endOfTheProgram += BYTES_PER_INSTRUCTIONS;
        numInstructions += 1;
    }

    public void addInstructionFail() {
        addInstruction(FAIL, ' ', -1, 0); // Ideally, compress this
    }

    public void addInstructionMatch(char label, int targetAddress, int output) {
        addInstruction(MATCH, label, targetAddress, output);
    }

    public void addInstructionMatchOrAccept(char label, int targetAddress, int output) {
        addInstruction(ACCEPT_OR_MATCH, label, targetAddress, output);
    }

    private void doubleBufferSize() {
        int currentSizePlusOneInstruction = (numInstructions + 1) * BYTES_PER_INSTRUCTIONS;

        if (currentSizePlusOneInstruction > instructionsSize) {
            // grow byte array by doubling the size of it.
            numInstructionsAllocated = numInstructionsAllocated << 1;
            instructionsSize = BYTES_PER_INSTRUCTIONS * numInstructionsAllocated;
            ByteBuffer newInstructions = ByteBuffer.allocate(instructionsSize);
            instruction.flip(); // limit ← position、 position ← 0
            newInstructions.put(instruction);
            instruction = newInstructions;
        }
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

        bbuf.flip();
        wChannel.write(bbuf);

        wChannel.close();
    }
}
