package com.palantir.code.ts.generator.examples;

import java.io.File;

import com.palantir.code.ts.generator.ImmutableTypescriptServiceGeneratorConfiguration;
import com.palantir.code.ts.generator.ServiceGenerator;

public class MyServiceGenerator {
    public static void main(String[] args) {
        String generatedFolderPath = "exampleTypescript/generated";

        String copyrightHeader = "// A potential copyright header";
        String generatedMessage = "// A desired generated message";

        ImmutableTypescriptServiceGeneratorConfiguration.Builder builder = ImmutableTypescriptServiceGeneratorConfiguration.builder();
        builder.copyrightHeader(copyrightHeader);
        builder.typescriptModule("MyProject.Http");
        builder.generatedMessage(generatedMessage);
        builder.generatedFolderLocation(new File(generatedFolderPath));
        // This example targets angular, angular $http returns ng.IPromise<%s> so we target that return type here
        builder.genericEndpointReturnType("ng.IPromise<%s>");

        ServiceGenerator generator = new ServiceGenerator(builder.build());
        generator.generateTypescriptService(MyService.class);
    }
}
