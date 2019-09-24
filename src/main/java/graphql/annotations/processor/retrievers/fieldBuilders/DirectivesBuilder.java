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

import graphql.annotations.annotationTypes.directives.activation.GraphQLDirectives;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.util.DirectiveJavaAnnotationUtil;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

    @Override
    public GraphQLDirective[] build() {
        // building directives from directives java annotations
        List<GraphQLDirective> graphQLDirectives = new ArrayList<>();
        DirectiveJavaAnnotationUtil.getDirectiveAnnotations(object)
                .forEach(annotation -> {
                    String name = DirectiveJavaAnnotationUtil.getName(annotation);
                    if (container.getDirectiveRegistry().containsKey(name)) {
                        GraphQLDirective graphQLDirective = transformArgs(container.getDirectiveRegistry().get(name).getDirective(), annotation);
                        graphQLDirectives.add(graphQLDirective);
                    } else {
                        throw new GraphQLAnnotationsException(String.format("No directive named %s is found in the directive registry", name), null);
                    }
                });

        // building directives from graphql-java-annotations directive annotation
        GraphQLDirectives directives = object.getAnnotation(GraphQLDirectives.class);
        if (directives != null) {
            List<GraphQLDirective> oldGraphQLDirectives = Arrays.stream(directives.value())
                    .map(x -> {
                                if (container.getDirectiveRegistry().containsKey(x.name())) {
                                    return transformArgs(container.getDirectiveRegistry().get(x.name()).getDirective(), x.argumentsValues());
                                } else {
                                    throw new GraphQLAnnotationsException(String.format("No directive named %s is found in the directive registry", x.name()), null);
                                }
                            }
                    ).collect(Collectors.toList());
            graphQLDirectives.addAll(oldGraphQLDirectives);
        }

        return graphQLDirectives.toArray(new GraphQLDirective[graphQLDirectives.size()]);
    }


    private GraphQLDirective transformArgs(GraphQLDirective graphQLDirective, Annotation annotation) {
        GraphQLDirective.Builder directiveBuilder = newDirective(graphQLDirective);
        directiveBuilder.clearArguments();

        List<GraphQLArgument> arguments = graphQLDirective.getArguments();

        if (annotation.annotationType().getDeclaredMethods().length > arguments.size()) {
            throw new GraphQLAnnotationsException(String.format("Directive '%s' is supplied with more argument values than it supports", graphQLDirective.getName()), null);
        }

        for (int i = 0; i < annotation.annotationType().getDeclaredMethods().length; i++) {
            transformArgument(annotation, directiveBuilder, arguments, i);
        }

        for (int i = annotation.annotationType().getDeclaredMethods().length; i < arguments.size(); i++) {
            int finalI = i;
            directiveBuilder.argument(arguments.get(i).transform(builder -> builder.value(arguments.get(finalI).getDefaultValue())));
        }
        return directiveBuilder.build();
    }

    private GraphQLDirective transformArgs(GraphQLDirective graphQLDirective, String[] argumentValues) {
        GraphQLDirective.Builder directiveBuilder = newDirective(graphQLDirective);
        directiveBuilder.clearArguments();

        List<GraphQLArgument> arguments = graphQLDirective.getArguments();

        if (argumentValues.length > arguments.size()) {
            throw new GraphQLAnnotationsException(String.format("Directive '%s' is supplied with more argument values than it supports", graphQLDirective.getName()), null);
        }

        for (int i = 0; i < argumentValues.length; i++) {
            transformArgument(argumentValues, directiveBuilder, arguments, i);
        }

        for (int i = argumentValues.length; i < arguments.size(); i++) {
            int finalI = i;
            directiveBuilder.argument(arguments.get(i).transform(builder -> builder.value(arguments.get(finalI).getDefaultValue())));
        }
        return directiveBuilder.build();
    }

    private void transformArgument(Annotation annotation, GraphQLDirective.Builder directiveBuilder, List<GraphQLArgument> arguments, int i) {
        int finalI = i;
        GraphQLArgument graphQLArgument = arguments.get(i);
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        directiveBuilder.argument(graphQLArgument.transform(builder -> {
            // todo: add support for list
            if (graphQLArgument.getType() instanceof GraphQLType) {
                try {
                    methods[finalI].setAccessible(true);
                    Object argumentValue = methods[finalI].invoke(annotation);
                    Object value;
                    if (graphQLArgument.getType() instanceof GraphQLScalarType) {
                        value = ((GraphQLScalarType) graphQLArgument.getType()).getCoercing().parseValue(argumentValue);
                    }
                    else{
                        value = argumentValue;
                    }
                    builder.value(value);
                } catch (Exception e) {
                    throw new GraphQLAnnotationsException("Could not parse argument value to argument type", e);
                }
            } else {
                throw new GraphQLAnnotationsException("Directive argument type must be a scalar!", null);
            }
        }));
    }


    private void transformArgument(String[] argumentValues, GraphQLDirective.Builder directiveBuilder, List<GraphQLArgument> arguments, int i) {
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
}
