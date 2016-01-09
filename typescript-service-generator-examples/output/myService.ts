// A potential copyright header
// A desired generated message
module Foundry.Http.MyService {

    export interface IMyObject {
        payload: string;
    }

    export interface IMyService {
        helloWorld(): HttpTypeWrapper<IMyObject>;
    }

    export class MyService implements IMyService {

        private httpApiBridge: IHttpApiBridge;
        constructor(restApiBridge: IHttpApiBridge) {
            this.httpApiBridge = restApiBridge;
        }

        public helloWorld() {
            var httpCallData = <IHttpEndpointOptions> {
                serviceIdentifier: "myService",
                endpointPath: "myservice/foo_get",
                method: "GET",
                mediaType: "application/json",
                requiredHeaders: [],
                pathArguments: [],
                queryArguments: {
                },
                data: null
            };
            return this.httpApiBridge.callEndpoint<IMyObject>(httpCallData);
        }
    }
}
