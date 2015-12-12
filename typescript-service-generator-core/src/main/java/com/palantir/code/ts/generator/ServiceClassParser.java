package com.palantir.code.ts.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cz.habarta.typescript.generator.ModelCompiler;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeScriptGenerator;


public class ServiceClassParser {

    public ServiceModel parseServiceClass(Class<?> serviceClass, GenerationSettings settings, OutputWriter writer) {
        ImmutableServiceModel.Builder serviceModel = ImmutableServiceModel.builder();
        Path pathAnnotation = serviceClass.getAnnotation(Path.class);
        serviceModel.servicePath(PathUtils.trimSlashes(pathAnnotation.value()));
        serviceModel.name(serviceClass.getSimpleName());
        serviceModel.pkg(serviceClass.getPackage().getName());

        Set<Type> referencedTypes = Sets.newHashSet();
        for (Method method : serviceClass.getMethods()) {
            referencedTypes.addAll(getTypesFromEndpoint(method));
        }
        serviceModel.directlyReferencedTypes(referencedTypes);

        TypeScriptGenerator typescriptGenerator = new TypeScriptGenerator(GenerationSettings.Utils.getSettings(settings.getCustomTypeProcessor()));
        ModelCompiler compiler = typescriptGenerator.getModelCompiler();

        Collection<ServiceEndpointModel> endpointModels = Lists.newArrayList();
        for (Method method : serviceClass.getMethods()) {
            endpointModels.add(computeEndpointModel(method, compiler, settings));
        }
        serviceModel.endpointModels(endpointModels);
        return serviceModel.build();
    }

    private ServiceEndpointModel computeEndpointModel(Method endpoint, ModelCompiler compiler, GenerationSettings settings) {
        ImmutableServiceEndpointModel.Builder ret = ImmutableServiceEndpointModel.builder();
        ret.endpointName(endpoint.getName());
        ret.javaReturnType(endpoint.getGenericReturnType());
        ret.tsReturnType(compiler.typeFromJavaWithReplacement(endpoint.getGenericReturnType()));
        ret.endpointMethodType(getMethodType(endpoint));
        ret.endpointPath(PathUtils.trimSlashes(endpoint.getAnnotation(Path.class).value()));
        Consumes consumes = endpoint.getAnnotation(Consumes.class);
        if (consumes != null && consumes.value() != null && consumes.value().length > 0) {
            if (consumes.value().length > 1) {
                throw new IllegalArgumentException("Don't know how to handle an endpoint with multiple consume types");
            }
            ret.endpointMediaType(consumes.value()[0]);
        }

        List<Map<Class<?>, Annotation>> annotationList = getParamterAnnotationMap(endpoint);
        int annotationListIndex = 0;
        List<ServiceEndpointParameterModel> mandatoryParameters = Lists.newArrayList();
        List<ServiceEndpointParameterModel> optionalParameters = Lists.newArrayList();
        for (Type javaParameterType : endpoint.getGenericParameterTypes()) {
            Map<Class<?>, Annotation> annotations = annotationList.get(annotationListIndex);
            ImmutableServiceEndpointParameterModel.Builder parameterModel = ImmutableServiceEndpointParameterModel.builder();

            if (!Collections.disjoint(annotations.keySet(), settings.getIgnoredAnnotations())) {
                continue;
            }
            PathParam path = (PathParam) annotations.get(PathParam.class);
            if (path != null) {
                parameterModel.pathParam(path.value());
            }
            HeaderParam header = (HeaderParam) annotations.get(HeaderParam.class);
            if (header != null) {
                parameterModel.headerParam(header.value());
            }
            QueryParam query = (QueryParam) annotations.get(QueryParam.class);
            if (query != null) {
                parameterModel.queryParam(query.value());
            }
            parameterModel.javaType(javaParameterType);
            TsType tsType = compiler.typeFromJavaWithReplacement(javaParameterType);
            parameterModel.tsType(tsType);
            if (tsType instanceof TsType.OptionalType || query != null) {
                optionalParameters.add(parameterModel.build());
            } else {
                mandatoryParameters.add(parameterModel.build());
            }
            annotationListIndex++;
        }
        ret.parameters(ImmutableList.<ServiceEndpointParameterModel> builder().addAll(mandatoryParameters).addAll(optionalParameters).build());
        return ret.build();
    }

    private static List<Map<Class<?>, Annotation>> getParamterAnnotationMap(Method endpoint) {
        List<Map<Class<?>, Annotation>> ret = Lists.newArrayList();
        Annotation[][] array = endpoint.getParameterAnnotations();
        for (int i = 0; i < array.length; i++) {
            Annotation[] annotations = array[i];
            Map<Class<?>, Annotation> map = Maps.newHashMap();
            for (Annotation a : annotations) {
                map.put(a.annotationType(), a);
            }
            ret.add(map);
        }
        return ret;
    }

    private static String getMethodType(Method endpoint) {
        @SuppressWarnings("unchecked")
        List<Class<? extends Annotation>> annotationClasses = Lists.newArrayList(POST.class, GET.class, DELETE.class, PUT.class, OPTIONS.class);
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (endpoint.getAnnotation(annotationClass) != null) {
                return annotationClass.getSimpleName();
            }
        }
        throw new IllegalArgumentException("All endpoints should specify their method type, but one didn't: " + endpoint);
    }

    private Set<Type> getTypesFromEndpoint(Method endpoint) {
        Set<Type> ret = Sets.newHashSet();
        Class<?> returnType = endpoint.getReturnType();
        ret.add(returnType);
        ret.add(endpoint.getGenericReturnType());
        ret.addAll(Lists.newArrayList(endpoint.getParameterTypes()));
        return ret;
    }
}
