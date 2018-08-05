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

import graphql.TypeResolutionEnvironment;
import graphql.introspection.Introspection;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLDirective.newDirective;
import static org.mockito.Mockito.*;

public class DirectiveWirerTest {

    private DirectiveWirer directiveWirer;

    @BeforeMethod
    public void setUp() throws Exception {
        directiveWirer = new DirectiveWirer();
    }

    // GraphQLFieldDefinition

    @Test
    public void wireFieldDefinition_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);
        AnnotationsDirectiveWiring lowerWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLFieldDefinition directiveContainer = GraphQLFieldDefinition.newFieldDefinition().name("bla")
                .type(GraphQLString).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));
        AnnotationsWiringEnvironmentImpl lowerCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("lowerCase"));

        when(upperWiring.onField(upperCaseEnv)).thenReturn(directiveContainer);
        when(lowerWiring.onField(lowerCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.FIELD_DEFINITION).build();
        GraphQLDirective lowerCase = newDirective().name("lowerCase").validLocations(Introspection.DirectiveLocation.FIELD).build();
        map.put(upperCase, upperWiring);
        map.put(lowerCase, lowerWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onField(upperCaseEnv);
        verify(lowerWiring).onField(lowerCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireFieldDefinition_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLFieldDefinition directiveContainer = GraphQLFieldDefinition.newFieldDefinition().name("bla")
                .type(GraphQLString).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onField(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.ENUM).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLObjectType

    @Test
    public void wireGraphQLObjectType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLObjectType directiveContainer = GraphQLObjectType.newObject().name("asdf").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onObject(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.OBJECT).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onObject(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLObjectType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLObjectType directiveContainer = GraphQLObjectType.newObject().name("asdf00").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onObject(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLArgument

    @Test
    public void wireGraphQLArgument_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLArgument directiveContainer = GraphQLArgument.newArgument().name("asdf").type(GraphQLString).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onArgument(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.ARGUMENT_DEFINITION).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onArgument(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLArgument_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLArgument directiveContainer = GraphQLArgument.newArgument().name("asdf0").type(GraphQLString).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onArgument(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLInterfaceType

    @Test
    public void wireGraphQLInterfaceType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInterfaceType directiveContainer = GraphQLInterfaceType.newInterface().name("asdf").typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(TypeResolutionEnvironment env) {
                return null;
            }
        }).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInterface(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.INTERFACE).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onInterface(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLInterfaceType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInterfaceType directiveContainer = GraphQLInterfaceType.newInterface().name("asdf").typeResolver(new TypeResolver() {
            @Override
            public GraphQLObjectType getType(TypeResolutionEnvironment env) {
                return null;
            }
        }).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInterface(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLUnionType

    @Test
    public void wireGraphQLUnionType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLUnionType directiveContainer = GraphQLUnionType.newUnionType().name("asdf")
                .possibleType(GraphQLObjectType.newObject().name("Asdfaaaa").build()).typeResolver(env -> null).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onUnion(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.UNION).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onUnion(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLUnionType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLUnionType directiveContainer = GraphQLUnionType.newUnionType().name("asdf")
                .possibleType(GraphQLObjectType.newObject().name("Asdfaaaa").build()).typeResolver(env -> null).build();


        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onUnion(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLEnumType

    @Test
    public void wireGraphQLEnumType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLEnumType directiveContainer = GraphQLEnumType.newEnum().name("asdf").value("asdfasdf").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onEnum(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.ENUM).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onEnum(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLEnumType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLEnumType directiveContainer = GraphQLEnumType.newEnum().name("asdf").value("asdfasdf").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onEnum(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLEnumValueDefinition

    @Test
    public void wireGraphQLEnumValueDefinition_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLEnumValueDefinition directiveContainer = GraphQLEnumValueDefinition.newEnumValueDefinition().name("asdf").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onEnumValue(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.ENUM_VALUE).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onEnumValue(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLEnumValueDefinition_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLEnumValueDefinition directiveContainer = GraphQLEnumValueDefinition.newEnumValueDefinition().name("asdf").build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onEnumValue(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLScalarType

    @Test
    public void wireGraphQLScalarType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLScalarType directiveContainer = GraphQLString;

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onScalar(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.SCALAR).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onScalar(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLScalarType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLScalarType directiveContainer = GraphQLString;

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onScalar(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLInputObjectType

    @Test
    public void wireGraphQLInputObjectType_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInputObjectType directiveContainer = GraphQLInputObjectType.newInputObject().name("asdf")
                .build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInputObjectType(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.INPUT_OBJECT).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onInputObjectType(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLInputObjectType_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInputObjectType directiveContainer = GraphQLInputObjectType.newInputObject().name("asdf")
                .build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInputObjectType(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

    // GraphQLInputObjectField

    @Test
    public void wireGraphQLInputObjectField_validLocations_correctMethodIsCalled() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInputObjectField directiveContainer = GraphQLInputObjectField.newInputObjectField().name("asdf")
                .type(GraphQLInputObjectType.newInputObject().name("dfdf").build()).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInputObjectField(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").validLocations(Introspection.DirectiveLocation.INPUT_FIELD_DEFINITION).build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);

        // Assert

        verify(upperWiring).onInputObjectField(upperCaseEnv);
    }

    @Test(expectedExceptions = InvalidDirectiveLocationException.class)
    public void wireGraphQLInputObjectField_invalidLocations_exceptionIsThrown() throws Exception {
        // Arrange
        AnnotationsDirectiveWiring upperWiring = mock(AnnotationsDirectiveWiring.class);

        GraphQLInputObjectField directiveContainer = GraphQLInputObjectField.newInputObjectField().name("asdf")
                .type(GraphQLInputObjectType.newInputObject().name("dfdf").build()).build();

        AnnotationsWiringEnvironmentImpl upperCaseEnv = new AnnotationsWiringEnvironmentImpl(directiveContainer, directiveContainer.getDirective("upperCase"));

        when(upperWiring.onInputObjectField(upperCaseEnv)).thenReturn(directiveContainer);

        HashMap<GraphQLDirective, AnnotationsDirectiveWiring> map = new HashMap<>();
        GraphQLDirective upperCase = newDirective().name("upperCase").build();
        map.put(upperCase, upperWiring);

        // Act
        directiveWirer.wire(directiveContainer, map);
    }

}
