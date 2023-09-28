/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.model;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.palantir.code.ts.generator.ImmutableTypescriptServiceGeneratorConfiguration;

import cz.habarta.typescript.generator.TsType;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
@JsonDeserialize(as = ImmutableTypescriptServiceGeneratorConfiguration.class)
@JsonSerialize(as = ImmutableTypescriptServiceGeneratorConfiguration.class)
public abstract class ServiceEndpointModel implements Comparable<ServiceEndpointModel> {
    public abstract Type javaReturnType();
    public abstract TsType tsReturnType();
    public abstract List<ServiceEndpointParameterModel> parameters();
    public abstract String endpointName();
    public abstract String endpointPath();
    public abstract String endpointMethodType();

    @Value.Default
    public String endpointRequestMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    public abstract Optional<String> endpointResponseMediaType();

    @Override
    public int compareTo(ServiceEndpointModel o) {
        return this.endpointName().compareTo(o.endpointName());
    }
}
