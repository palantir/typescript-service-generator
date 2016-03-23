/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.model;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
@JsonDeserialize(as = ImmutableInnerServiceModel.class)
@JsonSerialize(as = ImmutableInnerServiceModel.class)
public abstract class InnerServiceModel {
    public abstract String name();
    public abstract String servicePath();
    public abstract List<ServiceEndpointModel> endpointModels();
}
