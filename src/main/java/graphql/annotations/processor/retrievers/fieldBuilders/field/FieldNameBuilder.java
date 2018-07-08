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
package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLPrettify;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.Field;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class FieldNameBuilder implements Builder<String> {
    private Field field;

    private boolean alwaysPrettify = false;

    public FieldNameBuilder(Field field) {
        this.field = field;
    }

    public FieldNameBuilder alwaysPrettify(boolean alwaysPrettify) {
        this.alwaysPrettify = alwaysPrettify;
        return this;
    }

    @Override
    public String build() {
        if ((alwaysPrettify || field.isAnnotationPresent(GraphQLPrettify.class)) && !field.isAnnotationPresent(GraphQLName.class)) {
            return toGraphqlName(prettifyName(field.getName()));
        }
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? field.getName() : name.value());
    }

    private String prettifyName(String originalName) {
        String name = originalName.replaceFirst("^(is|get|set)(.+)", "$2");
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

}
