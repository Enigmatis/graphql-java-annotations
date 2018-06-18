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

import graphql.introspection.Introspection;
import graphql.schema.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectiveWirer {
    public GraphQLDirectiveContainer wire(GraphQLDirectiveContainer element, HashMap<GraphQLDirective, AnnotationsDirectiveWiring> directiveWiringMap) {
        for (Map.Entry<GraphQLDirective, AnnotationsDirectiveWiring> entry : directiveWiringMap.entrySet()) {
            System.out.println(entry.getKey());
            GraphQLDirective graphQLDirective = entry.getKey();
            AnnotationsDirectiveWiring wiring = entry.getValue();

            if (element instanceof GraphQLFieldDefinition) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
                element = wiring.onField(new AnnotationsWiringEnvironmentImpl<>((GraphQLFieldDefinition) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLObjectType) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.OBJECT);
                element = wiring
                        .onObject(new AnnotationsWiringEnvironmentImpl<>((GraphQLObjectType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLArgument) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
                element = wiring
                        .onArgument(new AnnotationsWiringEnvironmentImpl<>((GraphQLArgument) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLInterfaceType) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.INTERFACE);
                element = wiring
                        .onInterface(new AnnotationsWiringEnvironmentImpl<>((GraphQLInterfaceType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLUnionType) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.UNION);
                element = wiring
                        .onUnion(new AnnotationsWiringEnvironmentImpl<>((GraphQLUnionType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLEnumType) { // todo support enum
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.ENUM);
                element = wiring
                        .onEnum(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLEnumValueDefinition) { // todo support enum value
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.ENUM_VALUE);
                element = wiring
                        .onEnumValue(new AnnotationsWiringEnvironmentImpl<>((GraphQLEnumValueDefinition) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLScalarType) { // todo: support scalars
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.SCALAR);
                element = wiring
                        .onScalar(new AnnotationsWiringEnvironmentImpl<>((GraphQLScalarType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLInputObjectType) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.INPUT_OBJECT);
                element = wiring
                        .onInputObjectType(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectType) element, element.getDirective(graphQLDirective.getName())));
            } else if (element instanceof GraphQLInputObjectField) {
                assertLocation(graphQLDirective, element, Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION);
                element = wiring
                        .onInputObjectField(new AnnotationsWiringEnvironmentImpl<>((GraphQLInputObjectField) element, element.getDirective(graphQLDirective.getName())));
            }
        }
        return element;

    }

    private void assertLocation(GraphQLDirective graphQLDirective, GraphQLDirectiveContainer element, Introspection.DirectiveLocation... validLocations) {
        boolean isSupported = false;
        for (Introspection.DirectiveLocation validLocation : validLocations) {
            if (graphQLDirective.validLocations().contains(validLocation)) {
                isSupported = true;
            }
        }
        if (!isSupported) {
            throw getInvalidDirectiveLocationException(element, graphQLDirective, validLocations);
        }
    }

    private InvalidDirectiveLocationException getInvalidDirectiveLocationException(GraphQLDirectiveContainer element, GraphQLDirective graphQLDirective, Introspection.DirectiveLocation... validLocations) {
        return new InvalidDirectiveLocationException("The element: '" + element.getName() + "' is annotated with the directive: '"
                + graphQLDirective.getName() + "' which is not valid on the element location: '" + Arrays.toString(Arrays.stream(validLocations).map(Enum::name).toArray()) + "'", null);
    }

}
