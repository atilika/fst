package com.atilika.kuromoji.fst;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class FST {
    // Note that FST only allows the presorted dictionaries as input.

    private HashMap<Integer, List<State>> statesDictionary;
    public FSTCompiler fstCompiler = new FSTCompiler();

    // TODO: Rewrite this...
    public int MAX_WORD_LENGTH = 100;

    public FST() {
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
    public void createDictionaryIncremental(BufferedReader reader) throws IOException {
        String previousWord = "";
        State[] tempStates = initializeState(MAX_WORD_LENGTH + 1);
        tempStates[0] = this.getStartState(); // initial state

        int outputValue = 1; // Initialize output value

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("#.*$", "");

            if (line.trim().isEmpty()) {
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
        String previousWord = "";
        State[] tempStates = initializeState(MAX_WORD_LENGTH + 1);
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
//        System.out.println(currentOutput);
//        We minimize the states from the suffix of the previous word

        for (int i = previousWord.length(); i >= commonPrefixLengthPlusOne; i--) {
            State state = tempStates[i - 1];
            char previousWordChar = previousWord.charAt(i - 1);
            int output = state.findArc(previousWordChar).getOutput();
            state.arcs.remove(state.findArc(previousWordChar));
            Arc arcToFrozenArc = setTransition(state, findEquivalentState(tempStates[i]), output, previousWordChar);

            compileArc(arcToFrozenArc, false); // For FST Compiler, be sure to have it *AFTER* the setTransitionFunction

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
            Arc nextArc = currentState.findArc(inputWord.charAt(i));
            // commonStateOutput = nextArc.output
            currentOutput = excludePrefix(currentOutput, nextArc.getOutput());
            currentState = currentState.findArc(inputWord.charAt(i)).getDestination();
        }

        // currentOutput is the difference of outputs
        State suffixHeadState = tempStates[commonPrefixLengthPlusOne - 1];
        suffixHeadState.findArc(inputWord.charAt(commonPrefixLengthPlusOne - 1)).setOutput(currentOutput);

    }

    private void handleLastWord(String previousWord, String lastWord, State[] tempStates) {
        for (int i = lastWord.length(); i > 0; i--) {
            State state = tempStates[i - 1];
            char previousWordChar = previousWord.charAt(i - 1);
            int output = state.findArc(previousWordChar).getOutput();

            state.arcs.remove(state.findArc(previousWordChar));
            Arc arcToFrozenArc = setTransition(
                state,
                findEquivalentState(tempStates[i]), output, lastWord.charAt(i - 1)
            );
            compileArc(arcToFrozenArc, false); // For FST Compiler

        }
        compileStartingState(tempStates[0]); // For FST Compiler, caching
        findEquivalentState(tempStates[0]); // not necessary when compiling is enabled
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

    private Arc setTransition(State from, State to, int output, char transitionStr) {
        return from.setArc(transitionStr, output, to);
    }

    private void setTransition(State from, State to, char transitionStr) {
        from.setArc(transitionStr, to);
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
                if (isStateEquivalent(state, collidedState)) {
//                    System.out.println("Collided!");
                    // OK, these states point to the same state. Equivalent!
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

    private boolean isStateEquivalent(State state, State collidedState) {

        if (state.arcs.size() != collidedState.arcs.size()) {
            return false;
        }

        boolean isArcEquiv = true;

        for (int i = 0; i < collidedState.arcs.size(); i++) {
            // we cannot guarantee that the state has a given transition char since the hashCode() may
            // collide in coincidence.
            isArcEquiv &= isArcEquvivalent(collidedState.arcs.get(i), state.arcs.get(i));
        }
        return isArcEquiv;
    }

    private boolean isArcEquvivalent(Arc collidedArc, Arc currentArc) {

        //
        if (collidedArc.getLabel() != currentArc.getLabel()) {
            return false;
        }

        //
        if (collidedArc.getOutput() != currentArc.getOutput()) {
            return false;
        }

        if (!collidedArc.getDestination().equals(currentArc.getDestination())) {
            return false;
        }

        return true;
    }

    /**
     * Initialize the temp states. It clears all transitions and set it to non-final state
     *
     * @param state
     */
    private void clearState(State state) {
        state.arcs = new ArrayList<>();
        state.isFinal = false;
    }

    private void compileStartingState(State state) {
        for (Arc arc : state.arcs) {
            compileArc(arc, true);
        }
        fstCompiler.program.instruction.flip();
    }

    private void compileArc(Arc b, boolean isStartState) {
        fstCompiler.assignTargetAddressToDestinationState(b, isStartState);
    }

    public HashMap<Integer, List<State>> getStatesDictionary() {
        return this.statesDictionary;
    }
}
