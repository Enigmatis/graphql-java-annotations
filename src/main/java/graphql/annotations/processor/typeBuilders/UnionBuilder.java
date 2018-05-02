/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.processor.typeBuilders;


import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLType;
import graphql.annotations.annotationTypes.GraphQLUnion;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.annotations.typeResolvers.UnionTypeResolver;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLUnionType.Builder;
import graphql.schema.TypeResolver;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

import static graphql.annotations.processor.util.ReflectionKit.constructNewInstance;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLUnionType.newUnionType;

public class UnionBuilder {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;

    public UnionBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    /**
     * This will examine the class and return a {@link Builder} ready for further definition
     * @param container a class that hold several members that are required in order to build schema
     * @param iface interface to examine
     * @return a {@link Builder}
     * @throws GraphQLAnnotationsException if the class cannot be examined
     */

    public Builder getUnionBuilder(Class<?> iface, ProcessingElementsContainer container) throws GraphQLAnnotationsException, IllegalArgumentException {
        if (!iface.isInterface()) {
            throw new IllegalArgumentException(iface + " is not an interface");
        }
        Builder builder = newUnionType();

        GraphQLUnion unionAnnotation = iface.getAnnotation(GraphQLUnion.class);
        builder.name(graphQLObjectInfoRetriever.getTypeName(iface));
        GraphQLDescription description = iface.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLType typeAnnotation = iface.getAnnotation(GraphQLType.class);

        TypeFunction typeFunction = container.getDefaultTypeFunction();

        if (typeAnnotation != null) {
            typeFunction = newInstance(typeAnnotation.value());
        }

        TypeFunction finalTypeFunction = typeFunction;
        Arrays.stream(unionAnnotation.possibleTypes())
                .map(aClass -> finalTypeFunction.buildType(aClass, null, container))
                .map(v -> (GraphQLObjectType) v)
                .forEach(builder::possibleType);

        TypeResolver typeResolver = getTypeResolver(container, unionAnnotation);

        builder.typeResolver(typeResolver);
        return builder;
    }

    private TypeResolver getTypeResolver(ProcessingElementsContainer container, GraphQLUnion unionAnnotation) {
        Optional<Constructor<?>> typeResolverConstructorOptional = Arrays.stream(unionAnnotation.typeResolver().getConstructors())
                .filter(constructor -> constructor.getParameterCount() == 2 &&
                        container.getClass().isAssignableFrom(constructor.getParameterTypes()[1]) &&
                        Class[].class.isAssignableFrom(constructor.getParameterTypes()[0]))
                .findFirst();

        return typeResolverConstructorOptional
                .map(constructor -> (TypeResolver) constructNewInstance(constructor, unionAnnotation.possibleTypes(), container))
                .orElseGet(() -> new UnionTypeResolver(unionAnnotation.possibleTypes(), container));
    }
}
