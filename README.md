typescript-service-generator
====================
typescript-service-generator is a tool for creating strongly typed typescript http interfaces from jxrs annotated java interfaces.

For each such java interface, a corresponding .ts class is generated that contains
- Interfaces for all DTO objects used by the java interface, created using the typescript-generator project (https://github.com/vojtechhabarta/typescript-generator/)
- A class, ready to be instantiated with a simple "bridge" object (defined later) that has a callable method for each endpoint in the java class

For example for these Java files:

``` java
package com.palantir.code.ts.generator.examples;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/myservice")
public interface MyService {
    @GET
    @Path("/foo_get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    MyObject helloWorld();
}
```
``` java
package com.palantir.code.ts.generator.examples;

/**
 * Note that MyObject is jackson serializable
 */
public class MyObject {
    private String payload = "hello world";

    public String getPayload() {
        return payload;
    }
}
```

typescript-service-generator outputs this TypeScript file:
``` typescript
// A potential copyright header
// A desired generated message
module Foundry.Http.MyService {

    export interface IMyObject {
        payload: string;
    }

    export interface IMyService {
        helloWorld(): HttpTypeWrapper<IMyObject>;
    }

    export class MyService implements IMyService {

        private httpApiBridge: IHttpApiBridge;
        constructor(restApiBridge: IHttpApiBridge) {
            this.httpApiBridge = restApiBridge;
        }

        public helloWorld() {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "myService",
                endpointPath: "myservice/foo_get",
                method: "GET",
                mediaType: "application/json",
                requiredHeaders: [],
                pathArguments: [],
                queryArguments: {
                },
                data: null
            };
            return this.httpApiBridge.callEndpoint<IMyObject>(httpCallData);
        }
    }
}
```
See MyServiceGenerator.java for all details on this example

Instantiating the generated class requires an implementation of IHttpApiBridge

IHttpApiBridge
-----
This is an interface that serves as a "bridge" between the generated typescript service classes. The contract of this interface is that it should know how to issue http calls given the inputs, and returns an object of a configurable type (see TypescriptServiceGeneratorConfiguration.genericEndpointReturnType). Any generated service class can be instantiated by constructing it with an implementation of the httpApiBridge. For an example, see the end of output/angularHttpApiBridge.ts
