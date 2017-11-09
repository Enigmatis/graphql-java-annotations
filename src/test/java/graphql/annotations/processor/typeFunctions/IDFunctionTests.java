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

import graphql.annotations.processor.GraphQLAnnotations;
import graphql.annotations.annotationTypes.GraphQLID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static graphql.Scalars.GraphQLID;
import static org.testng.Assert.assertEquals;
import static graphql.annotations.processor.typeFunctions.DefaultTypeFunctionTestHelper.testedDefaultTypeFunction;


public class IDFunctionTests {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public @graphql.annotations.annotationTypes.GraphQLID String idStringMethod() {
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
    public void buildType_stringMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException, NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idStringMethod = IDFunctionTests.class.getMethod("idStringMethod");

        // Act+Assert
        assertEquals(instance.buildType(idStringMethod.getReturnType(), idStringMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_integerMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idIntegerMethod = IDFunctionTests.class.getMethod("idIntegerMethod");

        // Act+Assert
        assertEquals(instance.buildType(idIntegerMethod.getReturnType(), idIntegerMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_intMethodAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchMethodException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Method idIntMethod = IDFunctionTests.class.getMethod("idIntMethod");

        // Act+Assert
        assertEquals(instance.buildType(idIntMethod.getReturnType(), idIntMethod.getAnnotatedReturnType(),null), GraphQLID);
    }

    @Test
    public void buildType_stringFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idStringField = IDFunctionTests.class.getField("idStringField");

        // Act+Assert
        assertEquals(instance.buildType(idStringField.getType(), idStringField.getAnnotatedType(),null), GraphQLID);
    }

    @Test
    public void buildType_integerFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idIntegerField = IDFunctionTests.class.getField("idIntegerField");

        // Act+Assert
        assertEquals(instance.buildType(idIntegerField.getType(), idIntegerField.getAnnotatedType(),null), GraphQLID);
    }

    @Test
    public void buildType_intFieldAnnotatedWithGraphQLID_returnsGraphQLID() throws NoSuchFieldException {
        // Arrange
        DefaultTypeFunction instance = testedDefaultTypeFunction();
        Field idIntField = IDFunctionTests.class.getField("idIntField");

        // Act+Assert
        assertEquals(instance.buildType(idIntField.getType(), idIntField.getAnnotatedType(),null), GraphQLID);
    }


}
