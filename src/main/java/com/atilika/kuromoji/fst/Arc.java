package com.atilika.kuromoji.fst;

public class Arc {
    char label; // for dead end state
    int output = 0;
    State destination;
    int targetJumpAddress;


    public Arc(int output, State destination, char label) {
        this.output = output;
        this.destination = destination;
        this.label = label;
        this.targetJumpAddress = -1;
    }

    public Arc(State destination) {
        this.destination = destination;
    }

    public State getDestination() {
        return this.destination;
    }

    public Integer getOutput() {
        return this.output;
    }

    public char getLabel() {
        return this.label;
    }

    public int getTargetJumpAddress() { return this.targetJumpAddress; }

    public void setOutput(Integer output) {this.output = output;}

    public void setLabel(char label) {
        this.label = label;
    }

    public void setTargetJumpAddress(int address) {
        this.targetJumpAddress = address;
    }
}
