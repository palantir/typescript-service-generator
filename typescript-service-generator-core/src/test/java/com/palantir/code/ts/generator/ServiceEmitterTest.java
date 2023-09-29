/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.TestUtils.ConcreteObjectService;
import com.palantir.code.ts.generator.utils.TestUtils.DuplicateMethodNamesService;
import com.palantir.code.ts.generator.utils.TestUtils.EnumClass;
import com.palantir.code.ts.generator.utils.TestUtils.MyObject;
import com.palantir.code.ts.generator.utils.TestUtils.SimpleService1;
import com.palantir.code.ts.generator.utils.TestUtils.SimpleService2;
import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;
import com.palantir.code.ts.generator.utils.TestUtils.TestServiceClass;

public class ServiceEmitterTest {

    private TypescriptServiceGeneratorConfiguration settings;
    private IndentedOutputWriter writer;
    private ByteArrayOutputStream stream;
    private ServiceClassParser serviceClassParser;

    @Before
    public void before() {
        this.settings = ImmutableTypescriptServiceGeneratorConfiguration.builder()
                                                                        .copyrightHeader("")
                                                                        .emitDuplicateJavaMethodNames(false)
                                                                        .generatedFolderLocation(new File(""))
                                                                        .generatedMessage("")
                                                                        .genericEndpointReturnType("FooType<%s>")
                                                                        .typescriptModule("")
                                                                        .build();
        this.stream = new ByteArrayOutputStream();
        this.writer = new IndentedOutputWriter(stream, settings);
        this.serviceClassParser = new ServiceClassParser();
    }

    @Test
    public void testComplexServiceClassEmitTypes() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptTypes(settings, Lists.newArrayList());
        writer.close();
        String expectedOutput = "" +
"    /* tslint:disable */\n" +
"    /* eslint-disable */\n" +
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
        String res = new String(stream.toByteArray());
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
    public void testComplexServiceClassEmitClass() {
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
"            requestMediaType: \"application/json\",\n" +
"            responseMediaType: \"\",\n" +
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
"            requestMediaType: \"application/json\",\n" +
"            responseMediaType: \"\",\n" +
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
"            requestMediaType: \"application/json\",\n" +
"            responseMediaType: \"\",\n" +
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
"\n" +
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

    @Test
    public void testConcreteObjectService() {
        ServiceModel model = serviceClassParser.parseServiceClass(ConcreteObjectService.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptClass();
        writer.close();
        String expectedOutput = "\n" +
"export class ConcreteObjectServiceImpl implements ConcreteObjectService {\n" +
"\n" +
"    private httpApiBridge: HttpApiBridge;\n" +
"    constructor(httpApiBridge: HttpApiBridge) {\n" +
"        this.httpApiBridge = httpApiBridge;\n" +
"    }\n" +
"\n" +
"    public noPathGetter() {\n" +
"        var httpCallData = <HttpEndpointOptions> {\n" +
"            serviceIdentifier: \"concreteObjectService\",\n" +
"            endpointPath: \"concreteObject\",\n" +
"            endpointName: \"noPathGetter\",\n" +
"            method: \"GET\",\n" +
"            requestMediaType: \"application/json\",\n" +
"            responseMediaType: \"\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [],\n" +
"            queryArguments: {\n" +
"            },\n" +
"            data: null\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<string>(httpCallData);\n" +
"    }\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testAdditionalClassesToOutput() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestServiceClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptTypes(settings, Lists.newArrayList(MyObject.class));
        writer.close();
        String expectedOutput =
                "    /* tslint:disable */\n" +
                        "    /* eslint-disable */\n" +
"\n" +
"    export interface MyObject {\n" +
"        y: MyObject;\n" +
"    }\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testEnumClass() {
        ServiceModel model = serviceClassParser.parseServiceClass(EnumClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptTypes(settings, Lists.newArrayList());
        writer.close();
        String expectedOutput = "" +
"    /* tslint:disable */\n" +
"    /* eslint-disable */\n" +
"\n" +
"    export type MyEnum = \"VALUE1\" | \"VALUE2\";\n" +
"\n\n    // Added by 'EnumConstantsExtension' extension\n\n" +
"    export const MyEnum = {\n" +
"        VALUE1: <MyEnum>\"VALUE1\",\n" +
"        VALUE2: <MyEnum>\"VALUE2\",\n" +
"    }\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }

    @Test
    public void testEnumDataParameter() {
        ServiceModel model = serviceClassParser.parseServiceClass(EnumClass.class, settings);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptClass();
        writer.close();
        String expectedToContain = "data: `\"${myEnum}\"`";
        assertTrue(new String(stream.toByteArray()).contains(expectedToContain));
    }

    @Test
    public void testMultipleClasses() {
        ServiceModel model = serviceClassParser.parseServiceClass(SimpleService1.class, settings, SimpleService2.class);
        ServiceEmitter serviceEmitter = new ServiceEmitter(model, settings, writer);
        serviceEmitter.emitTypescriptInterface();
        writer.close();
        String expectedOutput = "\n" +
"export interface SimpleService1 {\n" +
"\n" +
"    // endpoints for service class: SimpleService1\n" +
"    method1(): FooType<string>;\n" +
"\n" +
"    // endpoints for service class: SimpleService2\n" +
"    method2(): FooType<string>;\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }
}
