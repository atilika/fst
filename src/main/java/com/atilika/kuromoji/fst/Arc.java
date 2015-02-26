package com.atilika.kuromoji.fst;

public class Arc {
    char label; // for dead end state
    int output = 0;
    State destination;

    public Arc(int output, State destination, char label) {
        this.output = output;
        this.destination = destination;
        this.label = label;
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

    public void setOutput(Integer output) {this.output = output;}

    public void setLabel(char label) {
        this.label = label;
    }
}
