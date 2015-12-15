package com.palantir.code.ts.generator.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.palantir.code.ts.generator.TypescriptServiceGeneratorConfiguration;

import cz.habarta.typescript.generator.TsType;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public abstract class ServiceEndpointParameterModel {
    @Nullable public abstract String pathParam();
    @Nullable public abstract String headerParam();
    @Nullable public abstract String queryParam();
    public abstract Type javaType();
    public abstract TsType tsType();

    public String getParameterName(TypescriptServiceGeneratorConfiguration settings) {
        if (pathParam() != null) {
            return pathParam();
        } else if (queryParam() != null) {
            return queryParam();
        } else {
            Class<?> nameClass = null;
            if (javaType() instanceof Class<?>) {
                nameClass = (Class<?>) javaType();
            } else if (javaType() instanceof ParameterizedType) {
                nameClass = (Class<?>) ((ParameterizedType) javaType()).getRawType();
            }
            return Character.toLowerCase(nameClass.getSimpleName().charAt(0)) + nameClass.getSimpleName().substring(1);
        }
    }
}
