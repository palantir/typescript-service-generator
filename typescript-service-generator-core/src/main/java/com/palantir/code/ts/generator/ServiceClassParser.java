/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.palantir.code.ts.generator.TypescriptServiceGeneratorConfiguration.MethodFilter;
import com.palantir.code.ts.generator.model.ImmutableInnerServiceModel;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointModel;
import com.palantir.code.ts.generator.model.ImmutableServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ImmutableServiceModel;
import com.palantir.code.ts.generator.model.ServiceEndpointModel;
import com.palantir.code.ts.generator.model.ServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.PathUtils;

import cz.habarta.typescript.generator.ModelCompiler;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeScriptGenerator;


public final class ServiceClassParser {
    @SuppressWarnings("unchecked")
    private final static List<Class<? extends Annotation>> ANNOTATION_CLASSES = Lists.newArrayList(POST.class, GET.class, DELETE.class, PUT.class, OPTIONS.class);

    public Set<Method> getAllServiceMethods(Class<?> serviceClass, MethodFilter methodFilter) {
        Set<Method> serviceMethods = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : ANNOTATION_CLASSES) {
            for (Method method : MethodUtils.getMethodsListWithAnnotation(serviceClass, annotation)) {
                if (methodFilter.shouldGenerateMethod(serviceClass, method)) {
                    serviceMethods.add(method);
                }
            }
        }
        return serviceMethods;
    }

    public ServiceModel parseServiceClass(Class<?> mainServiceClass, TypescriptServiceGeneratorConfiguration settings, Class<?>... serviceClassesToMerge) {
        List<Class<?>> serviceClazzes = Lists.newArrayList(mainServiceClass);
        serviceClazzes.addAll(Lists.newArrayList(serviceClassesToMerge));
        ImmutableServiceModel.Builder serviceModel = ImmutableServiceModel.builder();
        serviceModel.name(mainServiceClass.getSimpleName());
        for (Class<?> serviceClass : serviceClazzes) {
            ImmutableInnerServiceModel.Builder innerServiceModel = ImmutableInnerServiceModel.builder();
            Path servicePathAnnotation = serviceClass.getAnnotation(Path.class);
            innerServiceModel.servicePath(PathUtils.trimSlashes(servicePathAnnotation.value()));
            innerServiceModel.name(serviceClass.getSimpleName());

            Set<Method> serviceMethods = getAllServiceMethods(serviceClass, settings.methodFilter());
            // find and stores all types that are referenced by this service
            Set<Type> referencedTypes = Sets.newHashSet();
            for (Method method : serviceMethods) {
                referencedTypes.addAll(getTypesFromEndpoint(method, settings));
            }
            serviceModel.addAllReferencedTypes(referencedTypes);

            ModelCompiler compiler = new TypeScriptGenerator(settings.getSettings()).getModelCompiler();

            List<ServiceEndpointModel> endpointModels = Lists.newArrayList();
            endpointModels = computeEndpointModels(serviceMethods, compiler, settings);

            Collections.sort(endpointModels);
            innerServiceModel.endpointModels(endpointModels);
            serviceModel.addInnerServiceModels(innerServiceModel.build());
        }
        return serviceModel.build();
    }

    private static List<ServiceEndpointModel> computeEndpointModels(Set<Method> endpoints, ModelCompiler compiler, TypescriptServiceGeneratorConfiguration settings) {
        Multimap<String, Method> endpointNameMap = ArrayListMultimap.create();
        Map<Method, String> endpointNameGetter = Maps.newHashMap();
        for (Method endpoint : endpoints) {
            endpointNameMap.put(endpoint.getName(), endpoint);
            endpointNameGetter.put(endpoint, endpoint.getName());
        }
        for (String endpointName : endpointNameMap.keySet()) {
            List<Method> maybeDuplicates = Lists.newArrayList(endpointNameMap.get(endpointName).iterator());
            if (maybeDuplicates != null && maybeDuplicates.size() > 1) {
                endpointNameGetter.putAll(settings.duplicateEndpointNameResolver().resolveDuplicateNames(maybeDuplicates));
            }
        }
        List<ServiceEndpointModel> result = Lists.newArrayList();
        for (Method endpoint : endpoints) {
            ImmutableServiceEndpointModel.Builder ret = ImmutableServiceEndpointModel.builder();
            ret.endpointName(endpointNameGetter.get(endpoint));
            ret.javaReturnType(endpoint.getGenericReturnType());
            ret.tsReturnType(compiler.typeFromJavaWithReplacement(endpoint.getGenericReturnType()));
            ret.endpointMethodType(getMethodType(endpoint));

            String annotationValue = "";
            if (endpoint.getAnnotation(Path.class) != null) {
                annotationValue = endpoint.getAnnotation(Path.class).value();
            }
            ret.endpointPath(PathUtils.trimSlashes(annotationValue));
            Consumes consumes = endpoint.getAnnotation(Consumes.class);
            if (consumes != null) {
                if (consumes.value().length > 1) {
                    throw new IllegalArgumentException("Don't know how to handle an endpoint with multiple consume types");
                }
                if (consumes.value().length == 1) {
                    ret.endpointRequestMediaType(consumes.value()[0]);
                }
            }
            Produces produces = endpoint.getAnnotation(Produces.class);
            if (produces != null) {
                if (produces.value().length > 1) {
                    throw new IllegalArgumentException("Don't know how to handle an endpoint with multiple produce types");
                }
                if (produces.value().length == 1) {
                    ret.endpointResponseMediaType(produces.value()[0]);
                }
            }

            List<Map<Class<?>, Annotation>> annotationList = getParamterAnnotationMaps(endpoint);
            List<ServiceEndpointParameterModel> mandatoryParameters = Lists.newArrayList();
            List<ServiceEndpointParameterModel> optionalParameters = Lists.newArrayList();
            int annotationListIndex = 0;
            for (Type javaParameterType : endpoint.getGenericParameterTypes()) {
                Map<Class<?>, Annotation> annotations = annotationList.get(annotationListIndex);
                ImmutableServiceEndpointParameterModel.Builder parameterModel = ImmutableServiceEndpointParameterModel.builder();

                // if parameter is annotated with any ignored annotations, skip it entirely
                if (!Collections.disjoint(annotations.keySet(), settings.ignoredAnnotations())) {
                    annotationListIndex++;
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
            result.add(ret.build());
        }
        return result;
    }

    private static String getMethodType(Method endpoint) {
        for (Class<? extends Annotation> annotationClass : ANNOTATION_CLASSES) {
            if (endpoint.getAnnotation(annotationClass) != null) {
                return annotationClass.getSimpleName();
            }
        }
        throw new IllegalArgumentException("All endpoints should specify their method type, but one didn't: " + endpoint);
    }

    private static List<Map<Class<?>, Annotation>> getParamterAnnotationMaps(Method endpoint) {
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

    private static Set<Type> getTypesFromEndpoint(Method endpoint, TypescriptServiceGeneratorConfiguration settings) {
        Set<Type> ret = Sets.newHashSet();
        ret.add(endpoint.getReturnType());
        ret.add(endpoint.getGenericReturnType());

        List<Map<Class<?>, Annotation>> parameterAnnotationMaps = getParamterAnnotationMaps(endpoint);
        Parameter[] parameters = endpoint.getParameters();
        for (int i = 0; i < parameterAnnotationMaps.size(); i++) {
            Parameter parameter = parameters[i];
            Map<Class<?>, Annotation> parameterAnnotationMap = parameterAnnotationMaps.get(i);
            // if parameter is annotated with any ignored annotations, skip it entirely
            if (Collections.disjoint(parameterAnnotationMap.keySet(), settings.ignoredAnnotations())) {
                ret.add(parameter.getParameterizedType());
            }
        }
        return ret;
    }
}
