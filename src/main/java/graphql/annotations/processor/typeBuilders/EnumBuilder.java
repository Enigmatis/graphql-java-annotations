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
package graphql.annotations.processor.typeBuilders;

import graphql.annotations.annotations.GraphQLDescription;
import graphql.annotations.annotations.GraphQLName;
import graphql.annotations.processor.retrievers.GraphQLObjectInfoRetriever;
import graphql.schema.GraphQLEnumType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static graphql.schema.GraphQLEnumType.newEnum;

public class EnumBuilder {

    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;

    public EnumBuilder(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    /**
     * This will examine the object class and return a {@link GraphQLEnumType.Builder} ready for further definition
     *
     * @param aClass the object class to examine
     * @return a {@link GraphQLEnumType.Builder} that represents that object class
     */

    public GraphQLEnumType.Builder getEnumBuilder(Class<?> aClass) {
        String typeName = graphQLObjectInfoRetriever.getTypeName(aClass);
        //noinspection unchecked
        Class<? extends Enum> enumClass = (Class<? extends Enum>) aClass;
        GraphQLEnumType.Builder builder = newEnum();
        builder.name(typeName);

        GraphQLDescription description = aClass.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }

        List<Enum> constants = Arrays.asList(enumClass.getEnumConstants());

        Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).forEachOrdered(n -> {
            try {
                Field field = aClass.getField(n);
                GraphQLName fieldName = field.getAnnotation(GraphQLName.class);
                GraphQLDescription fieldDescription = field.getAnnotation(GraphQLDescription.class);
                Enum constant = constants.stream().filter(c -> c.name().contentEquals(n)).findFirst().get();
                String name_ = fieldName == null ? n : fieldName.value();
                builder.value(name_, constant, fieldDescription == null ? name_ : fieldDescription.value());
            } catch (NoSuchFieldException ignore) {
            }
        });
        return builder;
    }
}
