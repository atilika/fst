package com.atilika.kuromoji.fst.vm;

import com.atilika.kuromoji.fst.FSTBuilder;
import com.atilika.kuromoji.fst.FSTTestHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class ProgramTest {

    @Ignore("Enable by providing external dictionary file")
    @Test
    public void testReadProgramFromFile() throws Exception {

        String resource = "ipadic-allwords_uniq_sorted.csv";
//        String resource = "ipadic-allwords_uniqHead5000.csv";

        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FSTBuilder fstBuilder = fstTestHelper.readIncremental(resource);

        Program program = fstBuilder.fstCompiler.getProgram();
        program.outputProgramToFile("fstbytebuffer");


        VirtualMachine vm = new VirtualMachine();
        fstTestHelper.checkOutputWordByWord(resource, program, vm);

        Program readProgram = new Program();
        readProgram.readProgramFromFile("fstbytebuffer");

        fstTestHelper.checkOutputWordByWord(resource, readProgram, vm);
    }
}