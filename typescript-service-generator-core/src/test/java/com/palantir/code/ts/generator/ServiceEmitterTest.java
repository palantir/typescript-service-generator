/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.TestUtils.DuplicateMethodNamesService;
import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;

public class ServiceEmitterTest {

    private TypescriptServiceGeneratorConfiguration settings;
    private IndentedOutputWriter writer;
    private ByteArrayOutputStream stream;
    private ServiceClassParser serviceClassParser;

    @Before
    public void before() {
        this.settings = ImmutableTypescriptServiceGeneratorConfiguration.builder()
                                                                        .copyrightHeader("")
                                                                        .generatedFolderLocation(new File(""))
                                                                        .generatedMessage("")
                                                                        .genericEndpointReturnType("FooType<%s>")
                                                                        .typescriptModule("")
                                                                        .build();
        this.settings.getSettings().noFileComment = true;
        this.settings.getSettings().sortDeclarations = true;
        this.stream = new ByteArrayOutputStream();
        this.writer = new IndentedOutputWriter(stream, settings);
        this.serviceClassParser = new ServiceClassParser();
    }

    @Test
    public void testComplexServiceClassEmitTypes() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptTypes(settings);
        writer.close();
        String expectedOutput = "" +
"\n" +
"    export interface DataObject {\n" +
"        y: MyObject;\n" +
"    }\n" +
"\n" +
"    export interface GenericObject<T> {\n" +
"        y: T;\n" +
"    }\n" +
"\n" +
"    export interface ImmutablesObject {\n" +
"        y: string;\n" +
"    }\n" +
"\n" +
"    export interface MyObject {\n" +
"        y: MyObject;\n" +
"    }\n" +
"";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testComplexServiceClassEmitInterface() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptInterface();
        writer.close();
        String expectedOutput = "\n" +
"export interface TestComplexServiceClass {\n" +
"    allOptionsPost(a: string, dataObject: DataObject, b?: number): FooType<GenericObject<MyObject>>;\n" +
"    queryGetter(x?: boolean): FooType<MyObject>;\n" +
"    simplePut(dataObject: DataObject): FooType<ImmutablesObject>;\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testComplexServicClassEmitClass() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptClass();
        writer.close();
        String expectedOutput = "\n" +
"export class TestComplexServiceClassImpl implements TestComplexServiceClass {\n" +
"\n" +
"    private httpApiBridge: HttpApiBridge;\n" +
"    constructor(httpApiBridge: HttpApiBridge) {\n" +
"        this.httpApiBridge = httpApiBridge;\n" +
"    }\n" +
"\n" +
"    public allOptionsPost(a: string, dataObject: DataObject, b?: number) {\n" +
"        var httpCallData = <HttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/allOptionsPost/{a}\",\n" +
"            endpointName: \"allOptionsPost\",\n" +
"            method: \"POST\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [a],\n" +
"            queryArguments: {\n" +
"                b: b,\n" +
"            },\n" +
"            data: dataObject\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<GenericObject<MyObject>>(httpCallData);\n" +
"    }\n" +
"\n" +
"    public queryGetter(x?: boolean) {\n" +
"        var httpCallData = <HttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/queryGetter\",\n" +
"            endpointName: \"queryGetter\",\n" +
"            method: \"GET\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [],\n" +
"            queryArguments: {\n" +
"                x: x,\n" +
"            },\n" +
"            data: null\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<MyObject>(httpCallData);\n" +
"    }\n" +
"\n" +
"    public simplePut(dataObject: DataObject) {\n" +
"        var httpCallData = <HttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/simplePut\",\n" +
"            endpointName: \"simplePut\",\n" +
"            method: \"PUT\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [],\n" +
"            queryArguments: {\n" +
"            },\n" +
"            data: dataObject\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<ImmutablesObject>(httpCallData);\n" +
"    }\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testDuplicateMethodInterface() {
        ServiceModel model = serviceClassParser.parseServiceClass(DuplicateMethodNamesService.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptInterface();
        writer.close();
        String expectedOutput = "\n" +
"export interface DuplicateMethodNamesService {\n" +
"    // WARNING: not creating method declaration, java service has multiple methods with the name duplicate\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testDuplicateMethodClass() {
        ServiceModel model = serviceClassParser.parseServiceClass(DuplicateMethodNamesService.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptClass();
        writer.close();
        String expectedOutput = "\n" +
"export class DuplicateMethodNamesServiceImpl implements DuplicateMethodNamesService {\n" +
"\n" +
"    private httpApiBridge: HttpApiBridge;\n" +
"    constructor(httpApiBridge: HttpApiBridge) {\n" +
"        this.httpApiBridge = httpApiBridge;\n" +
"    }\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }
}
