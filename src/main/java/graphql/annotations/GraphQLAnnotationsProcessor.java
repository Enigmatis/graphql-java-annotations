/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.schema.*;
import graphql.schema.GraphQLType;

public interface GraphQLAnnotationsProcessor {
    /**
     * @deprecated See {@link #getOutputType(Class)}
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
     * This will examine the object class and return a {@link GraphQLEnumType.Builder} ready for further definition
     *
     * @param object the object class to examine
     *
     * @return a {@link GraphQLEnumType.Builder} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    GraphQLEnumType.Builder getEnumBuilder(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * @deprecated See {@link #getOutputType(Class)}
     */
    GraphQLObjectType getObject(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * This will examine the object and will return a {@link GraphQLOutputType} based on the class type and annotations.
     * - If its annotated with {@link GraphQLUnion} it will return a {@link GraphQLUnionType}
     * - If its annotated with {@link GraphQLTypeResolver} it will return a {@link GraphQLInterfaceType}
     * - It it's an Enum it will return a {@link GraphQLEnumType},
     * otherwise it will return a {@link GraphQLObjectType}.
     *
     * @param object the object class to examine
     *
     * @return a {@link GraphQLOutputType} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    GraphQLOutputType getOutputType(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * @deprecated See {@link #getOutputTypeOrRef(Class)}
     */
    GraphQLOutputType getObjectOrRef(Class<?> object) throws GraphQLAnnotationsException;

    /**
     * This will examine the object class and return a {@link GraphQLOutputType} representation
     * which may be a {@link GraphQLOutputType} or a {@link graphql.schema.GraphQLTypeReference}
     *
     * @param object the object class to examine
     *
     * @return a {@link GraphQLOutputType} that represents that object class
     *
     * @throws GraphQLAnnotationsException if the object class cannot be examined
     */
    GraphQLOutputType getOutputTypeOrRef(Class<?> object) throws GraphQLAnnotationsException;

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
     * @param newNamePrefix since graphql types MUST be unique, this prefix will be applied to the new input types
     *
     * @return a {@link GraphQLInputObjectType}
     */
    GraphQLInputType getInputObject(GraphQLType graphQLType, String newNamePrefix);

    /**
     * Register a new type extension class. This extension will be used when the extended object will be created.
     * The class must have a {@link GraphQLTypeExtension} annotation.
     *
     * @param objectClass The extension class to register
     */
    void registerTypeExtension(Class<?> objectClass);

    /**
     * Unregister a type extension class.
     *
     * @param objectClass The extension class to unregister
     */
    void unregisterTypeExtension(Class<?> objectClass);

}
