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

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;

import java.lang.reflect.Field;

import static graphql.schema.GraphQLArgument.newArgument;

public class DirectiveArgumentCreator {
    private CommonPropertiesCreator commonPropertiesCreator;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public DirectiveArgumentCreator(CommonPropertiesCreator commonPropertiesCreator, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.commonPropertiesCreator = commonPropertiesCreator;
        this.typeFunction = typeFunction;
        this.container = container;
    }


    public GraphQLArgument getArgument(Field field, Class<?> containingClass) {
        GraphQLArgument.Builder builder = newArgument();
        builder.name(commonPropertiesCreator.getName(field));
        builder.description(commonPropertiesCreator.getDescription(field));
        builder.type(getType(field));
        try {
            builder.defaultValue(getDefaultValue(field, containingClass));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new GraphQLAnnotationsException(e);
        }

        return builder.build();
    }

    private Object getDefaultValue(Field field, Class<?> containingClass) throws IllegalAccessException, InstantiationException {
        field.setAccessible(true);
        Object object = containingClass.newInstance();
        return field.get(object);
    }

    private GraphQLInputType getType(Field field) {
        return (GraphQLInputType) typeFunction.buildType(true, field.getType(),
                field.getAnnotatedType(), container);
    }


}
