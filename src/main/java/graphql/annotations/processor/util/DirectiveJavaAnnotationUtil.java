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
package graphql.annotations.processor.util;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.stream.Stream;

public class DirectiveJavaAnnotationUtil {

    public static Stream<Annotation> getDirectiveAnnotations(AnnotatedElement annotatedElement) {
        if (annotatedElement==null){
            throw new NullPointerException("supplied element is null");
        }
        return Arrays.stream(annotatedElement.getDeclaredAnnotations()).filter(annotation -> annotation.annotationType().isAnnotationPresent(GraphQLDirectiveDefinition.class));
    }

    public static String getName(Annotation annotation){
        if (annotation==null){
            throw new NullPointerException("supplied annotation is null");
        }
        if (annotation.annotationType().isAnnotationPresent(GraphQLName.class)) {
            return annotation.annotationType().getAnnotation(GraphQLName.class).value();
        } else {
            return annotation.annotationType().getSimpleName();
        }
    }
}
