package com.palantir.code.ts.generator;

import java.lang.reflect.Type;
import java.util.List;

import cz.habarta.typescript.generator.TsType;

public class ServiceEndpointModel implements Comparable<ServiceEndpointModel> {
    public Type javaReturnType;
    public TsType tsReturnType;
    public List<ServiceEndpointParameterModel> parameters;
    public String endpointName;
    public String endpointPath;
    public String endpointMethodType;
    public String endpointMediaType;

    @Override
    public int compareTo(ServiceEndpointModel o) {
        return this.endpointName.compareTo(o.endpointName);
    }
}
