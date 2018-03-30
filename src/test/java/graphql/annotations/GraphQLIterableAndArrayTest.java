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

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.annotations.annotationTypes.GraphQLDataFetcher;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLIterableAndArrayTest {
    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    static class TestMappedObject {
        @GraphQLField
        public String name;

        @GraphQLField
        public String foo;
    }

    static class TestObjectDB {
        private String foo;

        private String name;

        TestObjectDB(String name, String foo) {
            this.name = name;
            this.foo = foo;
        }
    }

    public static class IterableAndArrayTestQuery {
        @GraphQLField
        @GraphQLDataFetcher(ArrayFetcher.class)
        public TestMappedObject[] array;

        @GraphQLField
        @GraphQLDataFetcher(ListFetcher.class)
        public List<TestMappedObject> list;

        @GraphQLField
        @GraphQLDataFetcher(ArrayWithinArrayFetcher.class)
        public TestMappedObject[][] arrayWithinArray;
    }

    public static class ArrayFetcher implements DataFetcher<TestObjectDB[]> {

        @Override
        public TestObjectDB[] get(DataFetchingEnvironment environment) {
            return new TestObjectDB[]{new TestObjectDB("hello", "world")};
        }
    }

    public static class ListFetcher implements DataFetcher<List<TestObjectDB>> {

        @Override
        public List<TestObjectDB> get(DataFetchingEnvironment environment) {
            return Collections.singletonList(new TestObjectDB("test name", "test foo"));
        }
    }

    public static class ArrayWithinArrayFetcher implements DataFetcher<TestObjectDB[][]> {

        @Override
        public TestObjectDB[][] get(DataFetchingEnvironment environment) {
            return new TestObjectDB[][]{
                    {new TestObjectDB("hello", "world")},
                    {new TestObjectDB("a", "b"), new TestObjectDB("c", "d")}
            };
        }
    }

    @Test
    public void queryWithArray() {
        GraphQLObjectType object = GraphQLAnnotations.object(IterableAndArrayTestQuery.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{array {name foo}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((LinkedHashMap) getQueryResultAtIndex(result, "array", 0)).get("name"), "hello");
        assertEquals(((LinkedHashMap) getQueryResultAtIndex(result, "array", 0)).get("foo"), "world");
    }

    @Test
    public void queryWithList() {
        GraphQLObjectType object = GraphQLAnnotations.object(IterableAndArrayTestQuery.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{list {name foo}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((LinkedHashMap) getQueryResultAtIndex(result, "list", 0)).get("name"), "test name");
        assertEquals(((LinkedHashMap) getQueryResultAtIndex(result, "list", 0)).get("foo"), "test foo");
    }

    @Test
    public void queryWithArrayWithinAnArray() {
        GraphQLObjectType object = GraphQLAnnotations.object(IterableAndArrayTestQuery.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{arrayWithinArray {name foo}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 0, 0)).get("name"), "hello");
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 0, 0)).get("foo"), "world");
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 1, 0)).get("name"), "a");
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 1, 0)).get("foo"), "b");
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 1, 1)).get("name"), "c");
        assertEquals(((LinkedHashMap) getQueryResultAtCell(result, "arrayWithinArray", 1, 1)).get("foo"), "d");
    }

    private Object getQueryResultAtIndex(ExecutionResult result, String queryName, int index) {
        return ((ArrayList) (((LinkedHashMap) result.getData()).get(queryName))).get(index);
    }

    private Object getQueryResultAtCell(ExecutionResult result, String queryName, int rowIndex, int columnIndex) {
        return (((ArrayList) (getQueryResultAtIndex(result, queryName, rowIndex))).get(columnIndex));
    }
}
