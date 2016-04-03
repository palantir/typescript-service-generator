/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

package com.palantir.code.ts.generator.utils;

import javax.annotation.CheckForNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class TestUtils {

    @Path("/testService")
    public interface TestServiceClass {

        @GET
        @Path("/stringGetter/{a}/{b}")
        String stringGetter(@PathParam("a") String a, @PathParam("b") String b);
    }

    @Path("/testComplexService")
    public interface TestComplexServiceClass {

        @GET
        @Path("/queryGetter/")
        MyObject queryGetter(@QueryParam("x") Boolean x);

        @PUT
        @Path("simplePut")
        ImmutablesObject simplePut(DataObject dataObject);

        @POST
        @Path("/allOptionsPost/{a}")
        @Consumes(MediaType.APPLICATION_JSON)
        GenericObject<MyObject> allOptionsPost(@PathParam("a") String a, @QueryParam("b") Integer x, DataObject dataObject);
    }

    @Path("/ignoredParameters")
    public interface IgnoredParametersClass {

        @GET
        @Path("/stringGetter/{a}/{b}")
        String stringGetter(@CheckForNull Integer y, @PathParam("a") String a, @PathParam("b") String b);
    }

    @Path("/enumParametersClass")
    public interface EnumClass {

        @GET
        @Path("/enumGetter")
        MyEnum enumGetter();
    }

    @Path("/duplicateMethods")
    public interface DuplicateMethodNamesService {

        @GET
        @Path("/duplicate")
        String duplicate();

        @GET
        @Path("/duplicate/{a}")
        String duplicate(@PathParam("a") String a);
    }

    @Path("/simple1")
    public interface SimpleService1 {

        @GET
        @Path("/method1")
        String method1();
    }

    @Path("/simple2")
    public interface SimpleService2 {

        @GET
        @Path("/method2")
        String method2();
    }

    @Path("/concreteObject")
    public class ConcreteObjectService {

        @GET
        public String noPathGetter() {
            return "";
        };
    }

    @Path("/plainTextService")
    public interface PlainTextService {

        @GET
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Path("/plainText")
        public String plainText(String dataBody);
    }

    public enum MyEnum {
        VALUE1, VALUE2
    }

    public static class MyObject {
        // Ensure json property overrides
        @JsonProperty("y")
        public MyObject getZ() {
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

    @JsonDeserialize(as = ImmutableImmutablesObject.class)
    @JsonSerialize(as = ImmutableImmutablesObject.class)
    @Value.Immutable
    public interface ImmutablesObject {
        String y();
    }
}
