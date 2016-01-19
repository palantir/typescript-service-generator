/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.palantir.code.ts.generator.model.ServiceModel;

public final class ServiceGenerator {

    private final TypescriptServiceGeneratorConfiguration settings;

    public ServiceGenerator(TypescriptServiceGeneratorConfiguration settings) {
        this.settings = settings;

        // Write out httpApiBridge file
        String bridgeFile = "httpApiBridge.ts";
        IndentedOutputWriter writer;
        try {
            writer = new IndentedOutputWriter(new FileOutputStream(new File(settings.generatedFolderLocation(), bridgeFile)), settings);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        beginService(writer, null);

        writer.writeLine("");
        List<String> bridgeFileLines = null;
        try {
            bridgeFileLines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream(bridgeFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bridgeFileLines = Lists.newArrayList(String.format(Joiner.on(settings.getSettings().newline).join(bridgeFileLines), String.format(settings.genericEndpointReturnType(), "T")).split("\n"));
        for (String line : bridgeFileLines) {
            writer.writeLine(line);
        }

        endService(writer);
    }

    public void generateTypescriptService(Class<?> clazz) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(new File(settings.generatedFolderLocation(), Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1) + ".ts"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        IndentedOutputWriter writer = new IndentedOutputWriter(output, settings);
        beginService(writer, clazz.getSimpleName());

        ServiceModel serviceModel = new ServiceClassParser().parseServiceClass(clazz, settings);
        ServiceEmitter serviceEndpointEmitter = new ServiceEmitter(serviceModel, settings, writer);
        serviceEndpointEmitter.emitTypescriptTypes(settings);
        serviceEndpointEmitter.emitTypescriptInterface();
        serviceEndpointEmitter.emitTypescriptClass();

        endService(writer);
    }

    private void beginService(IndentedOutputWriter writer, String subModuleName) {
        String moduleName = settings.typescriptModule();
        if (subModuleName != null) {
            moduleName += "." + subModuleName;
        }
        writer.writeLine(settings.copyrightHeader());
        writer.writeLine(settings.generatedMessage());
        writer.writeLine("module " + moduleName + " {");
        writer.increaseIndent();
    }

    private void endService(IndentedOutputWriter writer) {
        writer.decreaseIndent();
        writer.writeLine("}");
        writer.close();
    }
}
