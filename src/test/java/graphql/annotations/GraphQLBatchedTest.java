package graphql.annotations;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.batched.BatchedExecutionStrategy;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import lombok.SneakyThrows;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static graphql.Scalars.GraphQLString;
import static graphql.annotations.DefaultTypeFunction.instance;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GraphQLBatchedTest {
    private static class SimpleBatchedField {
        @GraphQLField
        @GraphQLBatched
        public static List<String> a() {
            return Arrays.asList("one", "two");
        }
    }

    private static class TestBatchedObject {
        @GraphQLField
        public List<SimpleBatchedField> fields() {
            return Arrays.asList(new SimpleBatchedField(), new SimpleBatchedField());
        }
    }

    @Test
    @SneakyThrows
    public void batchedDataFetcher() {
        GraphQLObjectType nestedObject = GraphQLAnnotations.object(SimpleBatchedField.class);
        assertEquals(nestedObject.getFieldDefinition("a").getType(), GraphQLString);

        GraphQLObjectType object = GraphQLAnnotations.object(TestBatchedObject.class);
        GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphql = new GraphQL(schema, new BatchedExecutionStrategy());
        ExecutionResult result = graphql.execute("{ fields { a } }", new TestBatchedObject());
        List errors = result.getErrors();
        for (Object e : errors) {
            throw ((ExceptionWhileDataFetching) e).getException();
        }
        assertTrue(result.getErrors().isEmpty());
        Map<String, Object> data = (Map) result.getData();
        List<Map<String, Object>> fields = (List) data.get("fields");
        assertEquals(fields.get(0).get("a"), "one");
        assertEquals(fields.get(1).get("a"), "two");
    }

    private static class NoStaticBatchedField {
        @GraphQLField
        @GraphQLBatched
        public List<String> a() {
            return Arrays.asList("one", "two");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void noStaticField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }

    private static class NoListBatchedField {
        @GraphQLField
        @GraphQLBatched
        public String a() {
            return "one";
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void noListField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }

    private static class NoParameterizedBatchedField {
        @GraphQLField
        @GraphQLBatched
        public List a() {
            return Arrays.asList("one", "two");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class) @SneakyThrows
    public void noParameterizedReturnField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }
}
