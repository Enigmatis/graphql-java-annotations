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


import graphql.annotations.processor.retrievers.GraphQLInputObjectRetriever;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.retrievers.GraphQLTypeRetriever;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLType;

import java.util.Map;

import static graphql.annotations.processor.util.InputPropertiesUtil.DEFAULT_INPUT_PREFIX;

public class GraphQLInputProcessor {


    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLTypeRetriever graphQLOutputObjectRetriever;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;

    public GraphQLInputProcessor(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLTypeRetriever graphQLOutputObjectRetriever, GraphQLInputObjectRetriever graphQLInputObjectRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.graphQLOutputObjectRetriever = graphQLOutputObjectRetriever;
        this.graphQLInputObjectRetriever = graphQLInputObjectRetriever;
    }

    public GraphQLInputProcessor() {
        this(new GraphQLObjectInfoRetriever(), new GraphQLTypeRetriever(), new GraphQLInputObjectRetriever());
    }

    /**
     * This will examine the object class and return a {@link GraphQLInputType} representation
     *
     * @param object    the object class to examine
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link GraphQLInputType} that represents that object class
     */

    public GraphQLInputType getInputType(Class<?> object, ProcessingElementsContainer container) {
        boolean considerAsInput = true;
        if (Enum.class.isAssignableFrom(object)) {
            considerAsInput = false;
        }

        String typeName = graphQLObjectInfoRetriever.getTypeName(object);

        if (considerAsInput)
            typeName = DEFAULT_INPUT_PREFIX + typeName;

        Map<String, GraphQLType> typeRegistry = container.getTypeRegistry();

        if (typeRegistry.containsKey(typeName)) {
            return (GraphQLInputType) container.getTypeRegistry().get(typeName);
        } else {

            graphql.schema.GraphQLType graphQLType = graphQLOutputObjectRetriever.getGraphQLType(object, container, considerAsInput);
            GraphQLInputType inputObject = graphQLInputObjectRetriever.getInputObject(graphQLType, DEFAULT_INPUT_PREFIX, typeRegistry);
            typeRegistry.put(inputObject.getName(), inputObject);
            return inputObject;
        }
    }
}
