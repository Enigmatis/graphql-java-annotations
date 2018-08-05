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
package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLName;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.AssertJUnit.assertEquals;

public class MethodNameBuilderTest {

    @GraphQLName("testName")
    public void testMethod1() {
    }

    public void getTest() {
    }

    public void isTest(){
    }

    public void setTest(){

    }

    public void test(){

    }

    @Test
    public void build_graphQLNameAnnotationExists_returnAnnotatedName() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("testMethod1");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "testName");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithGetPrefix_returnCorrectName() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("getTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "getTest");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithGetPrefixAndPrettify_returnCorrectName() throws NoSuchMethodException {
        // arrange
        Method method = getClass().getMethod("getTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.alwaysPrettify(true).build();

        // assert
        assertEquals(name, "test");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithIsPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("isTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "isTest");
    }

    @Test
    public void build_graphQLNameAnnotationNotExistsWithSetPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("setTest");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "setTest");
    }


    @Test
    public void build_graphQLNameAnnotationNotExistsNoPrefix_returnCorrectName() throws NoSuchMethodException{
        // arrange
        Method method = getClass().getMethod("test");
        MethodNameBuilder methodNameBuilder = new MethodNameBuilder(method);

        // act
        String name = methodNameBuilder.build();

        // assert
        assertEquals(name, "test");
    }

}
