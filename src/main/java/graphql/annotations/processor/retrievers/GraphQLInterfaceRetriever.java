/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.processor.retrievers;


import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLOutputType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(service = GraphQLInterfaceRetriever.class, immediate = true)
public class GraphQLInterfaceRetriever {

    private GraphQLTypeRetriever graphQLTypeRetriever;

    /**
     * This will examine the class and return a {@link graphql.schema.GraphQLOutputType} ready for further definition
     *
     * @param iface     interface to examine
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link graphql.schema.GraphQLOutputType}
     * @throws GraphQLAnnotationsException if the class cannot be examined
     */
    public graphql.schema.GraphQLOutputType getInterface(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        return (GraphQLOutputType) graphQLTypeRetriever.getGraphQLType(iface, container, false);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setGraphQLTypeRetriever(GraphQLTypeRetriever graphQLTypeRetriever) {
        this.graphQLTypeRetriever = graphQLTypeRetriever;
    }

    public void unsetGraphQLTypeRetriever(GraphQLTypeRetriever graphQLOutputObjectRetriever) {
        this.graphQLTypeRetriever = null;
    }
}
