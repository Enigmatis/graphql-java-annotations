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
package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;

import java.lang.reflect.Field;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;

public class FieldNameBuilder implements Builder<String> {
    private Field field;

    public FieldNameBuilder(Field field) {
        this.field = field;
    }

    @Override
    public String build() {
        GraphQLName name = field.getAnnotation(GraphQLName.class);
        return toGraphqlName(name == null ? field.getName() : name.value());
    }
}
