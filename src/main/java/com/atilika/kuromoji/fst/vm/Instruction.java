package com.atilika.kuromoji.fst.vm;

public class Instruction {

    public static final byte MATCH = 1;
    public static final byte NOP = 2;
    public static final byte FAIL = 3;
    public static final byte HELLO = 4;
    public static final byte ACCEPT = 5;
    public static final byte ACCEPT_OR_MATCH = 6;

    public byte opcode;

    public char arg1;

    public int arg2;

    public int arg3; // used as a output for a FST arc

    @Override
    public String toString() {
        return "Instruction{" +
                "opcode=" + opcode +
                ", arg1=" + arg1 +
                ", arg2=" + arg2 +
                ", arg3=" + arg3 +
                '}';
    }
}
