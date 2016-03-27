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

import java.lang.reflect.AnnotatedParameterizedType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static graphql.Scalars.*;
import static graphql.annotations.DefaultTypeFunction.instance;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DefaultTypeFunctionTest {

    private enum A {
        @GraphQLName("someA") @GraphQLDescription("a") A, B
    }

    @Test
    public void enumeration() {
        GraphQLType enumeration = instance.apply(A.class, null);
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
        assertEquals(instance.apply(String.class, null), GraphQLString);
    }

    @Test
    public void bool() {
        assertEquals(instance.apply(boolean.class, null), GraphQLBoolean);
        assertEquals(instance.apply(Boolean.class, null), GraphQLBoolean);
    }

    @Test
    public void float_() {
        assertEquals(instance.apply(float.class, null), GraphQLFloat);
        assertEquals(instance.apply(Float.class, null), GraphQLFloat);
    }

    @Test
    public void integer() {
        assertEquals(instance.apply(int.class, null), GraphQLInt);
        assertEquals(instance.apply(Integer.class, null), GraphQLInt);
    }

    @Test
    public void long_() {
        assertEquals(instance.apply(long.class, null), GraphQLLong);
        assertEquals(instance.apply(Long.class, null), GraphQLLong);
    }


    @SuppressWarnings("unused")
    public List<List<@GraphQLNonNull String>> listMethod() { return null;};

    @Test
    public void list() throws NoSuchMethodException {
        graphql.schema.GraphQLType type = instance.apply(getClass().getMethod("listMethod").getReturnType(), (AnnotatedParameterizedType) getClass().getMethod("listMethod").getAnnotatedReturnType());
        assertTrue(type instanceof GraphQLList);
        GraphQLList subtype = (GraphQLList) ((GraphQLList) type).getWrappedType();
        assertTrue(subtype.getWrappedType() instanceof graphql.schema.GraphQLNonNull);
        graphql.schema.GraphQLNonNull wrappedType = (graphql.schema.GraphQLNonNull) subtype.getWrappedType();
        assertEquals(wrappedType.getWrappedType(), GraphQLString);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparametrizedList() {
        List v = new LinkedList();
        instance.apply(v.getClass(), null);
    }

    @SuppressWarnings("unused")
    public Optional<List<@GraphQLNonNull String>> optionalMethod() { return Optional.empty();};

    @Test
    public void optional() throws NoSuchMethodException {
        graphql.schema.GraphQLType type = instance.apply(getClass().getMethod("optionalMethod").getReturnType(), (AnnotatedParameterizedType) getClass().getMethod("listMethod").getAnnotatedReturnType());
        assertTrue(type instanceof GraphQLList);
        GraphQLType subtype = ((GraphQLList) type).getWrappedType();
        assertTrue(subtype instanceof graphql.schema.GraphQLNonNull);
        GraphQLType wrappedType = (((graphql.schema.GraphQLNonNull) subtype).getWrappedType());
        assertEquals(wrappedType, GraphQLString);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparametrizedOptional() {
        Optional v = Optional.empty();
        instance.apply(v.getClass(), null);
    }

}