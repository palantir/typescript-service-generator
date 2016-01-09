package com.palantir.code.ts.generator.model;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
public abstract class ServiceModel {
    public abstract Set<Type> referencedTypes();
    public abstract String name();
    public abstract String servicePath();
    public abstract List<ServiceEndpointModel> endpointModels();
}
