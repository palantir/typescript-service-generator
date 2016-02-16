/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;

public class ServiceGeneratorEteTest {

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Test
    public void test() throws URISyntaxException, IOException {
        File actualDirectory = outputFolder.getRoot();
        ImmutableTypescriptServiceGeneratorConfiguration config = ImmutableTypescriptServiceGeneratorConfiguration.builder()
                                                                                                                  .copyrightHeader("// Copyright")
                                                                                                                  .generatedMessage("// Generated")
                                                                                                                  .generatedFolderLocation(actualDirectory)
                                                                                                                  .genericEndpointReturnType("FooReturn<%s>")
                                                                                                                  .generatedInterfacePrefix("I")
                                                                                                                  .typescriptModule("ModuleName")
                                                                                                                  .build();
        ServiceGenerator serviceGenerator = new ServiceGenerator(config);
        serviceGenerator.generateTypescriptService(TestComplexServiceClass.class);

        File expectedDirectory = new File(this.getClass().getResource("/eteTestData/complexServiceTestOutput/").toURI());
        assertDirectoriesEqual(expectedDirectory, actualDirectory);
    }

    private void assertDirectoriesEqual(File a, File b) throws IOException {
        if (a.isDirectory() != b.isDirectory()) fail();
        if (a.isDirectory()) {
            File[] aFiles = a.listFiles();
            File[] bFiles = b.listFiles();
            Arrays.sort(aFiles);
            Arrays.sort(bFiles);
            if (aFiles.length != bFiles.length) fail();
            for (int i = 0; i < aFiles.length; i++) {
                assertDirectoriesEqual(aFiles[i], bFiles[i]);
            }
        } else {
            assertEquals(FileUtils.readLines(a), FileUtils.readLines(b));
        }
    }
}
