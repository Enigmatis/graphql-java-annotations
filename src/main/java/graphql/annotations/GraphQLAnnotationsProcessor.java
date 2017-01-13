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

import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLUnionType;

public interface GraphQLAnnotationsProcessor {
    /**
     * This will examine the class and if its annotated with {@link GraphQLUnion} it will return
     * a {@link GraphQLUnionType.Builder}, if its not annotated with {@link GraphQLTypeResolver} it will return
     * a {@link GraphQLObjectType} otherwise it will return a {@link GraphQLInterfaceType.Builder}.
     *
     * @param iface interface to examine
     *
     * @return a GraphQLType that represents that interface
     *
     * @throws GraphQLAnnotationsException if the interface cannot be examined
     * @throws IllegalArgumentException    if <code>iface</code> is not an interface
     */
    graphql.schema.GraphQLType getInterface(Class<?> iface) throws GraphQLAnnotationsException;

    /**
     * This will examine the class and return a {@link GraphQLUnionType.Builder} ready for further definition
     *
     * @param iface interface to examine
     *
     * @return a {@link GraphQLUnionType.Builder}
     *
     * @throws GraphQLAnnotationsException if the class cannot be examined
     * @throws IllegalArgumentException    if <code>iface</code> is not an interface
     */
    GraphQLUnionType.Builder getUnionBuilder(Class<?> iface) throws GraphQLAnnotationsException, IllegalArgumentException;

    /**
     * This will examine the class and return a {@link GraphQLInterfaceType.Builder} ready for further definition
     *
     * @param iface interface to examine
     *
     * @return a {@link GraphQLInterfaceType.Builder}
     *
     * @throws GraphQLAnnotationsException if the class cannot be examined
     * @throws IllegalArgumentException    if <code>iface</code> is not an interface
     */
    GraphQLInterfaceType.Builder getIfaceBuilder(Class<?> iface) throws GraphQLAnnotationsException, IllegalArgumentException;

    /**
     * This will examine the object class and return a {@link GraphQLObjectType} representation
     *
     * @param object the object class to examine
     *
     * @return a {@link GraphQLObjectType} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    GraphQLObjectType getObject(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * This will examine the object class and return a {@link GraphQLObjectType.Builder} ready for further definition
     *
     * @param object the object class to examine
     *
     * @return a {@link GraphQLObjectType.Builder} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    GraphQLObjectType.Builder getObjectBuilder(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * This will turn a {@link GraphQLObjectType} into a corresponding {@link GraphQLInputObjectType}
     *
     * @param graphQLType the graphql object type
     *
     * @return a {@link GraphQLInputObjectType}
     */
    GraphQLInputObjectType getInputObject(GraphQLObjectType graphQLType);
}
