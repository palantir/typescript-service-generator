/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.CheckForNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointModel;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceEndpointModel;
import com.palantir.code.ts.generator.model.ServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.TestUtils.DataObject;
import com.palantir.code.ts.generator.utils.TestUtils.GenericObject;
import com.palantir.code.ts.generator.utils.TestUtils.IgnoredParametersClass;
import com.palantir.code.ts.generator.utils.TestUtils.ImmutablesObject;
import com.palantir.code.ts.generator.utils.TestUtils.MyObject;
import com.palantir.code.ts.generator.utils.TestUtils.TestComplexServiceClass;
import com.palantir.code.ts.generator.utils.TestUtils.TestServiceClass;

import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;

public class ServiceClassParserTest {

    private ServiceClassParser serviceClassParser;
    private TypescriptServiceGeneratorConfiguration settings;

    @Before
    public void before() {
        this.serviceClassParser = new ServiceClassParser();
        this.settings = Mockito.mock(TypescriptServiceGeneratorConfiguration.class);

        Mockito.when(this.settings.getSettings()).thenReturn(new Settings());
    }

    @Test
    public void parseSimpleClassTest() {
        ServiceModel model = serviceClassParser.parseServiceClass(TestServiceClass.class, settings);
        assertEquals(ImmutableSet.of(String.class), model.referencedTypes());
        assertEquals("TestServiceClass", model.name());
        assertEquals("testService", model.servicePath());
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
        assertEquals(model.endpointModels(), Lists.newArrayList(stringGetterEndpointModel));
    }

    @Test
    public void parseComplexClassTest() throws NoSuchMethodException, SecurityException {
        ServiceModel model = serviceClassParser.parseServiceClass(TestComplexServiceClass.class, settings);
        Type genericReturnType = TestComplexServiceClass.class.getMethod("allOptionsPost", String.class, Integer.class, DataObject.class).getGenericReturnType();
        assertEquals(ImmutableSet.of(Boolean.class, ImmutablesObject.class, MyObject.class, GenericObject.class, Integer.class, String.class, DataObject.class, genericReturnType), model.referencedTypes());
        assertEquals("TestComplexServiceClass", model.name());
        assertEquals("testComplexService", model.servicePath());
        List<ServiceEndpointModel> endpoints = Lists.newArrayList();
        {
            ServiceEndpointParameterModel aParam = ImmutableServiceEndpointParameterModel.builder().pathParam("a").javaType(String.class).tsType(TsType.String).build();
            ServiceEndpointParameterModel bParam = ImmutableServiceEndpointParameterModel.builder().queryParam("b").javaType(Integer.class).tsType(TsType.Number).build();
            ServiceEndpointParameterModel dataParam = ImmutableServiceEndpointParameterModel.builder().javaType(DataObject.class).tsType(new TsType.StructuralType("DataObject")).build();
            endpoints.add(ImmutableServiceEndpointModel.builder().javaReturnType(genericReturnType)
                                                                 .tsReturnType(new TsType.StructuralType("GenericObject"))
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
        assertEquals(model.endpointModels().toString(), endpoints.toString());
    }

    @Test
    public void parseIgnoredTest() {
        Mockito.when(settings.ignoredAnnotations()).thenReturn(Sets.newHashSet(CheckForNull.class));
        ServiceModel model = serviceClassParser.parseServiceClass(IgnoredParametersClass.class, settings);
        assertEquals(ImmutableSet.of(String.class), model.referencedTypes());
        assertEquals("IgnoredParametersClass", model.name());
        assertEquals("ignoredParameters", model.servicePath());
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
        assertEquals(model.endpointModels(), Lists.newArrayList(stringGetterEndpointModel));
    }
}
