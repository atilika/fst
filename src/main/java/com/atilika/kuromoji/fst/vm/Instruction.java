package com.atilika.kuromoji.fst.vm;

public class Instruction {

    public static final short MATCH = 1;
    public static final short NOP = 2;
    public static final short FAIL = 3;
    public static final short HELLO = 4;
    public static final short ACCEPT = 5;
    public static final short ACCEPT_OR_MATCH = 6;

    public short opcode;

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
