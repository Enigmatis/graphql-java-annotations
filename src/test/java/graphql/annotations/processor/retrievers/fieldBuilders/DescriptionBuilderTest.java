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

import graphql.annotations.annotationTypes.GraphQLDescription;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

public class DescriptionBuilderTest {

    @GraphQLDescription("test description")
    public int testField;

    @GraphQLDescription("test description")
    public int testMethod(){return 1;}

    public int testField2;

    public int testsMethod2(){return 1;}

    @Test
    public void build_descriptionAnnotationExistsOnField_returnCorrectDescription() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(field);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertEquals(description, "test description");
    }

    @Test
    public void build_descriptionAnnotationExistsOnMethod_returnCorrectDescription() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(method);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertEquals(description, "test description");
    }

    @Test
    public void build_descriptionAnnotationNotExistsOnField_returnNull() throws NoSuchFieldException {
        // arrange
        Field field = getClass().getField("testField2");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(field);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertNull(description);
    }

    @Test
    public void build_descriptionAnnotationNotExistsOnMethod_returnNull() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testsMethod2");
        DescriptionBuilder descriptionBuilder = new DescriptionBuilder(method);

        // act
        String description = descriptionBuilder.build();

        // assert
        assertNull(description);
    }
}
