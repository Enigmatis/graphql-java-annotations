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

import graphql.annotations.annotationTypes.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLScalarType;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLDirective.newDirective;

public class DirectivesBuilder implements Builder<GraphQLDirective[]> {
    private AnnotatedElement object;
    private ProcessingElementsContainer container;

    public DirectivesBuilder(AnnotatedElement object, ProcessingElementsContainer container) {
        this.object = object;
        this.container = container;
    }

    private GraphQLDirective transformArgs(GraphQLDirective graphQLDirective, String[] argumentValues) {
        GraphQLDirective.Builder directiveBuilder = newDirective(graphQLDirective);
        directiveBuilder.clearArguments();

        List<GraphQLArgument> arguments = graphQLDirective.getArguments();

        if (argumentValues.length > arguments.size()) {
            throw new GraphQLAnnotationsException(String.format("Directive '%s' is supplied with more argument values than it supports", graphQLDirective.getName()), null);
        }

        for (int i = 0; i < argumentValues.length; i++) {
            int finalI = i;
            GraphQLArgument graphQLArgument = arguments.get(i);

            directiveBuilder.argument(graphQLArgument.transform(builder -> {
                String argumentValue = argumentValues[finalI];
                if (graphQLArgument.getType() instanceof GraphQLScalarType) {

                    try {
                        Object value = ((GraphQLScalarType) graphQLArgument.getType()).getCoercing().parseValue(argumentValue);
                        builder.value(value);
                    } catch (Exception e) {
                        throw new GraphQLAnnotationsException("Could not parse argument value to argument type", e);
                    }
                } else {
                    throw new GraphQLAnnotationsException("Directive argument type must be a scalar!", null);
                }
            }));
        }

        for (int i = argumentValues.length; i < arguments.size(); i++) {
            int finalI = i;
            directiveBuilder.argument(arguments.get(i).transform(builder -> builder.value(arguments.get(finalI).getDefaultValue())));
        }
        return directiveBuilder.build();
    }

    @Override
    public GraphQLDirective[] build() {
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives == null) return new GraphQLDirective[]{};
        List<GraphQLDirective> graphQLDirectives = Arrays.stream(directives.value())
                .map(x -> {
                            if (container.getDirectiveRegistry().containsKey(x.name())) {
                                return transformArgs(container.getDirectiveRegistry().get(x.name()), x.argumentsValues());
                            } else {
                                throw new GraphQLAnnotationsException(String.format("No directive named %s is found in the directive registry", x.name()), null);
                            }
                        }
                ).collect(Collectors.toList());

        return graphQLDirectives.toArray(new GraphQLDirective[graphQLDirectives.size()]);
    }
}
