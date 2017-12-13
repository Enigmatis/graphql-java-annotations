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
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLFieldRetriever;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.processor.searchAlgorithms.ParentalSearch;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static graphql.annotations.processor.util.InputPropertiesUtil.DEFAULT_INPUT_PREFIX;
import static graphql.annotations.processor.util.ObjectUtil.getAllFields;

public class InputObjectBuilder {
    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private BreadthFirstSearch breadthFirstSearch;
    private ParentalSearch parentalSearch;
    private GraphQLFieldRetriever graphQLFieldRetriever;

    public InputObjectBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, ParentalSearch parentalSearch, BreadthFirstSearch breadthFirstSearch, GraphQLFieldRetriever graphQLFieldRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.breadthFirstSearch=breadthFirstSearch;
        this.parentalSearch=parentalSearch;
        this.graphQLFieldRetriever=graphQLFieldRetriever;
    }

    /**
     * This will examine the object class and return a {@link graphql.schema.GraphQLInputObjectType.Builder} ready for further definition
     *
     * @param object the object class to examine
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link GraphQLInputObjectType.Builder} that represents that object class
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */

    public GraphQLInputObjectType.Builder getInputObjectBuilder(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException {
        GraphQLInputObjectType.Builder builder = GraphQLInputObjectType.newInputObject();
        builder.name(DEFAULT_INPUT_PREFIX + graphQLObjectInfoRetriever.getTypeName(object));
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
                GraphQLInputObjectField gqlField = graphQLFieldRetriever.getInputField(method,container);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }

        for (Field field : getAllFields(object).values()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (parentalSearch.isFound(field)) {
                GraphQLInputObjectField gqlField = graphQLFieldRetriever.getInputField(field,container);
                fieldsDefined.add(gqlField.getName());
                builder.field(gqlField);
            }
        }
        return builder;
    }

}
