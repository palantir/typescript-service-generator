// Copyright
// Generated
module ModuleName.TestComplexServiceClass {
    /* tslint:disable */
    /* eslint-disable */

    export interface IDataObject {
        y: IMyObject;
    }

    export interface IGenericObject<T> {
        y: T;
    }

    export interface IImmutablesObject {
        y: string;
    }

    export interface IMyObject {
        y: IMyObject;
    }

    export interface ITestComplexServiceClass {
        allOptionsPost(a: string, dataObject: IDataObject, b?: number): FooReturn<IGenericObject<IMyObject>>;
        queryGetter(x?: boolean): FooReturn<IMyObject>;
        simplePut(dataObject: IDataObject): FooReturn<IImmutablesObject>;
    }

    export class TestComplexServiceClassImpl implements ITestComplexServiceClass {

        private httpApiBridge: IHttpApiBridge;
        constructor(httpApiBridge: IHttpApiBridge) {
            this.httpApiBridge = httpApiBridge;
        }

        public allOptionsPost(a: string, dataObject: IDataObject, b?: number) {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "testComplexServiceClass",
                endpointPath: "testComplexService/allOptionsPost/{a}",
                endpointName: "allOptionsPost",
                method: "POST",
                requestMediaType: "application/json",
                responseMediaType: "",
                requiredHeaders: [],
                pathArguments: [a],
                queryArguments: {
                    b: b,
                },
                data: dataObject
            };
            return this.httpApiBridge.callEndpoint<IGenericObject<IMyObject>>(httpCallData);
        }

        public queryGetter(x?: boolean) {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "testComplexServiceClass",
                endpointPath: "testComplexService/queryGetter",
                endpointName: "queryGetter",
                method: "GET",
                requestMediaType: "application/json",
                responseMediaType: "",
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
                endpointName: "simplePut",
                method: "PUT",
                requestMediaType: "application/json",
                responseMediaType: "",
                requiredHeaders: [],
                pathArguments: [],
                queryArguments: {
                },
                data: dataObject
            };
            return this.httpApiBridge.callEndpoint<IImmutablesObject>(httpCallData);
        }
    }
}
