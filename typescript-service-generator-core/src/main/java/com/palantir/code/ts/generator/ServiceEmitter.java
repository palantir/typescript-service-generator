package com.palantir.code.ts.generator;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.BeanSerializer;
import org.codehaus.jackson.map.ser.BeanSerializerFactory;
import org.codehaus.jackson.type.JavaType;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.code.ts.generator.model.ServiceEndpointModel;
import com.palantir.code.ts.generator.model.ServiceEndpointParameterModel;
import com.palantir.code.ts.generator.model.ServiceModel;

import cz.habarta.typescript.generator.ModelCompiler;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TsType.EnumType;
import cz.habarta.typescript.generator.TypeProcessor;
import cz.habarta.typescript.generator.TypeProcessor.Context;
import cz.habarta.typescript.generator.TypeProcessor.Result;
import cz.habarta.typescript.generator.TypeScriptGenerator;

public final class ServiceEmitter {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final ServiceModel model;
    private final TypescriptServiceGeneratorConfiguration settings;

    public ServiceEmitter(ServiceModel model, TypescriptServiceGeneratorConfiguration settings) {
        this.model = model;
        this.settings = settings;
    }

    public void emitTypescriptTypes(IndentedOutputWriter writer, TypescriptServiceGeneratorConfiguration settings) {
        Settings settingsToUse = settings.getSettings();
        TypeProcessor baseTypeProcessor = settingsToUse.customTypeProcessor;

        Set<Type> referencedTypes = model.directlyReferencedTypes();
        Set<Class<?>> referencedClasses = getReferencedClasses(referencedTypes, settings);
        referencedClasses = filterInputClasses(referencedClasses);

        final Set<Type> discoveredTypes = Sets.newHashSet(referencedClasses.iterator());
        settingsToUse.customTypeProcessor = new TypeProcessor.Chain(new TypeProcessor() {
            @Override
            public Result processType(Type javaType, Context context) {
                discoveredTypes.add(javaType);
                return null;
            }
        }, baseTypeProcessor);

        TypeScriptGenerator typescriptGenerator = new TypeScriptGenerator(settingsToUse);
        ByteArrayOutputStream typeDeclarations = new ByteArrayOutputStream();
        typescriptGenerator.generateEmbeddableTypeScript(Lists.newArrayList(referencedClasses.iterator()), typeDeclarations, true, 1);
        writeEnums(writer, discoveredTypes, typescriptGenerator.getModelCompiler());
        writer.write(new String(typeDeclarations.toByteArray()));
    }

    public void emitTypescriptClass(IndentedOutputWriter classWriter) {
        classWriter.writeLine("");
        classWriter.writeLine("export class " + model.name() + " implements " + settings.getSettings().addTypeNamePrefix + model.name() + " {");
        classWriter.increaseIndent();

        classWriter.writeLine("");
        classWriter.writeLine("private httpApiBridge: IHttpApiBridge;");
        classWriter.writeLine("constructor(restApiBridge: IHttpApiBridge) {");
        classWriter.increaseIndent();
        classWriter.writeLine("this.httpApiBridge = restApiBridge;");
        classWriter.decreaseIndent();
        classWriter.writeLine("}");

        for (ServiceEndpointModel endpointModel: model.endpointModels()) {
            classWriter.writeLine("");
            String line = "public " + endpointModel.endpointName() + "(";
            line += getEndpointParametersString(endpointModel);
            line += ") {";
            classWriter.writeLine(line);
            classWriter.increaseIndent();
            classWriter.writeLine("var httpCallData = <IHttpEndpointOptions> {");
            classWriter.increaseIndent();
            classWriter.writeLine("serviceIdentifier: \"" + Character.toLowerCase(model.name().charAt(0)) + model.name().substring(1) + "\",");
            classWriter.writeLine("endpointPath: \"" + model.servicePath() + "/" + endpointModel.endpointPath() + "\",");
            classWriter.writeLine("method: \"" + endpointModel.endpointMethodType() + "\",");
            classWriter.writeLine("mediaType: \"" + endpointModel.endpointMediaType() + "\",");
            List<String> requiredHeaders = Lists.newArrayList();
            List<String> pathArguments = Lists.newArrayList();
            List<String> queryArguments = Lists.newArrayList();
            String dataArgument = null;
            for (ServiceEndpointParameterModel parameterModel : endpointModel.parameters()) {
                if (parameterModel.headerParam() != null) {
                    requiredHeaders.add("\"" + parameterModel.headerParam() + "\"");
                } else if (parameterModel.pathParam() != null) {
                    pathArguments.add(parameterModel.getParameterName(settings));
                } else if (parameterModel.queryParam() != null) {
                    queryArguments.add(parameterModel.queryParam());
                } else {
                    if (dataArgument != null) {
                        throw new IllegalStateException("There should only be one data argument per endpoint. Found both" + dataArgument + " and " + parameterModel.getParameterName(settings));
                    }
                    dataArgument = parameterModel.getParameterName(settings);
                    if (parameterModel.tsType().toString().equals("string")) {
                        // strings have to be wrapped in quotes in order to be valid json
                        dataArgument = "`\"${" + parameterModel.getParameterName(settings) + "}\"`";
                    }
                }
            }
            classWriter.writeLine("requiredHeaders: [" + Joiner.on(", ").join(requiredHeaders) + "],");
            classWriter.writeLine("pathArguments: [" + Joiner.on(", ").join(pathArguments) + "],");
            classWriter.writeLine("queryArguments: {");
            classWriter.increaseIndent();
            for (String queryArgument: queryArguments) {
                classWriter.writeLine(queryArgument + ": " + queryArgument + ",");
            }
            classWriter.decreaseIndent();
            classWriter.writeLine("},");
            classWriter.writeLine("data: " + dataArgument);
            classWriter.decreaseIndent();
            classWriter.writeLine("};");
            // TODO: assumes angular promises are used, which is less general than this could be
            classWriter.writeLine("return this.httpApiBridge.callEndpoint<" + endpointModel.tsReturnType().toString() + ">(httpCallData);");
            classWriter.decreaseIndent();
            classWriter.writeLine("}");
        }
        classWriter.decreaseIndent();
        classWriter.writeLine("}");
    }

    public void emitTypescriptInterface(IndentedOutputWriter interfaceWriter) {
        interfaceWriter.writeLine("");
        interfaceWriter.writeLine("export interface " + settings.getSettings().addTypeNamePrefix + model.name() + " {");
        interfaceWriter.increaseIndent();

        for (ServiceEndpointModel endpointModel: model.endpointModels()) {
            String line = endpointModel.endpointName() + "(";
            line += getEndpointParametersString(endpointModel);
            line += "): ng.IPromise<ng.IHttpPromiseCallbackArg<" + endpointModel.tsReturnType().toString() + ">>;";
            interfaceWriter.writeLine(line);
        }

        interfaceWriter.decreaseIndent();
        interfaceWriter.writeLine("}");
    }

    private String getEndpointParametersString(ServiceEndpointModel endpointModel) {
        List<String> parameterStrings = Lists.newArrayList();
        for (ServiceEndpointParameterModel parameterModel : endpointModel.parameters()) {
            if (parameterModel.headerParam() != null) {
                //continue, header params are implicit
                continue;
            }
            String optionalString = parameterModel.queryParam() != null ? "?" : "";
            parameterStrings.add(parameterModel.getParameterName(settings) + optionalString + ": " + parameterModel.tsType().toString());
        }

        return Joiner.on(", ").join(parameterStrings);
    }

    private void writeEnums(IndentedOutputWriter writer, Set<Type> referencedTypes, ModelCompiler compiler) {
        //TODO: this won't be necessary once typescript supports enums
        List<EnumType> enums = Lists.newArrayList();
        for (Type type : referencedTypes) {
            TsType tsType = compiler.typeFromJava(type);
            if (tsType instanceof EnumType) {
                enums.add((EnumType) tsType);
            }
        }
        Collections.sort(enums, new Comparator<EnumType>() {
            @Override
            public int compare(EnumType a, EnumType b) {
                return a.name.compareTo(b.name);
            }
        });
        for (EnumType tsEnum : enums) {
            writer.writeLine("");
            List<String> values = Lists.newArrayList(tsEnum.values.iterator());
            Collections.sort(values);
            writer.writeLine(String.format("export var %s = {", tsEnum.name));
            writer.increaseIndent();
            for (int i = 0; i < values.size(); i++) {
                String value = values.get(i);
                writer.writeLine(String.format("%s: \"%s\"%s", value, value, i == values.size() - 1 ? "" : ","));
            }
            writer.decreaseIndent();
            writer.writeLine("};");
        }
    }

    private Set<Class<?>> filterInputClasses(Set<Class<?>> referencedClasses) {
        Set<Class<?>> typesToUse = Sets.newHashSet();
        for (Class<?> beanClass : referencedClasses) {
            if (beanClass.isEnum()) {
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
            SerializationConfig serializationConfig = objectMapper.getSerializationConfig();
            final JavaType simpleType = objectMapper.constructType(beanClass);
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
            if (settings.customTypeProcessor().processType(t, nullContext) == null) {
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

}
