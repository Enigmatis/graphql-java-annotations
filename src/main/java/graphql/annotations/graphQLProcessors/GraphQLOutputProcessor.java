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
package graphql.annotations.graphQLProcessors;


import graphql.annotations.GraphQLAnnotationsException;
import graphql.annotations.ProcessingElementsContainer;
import graphql.annotations.util.GraphQLObjectInfoRetriever;
import graphql.annotations.util.GraphQLOutputObjectRetriever;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

public class GraphQLOutputProcessor {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLOutputObjectRetriever outputObjectRetriever;


    public GraphQLOutputProcessor(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLOutputObjectRetriever outputObjectRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.outputObjectRetriever = outputObjectRetriever;
    }

    public GraphQLOutputProcessor() {
        this(new GraphQLObjectInfoRetriever(), new GraphQLOutputObjectRetriever());
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

        return outputObjectRetriever.getOutputType(object, container);
    }

}
