// Copyright
// Generated
module ModuleName.TestComplexServiceClass {

    export interface IDataObject {
        y: IMyObject;
    }

    export interface IGenericObject<T> {
        y: T;
    }

    export interface IMyObject {
        y: IMyObject;
    }

    export interface ITestComplexServiceClass {
        allOptionsPost(a: string, dataObject: IDataObject, x?: number): FooReturn<IGenericObject<IMyObject>>;
        queryGetter(x?: boolean): FooReturn<IMyObject>;
        simplePut(dataObject: IDataObject): FooReturn<string>;
    }

    export class TestComplexServiceClass implements ITestComplexServiceClass {

        private httpApiBridge: IHttpApiBridge;
        constructor(httpApiBridge: IHttpApiBridge) {
            this.httpApiBridge = httpApiBridge;
        }

        public allOptionsPost(a: string, dataObject: IDataObject, x?: number) {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "testComplexServiceClass",
                endpointPath: "testComplexService/allOptionsPost/{a}",
                method: "POST",
                mediaType: "application/json",
                requiredHeaders: [],
                pathArguments: [a],
                queryArguments: {
                    x: x,
                },
                data: dataObject
            };
            return this.httpApiBridge.callEndpoint<IGenericObject<IMyObject>>(httpCallData);
        }

        public queryGetter(x?: boolean) {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "testComplexServiceClass",
                endpointPath: "testComplexService/queryGetter",
                method: "GET",
                mediaType: "application/json",
                requiredHeaders: [],
                pathArguments: [],
                queryArguments: {
                    x: x,
                },
                data: null
            };
            return this.httpApiBridge.callEndpoint<IMyObject>(httpCallData);
        }

        public simplePut(dataObject: IDataObject) {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "testComplexServiceClass",
                endpointPath: "testComplexService/simplePut",
                method: "PUT",
                mediaType: "application/json",
                requiredHeaders: [],
                pathArguments: [],
                queryArguments: {
                },
                data: dataObject
            };
            return this.httpApiBridge.callEndpoint<string>(httpCallData);
        }
    }
}
