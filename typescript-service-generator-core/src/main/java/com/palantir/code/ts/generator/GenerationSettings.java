package com.palantir.code.ts.generator;

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
public abstract class GenerationSettings {

    @Value.Parameter
    public abstract String getCopyrightHeader();
    @Value.Parameter
    public abstract TypeProcessor getCustomTypeProcessor();
    @Value.Parameter
    public abstract Set<Class<?>> getIgnoredAnnotations();
    @Value.Parameter
    public abstract String getTypescriptModule();
    @Value.Parameter
    public abstract String getGeneratedMessage();
    @Value.Parameter
    public abstract Set<Type> getIgnoredClasses();

    public static class Utils {
        public static Settings getSettings(TypeProcessor customTypeProcessor) {
            Settings settings = new Settings();

            TypeProcessor genericTypeProcessor = new GenericsTypeProcessor();
            List<TypeProcessor> typeProcessors = new ArrayList<>();
            typeProcessors.add(customTypeProcessor);
            typeProcessors.add(getOverridingTypeParser(customTypeProcessor));
            typeProcessors.add(genericTypeProcessor);
            settings.customTypeProcessor = new TypeProcessor.Chain(typeProcessors);
            settings.addTypeNamePrefix = "I";
            settings.sortDeclarations = true;
            settings.noFileComment = true;

            return settings;
        }

        public static TypeProcessor getOverridingTypeParser(TypeProcessor customTypeProcessor) {
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
            return new TypeProcessor.Chain(Lists.newArrayList(customTypeProcessor, defaultTypeProcessor));
        }
    }
}
