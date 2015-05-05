package com.atilika.kuromoji.fst;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FST {
    // Note that FST only allows the presorted dictionaries as input.

//    private HashMap<String, ArrayList<State>> statesDictionaryHashList;
    private HashMap<Integer, ArrayList<State>> statesDictionaryHashList;
    public FSTCompiler fstCompiler = new FSTCompiler();

    // TODO: Rewrite this...
    public int MAX_WORD_LENGTH = 100;

    public FST() {
//        this.statesDictionaryHashList = new HashMap<String, ArrayList<State>>();
        this.statesDictionaryHashList = new HashMap<Integer, ArrayList<State>>();
        ArrayList<State> stateList = new ArrayList<State>();
        stateList.add(new State());
//        this.statesDictionaryHashList.put("start state", stateList); // setting the start state
        this.statesDictionaryHashList.put(0, stateList); // temporary setting the start state

    }

    /**
     * Applies the transducer over the input text
     *
     * @param input input text to transduce
     * @return corresponding value on a match and -1 otherwise
     */
    public int transduce(String input) {
        State currentState = this.getStartState();
        int output = 0; // assuming that output is a int type

        // transitioning according to input
        for (int i = 0; i < input.length(); i++) {
            char currentTransition = input.charAt(i);
            Arc nextArc = currentState.getTransitionArc(currentTransition);
            if (nextArc == null) {
                return -1;
            }
            currentState = nextArc.getDestination();
            output += nextArc.getOutput();
        }

        return output;
    }


    public State getStartState() {
//        return this.statesDictionaryHashList.get("start state").get(0);
        return this.statesDictionaryHashList.get(0).get(0);
    }

    /**
     * For this method, once it reads the string, it throws away.
     *
     * @param reader
     * @throws IOException
     */
    public void createDictionaryIncremental(BufferedReader reader) throws IOException {
        int maxWordLength = MAX_WORD_LENGTH; // temporal setting
        String previousWord = "";
        State[] tempStates = initializeState(maxWordLength + 1);
        tempStates[0] = this.getStartState(); // initial state

        int outputValue = 1; // Initialize output value

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("#.*$", "");
            if (line.trim().length() == 0) {
                continue;
            }
            String inputWord = line;
            createDictionaryCommon(inputWord, previousWord, tempStates, outputValue);
            previousWord = inputWord;
            outputValue++; // allocate the next wordID
        }

        // for last word
        String lastWord = previousWord;
        handleLastWord(previousWord, lastWord, tempStates);
    }


    /**
     * builds FST given input words and output values
     *
     * @param inputWords
     * @param outputValues
     */
    public void createDictionary(String[] inputWords, int[] outputValues) {
        int maxWordLength = MAX_WORD_LENGTH; // temporal setting
        String previousWord = "";
        State[] tempStates = initializeState(maxWordLength + 1);
        tempStates[0] = this.getStartState(); // initial state

        for (int inputWordIdx = 0; inputWordIdx < inputWords.length; inputWordIdx++) {
            String inputWord = inputWords[inputWordIdx];
            createDictionaryCommon(inputWord, previousWord, tempStates, outputValues[inputWordIdx]);
            previousWord = inputWord;
        }

        // for last word
        String lastWord = previousWord;
        handleLastWord(previousWord, lastWord, tempStates);
    }

    private void createDictionaryCommon(String inputWord, String previousWord, State[] tempStates, int currentOutput) {

        int commonPrefixLengthPlusOne = commonPrefixIndice(previousWord, inputWord);

            /*
            we minimize the states from thee suffix of the previous word
             */

        for (int i = previousWord.length(); i >= commonPrefixLengthPlusOne; i--) {
            int output = tempStates[i - 1].linearSearchArc(previousWord.charAt(i - 1)).getOutput();
            Arc removingArc = tempStates[i - 1].linearSearchArc(previousWord.charAt(i - 1));

            State temp = findEquivalentCollisionHandled(tempStates[i]);
            setTransition(tempStates[i - 1], temp, output, previousWord.charAt(i - 1));
            tempStates[i - 1].arcs.remove(removingArc);

            compileState(tempStates[i - 1]); // For FST Compiler, be sure to have it *AFTER* the setTransitionFunction

        }
        for (int i = commonPrefixLengthPlusOne; i <= inputWord.length(); i++) {
            clearState(tempStates[i]);
            setTransition(tempStates[i - 1], tempStates[i], inputWord.charAt(i - 1));
        }
        tempStates[inputWord.length()].setFinal();


        // dealing with common prefix between previous word and the current word
        // (also note that its output must have common prefix too.)
        State currentState = tempStates[0];

        for (int i = 0; i < commonPrefixLengthPlusOne - 1; i++) {
            Arc nextArc = currentState.getNextArc(inputWord.charAt(i));
            int commonStateOutput = nextArc.output;
            currentOutput = excludePrefix(currentOutput, commonStateOutput);
            currentState = nextArc.getDestination();
        }

        int outputDiff = currentOutput;
        State suffixHeadState = tempStates[commonPrefixLengthPlusOne - 1];
        suffixHeadState.linearSearchArc(inputWord.charAt(commonPrefixLengthPlusOne - 1)).setOutput(outputDiff);
    }

    private void handleLastWord(String previousWord, String lastWord, State[] tempStates) {
        for (int i = lastWord.length(); i > 0; i--) {
            int output = tempStates[i - 1].linearSearchArc(previousWord.charAt(i - 1)).getOutput();
            Arc removingArc = tempStates[i - 1].linearSearchArc(previousWord.charAt(i - 1));
            tempStates[i - 1].arcs.remove(removingArc);
            setTransition(tempStates[i - 1], findEquivalentCollisionHandled(tempStates[i]), output, lastWord.charAt(i - 1));
            compileState(tempStates[i - 1]); // For FST Compiler

        }
        compileState(tempStates[0]); // For FST Compiler
        findEquivalentCollisionHandled(tempStates[0]);

        compileFinalWord(tempStates); // For FST compiler
    }


    /**
     * Allocate State object to each indice
     *
     * @param numStates
     * @return
     */
    private State[] initializeState(int numStates) {
        State[] retStates = new State[numStates];
        for (int i = 0; i < numStates; i++) {
            retStates[i] = new State();
        }
        return retStates;
    }

    /**
     * Returns the indice of common prefix + 1
     *
     * @param prevWord
     * @param currentWord
     * @return
     */
    private int commonPrefixIndice(String prevWord, String currentWord) {
        int i = 0;
//        System.out.println(prevWord);
        while (i < prevWord.length() && i < currentWord.length()) {
            if (prevWord.charAt(i) != currentWord.charAt(i)) {
                break;
            }
            i += 1;
        }
        return i + 1;
    }

    private int excludePrefix(int word, int prefix) {
        return word - prefix;
    }

    private void setTransition(State from, State to, int output, char transitionStr) {
        from.setArc(transitionStr, output, to);
    }

    private void setTransition(State from, State to, char transitionStr) {
        from.setArc(transitionStr, to);
    }

    /**
     * Find the equivalent state by checking its destination states to when collided.
     *
     * @param state
     * @return
     */
    private State findEquivalentCollisionHandled(State state) {
        // returns a equivalent state which is already in the stateDicitonary. nextState will be used when there is a collision
//        List<Character> transitionStrings = state.getAllTransitionStrings();
//        List<String> outputStrings = state.getAllOutputs(); // output of outgoing transition arcs

//        String key = transitionStrings.toString() + outputStrings.toString();
        Integer key = state.hashCode(); // this is going to be the hashCode.


        if (statesDictionaryHashList.containsKey(key)) {
            ArrayList<State> collidedStates = statesDictionaryHashList.get(key);

            if (state.getAllTransitionStrings().size() == 0) {
                // the dead end state (which is unique!)
                return collidedStates.get(0);
            }

            // Here, there are multiple states that has the same transition arc
            // Linear Probing the collidedStates!
            for (State collidedState : collidedStates) {
                boolean destStateDiff = false;
                List<Character> transitionStringsInCollidedState = collidedState.getAllTransitionStrings();

                for (int i = 0; i < transitionStringsInCollidedState.size(); i++) {
                    if (!state.getNextState(transitionStringsInCollidedState.get(i))
                            .equals(collidedState.getNextState(transitionStringsInCollidedState.get(i)))) {
                        // this state is not equivalent since there is a dest. state that is different.
                        destStateDiff = true;
                        break;
                    }
                }

                if (!destStateDiff) {
                    // OK, these states point to the same state. Equivalent!
                    return collidedState;
                }
            }
        }
        // At this point, we know that there is no equivalent compiled (finalized) node
        State newStateToDic = new State(state); // deep copy
        ArrayList<State> stateList = new ArrayList<State>();
        if (statesDictionaryHashList.containsKey(key)) {
            stateList = statesDictionaryHashList.get(key);
            // adding new state to a key
        }
        stateList.add(newStateToDic);
        statesDictionaryHashList.put(key, stateList);

        return newStateToDic;
    }

    /**
     * Initialize the temp states
     *
     * @param state
     */
    private void clearState(State state) {
        // clear all transitions and set it to non-final state
        state.arcs = new ArrayList<Arc>();
        state.isFinal = false;
    }

    private void compileFinalWord(State[] tempStates) {
        char transitionDummyChar = 'D';
        State dummyState = new State();
        dummyState.setArc(transitionDummyChar, 0, tempStates[0]); // trans. char.: ' ',  output: 0, dest. state: to starting state
        List<Character> transitionStrings = dummyState.getAllTransitionStrings();
        if (transitionStrings.size() != 0) {
            for (int i = 0; i < transitionStrings.size(); i++) {
                char transitionChar = transitionStrings.get(i);
                compileArc(transitionChar, dummyState);
            }
        }
        else {
            // This is the case when start state is an accepting state. It will not be used when empty string does not appear in the dictionary
//            char transitionChar = fstCompiler.KEY_FOR_DEADEND_ARC;
            dummyState.setArc(' ', 0, dummyState);
            compileArc(' ', dummyState);
        }
    }

    /**
     * Assign a target jump address to an arc that points to a given state object
     *
     * @param state
     */
    private void compileState(State state) {
        List<Character> transitionStrings = state.getAllTransitionStrings();
        if (transitionStrings.size() != 0) {
            for (int i = 0; i < transitionStrings.size(); i++) {
                char transitionChar = transitionStrings.get(i);
                compileArc(transitionChar, state);
            }
        }
        else {
            // Compile dead-end state
            fstCompiler.makeInstructionForDeadEndState();
        }
    }

    private void compileArc(char transitionChar, State state) {
        Arc b = state.getNextArc(transitionChar);
        fstCompiler.assignTargetAddressToArcB(b, transitionChar);
    }
}
