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
package graphql.annotations.builders;

import graphql.annotations.*;
import graphql.annotations.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.searchAlgorithms.ParentalSearch;
import graphql.annotations.util.GraphQLObjectInfoRetriever;
import graphql.annotations.util.GraphQLOutputObjectRetriever;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static graphql.annotations.util.ObjectUtil.getAllFields;
import static graphql.schema.GraphQLObjectType.newObject;


public class ObjectBuilder {
    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private BreadthFirstSearch breadthFirstSearch;
    private ParentalSearch parentalSearch;
    private GraphQLFieldRetriever graphQLFieldRetriever;
    private InterfaceBuilder interfaceBuilder;
    private GraphQLInterfaceRetriever graphQLInterfaceRetriever;

    public ObjectBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, ParentalSearch parentalSearch, BreadthFirstSearch breadthFirstSearch,GraphQLFieldRetriever graphQLFieldRetriever,InterfaceBuilder interfaceBuilder,GraphQLInterfaceRetriever graphQLInterfaceRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.breadthFirstSearch=breadthFirstSearch;
        this.parentalSearch=parentalSearch;
        this.graphQLFieldRetriever=graphQLFieldRetriever;
        this.interfaceBuilder=interfaceBuilder;
        this.graphQLInterfaceRetriever=graphQLInterfaceRetriever;
    }

    public GraphQLObjectType.Builder getObjectBuilder(Class<?> object, ProcessingElementsContainer container, GraphQLOutputObjectRetriever outputObjectRetriever) throws GraphQLAnnotationsException {
        GraphQLObjectType.Builder builder = newObject();
        builder.name(graphQLObjectInfoRetriever.getTypeName(object));
        GraphQLDescription description = object.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        List<String> fieldsDefined = new ArrayList<>();
        for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(object)) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (breadthFirstSearch.isFound(method)) {
                GraphQLFieldDefinition gqlField = graphQLFieldRetriever.getField(method,container);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Field field : getAllFields(object).values()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (parentalSearch.isFound(field)) {
                GraphQLFieldDefinition gqlField = graphQLFieldRetriever.getField(field,container);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Class<?> iface : object.getInterfaces()) {
            if (iface.getAnnotation(GraphQLTypeResolver.class) != null) {
                String ifaceName = graphQLObjectInfoRetriever.getTypeName(iface);
                if (container.getProcessing().contains(ifaceName)) {
                    builder.withInterface(new GraphQLTypeReference(ifaceName));
                } else {
                    builder.withInterface((GraphQLInterfaceType) graphQLInterfaceRetriever.getInterface(iface,container));
                }
                builder.fields(graphQLFieldRetriever.getExtensionFields(iface, fieldsDefined,container));
            }
        }

        builder.fields(graphQLFieldRetriever.getExtensionFields(object, fieldsDefined,container));

        return builder;
    }


}
