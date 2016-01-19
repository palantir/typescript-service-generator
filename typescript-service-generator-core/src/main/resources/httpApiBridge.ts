export interface %sHttpEndpointOptions {
    serviceIdentifier?: string;
    endpointPath: string;
    method: string;
    mediaType: string;
    requiredHeaders: string[];
    pathArguments: string[];
    queryArguments: any;
    data?: any;
}

export interface %sHttpApiBridge {
    callEndpoint<T>(parameters: %sHttpEndpointOptions): %s;
}
