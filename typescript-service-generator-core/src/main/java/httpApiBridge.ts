export interface IHttpEndpointOptions {
    serviceIdentifier?: string;
    endpointPath: string;
    method: string;
    mediaType: string;
    requiredHeaders: string[];
    pathArguments: string[];
    queryArguments: any;
    data?: any;
}

export interface IHttpApiBridge {
    callEndpoint<T>(parameters: IHttpEndpointOptions): ng.IPromise<ng.IHttpPromiseCallbackArg<T>>;
}