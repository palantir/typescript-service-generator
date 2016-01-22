/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.model;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
@JsonDeserialize(as = ImmutableServiceModel.class)
@JsonSerialize(as = ImmutableServiceModel.class)
public abstract class ServiceModel {
    public abstract Set<Type> referencedTypes();
    public abstract String name();
    public abstract String servicePath();
    public abstract List<ServiceEndpointModel> endpointModels();
}
