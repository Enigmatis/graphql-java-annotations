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
package graphql.annotations.processor.retrievers.fieldBuilders;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLInputObjectRetriever;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

public class ArgumentBuilder implements Builder<List<GraphQLArgument>> {
    private static final String DEFAULT_INPUT_PREFIX = "Input";

    private Method method;
    private TypeFunction typeFunction;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;
    private GraphQLFieldDefinition.Builder builder;
    private ProcessingElementsContainer container;
    private GraphQLOutputType outputType;

    public ArgumentBuilder(Method method, TypeFunction typeFunction, GraphQLInputObjectRetriever graphQLInputObjectRetriever, GraphQLFieldDefinition.Builder builder, ProcessingElementsContainer container, GraphQLOutputType outputType) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.graphQLInputObjectRetriever = graphQLInputObjectRetriever;
        this.builder = builder;
        this.container = container;
        this.outputType = outputType;
    }

    @Override
    public List<GraphQLArgument> build() {
        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.stream(method.getParameters()).
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLInputType graphQLType = (GraphQLInputType) finalTypeFunction.buildType(true, t, parameter.getAnnotatedType(), container);
                    return getArgument(parameter, graphQLType);
                }).collect(Collectors.toList());

        return args;
    }

    private GraphQLArgument getArgument(Parameter parameter, graphql.schema.GraphQLInputType inputType) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder argumentBuilder = newArgument().type(inputType);
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            argumentBuilder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            argumentBuilder.defaultValue(newInstance(defaultValue.value()).get());
        }
        GraphQLName name = parameter.getAnnotation(GraphQLName.class);
        if (name != null) {
            argumentBuilder.name(toGraphqlName(name.value()));
        } else {
            argumentBuilder.name(toGraphqlName(parameter.getName()));
        }
        return argumentBuilder.build();
    }

}
