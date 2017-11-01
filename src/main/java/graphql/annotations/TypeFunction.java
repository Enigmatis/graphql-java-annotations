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
package graphql.annotations;

import graphql.schema.GraphQLType;

import java.lang.reflect.AnnotatedType;
import java.util.Map;

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
     *
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @param container a class that hold several members that are required in order to build schema
     * @return The built
     */

    default GraphQLType buildType(Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container) {
        return buildType(false, aClass, annotatedType, container);
    }

    /**
     * Build a {@link GraphQLType} object from a java type.
     * @param input is InputType
     * @param aClass The java type to build the type name for
     * @param annotatedType The {@link AnnotatedType} of the java type, which may be a {link AnnotatedParameterizedType}
     * @return The built {@link GraphQLType}
     */

    GraphQLType buildType(boolean input, Class<?> aClass, AnnotatedType annotatedType, ProcessingElementsContainer container);
}
