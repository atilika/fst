package com.atilika.kuromoji.fst.vm;

public class Instruction {

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
