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


import graphql.schema.*;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;

import java.util.Map;
import java.util.stream.Collectors;

public class GraphQLInputObjectRetriever {


    /**
     * This will turn a {@link GraphQLObjectType} into a corresponding {@link GraphQLInputObjectType}
     *
     * @param graphQLType   the graphql object type
     * @param newNamePrefix since graphql types MUST be unique, this prefix will be applied to the new input types
     * @param typeRegistry object thata saves all the types
     * @return a {@link GraphQLInputObjectType}
     */

    public GraphQLInputType getInputObject(graphql.schema.GraphQLType graphQLType, String newNamePrefix, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (graphQLType instanceof GraphQLObjectType) {
            return handleGraphQLObjectType((GraphQLObjectType) graphQLType, newNamePrefix, typeRegistry);
        } else if (graphQLType instanceof GraphQLList) {
            return new GraphQLList(getInputObject(((GraphQLList) graphQLType).getWrappedType(), newNamePrefix, typeRegistry));
        } else if (graphQLType instanceof graphql.schema.GraphQLNonNull) {
            return new graphql.schema.GraphQLNonNull(getInputObject(((GraphQLNonNull) graphQLType).getWrappedType(), newNamePrefix, typeRegistry));
        } else if (graphQLType instanceof GraphQLTypeReference) {
            return new GraphQLTypeReference(newNamePrefix + (graphQLType).getName());
        } else if (graphQLType instanceof GraphQLInputType) {
            return (GraphQLInputType) graphQLType;
        }
        throw new IllegalArgumentException("Cannot convert type to input : " + graphQLType);
    }

    private GraphQLInputType handleGraphQLObjectType(GraphQLObjectType graphQLType, String newNamePrefix, Map<String, GraphQLType> typeRegistry) {
        GraphQLObjectType object = graphQLType;
        String newObjectName = newNamePrefix + object.getName();
        GraphQLType objectInTypeRegistry = typeRegistry.get(newObjectName);
        if (objectInTypeRegistry != null && objectInTypeRegistry instanceof GraphQLInputType) {
            return (GraphQLInputType) objectInTypeRegistry;
        }
        GraphQLInputObjectType inputObjectType = new GraphQLInputObjectType(newObjectName, object.getDescription(),
                object.getFieldDefinitions().stream().
                        map(field -> {
                            GraphQLOutputType type = field.getType();
                            GraphQLInputType inputType = getInputObject(type, newNamePrefix, typeRegistry);
                            return new GraphQLInputObjectField(field.getName(), field.getDescription(), inputType, null);
                        }).
                        collect(Collectors.toList()));
        typeRegistry.put(inputObjectType.getName(), inputObjectType);
        return inputObjectType;
    }
}
