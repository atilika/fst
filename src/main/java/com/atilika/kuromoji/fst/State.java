package com.atilika.kuromoji.fst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class State {
    // Two types of strings;
    // 1. Transition string,
    // 2. Output string
    ArrayList<Arc> arcs; // possible arcs given a transition string
    boolean isFinal = false;
    boolean visited; //for visualization purpose
//    int instructionAddress = -1;

    public State() {
        this.arcs = new ArrayList<>(1);
    } // INITIAL_CAPACITY = 1

    /**
     * Copy constructor
     */
    public State(State source) {
        arcs = source.arcs;
        this.isFinal = source.isFinal;
    }


    public void setArc(char transition, int output, State toState) {
        Arc existingArc = linearSearchArc(transition);
        if (existingArc != null) {
            // does override existing arc
            arcs.remove(existingArc);
        }
        Arc newArc = new Arc(output, toState, transition);
        arcs.add(newArc);
    }

    public void setArc(char transition, State toState) {
        if (linearSearchArc(transition) != null) {
//            does not override existing arc
            return;
        }
        Arc newArc = new Arc(toState);
        newArc.setLabel(transition);
        arcs.add(newArc);
    }

    public Arc getTransitionArc(char transition) {
        if (linearSearchArc(transition) != null) {
//            return arcs.get(transition).getDestination();
            return linearSearchArc(transition);
        }
        return null;
    }


    public Arc getNextArc(char transitionString) {
        Arc nextArc = null;
        if (linearSearchArc(transitionString) != null) {
            nextArc = linearSearchArc(transitionString);
        }
        return nextArc;
    }


    public State getNextState(char transitionString) {
        State nextState = null;
        if ((getTransitionArc(transitionString)) != null) {
            Arc nextArc = linearSearchArc(transitionString);
            nextState = nextArc.getDestination();
        }
        return nextState;
    }

    public List<Character> getAllTransitionStrings() {
        List<Character> retList = new ArrayList<Character>();

        for (Arc arc : arcs) {
            retList.add(arc.getLabel());
        }

        Collections.sort(retList);

        return retList;
    }

//    public List<String> getAllOutputs() {
//        List<Character> transitionStrings = getAllTransitionStrings();
//        List<String> retList = new ArrayList<String>();
//
//        for (char transitionString : transitionStrings) {
//            retList.add(linearSearchArc(transitionString).getOutput().toString()); // adding int output
//        }
//
//        Collections.sort(retList);
//
//        return retList;
//    }

//    public boolean hasArc(char transition) {
//        return linearSearchArc(transition) != null;
//    }

    public void setFinal() {
        this.isFinal = true;
    }

//    public void setInstructionAddress(int instructionAddress) { this.instructionAddress = instructionAddress;}

//    public int getInstructionAddress() { return this.instructionAddress;}

    public Arc linearSearchArc(char transition) {
        for (Arc arc : arcs) {
            if (arc.getLabel() == transition) {
                return arc;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (isFinal != state.isFinal) return false;
        if (arcs != null ? !arcs.equals(state.arcs) : state.arcs != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = arcs != null ? arcs.hashCode() : 0;
        result = 31 * result + (isFinal ? 1 : 0);
        return result;
    }
}
