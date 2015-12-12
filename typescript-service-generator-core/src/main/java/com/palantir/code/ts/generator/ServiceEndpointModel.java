package com.palantir.code.ts.generator;

import java.lang.reflect.Type;
import java.util.List;

import org.immutables.value.Value;

import cz.habarta.typescript.generator.TsType;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public abstract class ServiceEndpointModel implements Comparable<ServiceEndpointModel> {
    public abstract Type javaReturnType();
    public abstract TsType tsReturnType();
    public abstract List<ServiceEndpointParameterModel> parameters();
    public abstract String endpointName();
    public abstract String endpointPath();
    public abstract String endpointMethodType();
    @Value.Default
    public String endpointMediaType() {
        return "application/json";
    }

    @Override
    public int compareTo(ServiceEndpointModel o) {
        return this.endpointName().compareTo(o.endpointName());
    }
}
