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

public class GraphQLIterableTest {
    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public static class TestMappedObject {
        @GraphQLField
        public String name;

        @GraphQLField
        public String foo;
    }

    public static class TestObjectDB {
        private String foo;

        private String name;

        public String getName() {
            return name;
        }

        public String getFoo() {
            return foo;
        }

        TestObjectDB(String name, String foo) {
            this.name = name;
            this.foo = foo;
        }
    }

    public static class IterableTestQuery {
        @GraphQLField
        @GraphQLDataFetcher(ArrayFetcher.class)
        public TestMappedObject[] array;

        @GraphQLField
        @GraphQLDataFetcher(ListFetcher.class)
        public List<TestMappedObject> list;
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
            return Collections.singletonList(new TestObjectDB("test", "test"));
        }
    }

    @Test
    public void queryWithArray() {
        GraphQLObjectType object = GraphQLAnnotations.object(IterableTestQuery.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{array {name foo}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((LinkedHashMap) ((ArrayList) (((LinkedHashMap) result.getData()).get("array"))).get(0)).get("name"), "hello");
        assertEquals(((LinkedHashMap) ((ArrayList) (((LinkedHashMap) result.getData()).get("array"))).get(0)).get("foo"), "world");
    }

    @Test
    public void queryWithList() {
        GraphQLObjectType object = GraphQLAnnotations.object(IterableTestQuery.class);
        GraphQLSchema schema = newSchema().query(object).build();

        ExecutionResult result = GraphQL.newGraphQL(schema).build().execute("{list {name foo}}");
        assertTrue(result.getErrors().isEmpty());
        assertEquals(((LinkedHashMap) ((ArrayList) (((LinkedHashMap) result.getData()).get("list"))).get(0)).get("name"), "test");
        assertEquals(((LinkedHashMap) ((ArrayList) (((LinkedHashMap) result.getData()).get("list"))).get(0)).get("foo"), "test");
    }
}