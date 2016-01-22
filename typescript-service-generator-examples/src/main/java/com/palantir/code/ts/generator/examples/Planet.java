package com.palantir.code.ts.generator.examples;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(as = ImmutablePlanet.class)
@JsonSerialize(as = ImmutablePlanet.class)
@Value.Immutable
public interface Planet {
    String name();
    double radiusKm();
}
