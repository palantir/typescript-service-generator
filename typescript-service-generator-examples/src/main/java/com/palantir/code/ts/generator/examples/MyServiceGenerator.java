package com.palantir.code.ts.generator.examples;

import java.io.File;

import com.palantir.code.ts.generator.ImmutableTypescriptServiceGeneratorConfiguration;
import com.palantir.code.ts.generator.ServiceGenerator;

public class MyServiceGenerator {
    public static void main(String[] args) {
        String generatedFolderPath = "output";

        String copyrightHeader = "// A potential copyright header";
        String generatedMessage = "// A desired generated message";

        ImmutableTypescriptServiceGeneratorConfiguration.Builder builder = ImmutableTypescriptServiceGeneratorConfiguration.builder();
        builder.copyrightHeader(copyrightHeader);
        builder.typescriptModule("Foundry.Http");
        builder.generatedMessage(generatedMessage);
        builder.generatedFolderLocation(new File(generatedFolderPath));
        // HttpTypeWrapper is the type of whatever you want your endoint to return.
        // For example in the case of angular this might be ng.IHttpPromise<%s>
        builder.genericEndpointReturnType("HttpTypeWrapper<%s>");

        ServiceGenerator generator = new ServiceGenerator(builder.build());
        generator.generateTypescriptService(MyService.class);
    }
}
