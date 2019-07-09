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

import graphql.annotations.directives.creation.DirectiveLocations;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.GraphQLDirective;

import java.util.Arrays;

import static graphql.schema.GraphQLDirective.newDirective;

public class DirectiveCreator {

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
            throw new GraphQLAnnotationsException("No valid locations defined on directive", null);
        }
        builder.validLocations(validLocations);
        buildArguments(builder, annotatedClass);

        return builder.build();
    }

    private void buildArguments(GraphQLDirective.Builder builder, Class<?> annotatedClass) {
        Arrays.stream(annotatedClass.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .forEach(field -> builder.argument(directiveArgumentCreator.getArgument(field, annotatedClass)));
    }

    private Introspection.DirectiveLocation[] getValidLocations(Class<?> annotatedClass) {
        DirectiveLocations directiveLocationsAnnotation = annotatedClass.getAnnotation(DirectiveLocations.class);
        if (directiveLocationsAnnotation != null) {
            return directiveLocationsAnnotation.value();
        }
        return null;
    }
}
