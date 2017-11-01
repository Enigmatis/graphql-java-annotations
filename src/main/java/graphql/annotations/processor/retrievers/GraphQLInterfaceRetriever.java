/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.processor.retrievers;


import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;

public class GraphQLInterfaceRetriever {

    private GraphQLOutputObjectRetriever graphQLOutputObjectRetriever;

    public GraphQLInterfaceRetriever(GraphQLOutputObjectRetriever graphQLOutputObjectRetriever) {
        this.graphQLOutputObjectRetriever = graphQLOutputObjectRetriever;
    }

    public GraphQLInterfaceRetriever() {
        this(new GraphQLOutputObjectRetriever());
    }

    /**
     * This will examine the class and return a {@link graphql.schema.GraphQLOutputType} ready for further definition
     *
     * @param iface     interface to examine
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link graphql.schema.GraphQLOutputType}
     * @throws GraphQLAnnotationsException if the class cannot be examined
     */
    public graphql.schema.GraphQLOutputType getInterface(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        return graphQLOutputObjectRetriever.getOutputType(iface, container);
    }
}
