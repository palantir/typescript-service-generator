/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.palantir.code.ts.generator.TypescriptServiceGeneratorConfiguration.DuplicateMethodNameResolver;
import com.palantir.code.ts.generator.TypescriptServiceGeneratorConfiguration.MethodFilter;
import com.palantir.code.ts.generator.model.ImmutableInnerServiceModel;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointModel;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ImmutableServiceModel;
import com.palantir.code.ts.generator.model.InnerServiceModel;
import com.palantir.code.ts.generator.model.ServiceEndpointModel;
import com.palantir.code.ts.generator.model.ServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.TestUtils.DataObject;
import com.palantir.code.ts.generator.utils.TestUtils.DuplicateMethodNamesService;
import com.palantir.code.ts.generator.utils.TestUtils.GenericObject;
import com.palantir.code.ts.generator.utils.TestUtils.IgnoredParametersClass;
import com.palantir.code.ts.generator.utils.TestUtils.ImmutablesObject;
import com.palantir.code.ts.generator.utils.TestUtils.MyObject;
import com.palantir.code.ts.generator.utils.TestUtils.SimpleService1;
import com.palantir.code.ts.generator.utils.TestUtils.SimpleService2;
import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;
import com.palantir.code.ts.generator.utils.TestUtils.TestServiceClass;

import cz.habarta.typescript.generator.JsonLibrary;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TsType.StructuralType;
import cz.habarta.typescript.generator.TypeScriptOutputKind;

public class ServiceClassParserTest {

    private ServiceClassParser serviceClassParser;
    private TypescriptServiceGeneratorConfiguration settings;

    @Before
    public void before() {
        this.serviceClassParser = new ServiceClassParser();
        this.settings = Mockito.mock(TypescriptServiceGeneratorConfiguration.class);

        Settings settings = new Settings();
        settings.outputKind = TypeScriptOutputKind.global;
        settings.jsonLibrary = JsonLibrary.jackson2;
        Mockito.when(this.settings.getSettings()).thenReturn(settings);
        Mockito.when(this.settings.methodFilter()).thenReturn(new MethodFilter() {
            @Override
            public boolean shouldGenerateMethod(Class<?> parentClass, Method method) {
                return true;
            }
        });
    }

    @Test
    public void parseSimpleClassTest() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestServiceClass.class, settings);
        assertEquals(1, model.innerServiceModels().size());
        assertEquals(ImmutableSet.of(String.class), model.referencedTypes());
        assertEquals("TestServiceClass", model.name());
        assertEquals("testService", model.innerServiceModels().get(0).servicePath());
        ServiceEndpointParameterModel aParam = ImmutableServiceEndpointParameterModel.builder().pathParam("a").javaType(String.class).tsType(TsType.String).build();
        ServiceEndpointParameterModel bParam = ImmutableServiceEndpointParameterModel.builder().pathParam("b").javaType(String.class).tsType(TsType.String).build();
        ImmutableServiceEndpointModel stringGetterEndpointModel = ImmutableServiceEndpointModel.builder()
                                                                                               .javaReturnType(String.class)
                                                                                               .tsReturnType(TsType.String)
                                                                                               .parameters(Lists.newArrayList(aParam, bParam))
                                                                                               .endpointName("stringGetter")
                                                                                               .endpointPath("stringGetter/{a}/{b}")
                                                                                               .endpointMethodType("GET")
                                                                                               .build();
        assertEquals(model.innerServiceModels().get(0).endpointModels(), Lists.newArrayList(stringGetterEndpointModel));
    }

    @Test
    public void parseComplexClassTest() throws NoSuchMethodException, SecurityException {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        assertEquals(1, model.innerServiceModels().size());
        Type genericReturnType = TestComplexServiceClass.class.getMethod("allOptionsPost", String.class, Integer.class, DataObject.class).getGenericReturnType();
        assertEquals(ImmutableSet.of(Boolean.class, ImmutablesObject.class, MyObject.class, GenericObject.class, Integer.class, String.class, DataObject.class, genericReturnType), model.referencedTypes());
        assertEquals("TestComplexServiceClass", model.name());
        assertEquals("testComplexService", model.innerServiceModels().get(0).servicePath());
        List<ServiceEndpointModel> endpoints = Lists.newArrayList();
        {
            ServiceEndpointParameterModel aParam = ImmutableServiceEndpointParameterModel.builder().pathParam("a").javaType(String.class).tsType(TsType.String).build();
            ServiceEndpointParameterModel bParam = ImmutableServiceEndpointParameterModel.builder().queryParam("b").javaType(Integer.class).tsType(TsType.Number).build();
            ServiceEndpointParameterModel dataParam = ImmutableServiceEndpointParameterModel.builder().javaType(DataObject.class).tsType(new TsType.StructuralType("DataObject")).build();
            endpoints.add(ImmutableServiceEndpointModel.builder().javaReturnType(genericReturnType)
                                                                 .tsReturnType(new StructuralType("GenericObject<MyObject>"))
                                                                 .parameters(Lists.newArrayList(aParam, dataParam, bParam))
                                                                 .endpointName("allOptionsPost")
                                                                 .endpointPath("allOptionsPost/{a}")
                                                                 .endpointMethodType("POST")
                                                                 .build());
        }
        {
            ServiceEndpointParameterModel xParam = ImmutableServiceEndpointParameterModel.builder().queryParam("x").javaType(Boolean.class).tsType(TsType.Boolean).build();
            endpoints.add(ImmutableServiceEndpointModel.builder().javaReturnType(MyObject.class)
                                                                 .tsReturnType(new TsType.StructuralType("MyObject"))
                                                                 .parameters(Lists.newArrayList(xParam))
                                                                 .endpointName("queryGetter")
                                                                 .endpointPath("queryGetter")
                                                                 .endpointMethodType("GET")
                                                                 .build());
        }
        {
            ServiceEndpointParameterModel dataParam = ImmutableServiceEndpointParameterModel.builder().javaType(DataObject.class).tsType(new TsType.StructuralType("DataObject")).build();
            endpoints.add(ImmutableServiceEndpointModel.builder().javaReturnType(ImmutablesObject.class)
                                                                 .tsReturnType(new TsType.StructuralType("ImmutablesObject"))
                                                                 .parameters(Lists.newArrayList(dataParam))
                                                                 .endpointName("simplePut")
                                                                 .endpointPath("simplePut")
                                                                 .endpointMethodType("PUT")
                                                                 .build());
        }
        // To string because TsType has no equals method
        assertEquals(endpoints.toString(), model.innerServiceModels().get(0).endpointModels().toString());
    }

    @Test
    public void parseIgnoredTest() {
        Mockito.when(settings.ignoredAnnotations()).thenReturn(Sets.newHashSet(CheckForNull.class));
        ServiceModel model = serviceClassParser.parseServiceClass(IgnoredParametersClass.class, settings);
        assertEquals(1, model.innerServiceModels().size());
        assertEquals(ImmutableSet.of(String.class), model.referencedTypes());
        assertEquals("IgnoredParametersClass", model.name());
        assertEquals("ignoredParameters", model.innerServiceModels().get(0).servicePath());
        ServiceEndpointParameterModel aParam = ImmutableServiceEndpointParameterModel.builder().pathParam("a").javaType(String.class).tsType(TsType.String).build();
        ServiceEndpointParameterModel bParam = ImmutableServiceEndpointParameterModel.builder().pathParam("b").javaType(String.class).tsType(TsType.String).build();
        ImmutableServiceEndpointModel stringGetterEndpointModel = ImmutableServiceEndpointModel.builder()
                                                                                               .javaReturnType(String.class)
                                                                                               .tsReturnType(TsType.String)
                                                                                               .parameters(Lists.newArrayList(aParam, bParam))
                                                                                               .endpointName("stringGetter")
                                                                                               .endpointPath("stringGetter/{a}/{b}")
                                                                                               .endpointMethodType("GET")
                                                                                               .build();
        assertEquals(model.innerServiceModels().get(0).endpointModels(), Lists.newArrayList(stringGetterEndpointModel));
    }

    @Test
    public void duplicateNameResolutionTest() {
        // Mock out a simple resolver that does the resolution based on number of parameters
        Mockito.when(settings.duplicateEndpointNameResolver()).thenReturn(new DuplicateMethodNameResolver() {
            @Override
            public Map<Method, String> resolveDuplicateNames(List<Method> methodsWithSameName) {
                Map<Method, String> result = Maps.newHashMap();
                for (Method method : methodsWithSameName) {
                    if (method.getParameterTypes().length > 0) {
                        result.put(method, "nonZeroParameters");
                    } else {
                        result.put(method, "zeroParameters");
                    }
                }
                return result;
            }
        });
        ServiceModel model = serviceClassParser.parseServiceClass(DuplicateMethodNamesService.class, settings);
        assertEquals(1, model.innerServiceModels().size());
        List<ServiceEndpointModel> endpointModels = Lists.newArrayList(model.innerServiceModels().get(0).endpointModels().iterator());
        Collections.sort(endpointModels);
        assertEquals(Lists.newArrayList("nonZeroParameters", "zeroParameters"),
                     endpointModels.stream().map(endpoint -> endpoint.endpointName()).collect(Collectors.toList()));
        assertTrue(endpointModels.get(0).parameters().size() > 0);
        assertTrue(endpointModels.get(1).parameters().size() == 0);
    }

    @Test
    public void filterMethodTest() {
        Mockito.when(settings.methodFilter()).thenReturn(new MethodFilter() {
            @Override
            public boolean shouldGenerateMethod(Class<?> parentClass, Method method) {
                return false;
            }
        });
        ServiceModel model = serviceClassParser.parseServiceClass(SimpleService1.class, settings);
        assertEquals("SimpleService1", model.name());
        InnerServiceModel innerService1 = ImmutableInnerServiceModel.builder()
                                                                    .servicePath("simple1")
                                                                    .name("SimpleService1")
                                                                    .build();

        ServiceModel expectedServiceModel = ImmutableServiceModel.builder()
                                                                 .addInnerServiceModels(innerService1)
                                                                 .name("SimpleService1")
                                                                 .build();

        assertEquals(expectedServiceModel, model);
    }

    @Test
    public void multipleServiceClassParseTest() {
        ServiceModel model = serviceClassParser.parseServiceClass(SimpleService1.class, settings, SimpleService2.class);
        assertEquals(2, model.innerServiceModels().size());
        assertEquals(ImmutableSet.of(String.class), model.referencedTypes());
        assertEquals("SimpleService1", model.name());
        ImmutableServiceEndpointModel method1Model = ImmutableServiceEndpointModel.builder()
                                                                                  .javaReturnType(String.class)
                                                                                  .tsReturnType(TsType.String)
                                                                                  .parameters(Lists.newArrayList())
                                                                                  .endpointName("method1")
                                                                                  .endpointPath("method1")
                                                                                  .endpointMethodType("GET")
                                                                                  .build();

        ImmutableServiceEndpointModel method2Model = ImmutableServiceEndpointModel.builder()
                                                                                  .javaReturnType(String.class)
                                                                                  .tsReturnType(TsType.String)
                                                                                  .parameters(Lists.newArrayList())
                                                                                  .endpointName("method2")
                                                                                  .endpointPath("method2")
                                                                                  .endpointMethodType("GET")
                                                                                  .build();
        InnerServiceModel innerService1 = ImmutableInnerServiceModel.builder()
                                                                    .addEndpointModels(method1Model)
                                                                    .servicePath("simple1")
                                                                    .name("SimpleService1")
                                                                    .build();

        InnerServiceModel innerService2 = ImmutableInnerServiceModel.builder()
                                                                    .addEndpointModels(method2Model)
                                                                    .servicePath("simple2")
                                                                    .name("SimpleService2")
                                                                    .build();

        ServiceModel expectedServiceModel = ImmutableServiceModel.builder()
                                                         .addInnerServiceModels(innerService1, innerService2)
                                                         .name("SimpleService1")
                                                         .addReferencedTypes(String.class)
                                                         .build();

        assertEquals(expectedServiceModel, model);
    }

}
