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

import graphql.annotations.annotationTypes.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.annotations.directives.creation.DirectiveAnnotation;
import graphql.annotations.directives.creation.DirectiveLocations;
import graphql.annotations.processor.DirectiveAndWiring;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;

import static graphql.schema.GraphQLDirective.newDirective;

public class DirectiveCreator {

    public static final String NO_VALID_LOCATIONS_DEFINED_ON_DIRECTIVE = "No valid locations defined on directive";
    private DirectiveArgumentCreator directiveArgumentCreator;
    private CommonPropertiesCreator commonPropertiesCreator;


    public DirectiveCreator(DirectiveArgumentCreator directiveArgumentCreator, CommonPropertiesCreator commonPropertiesCreator) {
        this.directiveArgumentCreator = directiveArgumentCreator;
        this.commonPropertiesCreator = commonPropertiesCreator;
    }

    public GraphQLDirective getDirective(Class<?> annotatedClass) {
        GraphQLDirective.Builder builder = newDirective()
                .name(commonPropertiesCreator.getName(annotatedClass))
                .description(commonPropertiesCreator.getDescription(annotatedClass));
        Introspection.DirectiveLocation[] validLocations = getValidLocations(annotatedClass);
        if (validLocations == null || validLocations.length == 0) {
            throw new GraphQLAnnotationsException(NO_VALID_LOCATIONS_DEFINED_ON_DIRECTIVE, null);
        }
        builder.validLocations(validLocations);
        buildArguments(builder, annotatedClass);

        return builder.build();
    }

    public DirectiveAndWiring getDirective(Method directiveMethod){
        GraphQLDirective.Builder builder = newDirective()
                .name(commonPropertiesCreator.getName(directiveMethod))
                .description(commonPropertiesCreator.getDescription(directiveMethod));
        Introspection.DirectiveLocation[] validLocations = getValidLocations(directiveMethod);
        if (validLocations == null || validLocations.length == 0) {
            throw new GraphQLAnnotationsException(NO_VALID_LOCATIONS_DEFINED_ON_DIRECTIVE, null);
        }

        builder.validLocations(validLocations);
        buildArguments(builder, directiveMethod);

        GraphQLDirective builtDirective = builder.build();
        Class<? extends AnnotationsDirectiveWiring> wiringClass = directiveMethod.getAnnotation(GraphQLDirectiveDefinition.class).wiring();

        return new DirectiveAndWiring(builtDirective, wiringClass);
    }

    private void buildArguments(GraphQLDirective.Builder builder, Class<?> annotatedClass) {
        if (annotatedClass.isAnnotationPresent(DirectiveAnnotation.class)){
            Arrays.stream(annotatedClass.getDeclaredMethods())
                    .filter(method -> !method.isSynthetic())
                    .forEach(method -> builder.argument(directiveArgumentCreator.getArgument(method)));

        }
        else {
            Arrays.stream(annotatedClass.getDeclaredFields())
                    .filter(field -> !field.isSynthetic())
                    .forEach(field -> builder.argument(directiveArgumentCreator.getArgument(field, annotatedClass)));
        }
    }

    private void buildArguments(GraphQLDirective.Builder builder, Method directiveMethod) {
        Arrays.stream(directiveMethod.getParameters())
                .filter(parameter -> !parameter.isSynthetic())
                .forEach(parameter -> builder.argument(directiveArgumentCreator.getArgument(parameter)));
    }

    private Introspection.DirectiveLocation[] getValidLocations(AnnotatedElement annotatedElement) {
        DirectiveLocations directiveLocationsAnnotation = annotatedElement.getAnnotation(DirectiveLocations.class);
        if (directiveLocationsAnnotation != null) {
            return directiveLocationsAnnotation.value();
        }
        return null;
    }
}
