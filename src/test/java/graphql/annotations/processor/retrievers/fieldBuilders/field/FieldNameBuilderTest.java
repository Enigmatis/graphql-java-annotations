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
package graphql.annotations.processor.retrievers.fieldBuilders.field;

import graphql.annotations.annotationTypes.GraphQLName;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.testng.AssertJUnit.assertEquals;

public class FieldNameBuilderTest {

    public int test1;

    @GraphQLName("test1")
    public int test2;

    @Test
    public void build_fieldWithGraphQLNameAnnotation_returnAnnotationValue() throws NoSuchFieldException{
        // arrange
        Field field = getClass().getField("test2");
        FieldNameBuilder fieldNameBuilder = new FieldNameBuilder(field);

        // act
        String name = fieldNameBuilder.build();

        // assert
        assertEquals(name, "test1");
    }

    @Test
    public void build_fieldWithNoGraphQLNameAnnotation_returnFieldName() throws NoSuchFieldException{
        // arrange
        Field field = getClass().getField("test1");
        FieldNameBuilder fieldNameBuilder = new FieldNameBuilder(field);

        // act
        String name = fieldNameBuilder.build();

        // assert
        assertEquals(name, "test1");
    }
}
