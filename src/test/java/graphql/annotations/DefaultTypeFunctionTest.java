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
package graphql.annotations;

import graphql.schema.*;
import graphql.schema.GraphQLType;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.*;
import static org.testng.Assert.*;

public class DefaultTypeFunctionTest {

    private enum A {
        @GraphQLName("someA") @GraphQLDescription("a") A, B
    }


    @Test
    public void enumeration() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        GraphQLType enumeration = instance.buildType(A.class, null);
        assertTrue(enumeration instanceof GraphQLEnumType);
        List<GraphQLEnumValueDefinition> values = ((GraphQLEnumType) enumeration).getValues();
        assertEquals(values.stream().
                     map(GraphQLEnumValueDefinition::getName).collect(Collectors.toList()),
                     Arrays.asList("someA", "B"));
        assertEquals(values.stream().
                        map(GraphQLEnumValueDefinition::getDescription).collect(Collectors.toList()),
                Arrays.asList("a", "B"));

    }

    @Test
    public void string() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(String.class, null), GraphQLString);
    }

    @Test
    public void bool() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(boolean.class, null), GraphQLBoolean);
        assertEquals(instance.buildType(Boolean.class, null), GraphQLBoolean);
    }

    @Test
    public void float_() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(float.class, null), GraphQLFloat);
        assertEquals(instance.buildType(Float.class, null), GraphQLFloat);
        assertEquals(instance.buildType(Double.class, null), GraphQLFloat);
        assertEquals(instance.buildType(double.class, null), GraphQLFloat);
    }

    @Test
    public void integer() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(int.class, null), GraphQLInt);
        assertEquals(instance.buildType(Integer.class, null), GraphQLInt);
    }

    @Test
    public void long_() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        assertEquals(instance.buildType(long.class, null), GraphQLLong);
        assertEquals(instance.buildType(Long.class, null), GraphQLLong);
    }


    @SuppressWarnings("unused")
    public List<List<@GraphQLNonNull String>> listMethod() { return null;}

    @SuppressWarnings("unused")
    public Iterable<Iterable<@GraphQLNonNull String>> iterableMethod() { return null;}

    @SuppressWarnings("unused")
    public Collection<Collection<@GraphQLNonNull String>> collectionMethod() { return null;}


    @SuppressWarnings("unused")
    public Stream<List<@GraphQLNonNull String>> streamMethod() { return null;}

    @SuppressWarnings("unused")
    public Set<Set<@GraphQLNonNull String>> setMethod() { return null;}

    // GraphqlList(GraphqlList(GraphQlString) is expected here
    private void assertIsGraphListOfListOfString(GraphQLType type) {
        assertTrue(type instanceof GraphQLList);
        GraphQLList subtype = (GraphQLList) ((GraphQLList) type).getWrappedType();
        assertTrue(subtype.getWrappedType() instanceof graphql.schema.GraphQLNonNull);
        graphql.schema.GraphQLNonNull wrappedType = (graphql.schema.GraphQLNonNull) subtype.getWrappedType();
        assertEquals(wrappedType.getWrappedType(), GraphQLString);
    }

    @Test
    public void list() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("listMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType());
        assertIsGraphListOfListOfString(type);
    }


    @Test
    public void iterable() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("iterableMethod").getReturnType(), getClass().getMethod("iterableMethod").getAnnotatedReturnType());
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void collection() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("collectionMethod").getReturnType(), getClass().getMethod("collectionMethod").getAnnotatedReturnType());
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void set() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("setMethod").getReturnType(), getClass().getMethod("setMethod").getAnnotatedReturnType());
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void stream() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("streamMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType());
        assertIsGraphListOfListOfString(type);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparameterizedList() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        List v = new LinkedList();
        instance.buildType(v.getClass(), null);
    }

    @SuppressWarnings("unused")
    public Optional<List<@GraphQLNonNull String>> optionalMethod() { return Optional.empty();}

    @Test
    public void optional() throws NoSuchMethodException {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        graphql.schema.GraphQLType type = instance.buildType(getClass().getMethod("optionalMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType());
        assertTrue(type instanceof GraphQLList);
        GraphQLType subtype = ((GraphQLList) type).getWrappedType();
        assertTrue(subtype instanceof graphql.schema.GraphQLNonNull);
        GraphQLType wrappedType = (((graphql.schema.GraphQLNonNull) subtype).getWrappedType());
        assertEquals(wrappedType, GraphQLString);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparametrizedOptional() {
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Optional v = Optional.empty();
        instance.buildType(v.getClass(), null);
    }

    public static class Class1 {
        @GraphQLField
        public Class2 class2;
    }

    public static class Class2 {
        @GraphQLField
        public Class1 class1;
        @GraphQLField
        public Class2 class2;
    }

    @Test
    public void recursiveTypes() throws Exception {
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        DefaultTypeFunction instance = (DefaultTypeFunction) graphQLAnnotations.defaultTypeFunction;
        GraphQLType type = instance.buildType(Class1.class, Class2.class.getField("class1").getAnnotatedType());
        GraphQLFieldDefinition class1class2 = ((GraphQLObjectType) type).getFieldDefinition("class2");
        assertNotNull(class1class2);
        assertTrue(((GraphQLObjectType)class1class2.getType()).getFieldDefinition("class1").getType() instanceof GraphQLTypeReference);
        assertTrue(((GraphQLObjectType)class1class2.getType()).getFieldDefinition("class2").getType() instanceof GraphQLTypeReference);
        GraphQLAnnotations.instance = new GraphQLAnnotations();
    }

    private DefaultTypeFunction testedDefaultTypeFunction() {
        // wire up the ability
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        DefaultTypeFunction defaultTypeFunction = new DefaultTypeFunction();
        defaultTypeFunction.setAnnotationsProcessor(graphQLAnnotations);
        return defaultTypeFunction;
    }
}
