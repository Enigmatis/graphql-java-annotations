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
package graphql.annotations.directives;

import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.introspection.Introspection;
import graphql.schema.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DirectiveWirer {
    @FunctionalInterface
    interface WiringFunction {
        GraphQLDirectiveContainer apply(GraphQLDirective a, GraphQLDirectiveContainer b,
                                        AnnotationsDirectiveWiring wiring, GraphQLCodeRegistry.Builder codeRegistryBuilder, String parentName)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
    }

    private Map<Class, WiringFunction> functionMap;

    public DirectiveWirer() {
        functionMap = createFunctionsMap();
    }

    private void putInMap(Map<Class, WiringFunction> map, Class clazz, String functionName,
                          Introspection.DirectiveLocation... locations) {
        map.put(clazz, (d, e, wiring, codeRegistryBuilder, parentName) -> {
            assertLocation(d, e, locations);
            AnnotationsWiringEnvironmentImpl environment =
                    new AnnotationsWiringEnvironmentImpl(e, e.getDirective(d.getName()), parentName, codeRegistryBuilder);
            return (GraphQLDirectiveContainer) wiring.getClass().getMethod(functionName, AnnotationsWiringEnvironment.class)
                    .invoke(wiring, environment);
        });
    }

    private Map<Class, WiringFunction> createFunctionsMap() {
        Map<Class, WiringFunction> functionMap = new HashMap<>();
        putInMap(functionMap, GraphQLFieldDefinition.class, "onField", Introspection.DirectiveLocation.FIELD, Introspection.DirectiveLocation.FIELD_DEFINITION);
        putInMap(functionMap, GraphQLObjectType.class, "onObject", Introspection.DirectiveLocation.OBJECT);
        putInMap(functionMap, GraphQLArgument.class, "onArgument", Introspection.DirectiveLocation.ARGUMENT_DEFINITION);
        putInMap(functionMap, GraphQLInterfaceType.class, "onInterface", Introspection.DirectiveLocation.INTERFACE);
        putInMap(functionMap, GraphQLUnionType.class, "onUnion", Introspection.DirectiveLocation.UNION);
        putInMap(functionMap, GraphQLEnumType.class, "onEnum", Introspection.DirectiveLocation.ENUM);
        putInMap(functionMap, GraphQLEnumValueDefinition.class, "onEnumValue", Introspection.DirectiveLocation.ENUM_VALUE);
        putInMap(functionMap, GraphQLScalarType.class, "onScalar", Introspection.DirectiveLocation.SCALAR);
        putInMap(functionMap, GraphQLInputObjectType.class, "onInputObjectType", Introspection.DirectiveLocation.INPUT_OBJECT);
        putInMap(functionMap, GraphQLInputObjectField.class, "onInputObjectField", Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION);

        return functionMap;
    }

    public GraphQLDirectiveContainer wire(GraphQLDirectiveContainer element, HashMap<GraphQLDirective, AnnotationsDirectiveWiring> directiveWiringMap
            , GraphQLCodeRegistry.Builder codeRegistryBuilder, String parentName) {
        for (Map.Entry<GraphQLDirective, AnnotationsDirectiveWiring> entry : directiveWiringMap.entrySet()) {
            GraphQLDirective graphQLDirective = entry.getKey();
            AnnotationsDirectiveWiring wiring = entry.getValue();

            Class<? extends GraphQLDirectiveContainer> aClass = element.getClass();
            try {
                element = functionMap.get(aClass).apply(graphQLDirective, element, wiring, codeRegistryBuilder, parentName);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new GraphQLAnnotationsException(e.getMessage(), e);
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
