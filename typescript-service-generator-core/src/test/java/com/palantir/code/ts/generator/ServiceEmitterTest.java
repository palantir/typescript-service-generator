package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;

public class ServiceEmitterTest {

    private ServiceEmitter emitter;
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
"    export interface IDataObject {\n" +
"        y: IMyObject;\n" +
"    }\n" +
"\n" +
"    export interface IGenericObject<T> {\n" +
"        y: T;\n" +
"    }\n" +
"\n" +
"    export interface IMyObject {\n" +
"        y: IMyObject;\n" +
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
"export interface ITestComplexServiceClass {\n" +
"    allOptionsPost(a: string, dataObject: IDataObject, x?: number): FooType<IGenericObject<IMyObject>>;\n" +
"    queryGetter(x?: boolean): FooType<IMyObject>;\n" +
"    simplePut(dataObject: IDataObject): FooType<string>;\n" +
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
"export class TestComplexServiceClass implements ITestComplexServiceClass {\n" +
"\n" +
"    private httpApiBridge: IHttpApiBridge;\n" +
"    constructor(httpApiBridge: IHttpApiBridge) {\n" +
"        this.httpApiBridge = httpApiBridge;\n" +
"    }\n" +
"\n" +
"    public allOptionsPost(a: string, dataObject: IDataObject, x?: number) {\n" +
"        var httpCallData = <IHttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/allOptionsPost/{a}\",\n" +
"            method: \"POST\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [a],\n" +
"            queryArguments: {\n" +
"                x: x,\n" +
"            },\n" +
"            data: dataObject\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<IGenericObject<IMyObject>>(httpCallData);\n" +
"    }\n" +
"\n" +
"    public queryGetter(x?: boolean) {\n" +
"        var httpCallData = <IHttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/queryGetter\",\n" +
"            method: \"GET\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [],\n" +
"            queryArguments: {\n" +
"                x: x,\n" +
"            },\n" +
"            data: null\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<IMyObject>(httpCallData);\n" +
"    }\n" +
"\n" +
"    public simplePut(dataObject: IDataObject) {\n" +
"        var httpCallData = <IHttpEndpointOptions> {\n" +
"            serviceIdentifier: \"testComplexServiceClass\",\n" +
"            endpointPath: \"testComplexService/simplePut\",\n" +
"            method: \"PUT\",\n" +
"            mediaType: \"application/json\",\n" +
"            requiredHeaders: [],\n" +
"            pathArguments: [],\n" +
"            queryArguments: {\n" +
"            },\n" +
"            data: dataObject\n" +
"        };\n" +
"        return this.httpApiBridge.callEndpoint<string>(httpCallData);\n" +
"    }\n" +
"}\n";
        assertEquals(expectedOutput, new String(stream.toByteArray()));
    }
}
