package com.palantir.code.ts.generator;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import cz.habarta.typescript.generator.GenericsTypeProcessor;
import cz.habarta.typescript.generator.Settings;
import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeProcessor;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public abstract class TypescriptServiceGeneratorConfiguration {

    @Value.Parameter
    public abstract String copyrightHeader();
    @Value.Parameter
    public abstract TypeProcessor customTypeProcessor();
    @Value.Parameter
    public abstract Set<Class<?>> ignoredAnnotations();
    @Value.Parameter
    public abstract String typescriptModule();
    @Value.Parameter
    public abstract String generatedMessage();
    @Value.Parameter
    public abstract Set<Type> ignoredClasses();
    @Value.Parameter
    public abstract File generatedFolderLocation();

    public TypeProcessor getOverridingTypeParser() {
        TypeProcessor defaultTypeProcessor = new TypeProcessor() {
            @Override
            public Result processType(Type javaType, Context context) {
                TsType ret = null;
                if (javaType instanceof ParameterizedType) {
                    ParameterizedType param = (ParameterizedType) javaType;
                    if (param.getRawType() == Optional.class) {
                        Type arg = param.getActualTypeArguments()[0];
                        Result contextResponse = context.processType(arg);
                        if (contextResponse != null) {
                            return new Result(contextResponse.getTsType().optional(), contextResponse.getDiscoveredClasses());
                        } else {
                            return null;
                        }
                    }
                } else if (javaType == URI.class) {
                    ret = TsType.String;
                } if (ret == null) {
                    return null;
                } else {
                    return new Result(ret, new ArrayList<Class<?>>());
                }
            }
        };
        return new TypeProcessor.Chain(Lists.newArrayList(customTypeProcessor(), defaultTypeProcessor));
    }

    public Settings getSettings() {
        Settings settings = new Settings();

        TypeProcessor genericTypeProcessor = new GenericsTypeProcessor();
        List<TypeProcessor> typeProcessors = new ArrayList<>();
        typeProcessors.add(customTypeProcessor());
        typeProcessors.add(getOverridingTypeParser());
        typeProcessors.add(genericTypeProcessor);
        settings.customTypeProcessor = new TypeProcessor.Chain(typeProcessors);
        settings.addTypeNamePrefix = "I";
        settings.sortDeclarations = true;
        settings.noFileComment = true;

        return settings;
    }
}
