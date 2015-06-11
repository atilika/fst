package com.atilika.kuromoji.fst.vm;

import com.atilika.kuromoji.fst.FSTBuilder;
import com.atilika.kuromoji.fst.FSTTestHelper;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class ProgramTest {

    @Test
    public void testReadProgramFromFile() throws Exception {

//        String resource = "ipadic-allwords_uniq_sorted.csv";
        String resource = "ipadic-allwords_uniqHead5000.csv";

        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FSTBuilder fstBuilder = fstTestHelper.readIncremental(resource);

        Program program = fstBuilder.fstCompiler.getProgram();
        program.outputProgramToFile();
        program.readProgramFromFile();

        VirtualMachine vm = new VirtualMachine(false);

        int wordIDExpected = 1;

        // TODO: Make this a method in FSTTestHelper
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(getResource(resource), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            // Remove comments
            line = line.replaceAll("#.*$", "");

            // Skip empty lines or comment lines
            if (line.trim().length() == 0) {
                continue;
            }
            int wordID = vm.run(program, line);
            assertEquals(wordIDExpected, wordID);
            wordIDExpected++;
        }
        reader.close();
    }

    private InputStream getResource(String s) {
        return this.getClass().getClassLoader().getResourceAsStream(s);
    }
}