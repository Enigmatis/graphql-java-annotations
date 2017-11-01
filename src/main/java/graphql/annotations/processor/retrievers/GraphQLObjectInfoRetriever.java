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
package graphql.annotations.processor.retrievers;


import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class GraphQLObjectInfoRetriever {

    public String getTypeName(Class<?> objectClass) {
        GraphQLName name = objectClass.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? objectClass.getSimpleName() : name.value());
    }

    public List<Method> getOrderedMethods(Class c) {
        return Arrays.stream(c.getMethods())
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toList());
    }

    public static Boolean isGraphQLField(AnnotatedElement element) {
        GraphQLField annotation = element.getAnnotation(GraphQLField.class);
        if (annotation == null) {
            return null;
        }
        return annotation.value();
    }


}
