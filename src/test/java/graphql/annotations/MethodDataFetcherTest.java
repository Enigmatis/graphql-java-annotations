/**
 * Copyright 2016 Yurii Rashkovskii
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.*;
import graphql.annotations.dataFetchers.MethodDataFetcher;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static graphql.annotations.AnnotationsSchemaCreator.newAnnotationsSchema;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.testng.Assert.*;

@SuppressWarnings("unchecked")
public class MethodDataFetcherTest {

    private GraphQLAnnotations graphQLAnnotations;

    @BeforeMethod
    public void init() {
        this.graphQLAnnotations = new GraphQLAnnotations();
    }

    public static class StaticApi {
        @GraphQLField
        public static String name() {
            return "osher";
        }
    }

    @Test
    public void query_staticMethod_valueIsDeterminedByMethod() {
        GraphQLSchema schema = newAnnotationsSchema().query(StaticApi.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { name }").root(new StaticApi()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, String>) result.getData()).get("name").toString(), "osher");
    }


    /**
     * CASE 1 : Only Api class, value determined by field
     */
    public static class Api1 {
        @GraphQLField
        private String name = "yarin";
    }

    public static class Query1 {
        @GraphQLField
        public Api1 queryField() {
            return new Api1();
        }
    }

    @Test
    public void query_onlyApiClass_valueIsDeterminedByField() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query1.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { name } }").root(new Query1()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("name").toString(), "yarin");
    }


    /**
     * CASE 2 : Only Api class, value determined by method
     */
    public static class Api2 {
        @GraphQLField
        public String name() {
            return "guy";
        }
    }

    public static class Query2 {
        @GraphQLField
        public Api2 queryField() {
            return new Api2();
        }
    }

    @Test
    public void query_onlyApiClass_valueIsDeterminedByMethod() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query2.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { name } }").root(new Query2()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("name").toString(), "guy");
    }

    /**
     * Case 3: Api and a DB class with polymorphism, value is determined by the db field
     * name of api method <-> name of db field
     */
    public static class Api3 {
        @GraphQLField
        @GraphQLName("nameX")
        public String name() {
            return "dani";
        }
    }

    public static class SuperDb3 {
        private String name = "osher";

    }

    public static class DB3 extends SuperDb3 {
    }

    public static class Api3Resolver implements DataFetcher<DB3> {

        @Override
        public DB3 get(DataFetchingEnvironment environment) {
            return new DB3();
        }
    }

    public static class Query3 {
        @GraphQLField
        @GraphQLDataFetcher(Api3Resolver.class)
        public Api3 queryField;
    }

    @Test
    public void query_apiAndDbClass_valueIsDeterminedByDBField() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query3.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { nameX } }").root(new Query3()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("nameX").toString(), "osher");
    }

    /**
     * Case 4: Api and DB classes, value is determined by db method
     * api method name <-> (`get`) + db method name
     */

    public static class Api4 {
        @GraphQLField
        @GraphQLName("nameX")
        public String name() {
            return null;
        }
    }

    public static class SuperDB4 {
        private String name = "guy";

        public String getName() {
            return name + "/yarin";
        }
    }

    public static class DB4 extends SuperDB4 {
    }

    public static class Api4Resolver implements DataFetcher<DB4> {

        @Override
        public DB4 get(DataFetchingEnvironment environment) {
            return new DB4();
        }
    }

    public static class Query4 {
        @GraphQLField
        @GraphQLDataFetcher(Api4Resolver.class)
        public Api4 queryField;
    }

    @Test
    public void query_apiAndDbClass_valueIsDeterminedByGetPrefixDBMethod() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query4.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { nameX } }").root(new Query4()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("nameX").toString(), "guy/yarin");
    }

    /**
     * Case: Api and DB classes, value is determined by db method
     * api method name <-> (`is`) + db method name
     */

    public static class Api6 {
        @GraphQLField
        @GraphQLName("nameX")
        public String name() {
            return null;
        }
    }

    public static class SuperDB6 {
        private String name = "guy";

        public String isName() {
            return name + "/yarin";
        }
    }

    public static class DB6 extends SuperDB6 {
    }

    public static class Api6Resolver implements DataFetcher<DB6> {

        @Override
        public DB6 get(DataFetchingEnvironment environment) {
            return new DB6();
        }
    }

    public static class Query6 {
        @GraphQLField
        @GraphQLDataFetcher(Api6Resolver.class)
        public Api6 queryField;
    }

    @Test
    public void query_apiAndDbClass_valueIsDeterminedByIsPrefixDBMethod() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query6.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { nameX } }").root(new Query6()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("nameX").toString(), "guy/yarin");
    }

    /**
     * Case: Api and DB classes, value is determined by db method
     * api method name <-> db method name
     */

    public static class Api7 {
        @GraphQLField
        @GraphQLName("nameX")
        public String name() {
            return null;
        }
    }

    public static class SuperDB7 {
        private String name = "guy";

        public String name() {
            return name + "/yarin";
        }

        public String isName() {
            return "blabla";
        }
    }

    public static class DB7 extends SuperDB7 {
    }

    public static class Api7Resolver implements DataFetcher<DB7> {

        @Override
        public DB7 get(DataFetchingEnvironment environment) {
            return new DB7();
        }
    }

    public static class Query7 {
        @GraphQLField
        @GraphQLDataFetcher(Api7Resolver.class)
        public Api7 queryField;
    }

    @Test
    public void query_apiAndDbClass_valueIsDeterminedByDBMethod() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query7.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { nameX } }").root(new Query7()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("nameX").toString(), "guy/yarin");
    }


    /**
     * Case 5: Invoke Detached on method, both api and db classes, value is determined by the api method
     */

    public static class Api5 {
        private String name = "yarin";

        @GraphQLField
        @GraphQLInvokeDetached
        @GraphQLPrettify
        public String getName() {
            return name + "/guy/osher";
        }
    }

    public static class DB5 {
        private String name = "moshe";
    }

    public static class Api5Resolver implements DataFetcher<DB5> {

        @Override
        public DB5 get(DataFetchingEnvironment environment) {
            return new DB5();
        }
    }

    public static class Query5 {
        @GraphQLField
        @GraphQLDataFetcher(Api5Resolver.class)
        public Api5 queryField;
    }

    @Test
    public void query_apiAndDbClassAndApiIsInvokeDetached_valueIsDeterminedByApiMethod() throws Exception {
        GraphQLSchema schema = newAnnotationsSchema().query(Query5.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute(builder -> builder.query("query { queryField { name } }").root(new Query5()));
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, String>>) result.getData()).get("queryField").get("name").toString(), "yarin/guy/osher");
    }

    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////

    public class TestException extends Exception {
    }

    public String method() throws TestException {
        throw new TestException();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void exceptionRethrowing() {
        try {
            MethodDataFetcher methodDataFetcher = new MethodDataFetcher(getClass().getMethod("method"), null, null);
            methodDataFetcher.get(newDataFetchingEnvironment().source(this).arguments(new HashMap<>()).build());
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
        @GraphQLPrettify
        public int getX() {
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

        @GraphQLField
        @GraphQLPrettify
        @GraphQLDataFetcher(CanonizedFetcher.class)
        public CanonizedTypeApi getCanonizedType() {
            return null;
        }
    }

    public static class CanonizedFetcher implements DataFetcher<CanonizedType> {

        @Override
        public CanonizedType get(DataFetchingEnvironment environment) {
            return new CanonizedType();
        }
    }

    public static class CanonizedTypeApi {
        @GraphQLPrettify
        @GraphQLField
        public int getM() {
            return 1;
        }
    }

    public static class CanonizedType {
        public int m = 5;
    }

    public static class InternalType {
        public int a = 123;
        public int b;
        public int x = 5;
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
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { a } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("field").get("a").toString(), "123");
    }


    @Test
    public void queryingOneCanonizedFieldNotAnnotatedWithGraphQLInvokeDetached_valueIsDeterminedByEntity() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { canonizedType { m } } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Map<String, Integer>>>) result.getData()).get("field").get("canonizedType").get("m").toString(), "5");
    }

    @Test
    public void queryingOneFieldNotAnnotatedWithGraphQLInvokeDetachedAndNameIsPrettified_valueIsDeterminedByEntity() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { x } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("field").get("x").toString(), "5");
    }

    @Test
    public void queryingOneFieldAnnotatedWithGraphQLInvokeDetached_valueIsDeterminedByApiEntity() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { b } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("field").get("b").toString(), "2");
    }

    @Test
    public void queryingFieldsFromApiEntityFetcher_valueIsDeterminedByApiEntity() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { apiField { a b } }");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("apiField").get("a").toString(), "1");
        assertEquals(((Map<String, Map<String, Integer>>) result.getData()).get("apiField").get("b").toString(), "2");
    }

    @Test
    public void queryingFieldsFromNoApiEntityFetcher_noMatchingFieldInEntity_throwException() {
        GraphQLSchema schema = newAnnotationsSchema().query(Query.class).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("query { field { c } }");
        assertFalse(result.getErrors().isEmpty());
        assertTrue(((ExceptionWhileDataFetching) result.getErrors().get(0)).getException().getCause() instanceof NoSuchFieldException);
    }
}