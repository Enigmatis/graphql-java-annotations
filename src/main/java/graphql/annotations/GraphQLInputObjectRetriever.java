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
package graphql.annotations;


import graphql.schema.*;
import graphql.schema.GraphQLNonNull;

import java.util.Map;
import java.util.stream.Collectors;

public class GraphQLInputObjectRetriever {

    public GraphQLInputType getInputObject(graphql.schema.GraphQLType graphQLType, String newNamePrefix, Map<String, graphql.schema.GraphQLType> typeRegistry) {
        if (graphQLType instanceof GraphQLObjectType) {
            GraphQLObjectType object = (GraphQLObjectType) graphQLType;
            if (typeRegistry.containsKey(newNamePrefix + object.getName()) && typeRegistry.get(newNamePrefix + object.getName()) instanceof GraphQLInputType) {
                return (GraphQLInputType) typeRegistry.get(newNamePrefix + object.getName());
            }
            GraphQLInputObjectType inputObjectType = new GraphQLInputObjectType(newNamePrefix + object.getName(), object.getDescription(),
                    object.getFieldDefinitions().stream().
                            map(field -> {
                                GraphQLOutputType type = field.getType();
                                GraphQLInputType inputType = getInputObject(type, newNamePrefix,typeRegistry);
                                return new GraphQLInputObjectField(field.getName(), field.getDescription(), inputType, null);
                            }).
                            collect(Collectors.toList()));
            typeRegistry.put(inputObjectType.getName(), inputObjectType);
            return inputObjectType;
        } else if (graphQLType instanceof GraphQLList) {
            return new GraphQLList(getInputObject(((GraphQLList)graphQLType).getWrappedType(), newNamePrefix,typeRegistry));
        } else if (graphQLType instanceof graphql.schema.GraphQLNonNull) {
            return new graphql.schema.GraphQLNonNull(getInputObject(((GraphQLNonNull)graphQLType).getWrappedType(), newNamePrefix,typeRegistry));
        } else if (graphQLType instanceof GraphQLTypeReference) {
            return new GraphQLTypeReference(newNamePrefix + ((GraphQLTypeReference)graphQLType).getName());
        } else if (graphQLType instanceof GraphQLInputType){
            return (GraphQLInputType) graphQLType;
        }
        throw new IllegalArgumentException("Cannot convert type to input : "+graphQLType);
    }
}
