/*
 * Copyright © 2016 Palantir Technologies Inc.
 */

// An example http api bridge, intended to illustrate, not be used directly.
module MyProject.Http {

    export class AngularHttpApiBridge implements IHttpApiBridge {
        private $http: ng.IHttpService;

        public static $inject = ["$http"]
        constructor($http: ng.IHttpService) {
            this.$http = $http;
        }

        public callEndpoint<T>(parameters: IHttpEndpointOptions) {
            var url = this.createUrlPath(parameters);
            var requestConfig: ng.IRequestShortcutConfig = {};
            if (parameters.requiredHeaders.length > 0) {
                throw new Error("Required headers not supported for local http api bridge.");
            }
            requestConfig.params = parameters.queryArguments;
            return this.callHttpService(url, requestConfig, parameters, this.$http);
        }

        private createUrlPath(parameters: IHttpEndpointOptions) {
            var urlParameterRegex = /\{[^\}]+\}/;
            var path = parameters.endpointPath;
            parameters.pathArguments.forEach((pathArgument) => {
                path = path.replace(urlParameterRegex, pathArgument);
            });
            Object.keys(parameters.queryArguments).forEach((key) => {
                if (parameters.queryArguments[key] == null) {
                    delete parameters.queryArguments[key];
                }
            });
            return path;
        }

        private callHttpService(url: string,
                                requestConfig: ng.IRequestShortcutConfig,
                                parameters: IHttpEndpointOptions,
                                $http: ng.IHttpService) {
            switch (parameters.method) {
                case "GET":
                    return $http.get(url, requestConfig);
                case "DELETE":
                    return $http.delete(url, requestConfig);
                case "POST":
                    return $http.post(url, parameters.data, requestConfig);
                case "PUT":
                    return $http.put(url, parameters.data, requestConfig);
                default:
                    throw new Error(`Unrecognized http method ${parameters.method}`);
            }
        }
    }
    // Register AngularHttpApiBridge as a service
    // Inject AngularHttpApiBridge as httpBridge
    var myService = new MyService(httpBridge);
    // myService can now be used
}
