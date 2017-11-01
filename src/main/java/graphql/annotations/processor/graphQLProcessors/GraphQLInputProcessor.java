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
import graphql.annotations.processor.retrievers.GraphQLObjectHandler;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLType;

import java.util.Map;

public class GraphQLInputProcessor {

    private static final String DEFAULT_INPUT_PREFIX = "Input";

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLObjectHandler graphQLObjectHandler;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;

    public GraphQLInputProcessor(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever,GraphQLObjectHandler graphQLObjectHandler,GraphQLInputObjectRetriever graphQLInputObjectRetriever){
        this.graphQLObjectInfoRetriever=graphQLObjectInfoRetriever;
        this.graphQLObjectHandler=graphQLObjectHandler;
        this.graphQLInputObjectRetriever=graphQLInputObjectRetriever;
    }

    public GraphQLInputProcessor(){
        this(new GraphQLObjectInfoRetriever(), new GraphQLObjectHandler(),new GraphQLInputObjectRetriever());
    }
    public GraphQLInputObjectType getInputObject(Class<?> object, ProcessingElementsContainer container) {
        String typeName = DEFAULT_INPUT_PREFIX + graphQLObjectInfoRetriever.getTypeName(object);
        Map<String, GraphQLType> typeRegistry = container.getTypeRegistry();

        if (typeRegistry.containsKey(typeName)) {
            return (GraphQLInputObjectType) container.getTypeRegistry().get(typeName);
        } else {
            graphql.schema.GraphQLType graphQLType = graphQLObjectHandler.getObject(object,container);
            GraphQLInputObjectType inputObject = (GraphQLInputObjectType) graphQLInputObjectRetriever.getInputObject(graphQLType, DEFAULT_INPUT_PREFIX,typeRegistry);
            typeRegistry.put(inputObject.getName(), inputObject);
            return inputObject;
        }
    }


}
