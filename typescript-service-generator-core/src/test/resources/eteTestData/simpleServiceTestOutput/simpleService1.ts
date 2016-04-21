// Copyright
// Generated
import { HttpEndpointOptions, HttpApiBridge } from "./httpApiBridge";

export interface SimpleService1 {
    method1(): FooReturn<string>;
}

export class SimpleService1Impl implements SimpleService1 {

    private httpApiBridge: HttpApiBridge;
    constructor(httpApiBridge: HttpApiBridge) {
        this.httpApiBridge = httpApiBridge;
    }

    public method1() {
        var httpCallData = <HttpEndpointOptions> {
            serviceIdentifier: "simpleService1",
            endpointPath: "simple1/method1",
            endpointName: "method1",
            method: "GET",
            requestMediaType: "application/json",
            responseMediaType: "",
            requiredHeaders: [],
            pathArguments: [],
            queryArguments: {
            },
            data: null
        };
        return this.httpApiBridge.callEndpoint<string>(httpCallData);
    }
}
