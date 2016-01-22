// A potential copyright header
// A desired generated message
module MyProject.Http {

    export interface IHttpEndpointOptions {
        serviceIdentifier?: string;
        endpointPath: string;
        endpointName: string;
        method: string;
        mediaType: string;
        requiredHeaders: string[];
        pathArguments: string[];
        queryArguments: any;
        data?: any;
    }

    export interface IHttpApiBridge {
        callEndpoint<T>(parameters: IHttpEndpointOptions): ng.IPromise<T>;
    }
}
