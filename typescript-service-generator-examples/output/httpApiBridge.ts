// A potential copyright header
// A desired generated message
module Foundry.Http {

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
        callEndpoint<T>(parameters: IHttpEndpointOptions): HttpTypeWrapper<T>;
    }
}
