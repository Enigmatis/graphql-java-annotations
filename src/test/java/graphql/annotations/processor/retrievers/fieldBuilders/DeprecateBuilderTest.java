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

import graphql.annotations.annotationTypes.GraphQLDeprecate;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class DeprecateBuilderTest {

    @GraphQLDeprecate("test field deprecated")
    public String testField;

    @GraphQLDeprecate
    public String testField1;

    public String testField2;

    @GraphQLDeprecate("test deprecated")
    public int testMethod() {
        return 1;
    }

    @GraphQLDeprecate
    public int testMethod2() {
        return 1;
    }

    @Deprecated
    public int testMethod3() {
        return 1;
    }

    public int testMethod4() {
        return 1;
    }

    @Test
    public void build_graphQLDeprecateAnnotationExistsWithValue_returnAnnotationValue() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "test deprecated");
    }

    @Test
    public void build_graphQLDeprecateAnnotationExistsWithNoValue_returnEmptyString() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod2");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "Deprecated");
    }

    @Test
    public void build_deprecatedAnnotationExists_returnDefaultDeprecationComment() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod3");
        DeprecateBuilder deprecatedBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecatedBuilder.build();

        // assert
        assertEquals(deprecate, "Deprecated");
    }

    @Test
    public void build_noDeprecatedAnnotation_returnNull() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod4");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(method);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertNull(deprecate);
    }

    @Test
    public void build_deprecatedAnnotationDoesNotExistsOnField_returnNull() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField2");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(field);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertNull(deprecate);
    }

    @Test
    public void build_graphQLDeprecateAnnotationExistsOnFieldWithNoValue_returnEmptyString() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField1");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(field);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "Deprecated");
    }

    @Test
    public void build_graphQLDeprecateAnnotationExistsOnFieldWithValue_returnAnnotationValue() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField");
        DeprecateBuilder deprecateBuilder = new DeprecateBuilder(field);

        // act
        String deprecate = deprecateBuilder.build();

        // assert
        assertEquals(deprecate, "test field deprecated");
    }
}
