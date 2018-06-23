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
package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLPrettify;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.Method;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class MethodNameBuilder implements Builder<String> {
    private Method method;

    public MethodNameBuilder(Method method) {
        this.method = method;
    }

    @Override
    public String build() {
        if (method.isAnnotationPresent(GraphQLPrettify.class) && !method.isAnnotationPresent(GraphQLName.class)) {
            return toGraphqlName(prettifyName(method.getName()));
        }
        GraphQLName name = method.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? method.getName() : name.value());
    }

    private String prettifyName(String originalName) {
        String name = originalName.replaceFirst("^(is|get|set)(.+)", "$2");
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
