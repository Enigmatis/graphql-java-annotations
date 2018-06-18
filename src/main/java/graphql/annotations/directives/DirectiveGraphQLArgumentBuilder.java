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
package graphql.annotations.directives;

import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputType;

import static graphql.schema.GraphQLArgument.newArgument;

public class DirectiveGraphQLArgumentBuilder implements Builder<GraphQLArgument> {
    private DirectiveArgument directiveArgument;
    private String argumentValue;
    private TypeFunction typeFunction;
    private ProcessingElementsContainer container;

    public DirectiveGraphQLArgumentBuilder(DirectiveArgument directiveArgument, String argumentValue, TypeFunction typeFunction, ProcessingElementsContainer container) {
        this.directiveArgument = directiveArgument;
        this.argumentValue = argumentValue;
        this.typeFunction = typeFunction;
        this.container = container;
    }

    @Override
    public GraphQLArgument build() {
        TypeFunction finalTypeFunction = typeFunction;
        Class<?> t = directiveArgument.getType();
        GraphQLInputType graphQLInputType = (GraphQLInputType) finalTypeFunction.buildType(true, t, null, container);
        return getArgument(directiveArgument, graphQLInputType);
    }

    private GraphQLArgument getArgument(DirectiveArgument directiveArgument, GraphQLInputType inputType) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder argumentBuilder = newArgument().type(inputType);
        argumentBuilder.description(directiveArgument.getDescription());

        if (directiveArgument.getDefaultValue() != null) {
            try {
                argumentBuilder.defaultValue(toObject(directiveArgument.getType(), directiveArgument.getDefaultValue()));
            } catch (Exception e) {
                throw new GraphQLAnnotationsException(String.format("The directive argument '%s' default value is of type '%s', but provided with '%s'", directiveArgument.getName(),
                        directiveArgument.getType(), directiveArgument.getDefaultValue()), e);
            }
        }

        argumentBuilder.name(directiveArgument.getName());
        
        if (argumentValue == null && directiveArgument.getDefaultValue() != null) {
            argumentValue = directiveArgument.getDefaultValue();
        } else if (argumentValue == null) {
            throw new GraphQLAnnotationsException(String.format("The directive argument '%s' is not supplied with a value nor a default value", directiveArgument.getName()), null);
        }

        try {
            argumentBuilder.value(toObject(directiveArgument.getType(), argumentValue));
        } catch (Exception e) {
            throw new GraphQLAnnotationsException(String.format("The directive '%s' value is of type '%s', but provided with '%s'", directiveArgument.getName(),
                    directiveArgument.getType(), argumentValue), e);
        }

        return argumentBuilder.build();
    }

    private Object toObject(Class clazz, String value) {
        if (Boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz) return Double.parseDouble(value);
        return value;
    }


}
