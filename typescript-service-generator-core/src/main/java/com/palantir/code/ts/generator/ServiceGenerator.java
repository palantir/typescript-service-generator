package com.palantir.code.ts.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class ServiceGenerator {

    private final File generatedFolderLocation;
    private final GenerationSettings settings;

    public ServiceGenerator(File generatedFolderLocation, GenerationSettings settings) {
        this.generatedFolderLocation = generatedFolderLocation;
        this.settings = settings;

        String bridgeFile = "httpApiBridge.ts";
        OutputWriter writer;
        try {
            writer = new OutputWriter(0, new FileOutputStream(new File(this.generatedFolderLocation, bridgeFile)), settings);
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        }
        beginService(writer, null);
        writer.writeLine("");

        List<String> bridgeFileLines = null;
        try {
            bridgeFileLines = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream(bridgeFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String line : bridgeFileLines) {
            writer.writeLine(line);
        }
        endService(writer);
    }

    public void generateTypescriptService(Class<?> clazz) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(new File(this.generatedFolderLocation, Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1) + ".ts"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        OutputWriter writer = new OutputWriter(0, output, settings);
        beginService(writer, clazz.getSimpleName());

        ServiceModel serviceModel = new ServiceClassParser().parseServiceClass(clazz, settings, writer);
        ServiceEmitter serviceEndpointEmitter = new ServiceEmitter(serviceModel, settings);
        serviceEndpointEmitter.emitTypescriptTypes(writer, settings);
        serviceEndpointEmitter.emitTypescriptInterface(writer);
        serviceEndpointEmitter.emitTypescriptClass(writer);

        endService(writer);
    }

    private void beginService(OutputWriter writer, String subModuleName) {
        String moduleName = settings.getTypescriptModule();
        if (subModuleName != null) {
            moduleName += "." + subModuleName;
        }
        writer.writeLine(settings.getCopyrightHeader());
        writer.writeLine(settings.getGeneratedMessage());
        writer.writeLine("module " + moduleName + " {");
        writer.increaseIndent();
    }

    private void endService(OutputWriter writer) {
        writer.decreaseIndent();
        writer.writeLine("}");
        writer.close();
    }
}
