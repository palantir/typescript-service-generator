/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.type.JavaType;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.code.ts.generator.model.InnerServiceModel;
import com.palantir.code.ts.generator.model.ServiceEndpointModel;
import com.palantir.code.ts.generator.model.ServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceModel;
import com.palantir.code.ts.generator.utils.PathUtils;

import cz.habarta.typescript.generator.Input;
import cz.habarta.typescript.generator.Output;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TypeProcessor;
import cz.habarta.typescript.generator.TypeProcessor.Context;
import cz.habarta.typescript.generator.TypeProcessor.Result;
import cz.habarta.typescript.generator.TypeScriptGenerator;

public final class ServiceEmitter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ServiceModel model;
    private final TypescriptServiceGeneratorConfiguration settings;
    private final IndentedOutputWriter writer;

    public ServiceEmitter(ServiceModel model, TypescriptServiceGeneratorConfiguration settings, IndentedOutputWriter writer) {
        this.model = model;
        this.settings = settings;
        this.writer = writer;
    }

    public void emitTypescriptTypes(TypescriptServiceGeneratorConfiguration settings, List<Type> additionalTypesToOutput) {
        Settings settingsToUse = settings.getSettings();
        TypeProcessor baseTypeProcessor = settingsToUse.customTypeProcessor;

        Set<Type> referencedTypes = Sets.newHashSet(model.referencedTypes().iterator());
        referencedTypes.addAll(additionalTypesToOutput);
        Set<Class<?>> referencedClasses = getReferencedClasses(referencedTypes, settings);
        final Set<Type> discoveredTypes = Sets.newHashSet(referencedClasses.iterator());
        referencedClasses = filterInputClasses(referencedClasses);
        TypeProcessor discoveringProcessor = new TypeProcessor() {
            @Override
            public Result processType(Type javaType, Context context) {
                discoveredTypes.add(javaType);
                return null;
            }
        };

        settingsToUse.customTypeProcessor = discoveringProcessor;
        if (baseTypeProcessor != null) {
            settingsToUse.customTypeProcessor = new TypeProcessor.Chain(discoveringProcessor, baseTypeProcessor);
        }

        TypeScriptGenerator typescriptGenerator = new TypeScriptGenerator(settingsToUse);
        ByteArrayOutputStream typeDeclarations = new ByteArrayOutputStream();
        Type[] types = new Type[referencedClasses.size()];
        referencedClasses.toArray(types);
        int intendationLevel = 1;
        if (!settings.typescriptModule().isPresent()) {
        	intendationLevel = 0;
        }
        typescriptGenerator.generateEmbeddableTypeScript(Input.from(types), Output.to(typeDeclarations), true, intendationLevel);
        writer.write(new String(typeDeclarations.toByteArray()));
    }

    public void emitTypescriptClass() {
        Set<String> endpointsToWarnAboutDuplicateNames = Sets.newHashSet();
        if (!this.settings.emitDuplicateJavaMethodNames()) {
            endpointsToWarnAboutDuplicateNames = getDuplicateEndpointNames();
        }
        writer.writeLine("");
        // Adding "Impl" ensures the class name is different from the impl name, which is a compilation requirement.
        writer.writeLine("export class " + model.name() + "Impl" + " implements " + settings.getSettings().addTypeNamePrefix + model.name() + " {");
        writer.increaseIndent();

        writer.writeLine("");
        writer.writeLine(String.format("private httpApiBridge: %sHttpApiBridge;", settings.generatedInterfacePrefix()));
        writer.writeLine(String.format("constructor(httpApiBridge: %sHttpApiBridge) {", settings.generatedInterfacePrefix()));
        writer.increaseIndent();
        writer.writeLine("this.httpApiBridge = httpApiBridge;");
        writer.decreaseIndent();
        writer.writeLine("}");

        for (InnerServiceModel innerServiceModel : model.innerServiceModels()) {
            if (model.innerServiceModels().size() > 1) {
                writer.writeLine("");
                writer.writeLine("// endpoints for service class: " + innerServiceModel.name());
            }
            for (ServiceEndpointModel endpointModel: innerServiceModel.endpointModels()) {
                if (endpointsToWarnAboutDuplicateNames.contains(endpointModel.endpointName())) {
                    // don't output any duplicates
                    continue;
                }
                writer.writeLine("");
                String line = "public " + endpointModel.endpointName() + "(";
                line += getEndpointParametersString(endpointModel);
                line += ") {";
                writer.writeLine(line);
                writer.increaseIndent();
                writer.writeLine(String.format("var httpCallData = <%sHttpEndpointOptions> {", settings.generatedInterfacePrefix()));
                writer.increaseIndent();
                writer.writeLine("serviceIdentifier: \"" + Character.toLowerCase(model.name().charAt(0)) + model.name().substring(1) + "\",");
                writer.writeLine("endpointPath: \"" + getEndpointPathString(innerServiceModel, endpointModel) + "\",");
                writer.writeLine("endpointName: \"" + endpointModel.endpointName() + "\",");
                writer.writeLine("method: \"" + endpointModel.endpointMethodType() + "\",");
                writer.writeLine("requestMediaType: \"" + endpointModel.endpointRequestMediaType() + "\",");
                writer.writeLine("responseMediaType: \"" + optionalToString(endpointModel.endpointResponseMediaType()) + "\",");
                List<String> requiredHeaders = Lists.newArrayList();
                List<String> pathArguments = Lists.newArrayList();
                List<String> queryArguments = Lists.newArrayList();
                String dataArgument = null;
                for (ServiceEndpointParameterModel parameterModel : endpointModel.parameters()) {
                    if (parameterModel.headerParam() != null) {
                        requiredHeaders.add("\"" + parameterModel.headerParam() + "\"");
                    } else if (parameterModel.pathParam() != null) {
                        pathArguments.add(parameterModel.getParameterName());
                    } else if (parameterModel.queryParam() != null) {
                        queryArguments.add(parameterModel.queryParam());
                    } else {
                        if (dataArgument != null) {
                            throw new IllegalStateException("There should only be one data argument per endpoint. Found both" + dataArgument + " and " + parameterModel.getParameterName());
                        }
                        dataArgument = parameterModel.getParameterName();
                        boolean isEnum = false;
                        if (parameterModel.javaType() instanceof Class<?>) {
                            isEnum = ((Class<?>) parameterModel.javaType()).isEnum();
                        }
                        if (endpointModel.endpointRequestMediaType().equals(MediaType.APPLICATION_JSON) && (parameterModel.tsType().toString().equals("string") || isEnum)) {
                            // strings (and enums, the wire format of an enum is a string) have to be wrapped in quotes in order to be valid json
                            dataArgument = "`\"${" + parameterModel.getParameterName() + "}\"`";
                        }
                    }
                }
                writer.writeLine("requiredHeaders: [" + Joiner.on(", ").join(requiredHeaders) + "],");
                writer.writeLine("pathArguments: [" + Joiner.on(", ").join(pathArguments) + "],");
                writer.writeLine("queryArguments: {");
                writer.increaseIndent();
                for (String queryArgument: queryArguments) {
                    writer.writeLine(queryArgument + ": " + queryArgument + ",");
                }
                writer.decreaseIndent();
                writer.writeLine("},");
                writer.writeLine("data: " + dataArgument);
                writer.decreaseIndent();
                writer.writeLine("};");
                writer.writeLine("return this.httpApiBridge.callEndpoint<" + endpointModel.tsReturnType().toString() + ">(httpCallData);");
                writer.decreaseIndent();
                writer.writeLine("}");
            }
        }
        writer.decreaseIndent();
        writer.writeLine("}");
    }

    public void emitTypescriptInterface() {
        Set<String> endpointsToWarnAboutDuplicateNames = Sets.newHashSet();
        if (!this.settings.emitDuplicateJavaMethodNames()) {
            endpointsToWarnAboutDuplicateNames = getDuplicateEndpointNames();
        }

        writer.writeLine("");
        writer.writeLine("export interface " + settings.getSettings().addTypeNamePrefix + model.name() + " {");
        writer.increaseIndent();

        for (InnerServiceModel innerServiceModel : model.innerServiceModels()) {
            if (model.innerServiceModels().size() > 1) {
                writer.writeLine("");
                writer.writeLine("// endpoints for service class: " + innerServiceModel.name());
            }

            for (ServiceEndpointModel endpointModel: innerServiceModel.endpointModels()) {
                if (!endpointsToWarnAboutDuplicateNames.contains(endpointModel.endpointName())) {
                    String line = endpointModel.endpointName() + "(";
                    line += getEndpointParametersString(endpointModel);
                    line += String.format("): " + settings.genericEndpointReturnType(), endpointModel.tsReturnType().toString()) + ";";
                    writer.writeLine(line);
                }
            }
        }
        if (!endpointsToWarnAboutDuplicateNames.isEmpty()) {
            writer.writeLine("");
        }
        for (String endpointName : endpointsToWarnAboutDuplicateNames) {
            writer.writeLine(String.format("// WARNING: not creating method declaration, java service has multiple methods with the name %s", endpointName));
        }

        writer.decreaseIndent();
        writer.writeLine("}");
    }

    private Set<String> getDuplicateEndpointNames() {
        Set<String> seenEndpointNames = Sets.newHashSet();
        Set<String> duplicateEndpointNames = Sets.newHashSet();
        model.innerServiceModels().stream().flatMap(innerServiceModel -> innerServiceModel.endpointModels().stream()).forEach(model -> {
            String endpointName = model.endpointName();
            if (seenEndpointNames.contains(endpointName)) {
                duplicateEndpointNames.add(endpointName);
            }
            seenEndpointNames.add(endpointName);
        });
        return duplicateEndpointNames;
    }

    private String getEndpointPathString(InnerServiceModel model, ServiceEndpointModel endpointModel) {
        String endpointPath = model.servicePath() + "/" + endpointModel.endpointPath();
        return PathUtils.trimSlashes(endpointPath);
    }

    private String getEndpointParametersString(ServiceEndpointModel endpointModel) {
        List<String> parameterStrings = Lists.newArrayList();
        for (ServiceEndpointParameterModel parameterModel : endpointModel.parameters()) {
            if (parameterModel.headerParam() != null) {
                //continue, header params are implicit
                continue;
            }
            String optionalString = parameterModel.queryParam() != null ? "?" : "";
            parameterStrings.add(parameterModel.getParameterName() + optionalString + ": " + parameterModel.tsType().toString());
        }

        return Joiner.on(", ").join(parameterStrings);
    }

    private Set<Class<?>> filterInputClasses(Set<Class<?>> referencedClasses) {
        Set<Class<?>> typesToUse = Sets.newHashSet();
        for (Class<?> beanClass : referencedClasses) {
            if (beanClass.isEnum()) {
                typesToUse.add(beanClass);
                continue;
            }
            if (beanClass.equals(void.class)) {
                continue;
            }
            if (beanClass instanceof Class && beanClass.isEnum()) {
                typesToUse.add(beanClass);
                continue;
            }
            if (beanClass == URI.class) {
                continue;
            }

            // Classes directly passed in to typescript-generator need to be directly serializable, so filter out the ones that serializers
            // exist for.
            SerializationConfig serializationConfig = OBJECT_MAPPER.getSerializationConfig();
            final JavaType simpleType = OBJECT_MAPPER.constructType(beanClass);
            try {
                final JsonSerializer<?> jsonSerializer = BeanSerializerFactory.instance.createSerializer(serializationConfig, simpleType, null);
                if (jsonSerializer == null || jsonSerializer instanceof BeanSerializer) {
                    typesToUse.add(beanClass);
                }
            } catch(Exception e) {

            }
        }
        return typesToUse;
    }

    public static Set<Class<?>> getReferencedClasses(Set<Type> referencedTypes, TypescriptServiceGeneratorConfiguration settings) {
        Set<Class<?>> ret = Sets.newHashSet();
        for (Type t : referencedTypes) {
            if (settings.ignoredClasses().contains(t)) {
                continue;
            }

            // dummy context used for below check
            Context nullContext = new Context() {

                @Override
                public Result processType(Type javaType) {
                    return null;
                }

                @Override
                public String getMappedName(Class<?> cls) {
                    return null;
                }
            };

            if (t instanceof Class && ((Class<?>) t).isEnum()) {
                ret.add((Class<?>) t);
                continue;
            }

            // Don't add any classes that the user has made an exception for
            if (settings.customTypeProcessor() == null || settings.customTypeProcessor().processType(t, nullContext) == null) {
                if (t instanceof Class) {
                    ret.add((Class<?>) t);
                } else if(t instanceof ParameterizedType) {
                    ParameterizedType parameterized = (ParameterizedType) t;
                    ret.addAll(getReferencedClasses(Sets.newHashSet(parameterized.getRawType()), settings));
                    ret.addAll(getReferencedClasses(Sets.newHashSet(Arrays.asList(parameterized.getActualTypeArguments()).iterator()), settings));
                }
            }
        }
        return ret;
    }

    private static <T> String optionalToString(Optional<T> payload) {
        if (payload.isPresent()) {
            return payload.get().toString();
        }
        return "";
    }

}
