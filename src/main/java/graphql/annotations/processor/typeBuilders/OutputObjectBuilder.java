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
package graphql.annotations.processor.typeBuilders;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.GraphQLInterfaceRetriever;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static graphql.annotations.processor.util.ObjectUtil.getAllFields;
import static graphql.schema.GraphQLObjectType.newObject;


public class OutputObjectBuilder {
    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private BreadthFirstSearch breadthFirstSearch;
    private ParentalSearch parentalSearch;
    private GraphQLFieldRetriever graphQLFieldRetriever;
    private GraphQLInterfaceRetriever graphQLInterfaceRetriever;

    public OutputObjectBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, ParentalSearch parentalSearch, BreadthFirstSearch breadthFirstSearch, GraphQLFieldRetriever graphQLFieldRetriever, GraphQLInterfaceRetriever graphQLInterfaceRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.breadthFirstSearch=breadthFirstSearch;
        this.parentalSearch=parentalSearch;
        this.graphQLFieldRetriever=graphQLFieldRetriever;
        this.graphQLInterfaceRetriever=graphQLInterfaceRetriever;
    }

    /**
     * This will examine the object class and return a {@link GraphQLObjectType.Builder} ready for further definition
     *
     * @param object the object class to examine
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link GraphQLObjectType.Builder} that represents that object class
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */

    public GraphQLObjectType.Builder getOutputObjectBuilder(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLObjectType.Builder builder = newObject();
        builder.name(graphQLObjectInfoRetriever.getTypeName(object));
        GraphQLDescription description = object.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        List<String> definedFields = new ArrayList<>();
        for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(object)) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (breadthFirstSearch.isFound(method)) {
                GraphQLFieldDefinition gqlField = graphQLFieldRetriever.getField(method,container);
                definedFields.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Field field : getAllFields(object).values()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (parentalSearch.isFound(field)) {
                GraphQLFieldDefinition gqlField = graphQLFieldRetriever.getField(field,container);
                definedFields.add(gqlField.getName());
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
                builder.fields(graphQLFieldRetriever.getExtensionFields(iface, definedFields,container));
            }
        }

        builder.fields(graphQLFieldRetriever.getExtensionFields(object, definedFields,container));

        return builder;
    }


}
