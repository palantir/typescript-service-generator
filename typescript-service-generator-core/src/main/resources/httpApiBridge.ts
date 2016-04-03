export interface %sHttpEndpointOptions {
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

export interface %sHttpApiBridge {
    callEndpoint<T>(parameters: %sHttpEndpointOptions): %s;
}
