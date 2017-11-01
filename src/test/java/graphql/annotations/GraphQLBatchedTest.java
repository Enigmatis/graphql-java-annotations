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
import graphql.annotations.annotationTypes.GraphQLBatched;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.processor.GraphQLAnnotations;
import graphql.execution.batched.BatchedExecutionStrategy;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLSchema.newSchema;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class GraphQLBatchedTest {

    @BeforeMethod
    public void init() {
        GraphQLAnnotations.getInstance().getTypeRegistry().clear();
    }

    public static class SimpleBatchedField {
        @GraphQLField
        @GraphQLBatched
        public static List<String> a() {
            return Arrays.asList("one", "two");
        }
    }

    public static class TestBatchedObject {
        @GraphQLField
        public List<SimpleBatchedField> fields() {
            return Arrays.asList(new SimpleBatchedField(), new SimpleBatchedField());
        }
    }

    @Test
    public void batchedDataFetcher() throws Throwable {
        GraphQLObjectType nestedObject = GraphQLAnnotations.object(SimpleBatchedField.class);
        assertEquals(nestedObject.getFieldDefinition("a").getType(), GraphQLString);

        GraphQLObjectType object = GraphQLAnnotations.object(TestBatchedObject.class);
        GraphQLSchema schema = newSchema().query(object).build();
        GraphQL graphql = GraphQL.newGraphQL(schema).queryExecutionStrategy(new BatchedExecutionStrategy()).build();
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

    public static class NoStaticBatchedField {
        @GraphQLField
        @GraphQLBatched
        public List<String> a() {
            return Arrays.asList("one", "two");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void noStaticField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }

    public static class NoListBatchedField {
        @GraphQLField
        @GraphQLBatched
        public String a() {
            return "one";
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void noListField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }

    public static class NoParameterizedBatchedField {
        @GraphQLField
        @GraphQLBatched
        public List a() {
            return Arrays.asList("one", "two");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void noParameterizedReturnField() {
        GraphQLObjectType object = GraphQLAnnotations.object(NoStaticBatchedField.class);
    }
}
