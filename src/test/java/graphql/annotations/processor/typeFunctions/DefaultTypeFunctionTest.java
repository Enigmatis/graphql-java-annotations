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
package graphql.annotations.processor.typeFunctions;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLID;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.schema.*;
import graphql.schema.GraphQLType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.Scalars.GraphQLString;
import static org.testng.Assert.*;

public class DefaultTypeFunctionTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    private enum A {
        @GraphQLName("someA") @GraphQLDescription("a") A, B
    }

    public @GraphQLID String idStringMethod() {
        return "asd";
    }

    public @GraphQLID Integer idIntegerMethod() {
        return 5;
    }

    public @GraphQLID int idIntMethod() {
        return 5;
    }

    public @GraphQLID String idStringField;
    public @GraphQLID Integer idIntegerField;
    public @GraphQLID int idIntField;

    @Test
    public void enumeration() {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        GraphQLType enumeration = container.getDefaultTypeFunction().buildType(A.class, null,container);
        assertTrue(enumeration instanceof GraphQLEnumType);
        List<GraphQLEnumValueDefinition> values = ((GraphQLEnumType) enumeration).getValues();
        assertEquals(values.stream().
                        map(GraphQLEnumValueDefinition::getName).collect(Collectors.toList()),
                Arrays.asList("someA", "B"));
        assertEquals(values.stream().
                        map(GraphQLEnumValueDefinition::getDescription).collect(Collectors.toList()),
                Arrays.asList("a", "B"));

    }




    @SuppressWarnings("unused")
    public List<List<@graphql.annotations.annotationTypes.GraphQLNonNull String>> listMethod() {
        return null;
    }

    @SuppressWarnings("unused")
    public Iterable<Iterable<@graphql.annotations.annotationTypes.GraphQLNonNull String>> iterableMethod() {
        return null;
    }

    @SuppressWarnings("unused")
    public Collection<Collection<@graphql.annotations.annotationTypes.GraphQLNonNull String>> collectionMethod() {
        return null;
    }


    @SuppressWarnings("unused")
    public Stream<List<@graphql.annotations.annotationTypes.GraphQLNonNull String>> streamMethod() {
        return null;
    }

    @SuppressWarnings("unused")
    public Set<Set<@graphql.annotations.annotationTypes.GraphQLNonNull String>> setMethod() {
        return null;
    }

    // GraphqlList(GraphqlList(GraphQlString) is expected here
    private void assertIsGraphListOfListOfString(GraphQLType type) {
        assertTrue(type instanceof GraphQLList);
        GraphQLList subtype = (GraphQLList) ((GraphQLList) type).getWrappedType();
        assertTrue(subtype.getWrappedType() instanceof graphql.schema.GraphQLNonNull);
        graphql.schema.GraphQLNonNull wrappedType = (graphql.schema.GraphQLNonNull) subtype.getWrappedType();
        assertEquals(wrappedType.getWrappedType(), GraphQLString);
    }

    @Test
    public void buildType_listType_returnsCorrectGraphQLType() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("listMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType(),null);
        assertIsGraphListOfListOfString(type);
    }


    @Test
    public void buildType_iterableType_returnsCorrectGraphQLType() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("iterableMethod").getReturnType(), getClass().getMethod("iterableMethod").getAnnotatedReturnType(),null);
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void buildType_collectionType_returnsCorrectGraphQLType() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("collectionMethod").getReturnType(), getClass().getMethod("collectionMethod").getAnnotatedReturnType(),null);
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void buildType_setType_returnsCorrectGraphQLType() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("setMethod").getReturnType(), getClass().getMethod("setMethod").getAnnotatedReturnType(),null);
        assertIsGraphListOfListOfString(type);
    }

    @Test
    public void buildType_streamType_returnsCorrectGraphQLType() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("streamMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType(),null);
        assertIsGraphListOfListOfString(type);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparameterizedList() {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        List v = new LinkedList();
        container.getDefaultTypeFunction().buildType(v.getClass(), null,null);
    }

    @SuppressWarnings("unused")
    public Optional<List<@graphql.annotations.annotationTypes.GraphQLNonNull String>> optionalMethod() {
        return Optional.empty();
    }

    @Test
    public void optional() throws NoSuchMethodException {
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        graphql.schema.GraphQLType type = container.getDefaultTypeFunction().buildType(getClass().getMethod("optionalMethod").getReturnType(), getClass().getMethod("listMethod").getAnnotatedReturnType(),null);
        assertTrue(type instanceof GraphQLList);
        GraphQLType subtype = ((GraphQLList) type).getWrappedType();
        assertTrue(subtype instanceof graphql.schema.GraphQLNonNull);
        GraphQLType wrappedType = (((graphql.schema.GraphQLNonNull) subtype).getWrappedType());
        assertEquals(wrappedType, GraphQLString);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unparametrizedOptional() {
        ProcessingElementsContainer oontainer = testedProcessingElementsContainer();
        Optional v = Optional.empty();
        oontainer.getDefaultTypeFunction().buildType(v.getClass(), null,null);
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
        ProcessingElementsContainer container = testedProcessingElementsContainer();
        GraphQLType type = container.getDefaultTypeFunction().buildType(Class1.class, Class2.class.getField("class1").getAnnotatedType(),container);
        GraphQLFieldDefinition class1class2 = ((GraphQLObjectType) type).getFieldDefinition("class2");
        assertNotNull(class1class2);
        assertTrue(((GraphQLObjectType) class1class2.getType()).getFieldDefinition("class1").getType() instanceof GraphQLTypeReference);
        assertTrue(((GraphQLObjectType) class1class2.getType()).getFieldDefinition("class2").getType() instanceof GraphQLTypeReference);
        GraphQLAnnotations.instance = new GraphQLAnnotations();
    }

    private ProcessingElementsContainer testedProcessingElementsContainer() {
        // wire up the ability
        GraphQLAnnotations graphQLAnnotations = new GraphQLAnnotations();
        return graphQLAnnotations.getContainer();
    }
}
