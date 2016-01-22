/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.utils;

import javax.annotation.CheckForNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TestUtils {

    @Path("/testService")
    public interface TestServiceClass {

        @GET
        @Path("/stringGetter/{a}/{b}")
        String stringGetter(@PathParam("a") String a, @PathParam("b") String b);

        @GET
        @Path("/value")
        MyValue value();
    }

    @Path("/testComplexService")
    public interface TestComplexServiceClass {

        @GET
        @Path("/queryGetter/")
        MyObject queryGetter(@QueryParam("x") Boolean x);

        @PUT
        @Path("simplePut")
        String simplePut(DataObject dataObject);

        @POST
        @Path("/allOptionsPost/{a}")
        GenericObject<MyObject> allOptionsPost(@PathParam("a") String a, @QueryParam("x") Integer b, DataObject dataObject);
    }

    @Path("/ignoredParameters")
    public interface IgnoredParametersClass {

        @GET
        @Path("/stringGetter/{a}/{b}")
        String stringGetter(@CheckForNull Integer y, @PathParam("a") String a, @PathParam("b") String b);
    }

    public static class MyObject {
        public MyObject getY() {
            return null;
        }
    }

    public static class DataObject {
        public MyObject getY() {
            return null;
        }
    }

    public static class GenericObject<T> {
        public T getY() {
            return null;
        }
    }

    @JsonDeserialize(as = ImmutableMyValue.class)
    @JsonSerialize(as = ImmutableMyValue.class)
    @Value.Immutable
    public interface MyValue {
        String z();
    }
}
