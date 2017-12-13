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
package graphql.annotations.processor.graphQLProcessors;


import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.retrievers.GraphQLTypeRetriever;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

public class GraphQLOutputProcessor {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLTypeRetriever graphQLTypeRetriever;


    public GraphQLOutputProcessor(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLTypeRetriever graphQLTypeRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.graphQLTypeRetriever = graphQLTypeRetriever;
    }

    public GraphQLOutputProcessor() {
        this(new GraphQLObjectInfoRetriever(), new GraphQLTypeRetriever());
    }

    /**
     * This will examine the object class and return a {@link GraphQLOutputType} representation
     * which may be a {@link GraphQLOutputType} or a {@link graphql.schema.GraphQLTypeReference}
     *
     * @param object the object class to examine
     * @param container a class that hold several members that are required in order to build schema

     * @return a {@link GraphQLOutputType} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */

    public GraphQLOutputType getOutputTypeOrRef(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        String typeName = graphQLObjectInfoRetriever.getTypeName(object);
        if (container.getProcessing().contains(typeName)) {
            return new GraphQLTypeReference(typeName);
        }

        return (GraphQLOutputType) graphQLTypeRetriever.getGraphQLType(object, container, false);
    }

}
