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

import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedType;

/**
 * A GraphQLType builder for java types.
 */
public interface TypeFunction {
    /**
     * Get the graphql type name that will be used to build the type.
     * The type name is passed to the {@link #buildType} method when building the type.
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @return The graphql type name
     */
    default String getTypeName(Class<?> aClass, AnnotatedType annotatedType) {
        return null;
    }

    /**
     * Get whether this builder handles the given type.
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @return True if this builder can build the type
     */
    boolean canBuildType(Class<?> aClass, AnnotatedType annotatedType);

    /**
     * Build a {@link GraphQLType} object from a java type.
     * This is a convenience method for calling {@link #buildType(boolean, Class, AnnotatedType)} without a type name.
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @return The built {@link GraphQLType}
     */
    default GraphQLType buildType(Class<?> aClass, AnnotatedType annotatedType) {
        return buildType(false, aClass, annotatedType);
    }

    /**
     * Build a {@link GraphQLType} object from a java type.
     * @param input is InputType
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @return The built {@link GraphQLType}
     */
    GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType);
}
