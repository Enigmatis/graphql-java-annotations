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
package graphql.annotations.processor.directives;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

public class CommonPropertiesCreator {
    public String getDescription(AnnotatedElement annotatedElement) {
        GraphQLDescription graphQLDescriptionAnnotation = annotatedElement.getAnnotation(GraphQLDescription.class);
        if (graphQLDescriptionAnnotation != null) {
            return graphQLDescriptionAnnotation.value();
        }
        return null;
    }

    public String getName(AnnotatedElement annotatedElement) {
        GraphQLName graphQLNameAnnotation = annotatedElement.getAnnotation(GraphQLName.class);
        if (graphQLNameAnnotation != null) {
            return graphQLNameAnnotation.value();
        } else if (annotatedElement instanceof Class<?>) {
            return ((Class<?>) annotatedElement).getSimpleName();
        } else if (annotatedElement instanceof Field) {
            return ((Field) annotatedElement).getName();
        }
        return null;
    }
}
