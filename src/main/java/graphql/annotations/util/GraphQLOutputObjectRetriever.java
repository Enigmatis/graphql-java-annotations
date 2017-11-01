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
package graphql.annotations.util;

import graphql.annotations.*;
import graphql.annotations.builders.EnumBuilder;
import graphql.annotations.builders.InterfaceBuilder;
import graphql.annotations.builders.ObjectBuilder;
import graphql.annotations.builders.UnionBuilder;
import graphql.annotations.searchAlgorithms.BreadthFirstSearch;
import graphql.annotations.searchAlgorithms.CannotCastMemberException;
import graphql.annotations.searchAlgorithms.ParentalSearch;
import graphql.schema.*;

public class GraphQLOutputObjectRetriever {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private GraphQLFieldRetriever graphQLFieldRetriever;

    public GraphQLOutputObjectRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever, GraphQLFieldRetriever graphQLFieldRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
        this.graphQLFieldRetriever = graphQLFieldRetriever;
    }

    public GraphQLOutputObjectRetriever() {
        this(new GraphQLObjectInfoRetriever(), new GraphQLFieldRetriever());
    }

    /**
     * This will examine the object and will return a {@link GraphQLOutputType} based on the class type and annotations.
     * - If its annotated with {@link GraphQLUnion} it will return a {@link GraphQLUnionType}
     * - If its annotated with {@link GraphQLTypeResolver} it will return a {@link GraphQLInterfaceType}
     * - It it's an Enum it will return a {@link GraphQLEnumType},
     * otherwise it will return a {@link GraphQLObjectType}.
     *
     * @param object    the object class to examine*
     * @param container a class that hold several members that are required in order to build schema
     * @return a {@link GraphQLOutputType} that represents that object class
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     * @throws CannotCastMemberException   if the object class cannot be examined
     */
    public GraphQLOutputType getOutputType(Class<?> object, ProcessingElementsContainer container) throws GraphQLAnnotationsException, CannotCastMemberException {
        // because the TypeFunction can call back to this processor and
        // Java classes can be circular, we need to protect against
        // building the same type twice because graphql-java 3.x requires
        // all type instances to be unique singletons
        String typeName = graphQLObjectInfoRetriever.getTypeName(object);

        GraphQLOutputType type = (GraphQLOutputType) container.getTypeRegistry().get(typeName);
        if (type != null) { // type already exists, do not build a new new one
            return type;
        }

        container.getProcessing().push(typeName);
        if (object.getAnnotation(GraphQLUnion.class) != null) {
            type = new UnionBuilder(graphQLObjectInfoRetriever).getUnionBuilder(object, container).build();
        } else if (object.isAnnotationPresent(GraphQLTypeResolver.class)) {
            type = new InterfaceBuilder(graphQLObjectInfoRetriever, graphQLFieldRetriever).getInterfaceBuilder(object, container).build();
        } else if (Enum.class.isAssignableFrom(object)) {
            type = new EnumBuilder(graphQLObjectInfoRetriever).getEnumBuilder(object).build();
        } else {
            type = new ObjectBuilder(graphQLObjectInfoRetriever, new ParentalSearch(graphQLObjectInfoRetriever), new BreadthFirstSearch(graphQLObjectInfoRetriever), graphQLFieldRetriever, new GraphQLInterfaceRetriever()).getObjectBuilder(object, container).build();
        }

        container.getTypeRegistry().put(typeName, type);
        container.getProcessing().pop();

        return type;
    }

}
