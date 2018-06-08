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

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLType;
import graphql.annotations.dataFetchers.MethodDataFetcher;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.*;

@SuppressWarnings("unchecked")
public class MethodDataFetcherTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }


    public class TestException extends Exception {
    }

    public String method() throws TestException {
        throw new TestException();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void exceptionRethrowing() {
        try {
            MethodDataFetcher methodDataFetcher = new MethodDataFetcher(getClass().getMethod("method"), null, null);
            methodDataFetcher.get(new DataFetchingEnvironmentImpl(this, new HashMap<>(),
                    null, null, null, new ArrayList<>(),
                    null, null, null, null,
                    null, null, null, null));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    @GraphQLType
    public static class ApiType {
        @GraphQLField
        public int a() {
            return 1;
        }

        @GraphQLField
        @GraphQLInvokeDetached
        public int b() {
            return 2;
        }

        @GraphQLField
        public int c() {
            return 4;
        }
    }

    public static class InternalType {
        public int a = 123;
        public int b;
    }

    @GraphQLType
    public static class Query {
        @GraphQLField
        @GraphQLDataFetcher(MyFetcher.class)
        public ApiType field;

        @GraphQLField
        @GraphQLDataFetcher(MyApiFetcher.class)
        public ApiType apiField;
    }

    public static class MyFetcher implements DataFetcher<InternalType> {
        public InternalType get(DataFetchingEnvironment environment) {
            return new InternalType();
        }
    }

    public static class MyApiFetcher implements DataFetcher<ApiType> {
        public ApiType get(DataFetchingEnvironment environment) {
            return new ApiType();
        }
    }


    @Test
    public void queryingOneFieldNotAnnotatedWithGraphQLInvokeDetached_valueIsDeterminedByEntity() {
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { a } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("field").get("a").toString(), "123");
    }

    @Test
    public void queryingOneFieldAnnotatedWithGraphQLInvokeDetached_valueIsDeterminedByApiEntity() {
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { b } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("field").get("b").toString(), "2");
    }

    @Test
    public void queryingFieldsFromApiEntityFetcher_valueIsDeterminedByApiEntity() {
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { apiField { a b } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("apiField").get("a").toString(), "1");
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("apiField").get("b").toString(), "2");
    }

    @Test
    public void queryingFieldsFromNoApiEntityFetcher_noMatchingFieldInEntity_throwException() {
        GraphQLObjectType object = GraphQLAnnotations.object(Query.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { c } }");
        assertFalse(result.getErrors().isEmpty());
        assertTrue(((ExceptionWhileDataFetching) result.getErrors().get(0)).getException().getCause() instanceof NoSuchFieldException);
    }
}