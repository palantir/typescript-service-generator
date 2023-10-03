/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
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

        List<String> bridgeFileLines = null;
        try {
            bridgeFileLines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream(bridgeFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String generatedInterfacePrefix = settings.generatedInterfacePrefix();
        bridgeFileLines = Lists.newArrayList(String.format(Joiner.on(settings.getSettings().newline).join(bridgeFileLines), generatedInterfacePrefix, generatedInterfacePrefix, generatedInterfacePrefix, String.format(settings.genericEndpointReturnType(), "T")).split("\n"));
        for (String line : bridgeFileLines) {
            writer.writeLine(line);
        }

        endService(writer);
    }

    public void generateTypescriptService(Class<?> clazz) {
        this.generateTypescriptService(clazz, Lists.newArrayList());
    }

    public void generateTypescriptService(Class<?> clazz, List<Type> additionalClassesToOutput) {
        this.generateTypescriptService(clazz, additionalClassesToOutput, new Class<?>[0]);
    }

    public void generateTypescriptService(Class<?> serviceClass, List<Type> additionalClassesToOutput, Class<?>... serviceClassesToMerge) {
        System.out.println("Hello world, generateTypescriptService");
        OutputStream output = null;
        String firstSimpleName = serviceClass.getSimpleName();
        try {
            output = new FileOutputStream(new File(settings.generatedFolderLocation(), Character.toLowerCase(firstSimpleName.charAt(0)) + firstSimpleName.substring(1) + ".ts"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        IndentedOutputWriter writer = new IndentedOutputWriter(output, settings);
        beginService(writer, firstSimpleName);

        ServiceModel serviceModel = new ServiceClassParser().parseServiceClass(serviceClass, settings, serviceClassesToMerge);
        ServiceEmitter serviceEndpointEmitter = new ServiceEmitter(serviceModel, settings, writer);

        if (settings.emitES6()) {
            String generatedInterfacePrefix = settings.generatedInterfacePrefix();
            String endpointOptionsName = generatedInterfacePrefix + "HttpEndpointOptions";
            String apiBridgeName = generatedInterfacePrefix + "HttpApiBridge";
            writer.writeLine("import { " + endpointOptionsName + ", " + apiBridgeName + " } from \"./httpApiBridge\";");
        }

        serviceEndpointEmitter.emitTypescriptTypes(settings, additionalClassesToOutput);
        serviceEndpointEmitter.emitTypescriptInterface();
        serviceEndpointEmitter.emitTypescriptClass();

        endService(writer);
    }

    private void beginService(IndentedOutputWriter writer, String subModuleName) {
        writer.writeLine(settings.copyrightHeader());
        writer.writeLine(settings.generatedMessage());
    	if (settings.typescriptModule().isPresent()) {
            String moduleName = settings.typescriptModule().get();
            if (subModuleName != null) {
                moduleName += "." + subModuleName;
            }
            writer.writeLine("module " + moduleName + " {");
            writer.increaseIndent();
    	}
    }

    private void endService(IndentedOutputWriter writer) {
    	if (settings.typescriptModule().isPresent()) {
            writer.decreaseIndent();
            writer.writeLine("}");
    	}
        writer.close();
    }
}
