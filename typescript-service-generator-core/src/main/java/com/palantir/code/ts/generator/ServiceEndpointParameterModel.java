package com.palantir.code.ts.generator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cz.habarta.typescript.generator.TsType;

public class ServiceEndpointParameterModel {
    public String pathParam;
    public String headerParam;
    public String queryParam;
    public Type javaType;
    public TsType tsType;

    public String getParameterName(GenerationSettings settings) {
        if (pathParam != null) {
            return pathParam;
        } else if (queryParam != null) {
            return queryParam;
        } else {
            Class<?> nameClass = null;
            if (javaType instanceof Class<?>) {
                nameClass = (Class<?>) javaType;
            } else if (javaType instanceof ParameterizedType) {
                nameClass = (Class<?>) ((ParameterizedType) javaType).getRawType();
            }
            return Character.toLowerCase(nameClass.getSimpleName().charAt(0)) + nameClass.getSimpleName().substring(1);
        }
    }
}
