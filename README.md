typescript-service-generator
====================
typescript-service-generator is a tool for creating strongly typed typescript http interfaces from jxrs annotated java interfaces.

For each such java interface, a corresponding .ts class is generated that contains
- Interfaces for all DTO objects used by the java interface, created using the typescript-generator project (https://github.com/vojtechhabarta/typescript-generator/)
- A class, ready to be instantiated with a simple "bridge" object (defined later) that has a callable method for each endpoint in the java class

For example for these Java files:

``` java
package com.palantir.code.ts.generator.examples;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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

Assumptions
-----
The typescript-service-generator generates 1.8.x+ typescript code. This restriction is inherited from one of its dependencies, [typescript-generator](https://github.com/vojtechhabarta/typescript-generator).

Contributing
-----
- Write your code
- Add tests for new functionality
- Fill out the [Individual](https://github.com/palantir/typescript-service-generator/blob/master/Palantir_Individual_Contributor_License_Agreement.pdf?raw=true) or [Corporate](https://github.com/palantir/typescript-service-generator/blob/master/Palantir_Corporate_Contributor_License_Agreement.pdf?raw=true) Contributor License Agreement and send it to [opensource@palantir.com](mailto:opensource@palantir.com)
  - You can do this easily on a Mac by using the Tools - Annotate - Signature feature in Preview.
- Submit a pull request

Depending on Published Artifacts
-----
typescript-service-generator is hosted on [bintray](https://bintray.com/palantir/releases/typescript-service-generator/view). To include in a gradle project:

Add bintray to your repository list:

```
repositories { maven { url 'https://dl.bintray.com/palantir/releases/' } }
```

Add the typescript-service-generator-core dependency to projects:

```
dependencies { compile "com.palantir.ts:typescript-service-generator-core:x.x.x" }
```

