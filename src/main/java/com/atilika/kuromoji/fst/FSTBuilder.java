package com.atilika.kuromoji.fst;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FSTBuilder {
    // Note that FST only allows the presorted dictionaries as input.

    private Map<Integer, List<State>> statesDictionary;

    private FSTCompiler fstCompiler = new FSTCompiler();

    private List<State> tempStates;

    public FSTBuilder() {
        tempStates = new ArrayList<>();
        List<State> stateList = new LinkedList<>();
        stateList.add(new State());
        this.statesDictionary = new HashMap<>();
        this.statesDictionary.put(0, stateList); // temporary setting the start state
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
            Arc nextArc = currentState.findArc(currentTransition);
            if (nextArc == null) {
                return -1;
            }
            currentState = nextArc.getDestination();
            output += nextArc.getOutput();
        }

        return output;
    }

    /**
     * Get starting state. Note that only start state uses 0 as the key for states dictionary.
     *
     * @return starting state
     */
    public State getStartState() {
        return this.statesDictionary.get(0).get(0);
    }

    /**
     * For this method, once it reads the string, it throws away.
     *
     * @param reader
     * @throws IOException
     */
    public void createDictionaryIncremental(Reader reader) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        String previousWord = "";
        tempStates.add(this.getStartState()); // initial state

        int outputValue = 1; // Initialize output value

        String line;
        while ((line = lineNumberReader.readLine()) != null) {
            line = line.replaceAll("#.*$", "");

            if (line.trim().isEmpty()) {
                continue;
            }
            String inputWord = line;
            createDictionaryCommon(inputWord, previousWord, tempStates, outputValue);
            previousWord = inputWord;
            outputValue++; // allocate the next wordID
        }

        handleLastWord(previousWord, tempStates);
    }


    /**
     * builds FST given input words and output values
     *
     * @param inputWords
     * @param outputValues
     */
    public void createDictionary(String[] inputWords, int[] outputValues) {
        String previousWord = "";
//        this.maxWordLength = getMaxWordLength(inputWords);
        tempStates.add(this.getStartState()); // initial state

        for (int inputWordIdx = 0; inputWordIdx < inputWords.length; inputWordIdx++) {
            String inputWord = inputWords[inputWordIdx];
            createDictionaryCommon(inputWord, previousWord, tempStates, outputValues[inputWordIdx]);
            previousWord = inputWord;
        }

        handleLastWord(previousWord, tempStates);
    }

    private void createDictionaryCommon(String inputWord, String previousWord, List<State> tempStates, int currentOutput) {
        // TODO: Remove tempstates as input of this method and instead use the field of this class

        int commonPrefixLengthPlusOne = commonPrefixIndice(previousWord, inputWord);
//        System.out.println(currentOutput);
//        We minimize the states from the suffix of the previous word

        // Dynamically adding additional temporary states if necessary
        if (inputWord.length() >= tempStates.size()) {
            for (int j = tempStates.size(); j <= inputWord.length(); j++) {
                tempStates.add(new State());
            }
        }

        for (int i = previousWord.length(); i >= commonPrefixLengthPlusOne; i--) {
            freezeAndPointToNewState(previousWord, tempStates, i);
        }

        for (int i = commonPrefixLengthPlusOne; i <= inputWord.length(); i++) {
            clearState(tempStates.get(i));
            tempStates.get(i - 1).setArc(inputWord.charAt(i - 1), tempStates.get(i));
        }
        tempStates.get(inputWord.length()).setFinal();

        // dealing with common prefix between previous word and the current word
        // (also note that its output must have common prefix too.)
        State currentState = tempStates.get(0);

        for (int i = 0; i < commonPrefixLengthPlusOne - 1; i++) {
            Arc nextArc = currentState.findArc(inputWord.charAt(i));
            currentOutput = excludePrefix(currentOutput, nextArc.getOutput());
            currentState = nextArc.getDestination();
        }

        // currentOutput is the difference of outputs
        State suffixHeadState = tempStates.get(commonPrefixLengthPlusOne - 1);
        suffixHeadState.findArc(inputWord.charAt(commonPrefixLengthPlusOne - 1)).setOutput(currentOutput);

    }

    /**
     * Freeze a new state if there is no equivalent state in the states dictionary.
     *  @param previousWord
     * @param tempStates
     * @param i
     */
    private void freezeAndPointToNewState(String previousWord, List<State> tempStates, int i) {
        State state = tempStates.get(i - 1);
        char previousWordChar = previousWord.charAt(i - 1);
        int output = state.findArc(previousWordChar).getOutput();
        state.arcs.remove(state.findArc(previousWordChar));
        Arc arcToFrozenState = state.setArc(previousWordChar, output, findEquivalentState(tempStates.get(i)));
        fstCompiler.compileState(arcToFrozenState.getDestination()); // For FST Compiler, be sure to have it *AFTER* the setTransitionFunction
    }

    /**
     * Freezing temp states which represent the last word of the input words
     *  @param previousWord
     * @param tempStates
     */
    private void handleLastWord(String previousWord, List<State> tempStates) {
        for (int i = previousWord.length(); i > 0; i--) {
            freezeAndPointToNewState(previousWord, tempStates, i);
        }
        fstCompiler.compileStartingState(tempStates.get(0)); // For FST Compiler, caching
        fstCompiler.program.instruction.flip(); // storing limit as the limit of the bytebuffer
        fstCompiler.program.storeCache(); // Should come after the filp. Else the limit will be the end of first arcs.
        findEquivalentState(tempStates.get(0)); // not necessary when compiling is enabled
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

        while (i < prevWord.length() && i < currentWord.length()) {
            if (prevWord.charAt(i) != currentWord.charAt(i)) {
                break;
            }
            i += 1;
        }
        return i + 1;
    }

    /**
     * Exclude output of the common prefix from the current output
     *
     * @param word
     * @param prefix
     * @return
     */
    private int excludePrefix(int word, int prefix) {
        return word - prefix;
    }

    /**
     * Find the equivalent state by checking its destination states to when collided.
     *
     * @param state
     * @return returns an equivalent state which is already in the stateDicitonary. If there is no equivalent state,
     * then a new state will created and put into statesDictionary.
     */
    private State findEquivalentState(State state) {
        Integer key = state.hashCode(); // this is going to be the hashCode.

        if (statesDictionary.containsKey(key)) {

            if (state.arcs.size() == 0) {
                // the dead end state (which is unique!)
                return statesDictionary.get(key).get(0);
            }

            // Here, there are multiple states that has the same hashcode. Linear Probing the collidedStates.
            for (State collidedState : statesDictionary.get(key)) {
                if (state.equals(collidedState)) {
                    return collidedState;
                }
            }
        }
        // At this point, we know that there is no equivalent compiled (finalized) node
        State newStateToDic = new State(state); // deep copy
        List<State> stateList = new LinkedList<>();
        if (statesDictionary.containsKey(key)) {
            stateList = statesDictionary.get(key);
            // adding new state to a key
        }
        stateList.add(newStateToDic);
        statesDictionary.put(key, stateList);

        return newStateToDic;
    }

    /**
     * Initialize the temp states. It clears all transitions and set it to non-final state
     *
     * @param state
     */
    private void clearState(State state) {
        state.arcs = new ArrayList<>();
        state.initFinal();
    }

    public FSTCompiler getFstCompiler() {
        return fstCompiler;
    }
}
