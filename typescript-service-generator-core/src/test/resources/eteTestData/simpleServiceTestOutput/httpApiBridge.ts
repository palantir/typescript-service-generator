// Copyright
// Generated
export interface HttpEndpointOptions {
    serviceIdentifier?: string;
    endpointPath: string;
    endpointName: string;
    method: string;
    requestMediaType: string;
    responseMediaType: string;
    requiredHeaders: string[];
    pathArguments: any[];
    queryArguments: any;
    data?: any;
}

export interface HttpApiBridge {
    callEndpoint<T>(parameters: HttpEndpointOptions): FooReturn<T>;
}