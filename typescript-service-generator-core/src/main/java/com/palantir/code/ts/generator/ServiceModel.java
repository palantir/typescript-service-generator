package com.palantir.code.ts.generator;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

public class ServiceModel {
    private final Set<Type> directlyReferencedTypes;
    private final String name;
    private final String pkg;
    private final String servicePath;
    private final List<ServiceEndpointModel> endpointModels;

    public ServiceModel(Set<Type> directlyReferencedTypes2, String servicePath, String name, String pkg, Collection<ServiceEndpointModel> endpointModels) {
        this.directlyReferencedTypes = directlyReferencedTypes2;
        this.name = name;
        this.pkg = pkg;
        this.servicePath = servicePath;
        this.endpointModels = Lists.newArrayList(endpointModels.iterator());
        Collections.sort(this.endpointModels);
    }

    public String getApiPrefix() {
        return servicePath;
    }

    public String getPackage() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getPkg() {
        return pkg;
    }

    public String getServicePath() {
        return servicePath;
    }

    public Collection<ServiceEndpointModel> getEndpointModels() {
        return endpointModels;
    }

    public Set<Type> getDirectlyReferencedTypes() {
        return directlyReferencedTypes;
    }

    public static class Builder {
        private Set<Type> directlyReferencedTypes;
        private String name;
        private String servicePath;
        private String pkg;
        private Collection<ServiceEndpointModel> endpointModels;

        public Builder() {

        }

        public void setName(String name) {
            this.name = name;
        }

        public Builder setServicePath(String servicePath) {
            this.servicePath = servicePath;
            return this;
        }

        public void setDirectlyReferencedTypes(Set<Type> referencedTypes) {
            this.directlyReferencedTypes = referencedTypes;
        }

        public ServiceModel build() {
            return new ServiceModel(directlyReferencedTypes, servicePath, name, pkg, endpointModels);
        }

        public void setPackage(String pkg) {
            this.pkg = pkg;
        }

        public void setEndpointModels(Collection<ServiceEndpointModel> endpointModels) {
            this.endpointModels = endpointModels;
        }
    }
}
